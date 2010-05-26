/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.source.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;

import com.sun.source.tree.ClassTree;

/**
 * This class is an abstract annotation processor designed to be a
 * convenient superclass for concrete "type processors", processors that
 * require the type information in the processed source.
 *
 * <p>Type processing occurs in one round after the tool (e.g. java compiler)
 * analyzes the source (all sources taken as input to the tool and sources
 * generated by other annotation processors).
 *
 * <p>The tool infrastructure will interact with classes extending this abstract
 * class as follows:
 *
 * <ol>
 * [1-3: Identical to {@link Processor} life cycle]
 *
 * <li>If an existing {@code Processor} object is not being used, to
 * create an instance of a processor the tool calls the no-arg
 * constructor of the processor class.
 *
 * <li>Next, the tool calls the {@link #init init} method with
 * an appropriate {@code ProcessingEnvironment}.
 *
 * <li>Afterwards, the tool calls {@link #getSupportedAnnotationTypes
 * getSupportedAnnotationTypes}, {@link #getSupportedOptions
 * getSupportedOptions}, and {@link #getSupportedSourceVersion
 * getSupportedSourceVersion}.  These methods are only called once per
 * run, not on each round.
 *
 * [4-5Unique to {@code AbstractTypeProcessor} subclasses]
 *
 * <li>For each class containing a supported annotation, the tool calls
 * {@link #typeProcess(TypeElement, TreePath) typeProcess} method on the
 * {@code Processor}.  The class is guaranteed to be type-checked Java code
 * and all the tree type and symbol information is resolved.
 *
 * <li>Finally, the tools calls the
 * {@link #typeProcessingOver() typeProcessingOver} method
 * on the {@code Processor}.
 *
 * </ol>
 *
 * <p>The tool is permitted to ask type processors to process a class once
 * it is analyzed before the rest of classes are analyzed.  The tool is also
 * permitted to stop type processing immediately if any errors are raised,
 * without invoking {@code typeProcessingOver}
 *
 * <p>A subclass may override any of the methods in this class, as long as the
 * general {@link javax.annotation.processing.Processor Processor}
 * contract is obeyed, with one notable exception.
 * {@link #process(Set, RoundEnvironment)} may not be overridden, as it
 * is called during the regular annotation phase before classes are analyzed.
 *
 * @author Mahmood Ali
 * @since 1.7
 */
public abstract class AbstractTypeProcessor extends AbstractProcessor {
    private final Set<Name> elements = new HashSet<Name>();
    private boolean hasInvokedTypeProcessingOver = false;
    private JavacProcessingEnvironment env;
    private final AttributionTaskListener listener = new AttributionTaskListener();

    /**
     * Constructor for subclasses to call.
     */
    protected AbstractTypeProcessor() { }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(ProcessingEnvironment env) {
        super.init(env);
        this.env = (JavacProcessingEnvironment)env;
        prepareContext(this.env.getContext());
    }

    /**
     * The use of this method is obsolete in type processors.  The method is
     * called during regular annotation processing phase only.
     */
    @Override
    public final boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        for (TypeElement elem : ElementFilter.typesIn(roundEnv.getRootElements())) {
            elements.add(elem.getQualifiedName());
        }
        return false;
    }

    /**
     * Processes a fully analyzed class that contains a supported annotation
     * (look {@link #getSupportedAnnotationTypes()}).
     *
     * <p>The passed class is always a valid type-checked Java code.
     *
     * @param element       element of the analyzed class
     * @param tree  the tree path to the element, with the leaf being a
     *              {@link ClassTree}
     */
    public abstract void typeProcess(TypeElement element, TreePath tree);

    /**
     * A method to be called once all the classes are processed and no error
     * is reported.
     *
     * <p>Subclasses may override this method to do any aggregate analysis
     * (e.g. generate report, persistence) or resource deallocation.
     *
     * <p>If an error (a Java error or a processor error) is reported, this
     * method is not guaranteed to be invoked.
     */
    public void typeProcessingOver() { }

    /**
     * adds a listener for attribution.
     */
    private void prepareContext(Context context) {
        TaskListener otherListener = context.get(TaskListener.class);
        if (otherListener == null) {
            context.put(TaskListener.class, listener);
        } else {
            // handle cases of multiple listeners
            context.put(TaskListener.class, (TaskListener)null);
            TaskListeners listeners = new TaskListeners();
            listeners.add(otherListener);
            listeners.add(listener);
            context.put(TaskListener.class, listeners);
        }
    }

    /**
     * A task listener that invokes the processor whenever a class is fully
     * analyzed.
     */
    private final class AttributionTaskListener implements TaskListener {

        @Override
        public void finished(TaskEvent e) {
            Log log = Log.instance(env.getContext());

            if (!hasInvokedTypeProcessingOver && elements.isEmpty() && log.nerrors == 0) {
                typeProcessingOver();
                hasInvokedTypeProcessingOver = true;
            }

            if (e.getKind() != TaskEvent.Kind.ANALYZE)
                return;

            if (e.getTypeElement() == null)
                throw new AssertionError("event task without a type element");
            if (e.getCompilationUnit() == null)
                throw new AssertionError("even task without compilation unit");

            if (!elements.remove(e.getTypeElement().getQualifiedName()))
                return;

            if (log.nerrors != 0)
                return;

            TypeElement elem = e.getTypeElement();
            TreePath p = Trees.instance(env).getPath(elem);

            typeProcess(elem, p);

            if (!hasInvokedTypeProcessingOver && elements.isEmpty() && log.nerrors == 0) {
                typeProcessingOver();
                hasInvokedTypeProcessingOver = true;
            }
        }

        @Override
        public void started(TaskEvent e) { }

    }

    /**
     * A task listener multiplexer.
     */
    private static class TaskListeners implements TaskListener {
        private final List<TaskListener> listeners = new ArrayList<TaskListener>();

        public void add(TaskListener listener) {
            listeners.add(listener);
        }

        public void remove(TaskListener listener) {
            listeners.remove(listener);
        }

        @Override
        public void finished(TaskEvent e) {
            for (TaskListener listener : listeners)
                listener.finished(e);
        }

        @Override
        public void started(TaskEvent e) {
            for (TaskListener listener : listeners)
                listener.started(e);
        }
    }
}
