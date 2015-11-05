/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
 * @summary Test --no-state option
 * @bug 8135131
  * @library /tools/lib
 * @modules jdk.compiler/com.sun.tools.javac.api
 *          jdk.compiler/com.sun.tools.javac.file
 *          jdk.compiler/com.sun.tools.javac.main
 *          jdk.compiler/com.sun.tools.sjavac
 * @build Wrapper ToolBox
 * @run main Wrapper NoState
 */

import com.sun.tools.javac.util.Assert;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

public class NoState extends SJavacTester {
    public static void main(String... args) throws Exception {
        new NoState().run();
    }

    public void run() throws Exception {
        clean(TEST_ROOT);
        ToolBox tb = new ToolBox();
        tb.writeFile(GENSRC.resolve("pkg/A.java"), "package pkg; class A {}");
        Files.createDirectory(BIN);
        compile("-d", BIN.toString(),
                "--server:portfile=testserver,background=false",
                GENSRC + "/pkg/A.java");

        // Make sure file was compiled
        Assert.check(Files.exists(BIN.resolve("pkg/A.class")));

        // Make sure we have no other files (such as a javac_state file) in the bin directory
        Assert.check(countPathsInDir(BIN) == 1);
        Assert.check(countPathsInDir(BIN.resolve("pkg")) == 1);
    }

    private long countPathsInDir(Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.count();
        }
    }
}
