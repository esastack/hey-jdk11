/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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


/*
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/, and in the file LICENSE.html in the
 * doc directory.
 *
 * The Original Code is HAT. The Initial Developer of the
 * Original Code is Bill Foote, with contributions from others
 * at JavaSoft/Sun. Portions created by Bill Foote and others
 * at Javasoft/Sun are Copyright (C) 1997-2004. All Rights Reserved.
 *
 * In addition to the formal license, I ask that you don't
 * change the history or donations files without permission.
 *
 */

package com.sun.tools.hat.internal.model;

/**
 *
 * @author      Bill Foote
 */

/**
 * Represents the value of a static field of a JavaClass
 */

public class JavaStatic {

    private JavaField field;
    private JavaThing value;

    public JavaStatic(JavaField field, JavaThing value) {
        this.field = field;
        this.value = value;
    }

    public void resolve(JavaClass clazz, Snapshot snapshot) {
        long id = -1;
        if (value instanceof JavaObjectRef) {
            id = ((JavaObjectRef)value).getId();
        }
        value = value.dereference(snapshot, field);
        if (value.isHeapAllocated()) {
            JavaHeapObject ho = (JavaHeapObject) value;
            String s = "Static reference from " + clazz.getName()
                       + "." + field.getName();
            snapshot.addRoot(new Root(id, clazz.getId(),
                                      Root.JAVA_STATIC, s));
        }
    }

    public JavaField getField() {
        return field;
    }

    public JavaThing getValue() {
        return value;
    }
}
