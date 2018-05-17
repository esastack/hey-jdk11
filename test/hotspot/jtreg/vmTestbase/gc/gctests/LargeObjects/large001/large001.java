/*
 * Copyright (c) 2004, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * @test
 * @key stress gc
 *
 * @summary converted from VM Testbase gc/gctests/LargeObjects/large001.
 * VM Testbase keywords: [gc, stress, stressopt, nonconcurrent, quick]
 * VM Testbase readme:
 * DESCRIPTION
 *     The test checks that Garbage Collector correctly does not throw any
 *     unexpected exceptions/errors while allocating large objects (classes
 *     that have more than 65535 fields and classes that have less than 65535
 *     fields). 65535 of fields is a limitation for JVM (see JVM specification
 *     Second edition 4.10).
 *     Since it is impossible to create one class with about 65535 of fields
 *     (javac cannot compile it), a child class extends a parent class, so the
 *     fields are devided into two subsets. However, the child class still has
 *     about 65535 of fields.
 *     The test starts a number of threads. This number is either set in *.cfg
 *     file or is calculated by the test itself based on the machine (see
 *     nsk.share.gc.Algorithms.getThreadsCount() method). As soon as all threads
 *     are started, each thread begins its checking.
 *     There are 13 classes to be loaded by each thread. These classes are
 *     generated by nsk.share.gc.Generator (see its javadoc for more details).
 *     Each class has a huge number of fields, but this number is less than the JVM
 *     limitation.
 *     The test loads the classes with nsk.share.gc.GCClassUnloader class that
 *     extends nsk.share.ClassUnloader and has a bit different algorith of eating
 *     heap. As soon as a class is loaded, the test creates an instance of
 *     it - allocates an object of that type. Then it drops references to the
 *     class and to the instance and tries to unload the class. The test does not
 *     expect any exceptions to be thrown.
 *
 * @library /vmTestbase
 *          /test/lib
 * @run driver jdk.test.lib.FileInstaller . .
 *
 * @comment generate and compile nsk.share.gc.newclass.* classes
 * @run driver nsk.share.gc.GenClassesBuilder
 *
 * @run main/othervm
 *      -XX:-UseGCOverheadLimit
 *      -Xlog:gc*
 *      gc.gctests.LargeObjects.large001.large001
 *      -largeClassesPath classes
 *      -isOverLimitFields false
 *      -aggregationDepth 0
 *      -t 1
 */

package gc.gctests.LargeObjects.large001;

import java.lang.reflect.*;
import java.lang.ref.WeakReference;
import java.util.*;
import nsk.share.TestFailure;


import nsk.share.gc.*;
import nsk.share.*;

public class large001 extends ThreadedGCTest {

    // Package of the classes to be loaded
    final static String PREFIX = "nsk.share.gc.newclass.";
    // A bunch of classes that have number of fields more than JVM limitation
    final static String[] LCLASSES = {PREFIX + "private_int_lchild",
        PREFIX + "protected_short_lchild",
        PREFIX + "public_long_lchild",
        PREFIX + "public_Object_lchild",
        PREFIX + "static_byte_lchild",
        PREFIX + "static_float_lchild",
        PREFIX + "transient_boolean_lchild",
        PREFIX + "volatile_double_lchild",
        PREFIX + "protected_combination_lchild",
        PREFIX + "public_combination_lchild",
        PREFIX + "static_combination_lchild",
        PREFIX + "transient_combination_lchild",
        PREFIX + "volatile_combination_lchild"
    };
    // A bunch of classes that have number of fields less than JVM limitation
    final static String[] SCLASSES = {PREFIX + "private_int_schild",
        PREFIX + "protected_short_schild",
        PREFIX + "public_long_schild",
        PREFIX + "public_Object_schild",
        PREFIX + "static_byte_schild",
        PREFIX + "static_float_schild",
        PREFIX + "transient_boolean_schild",
        PREFIX + "volatile_double_schild",
        PREFIX + "protected_combination_schild",
        PREFIX + "public_combination_schild",
        PREFIX + "static_combination_schild",
        PREFIX + "transient_combination_schild",
        PREFIX + "volatile_combination_schild"
    };
    boolean isOverLimitFields = true;
    int aggregationDepth = 0;
    String largeClassesPath;

