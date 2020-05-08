/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

/**
 * @test
 * @bug 8209790
 * @summary Checks ability for connecting to debug server (jstack, jmap, jinfo, jsnap)
 * @requires vm.hasSAandCanAttach
 * @requires os.family != "windows"
 * @modules java.base/jdk.internal.misc
 * @library /test/lib
 *
 * @run main/othervm DebugdConnectTest
 */

import java.io.IOException;

import jdk.test.lib.JDKToolLauncher;
import jdk.test.lib.apps.LingeredApp;
import jdk.test.lib.process.OutputAnalyzer;


public class DebugdConnectTest {

    private static OutputAnalyzer runJHSDB(String command, String id) throws IOException, InterruptedException {
        JDKToolLauncher jhsdbLauncher = JDKToolLauncher.createUsingTestJDK("jhsdb");
        jhsdbLauncher.addToolArg(command);
        jhsdbLauncher.addToolArg("--connect");
        if (id != null) {
            jhsdbLauncher.addToolArg(id + "@localhost");
        } else {
            jhsdbLauncher.addToolArg("localhost");
        }

        Process jhsdb = (new ProcessBuilder(jhsdbLauncher.getCommand())).start();
        OutputAnalyzer out = new OutputAnalyzer(jhsdb);

        jhsdb.waitFor();

        System.out.println(out.getStdout());
        System.err.println(out.getStderr());

        return out;
    }

    private static void runJSTACK(String id) throws IOException, InterruptedException {
        OutputAnalyzer out = runJHSDB("jstack", id);

        out.shouldContain("LingeredApp");
        out.stderrShouldBeEmpty();
        out.shouldHaveExitValue(0);
    }

    private static void runJMAP(String id) throws IOException, InterruptedException {
        OutputAnalyzer out = runJHSDB("jmap", id);

        out.shouldContain("JVM version is");
        out.stderrShouldBeEmpty();
        out.shouldHaveExitValue(0);
    }

    private static void runJINFO(String id) throws IOException, InterruptedException {
        OutputAnalyzer out = runJHSDB("jinfo", id);

        out.shouldContain("Java System Properties:");
        out.stderrShouldBeEmpty();
        out.shouldHaveExitValue(0);
    }

    private static void runJSNAP(String id) throws IOException, InterruptedException {
        OutputAnalyzer out = runJHSDB("jsnap", id);

        out.shouldContain("java.vm.name=");
        out.stderrShouldBeEmpty();
        out.shouldHaveExitValue(0);
    }

    private static void runTests(String id, long debuggeePid) throws IOException, InterruptedException {
        DebugdUtils debugd = new DebugdUtils(id);
        debugd.attach(debuggeePid);

        try {
            runJSTACK(id);
            runJMAP(id);
            runJINFO(id);
            runJSNAP(id);
        } finally {
            debugd.detach();
        }
    }

    public static void main(String[] args) throws Exception {
        LingeredApp app = null;

        try {
            app = LingeredApp.startApp();
            System.out.println("Started LingeredApp with pid " + app.getPid());

            System.out.println("debugd connection test with server id:");
            runTests("test", app.getPid());

            System.out.println("debugd connection test without server id:");
            runTests(null, app.getPid());
        } finally {
            LingeredApp.stopApp(app);
        }

    }

}
