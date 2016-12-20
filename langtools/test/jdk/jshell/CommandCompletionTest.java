/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8144095 8164825 8169818 8153402
 * @summary Test Command Completion
 * @modules jdk.compiler/com.sun.tools.javac.api
 *          jdk.compiler/com.sun.tools.javac.main
 *          jdk.jdeps/com.sun.tools.javap
 *          jdk.jshell/jdk.internal.jshell.tool
 * @library /tools/lib
 * @build toolbox.ToolBox toolbox.JarTask toolbox.JavacTask
 * @build ReplToolTesting TestingInputStream Compiler
 * @run testng CommandCompletionTest
 */

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.testng.annotations.Test;
import jdk.internal.jshell.tool.JShellTool;
import jdk.internal.jshell.tool.JShellToolBuilder;
import jdk.jshell.SourceCodeAnalysis.Suggestion;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class CommandCompletionTest extends ReplToolTesting {


    private JShellTool repl;

    @Override
    protected void testRawRun(Locale locale, String[] args) {
        repl = ((JShellToolBuilder) builder(locale))
                .rawTool();
        try {
            repl.start(args);
        } catch (Exception ex) {
            fail("Repl tool died with exception", ex);
        }
    }

    public void assertCompletion(boolean after, String code, boolean isSmart, String... expected) {
        if (!after) {
            setCommandInput("\n");
        } else {
            assertCompletion(code, isSmart, expected);
        }
    }

    public void assertCompletion(String code, boolean isSmart, String... expected) {
        List<String> completions = computeCompletions(code, isSmart);
        assertEquals(completions, Arrays.asList(expected), "Command: " + code + ", output: " +
                completions.toString());
    }

    private List<String> computeCompletions(String code, boolean isSmart) {
        int cursor =  code.indexOf('|');
        code = code.replace("|", "");
        assertTrue(cursor > -1, "'|' not found: " + code);
        List<Suggestion> completions =
                repl.commandCompletionSuggestions(code, cursor, new int[] {-1}); //XXX: ignoring anchor for now
        return completions.stream()
                          .filter(s -> isSmart == s.matchesType())
                          .map(s -> s.continuation())
                          .distinct()
                          .collect(Collectors.toList());
    }

    @Test
    public void testCommand() {
        testNoStartUp(
                a -> assertCompletion(a, "/deb|", false),
                a -> assertCompletion(a, "/re|", false, "/reload ", "/reset "),
                a -> assertCompletion(a, "/h|", false, "/help ", "/history ")
        );
    }

    @Test
    public void testList() {
        test(false, new String[] {"--no-startup"},
                a -> assertCompletion(a, "/l|", false, "/list "),
                a -> assertCompletion(a, "/list |", false, "-all", "-history", "-start "),
                a -> assertCompletion(a, "/list -h|", false, "-history"),
                a -> assertCompletion(a, "/list q|", false),
                a -> assertVariable(a, "int", "xray"),
                a -> assertCompletion(a, "/list |", false, "-all", "-history", "-start ", "1 ", "xray "),
                a -> assertCompletion(a, "/list x|", false, "xray "),
                a -> assertCompletion(a, "/list xray |", false)
        );
    }

    @Test
    public void testDrop() {
        test(false, new String[] {"--no-startup"},
                a -> assertCompletion(a, "/d|", false, "/drop "),
                a -> assertClass(a, "class cTest {}", "class", "cTest"),
                a -> assertMethod(a, "int mTest() { return 0; }", "()I", "mTest"),
                a -> assertVariable(a, "int", "fTest"),
                a -> assertCompletion(a, "/drop |", false, "1 ", "2 ", "3 ", "cTest ", "fTest ", "mTest "),
                a -> assertCompletion(a, "/drop f|", false, "fTest ")
        );
    }

    @Test
    public void testEdit() {
        test(false, new String[]{"--no-startup"},
                a -> assertCompletion(a, "/e|", false, "/edit ", "/exit "),
                a -> assertCompletion(a, "/ed|", false, "/edit "),
                a -> assertClass(a, "class cTest {}", "class", "cTest"),
                a -> assertMethod(a, "int mTest() { return 0; }", "()I", "mTest"),
                a -> assertVariable(a, "int", "fTest"),
                a -> assertCompletion(a, "/edit |", false,
                        "-all" , "-start " , "1 ", "2 ", "3 ", "cTest ", "fTest ", "mTest "),
                a -> assertCompletion(a, "/edit cTest |", false,
                        "2 ", "3 ", "fTest ", "mTest "),
                a -> assertCompletion(a, "/edit 1 fTest |", false,
                        "2 ", "mTest "),
                a -> assertCompletion(a, "/edit f|", false, "fTest "),
                a -> assertCompletion(a, "/edit mTest f|", false, "fTest ")
        );
    }

    @Test
    public void testHelp() {
        testNoStartUp(
                a -> assertCompletion(a, "/help |", false,
                "/! ", "/-<n> ", "/<id> ", "/? ", "/classpath ", "/drop ",
                "/edit ", "/exit ", "/help ", "/history ", "/imports ",
                "/list ", "/methods ", "/open ", "/reload ", "/reset ",
                "/save ", "/set ", "/types ", "/vars ", "intro ", "shortcuts "),
                a -> assertCompletion(a, "/? |", false,
                "/! ", "/-<n> ", "/<id> ", "/? ", "/classpath ", "/drop ",
                "/edit ", "/exit ", "/help ", "/history ", "/imports ",
                "/list ", "/methods ", "/open ", "/reload ", "/reset ",
                "/save ", "/set ", "/types ", "/vars ", "intro ", "shortcuts "),
                a -> assertCompletion(a, "/help /s|", false,
                "/save ", "/set "),
                a -> assertCompletion(a, "/help /set |", false,
                "editor", "feedback", "format", "mode", "prompt", "start", "truncation"),
                a -> assertCompletion(a, "/help /edit |", false)
        );
    }

    @Test
    public void testReload() {
        testNoStartUp(
                a -> assertCompletion(a, "/reload |", false, "-quiet ", "-restore "),
                a -> assertCompletion(a, "/reload -restore |", false, "-quiet"),
                a -> assertCompletion(a, "/reload -quiet |", false, "-restore"),
                a -> assertCompletion(a, "/reload -restore -quiet |", false)
        );
    }

    @Test
    public void testVarsMethodsTypes() {
        test(false, new String[]{"--no-startup"},
                a -> assertCompletion(a, "/v|", false, "/vars "),
                a -> assertCompletion(a, "/m|", false, "/methods "),
                a -> assertCompletion(a, "/t|", false, "/types "),
                a -> assertClass(a, "class cTest {}", "class", "cTest"),
                a -> assertMethod(a, "int mTest() { return 0; }", "()I", "mTest"),
                a -> assertVariable(a, "int", "fTest"),
                a -> assertCompletion(a, "/vars |", false, "-all", "-start ", "3 ", "fTest "),
                a -> assertCompletion(a, "/meth |", false, "-all", "-start ", "2 ", "mTest "),
                a -> assertCompletion(a, "/typ |", false, "-all", "-start ", "1 ", "cTest "),
                a -> assertCompletion(a, "/var f|", false, "fTest ")
        );
    }

    @Test
    public void testOpen() throws IOException {
        Compiler compiler = new Compiler();
        testNoStartUp(
                a -> assertCompletion(a, "/o|", false, "/open ")
        );
        List<String> p1 = listFiles(Paths.get(""));
        getRootDirectories().forEach(s -> p1.add(s.toString()));
        Collections.sort(p1);
        testNoStartUp(
                a -> assertCompletion(a, "/open |", false, p1.toArray(new String[p1.size()]))
        );
        Path classDir = compiler.getClassDir();
        List<String> p2 = listFiles(classDir);
        testNoStartUp(
                a -> assertCompletion(a, "/open " + classDir + "/|", false, p2.toArray(new String[p2.size()]))
        );
    }

    @Test
    public void testSave() throws IOException {
        Compiler compiler = new Compiler();
        testNoStartUp(
                a -> assertCompletion(a, "/s|", false, "/save ", "/set ")
        );
        List<String> p1 = listFiles(Paths.get(""));
        Collections.addAll(p1, "-all ", "-history ", "-start ");
        getRootDirectories().forEach(s -> p1.add(s.toString()));
        Collections.sort(p1);
        testNoStartUp(
                a -> assertCompletion(a, "/save |", false, p1.toArray(new String[p1.size()]))
        );
        Path classDir = compiler.getClassDir();
        List<String> p2 = listFiles(classDir);
        testNoStartUp(
                a -> assertCompletion(a, "/save " + classDir + "/|",
                false, p2.toArray(new String[p2.size()])),
                a -> assertCompletion(a, "/save -all " + classDir + "/|",
                false, p2.toArray(new String[p2.size()]))
        );
    }

    @Test
    public void testClassPath() throws IOException {
        testNoStartUp(
                a -> assertCompletion(a, "/classp|", false, "/classpath ")
        );
        Compiler compiler = new Compiler();
        Path outDir = compiler.getPath("testClasspathCompletion");
        Files.createDirectories(outDir);
        Files.createDirectories(outDir.resolve("dir"));
        createIfNeeded(outDir.resolve("test.jar"));
        createIfNeeded(outDir.resolve("test.zip"));
        compiler.compile(outDir, "package pkg; public class A { public String toString() { return \"A\"; } }");
        String jarName = "test.jar";
        compiler.jar(outDir, jarName, "pkg/A.class");
        compiler.getPath(outDir).resolve(jarName);
        List<String> paths = listFiles(outDir, CLASSPATH_FILTER);
        testNoStartUp(
                a -> assertCompletion(a, "/classpath " + outDir + "/|", false, paths.toArray(new String[paths.size()]))
        );
    }

    @Test
    public void testUserHome() throws IOException {
        List<String> completions;
        Path home = Paths.get(System.getProperty("user.home"));
        try (Stream<Path> content = Files.list(home)) {
            completions = content.filter(CLASSPATH_FILTER)
                                 .map(file -> file.getFileName().toString() + (Files.isDirectory(file) ? "/" : ""))
                                 .sorted()
                                 .collect(Collectors.toList());
        }
        testNoStartUp(
                a -> assertCompletion(a, "/classpath ~/|", false, completions.toArray(new String[completions.size()]))
        );
    }

    @Test
    public void testSet() throws IOException {
        List<String> p1 = listFiles(Paths.get(""));
        getRootDirectories().forEach(s -> p1.add(s.toString()));
        Collections.sort(p1);

        String[] modes = {"concise ", "normal ", "silent ", "verbose "};
        String[] options = {"-command", "-delete", "-quiet"};
        String[] modesWithOptions = Stream.concat(Arrays.stream(options), Arrays.stream(modes)).sorted().toArray(String[]::new);
        test(false, new String[] {"--no-startup"},
                a -> assertCompletion(a, "/se|", false, "/set "),
                a -> assertCompletion(a, "/set |", false, "editor ", "feedback ", "format ", "mode ", "prompt ", "start ", "truncation "),

                // /set editor
                a -> assertCompletion(a, "/set e|", false, "editor "),
                a -> assertCompletion(a, "/set editor |", false, p1.toArray(new String[p1.size()])),

                // /set feedback
                a -> assertCompletion(a, "/set fe|", false, "feedback "),
                a -> assertCompletion(a, "/set fe |", false, modes),

                // /set format
                a -> assertCompletion(a, "/set fo|", false, "format "),
                a -> assertCompletion(a, "/set fo |", false, modes),

                // /set mode
                a -> assertCompletion(a, "/set mo|", false, "mode "),
                a -> assertCompletion(a, "/set mo |", false),
                a -> assertCompletion(a, "/set mo newmode |", false, modesWithOptions),
                a -> assertCompletion(a, "/set mo newmode -|", false, options),
                a -> assertCompletion(a, "/set mo newmode -command |", false),
                a -> assertCompletion(a, "/set mo newmode normal |", false, options),

                // /set prompt
                a -> assertCompletion(a, "/set pro|", false, "prompt "),
                a -> assertCompletion(a, "/set pro |", false, modes),

                // /set start
                a -> assertCompletion(a, "/set st|", false, "start "),
                a -> assertCompletion(a, "/set st |", false, p1.toArray(new String[p1.size()])),

                // /set truncation
                a -> assertCompletion(a, "/set tr|", false, "truncation "),
                a -> assertCompletion(a, "/set tr |", false, modes)
        );
    }

    private void createIfNeeded(Path file) throws IOException {
        if (!Files.exists(file))
            Files.createFile(file);
    }
    private List<String> listFiles(Path path) throws IOException {
        return listFiles(path, ACCEPT_ALL);
    }

    private List<String> listFiles(Path path, Predicate<? super Path> filter) throws IOException {
        try (Stream<Path> stream = Files.list(path)) {
            return stream.filter(filter)
                         .map(p -> p.getFileName().toString() + (Files.isDirectory(p) ? "/" : ""))
                         .sorted()
                         .collect(Collectors.toList());
        }
    }

    private static final Predicate<? super Path> ACCEPT_ALL =
            (file) -> !file.endsWith(".") && !file.endsWith("..");

    private static final Predicate<? super Path> CLASSPATH_FILTER =
            (file) -> ACCEPT_ALL.test(file) &&
                    (Files.isDirectory(file) ||
                     file.getFileName().toString().endsWith(".jar") ||
                     file.getFileName().toString().endsWith(".zip"));

    private static Iterable<? extends Path> getRootDirectories() {
        return StreamSupport.stream(FileSystems.getDefault()
                                               .getRootDirectories()
                                               .spliterator(),
                                    false)
                            .filter(p -> Files.exists(p))
                            .collect(Collectors.toList());
    }
}