    private class Worker implements Runnable {

        int id;

        public Worker(int id) {
            this.id = id;
        }

        public void run() {
            try {
                // Use special ClassUnloader to load/unload classes
                ClassUnloader unloader = new ClassUnloader();
                String[] classes = isOverLimitFields ? LCLASSES : SCLASSES;

                for (String name : classes) {
                    // Load the class
                    log.debug(id + ": Loading class: " + name);
                    unloader.loadClass(name, largeClassesPath);
                    log.debug(id + ": Class loaded: " + name);

                    Class loadedClass = unloader.getLoadedClass();
                    Object loadedClassInstance = loadedClass.newInstance();

                    log.debug(id + ": Instance of the class: " + loadedClassInstance);
                    int depth = aggregationDepth;
                    List<WeakReference> refs = new ArrayList<WeakReference>(depth);
                    addObjRef(loadedClassInstance, loadedClass, depth, refs);

                    // Drop all references to the class and try to unload it
                    Algorithms.eatMemory(getExecutionController());
                    log.debug(id + ": Testing non-null after GC force for: " + name);
                    if (loadedClass == null || loadedClassInstance == null) {
                        throw new Exception("Null class");
                    }
                    verifyObjRef(loadedClassInstance, depth);
                    for (WeakReference ref : refs) {
                        if (ref.get() == null) {
                            throw new Exception("Unexpected null reference");
                        }
                    }
                    refs = null;
                    loadedClass = null;
                    loadedClassInstance = null;

                    log.debug(id + ": Unloading class: "
                            + name);
                    boolean result = unloader.unloadClass(getExecutionController());
                    log.debug(id + ": Result of uloading "
                            + "class " + name + ": " + result);
                }
            } catch (OutOfMemoryError oome) {
                // just skip if we eat memory in several threads...
                // rethrow in the case of one thread
                if (runParams.getNumberOfThreads() == 1) {
                    throw oome;
                }
            } catch (Throwable t) {
                throw new TestFailure("Unexpected exception: ", t);
            }
        }

        // This method recursively create chain of aggregated objects for given object
        public void addObjRef(Object object, Class clazz, int count, List<WeakReference> list) throws Throwable {
            if (count == 0) {
                return;
            }

            Field[] fields = object.getClass().getFields();
            for (Field field : fields) {
                if (field.getName().startsWith("obj")) {
                    Object addedObject = clazz.newInstance();
                    field.set(object, addedObject);
                    System.out.println("Added field " + field.getName() + "  .... " + count);
                    addObjRef(addedObject, clazz, count - 1, list);
                    list.add(new WeakReference<Object>(addedObject));
                }
            }
        }

        // This method recursively verfiy chain of aggregated objects for given object.
        // Throws null pointer exception of objP/C field is null
        public void verifyObjRef(Object object, int count) throws Throwable {
            if (count == 0) {
                return;
            }

            Field[] fields = object.getClass().getFields();
            for (Field field : fields) {
                if (field.getName().startsWith("obj")) {
                    Object obj = field.get(object);
                    verifyObjRef(obj, count - 1);
                }
            }
        }
    }

    public large001(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-largeClassesPath")) {
                largeClassesPath = args[++i];
            } else if (args[i].equals("-isOverLimitFields")) {
                isOverLimitFields = Boolean.getBoolean(args[++i]);
            } else if (args[i].equals("-aggregationDepth")) {
                aggregationDepth = Integer.parseInt(args[++i]);
            }
        }
        if (largeClassesPath == null || largeClassesPath.length() == 0) {
            throw new TestFailure("No classpath for large classes is given");
        }
    }

    @Override
    protected Runnable createRunnable(int i) {
        return new Worker(i);
    }

    @Override
    public void run() {
        if (isOverLimitFields) {
            log.debug("Loading classes that have number "
                    + "of fields over limitation (more "
                    + "than 65535)");
        } else {
            log.debug("Loading classes that have number "
                    + "of fields under limitation (less "
                    + "than 65535)");
        }
        super.run();
    }

    public static void main(String args[]) {
        GC.runTest(new large001(args), args);
    }
}
