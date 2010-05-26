/*
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 4629327
 * @summary Compiler crash on explicit use of synthetic name for inner class.
 * @author Neal Gafter
 *
 * @compile/fail FlatnameClash2.java
 */

package tests;

class T1 {
    public void print(Inner1 inf) {
        inf.print();
    }

    public class Inner1 {
        public void print() {
            System.out.println("Inner1");
        }

    }
}


class T2 extends T1 {
    public void print() {
        super.print(new Inner2());
    }

    private class Inner2
        extends tests.T1$Inner1 // ERROR: name not found
    {
        public void print() {
            System.out.println("Inner2");
        }
    }
}
