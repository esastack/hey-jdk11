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
import javax.tools.*;
import com.sun.tools.javac.file.*;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.util.*;

/*
 * @test
 * @bug 6625520
 * @summary javac handles missing entries on classpath badly
 */
public class T6625520 {
    public static void main(String[] args) throws Exception {
        new T6625520().run();
    }

    void run() throws Exception {
        Context c = new Context();
        DiagnosticCollector<JavaFileObject> dc =
            new DiagnosticCollector<JavaFileObject>();
        c.put(DiagnosticListener.class, dc);
        StandardJavaFileManager fm = new JavacFileManager(c, false, null);
        fm.setLocation(StandardLocation.CLASS_PATH,
                       Arrays.asList(new File("DOES_NOT_EXIST.jar")));
        FileObject fo = fm.getFileForInput(StandardLocation.CLASS_PATH,
                                           "p", "C.java");
        System.err.println(fo + "\n" + dc.getDiagnostics());
        if (dc.getDiagnostics().size() > 0)
            throw new Exception("unexpected diagnostics found");
    }
}
