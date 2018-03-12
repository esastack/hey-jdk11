/*
 * Copyright (c) 2013, 2018, Oracle and/or its affiliates. All rights reserved.
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
 * @bug 8008164 8169819 8183037 8182765
 * @summary Test styles on HTML tables generated by javadoc.
 * @author Bhavesh Patel
 * @library ../lib
 * @modules jdk.javadoc/jdk.javadoc.internal.tool
 * @build JavadocTester
 * @run main TestHtmlTableStyles
 */

public class TestHtmlTableStyles extends JavadocTester {

    public static void main(String... args) throws Exception {
        TestHtmlTableStyles tester = new TestHtmlTableStyles();
        tester.runTests();
    }

    @Test
    void test() {
        javadoc("-d", "out",
                "-sourcepath", testSrc,
                "-use",
                "pkg1", "pkg2");
        checkExit(Exit.ERROR);
        checkOutput(Output.OUT, true,
                "attribute not supported in HTML5: summary",
                "attribute border for table only accepts \"\" or \"1\", use CSS instead: BORDER",
                "attribute not supported in HTML5: cellpadding",
                "attribute not supported in HTML5: cellspacing",
                "attribute not supported in HTML5: align");

        checkOutput("pkg1/TestTable.html", true,
                "<table summary=\"Summary\" border cellpadding=3 cellspacing=1>",
                "<table class=\"memberSummary\">",
                "<table class=\"memberSummary\">",
                "<table class=\"memberSummary\">");

        checkOutput("pkg1/package-summary.html", true,
                "<table class=\"typeSummary\">");

        checkOutput("pkg1/class-use/TestTable.html", true,
                "<table class=\"useSummary\">");

        checkOutput("overview-summary.html", true,
                "<table class=\"overviewSummary\">");

        checkOutput("deprecated-list.html", true,
            "<table class=\"deprecatedSummary\">");

        checkOutput("constant-values.html", true,
            "<table class=\"constantsSummary\">");
    }

    @Test
    void test_html4() {
        javadoc("-d", "out-html4",
                "-html4",
                "-sourcepath", testSrc,
                "-use",
                "pkg1", "pkg2");
        checkExit(Exit.OK);

        checkOutput("pkg1/TestTable.html", true,
                "<table summary=\"Summary\" border cellpadding=3 cellspacing=1>",
                "<table class=\"memberSummary\" summary=\"Field Summary table, listing fields, "
                + "and an explanation\">",
                "<table class=\"memberSummary\" summary=\"Constructor Summary table, listing "
                + "constructors, and an explanation\">",
                "<table class=\"memberSummary\" summary=\"Method Summary table, listing methods, "
                + "and an explanation\">");

        checkOutput("pkg1/package-summary.html", true,
                "<table class=\"typeSummary\" summary=\"Class Summary table, listing classes, "
                + "and an explanation\">");

        checkOutput("pkg1/class-use/TestTable.html", true,
                "<table class=\"useSummary\" summary=\"Use table, listing fields, and an explanation\">");

        checkOutput("overview-summary.html", true,
                "<table class=\"overviewSummary\" "
                + "summary=\"Package Summary table, listing packages, and an explanation\">");

        checkOutput("deprecated-list.html", true,
            "<table class=\"deprecatedSummary\" summary=\"Methods table, listing " +
            "methods, and an explanation\">");

        checkOutput("constant-values.html", true,
            "<table class=\"constantsSummary\" summary=\"Constant Field Values table, listing " +
            "constant fields, and values\">");
    }
}
