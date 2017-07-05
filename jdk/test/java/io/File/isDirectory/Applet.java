/*
 * Copyright 1998 Sun Microsystems, Inc.  All Rights Reserved.
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
   @bug 4054511
   @summary Check that applets don't get a security exception when invoking
            the File.isDirectory method on a non-existent directory
   @author Mark Reinhold
   @run applet Applet.html
 */

import java.io.*;


public class Applet extends java.applet.Applet {

    void go(String fn) {
        File f = new File(fn);
        System.err.println(fn + ": " + f.isDirectory());
    }

    public void init() {
        String nxdir = "non_EX_is_TENT_dir_EC_tory";
        go(nxdir);
        go(nxdir + File.separator + "bar" + File.separator + "baz");
    }

}
