/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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

/*
 * @test
 * @summary Unit test for java.net.CookieManager API
 * @bug 6277794
 * @run main/othervm -ea B6277794
 * @author Edward Wang
 */

import java.net.*;
import java.util.*;
import java.io.*;

public class B6277794 {
    public static void main(String[] args) throws Exception {
        testCookieStore();
    }

    private static void testCookieStore() throws Exception {
        CookieManager cm = new CookieManager();
        CookieStore cs = cm.getCookieStore();

        HttpCookie c1 = new HttpCookie("COOKIE1", "COOKIE1");
        HttpCookie c2 = new HttpCookie("COOKIE2", "COOKIE2");
        cs.add(new URI("http://www.sun.com/solaris"), c1);
        cs.add(new URI("http://www.sun.com/java"), c2);

        List<URI> uris = cs.getURIs();
        if (uris.size() != 1 ||
                !uris.get(0).equals(new URI("http://www.sun.com"))) {
            fail("CookieStore.getURIs() fail.");
        }
    }

    private static void fail(String msg) throws Exception {
        throw new RuntimeException(msg);
    }
}
