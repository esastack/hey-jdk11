/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * @test
 * @bug 6270015
 * @summary  Light weight HTTP server
 */

import com.sun.net.httpserver.*;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;

/* Same as Test1 but requests run in parallel.
 */

public class Test9 extends Test {

    static SSLContext ctx;
    static boolean error = false;

    public static void main (String[] args) throws Exception {
        HttpServer s1 = null;
        HttpsServer s2 = null;
        ExecutorService executor=null;
        try {
            String root = System.getProperty ("test.src")+ "/docs";
            System.out.print ("Test9: ");
            InetSocketAddress addr = new InetSocketAddress (0);
            s1 = HttpServer.create (addr, 0);
            s2 = HttpsServer.create (addr, 0);
            HttpHandler h = new FileServerHandler (root);
            HttpContext c1 = s1.createContext ("/test1", h);
            HttpContext c2 = s2.createContext ("/test1", h);
            executor = Executors.newCachedThreadPool();
            s1.setExecutor (executor);
            s2.setExecutor (executor);
            ctx = new SimpleSSLContext(System.getProperty("test.src")).get();
            s2.setHttpsConfigurator(new HttpsConfigurator (ctx));
            s1.start();
            s2.start();

            int p1 = s1.getAddress().getPort();
            int p2 = s2.getAddress().getPort();
            error = false;
            Thread[] t = new Thread[100];

            t[0] = test (true, "http", root+"/test1", p1, "smallfile.txt", 23);
            t[1] = test (true, "http", root+"/test1", p1, "largefile.txt", 2730088);
            t[2] = test (true, "https", root+"/test1", p2, "smallfile.txt", 23);
            t[3] = test (true, "https", root+"/test1", p2, "largefile.txt", 2730088);
            t[4] = test (false, "http", root+"/test1", p1, "smallfile.txt", 23);
            t[5] = test (false, "http", root+"/test1", p1, "largefile.txt", 2730088);
            t[6] = test (false, "https", root+"/test1", p2, "smallfile.txt", 23);
            t[7] = test (false, "https", root+"/test1", p2, "largefile.txt", 2730088);
            t[8] = test (true, "http", root+"/test1", p1, "smallfile.txt", 23);
            t[9] = test (true, "http", root+"/test1", p1, "largefile.txt", 2730088);
            t[10] = test (true, "https", root+"/test1", p2, "smallfile.txt", 23);
            t[11] = test (true, "https", root+"/test1", p2, "largefile.txt", 2730088);
            t[12] = test (false, "http", root+"/test1", p1, "smallfile.txt", 23);
            t[13] = test (false, "http", root+"/test1", p1, "largefile.txt", 2730088);
            t[14] = test (false, "https", root+"/test1", p2, "smallfile.txt", 23);
            t[15] = test (false, "https", root+"/test1", p2, "largefile.txt", 2730088);
            for (int i=0; i<16; i++) {
                t[i].join();
            }
            if (error) {
                throw new RuntimeException ("error");
            }

            System.out.println ("OK");
        } finally {
            delay();
            s1.stop(2);
            s2.stop(2);
            executor.shutdown ();
        }
    }

    static int foo = 1;

    static ClientThread test (boolean fixedLen, String protocol, String root, int port, String f, int size) throws Exception {
        ClientThread t = new ClientThread (fixedLen, protocol, root, port, f, size);
        t.start();
        return t;
    }

    static Object fileLock = new Object();

    static class ClientThread extends Thread {

        boolean fixedLen;
        String protocol;
        String root;
        int port;
        String f;
        int size;

        ClientThread (boolean fixedLen, String protocol, String root, int port, String f, int size) {
            this.fixedLen = fixedLen;
            this.protocol = protocol;
            this.root = root;
            this.port = port;
            this.f =  f;
            this.size = size;
        }

        public void run () {
            try {
                URL url = new URL (protocol+"://localhost:"+port+"/test1/"+f);
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                if (urlc instanceof HttpsURLConnection) {
                    HttpsURLConnection urlcs = (HttpsURLConnection) urlc;
                    urlcs.setHostnameVerifier (new HostnameVerifier () {
                        public boolean verify (String s, SSLSession s1) {
                            return true;
                        }
                    });
                    urlcs.setSSLSocketFactory (ctx.getSocketFactory());
                }
                byte [] buf = new byte [4096];

                String s = "chunk";
                if (fixedLen) {
                    urlc.setRequestProperty ("XFixed", "yes");
                    s = "fixed";
                }
                InputStream is = urlc.getInputStream();
                File temp;
                synchronized (fileLock) {
                    temp = File.createTempFile (s, null);
                    temp.deleteOnExit();
                }
                OutputStream fout = new BufferedOutputStream (new FileOutputStream(temp));
                int c, count = 0;
                while ((c=is.read(buf)) != -1) {
                    count += c;
                    fout.write (buf, 0, c);
                }
                is.close();
                fout.close();

                if (count != size) {
                    System.out.println ("wrong amount of data returned");
                    System.out.println ("fixedLen = "+fixedLen);
                    System.out.println ("protocol = "+protocol);
                    System.out.println ("root = "+root);
                    System.out.println ("port = "+port);
                    System.out.println ("f = "+f);
                    System.out.println ("size = "+size);
                    System.out.println ("temp = "+temp);
                    System.out.println ("count = "+count);
                    error = true;
                }
                String orig = root + "/" + f;
                compare (new File(orig), temp);
                temp.delete();
            } catch (IOException e) {
                error = true;
            }
        }
    }

    /* compare the contents of the two files */

    static void compare (File f1, File f2) throws IOException {
        InputStream i1 = new BufferedInputStream (new FileInputStream(f1));
        InputStream i2 = new BufferedInputStream (new FileInputStream(f2));

        int c1,c2;
        try {
            while ((c1=i1.read()) != -1) {
                c2 = i2.read();
                if (c1 != c2) {
                    throw new RuntimeException ("file compare failed 1");
                }
            }
            if (i2.read() != -1) {
                throw new RuntimeException ("file compare failed 2");
            }
        } finally {
            i1.close();
            i2.close();
        }
    }
}
