/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8006298
 * @summary Setting an invalid value for a bool argument should result in a useful error message
 * @library /testlibrary
 * @modules java.base/sun.misc
 *          java.management
 */

import com.oracle.java.testlibrary.*;

public class BooleanFlagWithInvalidValue {
  public static void main(String[] args) throws Exception {
    ProcessBuilder pb = ProcessTools.createJavaProcessBuilder(
        "-XX:+PrintWarnings=8", "-version");

    OutputAnalyzer output = new OutputAnalyzer(pb.start());
    output.shouldContain("Improperly specified VM option 'PrintWarnings=8'");
    output.shouldHaveExitValue(1);

    pb = ProcessTools.createJavaProcessBuilder(
        "-XX:-PrintWarnings=8", "-version");

    output = new OutputAnalyzer(pb.start());
    output.shouldContain("Improperly specified VM option 'PrintWarnings=8'");
    output.shouldHaveExitValue(1);
  }
}
