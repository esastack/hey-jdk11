/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.internal.xjc.model;

import com.sun.tools.internal.xjc.Plugin;
import com.sun.xml.internal.xsom.XSComponent;

import org.xml.sax.Locator;

/**
 * Implemented by model components that can have customizations contributed by {@link Plugin}s.
 *
 * @author Kohsuke Kawaguchi
 */
public interface CCustomizable {
    /**
     * Gets the list of customizations attached to this model component.
     *
     * @return
     *      can be an empty list but never be null. The returned list is read-only.
     *      Do not modify.
     *
     * @see Plugin#getCustomizationURIs()
     */
    CCustomizations getCustomizations();

    /**
     * Gets the source location in the schema from which this model component is created.
     *
     * @return never null.
     */
    Locator getLocator();

    /**
     * If this model object is built from XML Schema,
     * this property returns a schema component from which the model is built.
     *
     * @return
     *      null if the model is built from sources other than XML Schema
     *      (such as DTD.)
     */
    XSComponent getSchemaComponent();
}
