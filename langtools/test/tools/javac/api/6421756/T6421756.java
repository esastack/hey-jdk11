/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * @test
 * @bug     6421756
 * @summary 6421756 JSR 199: In the method JavaCompilerTool.getTask 'options' can be supplied in the place of 'classes'
 * @author  Peter von der Ah\u00e9
 * @library ../lib
 * @compile T6421756.java
 * @run main T6421756
 */

import java.util.Collections;

public class T6421756 extends ToolTester {
    void test(String... args) {
        Iterable<String> options = Collections.singleton("-verbose");
        try {
            task = tool.getTask(null, fm, null, null, options, null);
            throw new AssertionError("Expected IllegalArgumentException!");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: got expected error " + e.getLocalizedMessage());
        }
    }
    public static void main(String... args) {
        new T6421756().test(args);
    }
}
