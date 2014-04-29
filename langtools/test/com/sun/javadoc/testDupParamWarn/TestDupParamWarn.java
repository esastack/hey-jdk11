/*
 * Copyright (c) 2002, 2014, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 4745855
 * @summary Test to ensure that the doclet does not print out bad
 * warning messages about duplicate param tags.
 * @author jamieh
 * @library ../lib/
 * @build JavadocTester
 * @build TestDupParamWarn
 * @run main TestDupParamWarn
 */

public class TestDupParamWarn extends JavadocTester {

    private static final String[] ARGS =
        new String[] {"-d", OUTPUT_DIR, "-sourcepath",
                SRC_DIR + "/", "pkg"};
    private static final String[][] NEGATED_TEST =
        new String[][] {{WARNING_OUTPUT,
            "Parameter \"a\" is documented more than once."}};

    /**
     * The entry point of the test.
     * @param args the array of command line arguments.
     */
    public static void main(String[] args) {
        JavadocTester tester = new TestDupParamWarn();
        tester.run(ARGS, NO_TEST, NEGATED_TEST);
        tester.printSummary();
    }
}
