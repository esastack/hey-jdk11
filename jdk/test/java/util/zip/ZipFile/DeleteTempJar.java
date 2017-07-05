/*
 * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

/* @test
   @bug 4166799
   @summary Make sure URL-downloaded jar files (jar_cache files)
            will be deleted when VM exits.

   @build DeleteTempJar
   @run shell deletetempjar.sh
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.JarURLConnection;
import java.net.URL;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import com.sun.net.httpserver.*;


public class DeleteTempJar
{
    public static void realMain(String args[]) throws Exception
    {
        final File zf = File.createTempFile("deletetemp", ".jar");
        zf.deleteOnExit();
        JarOutputStream jos = new JarOutputStream(
                new FileOutputStream(zf));
        JarEntry je = new JarEntry("entry");
        jos.putNextEntry(je);
        jos.write("hello, world".getBytes("ASCII"));
        jos.close();

        HttpServer server = HttpServer.create(
                new InetSocketAddress((InetAddress) null, 0), 0);
        HttpContext context = server.createContext("/",
            new HttpHandler() {
                public void handle(HttpExchange e) {
                try {
                    FileInputStream fis = new FileInputStream(zf);
                    e.sendResponseHeaders(200, zf.length());
                    OutputStream os = e.getResponseBody();
                    byte[] buf = new byte[1024];
                    int count = 0;
                    while ((count = fis.read(buf)) != -1) {
                        os.write(buf, 0, count);
                    }
                    fis.close();
                    e.close();
                } catch (Exception ex) {
                    unexpected(ex);
                }
            }
        });
        server.start();

        URL url = new URL("jar:http://localhost:"
                          + new Integer(server.getAddress().getPort()).toString()
                          + "/deletetemp.jar!/");
        JarURLConnection c = (JarURLConnection)url.openConnection();
        JarFile f = c.getJarFile();
        check(f.getEntry("entry") != null);
        System.out.println(f.getName());
        server.stop(0);
    }

    //--------------------- Infrastructure ---------------------------
    static volatile int passed = 0, failed = 0;
    static boolean pass() {passed++; return true;}
    static boolean fail() {failed++; Thread.dumpStack(); return false;}
    static boolean fail(String msg) {System.out.println(msg); return fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static boolean check(boolean cond) {if (cond) pass(); else fail(); return cond;}
    static boolean equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) return pass();
        else return fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
