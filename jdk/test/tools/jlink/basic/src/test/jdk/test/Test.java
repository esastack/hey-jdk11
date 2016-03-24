/**
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

package jdk.test;

import java.lang.reflect.Module;
import java.lang.reflect.Layer;

public class Test {
    public static void main(String[] args) {
        System.out.println(Test.class + " ...");
        for (String arg: args) {
            System.out.println(arg);
        }

        ClassLoader scl = ClassLoader.getSystemClassLoader();
        ClassLoader cl1 = Test.class.getClassLoader();
        Module testModule = Test.class.getModule();
        ClassLoader cl2 = Layer.boot().findLoader(testModule.getName());

        if (cl1 != scl)
            throw new RuntimeException("Not loaded by system class loader");
        if (cl2 != scl)
            throw new RuntimeException("Not associated with system class loader");

    }
}
