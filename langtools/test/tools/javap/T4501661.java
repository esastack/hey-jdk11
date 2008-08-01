/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.*;
import java.util.*;

/*
 * @test
 * @bug 4501661
 * @summary disallow mixing -public, -private, and -protected
 */
public class T4501661 {
    public static void main(String... args) throws Exception {
        new T4501661().run();
    }

    void run() throws Exception {
        File javaFile = writeTestFile();
        File classFile = compileTestFile(javaFile);
        boolean[] values = { false, true };
        for (boolean priv: values) {
            for (boolean prot: values) {
                for (boolean publ: values) {
                    test(priv, prot, publ, classFile);
                }
            }
        }

        if (errors > 0)
            throw new Exception(errors + " errors found");
    }

    void test(boolean priv, boolean prot, boolean publ, File classFile) {
        List<String> args = new ArrayList<String>();
        if (priv)
            args.add("-private");
        if (prot)
            args.add("-protected");
        if (publ)
            args.add("-public");
        boolean expectOK = (args.size() <= 1);
        args.add(classFile.getPath());
        String out = javap(args, expectOK);
        if (out == null)
            return;
        if (!priv && !prot && !publ)
            checkNone(out, "private");
        if (prot)
            checkNone(out, "private", "package");
        if (publ)
            checkNone(out, "private", "package", "protected");
    }

    File writeTestFile() throws IOException {
        File f = new File("Test.java");
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        out.println("abstract class Test { ");
        out.println("  public void public_m() { }");
        out.println("  protected void protected_m() { }");
        out.println("  private void private_m() { }");
        out.println("  void package_m() { }");
        out.println("}");
        out.close();
        return f;
    }

    File compileTestFile(File f) {
        int rc = com.sun.tools.javac.Main.compile(new String[] { "-g", f.getPath() });
        if (rc != 0)
            throw new Error("compilation failed. rc=" + rc);
        String path = f.getPath();
        return new File(path.substring(0, path.length() - 5) + ".class");
    }

    String javap(List<String> args, boolean expectOK) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args.toArray(new String[args.size()]), pw);
        System.err.println(args);
        System.err.println(sw);
        if (expectOK) {
            if (rc == 0)
                return sw.toString();
            else
                error("javap failed unexpectedly; rc=" + rc + "\n" + sw);
        } else {
            if (rc == 0)
                error("javap succeeded unexpectedly");
        }
        return null;
    }

    void checkNone(String log, String... words) {
        for (String word: words) {
            if (log.indexOf(word) != -1)
                error("\"" + word + "\" unexpectedly found in output");
        }
    }

    void error(String msg) {
        System.err.println("error: " + msg);
        errors++;
    }

    int errors;
}
