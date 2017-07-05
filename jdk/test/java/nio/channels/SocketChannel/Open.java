/*
 * Copyright 2002-2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 4614065
 * @summary Test SocketChannel gc after running out of fds
 * @build Open
 * @run shell Open.sh
 */

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;

public class Open {

    static void test1() {
        for (int i=0; i<11000; i++) {
            try {
                SocketChannel sc = SocketChannel.open();
            } catch (Exception e) {
                // Presumably "Too many open files"
            }
        }
    }
    static void test2() {
        for (int i=0; i<11000; i++) {
            try {
                DatagramChannel sc = DatagramChannel.open();
            } catch (Exception e) {
                // Presumably "Too many open files"
            }
        }
    }
    static void test3() {
        SelectorProvider sp = SelectorProvider.provider();
        for (int i=0; i<11000; i++) {
            try {
                Pipe p = sp.openPipe();
            } catch (Exception e) {
                // Presumably "Too many open files"
            }
        }
    }
    static void test4() {
        for (int i=0; i<11000; i++) {
            try {
                ServerSocketChannel sc = ServerSocketChannel.open();
            } catch (Exception e) {
                // Presumably "Too many open files"
            }
        }
    }

    public static void main(String[] args) throws Exception {

        // Load necessary classes ahead of time
        DatagramChannel dc = DatagramChannel.open();
        Exception se = new SocketException();
        SelectorProvider sp = SelectorProvider.provider();
        Pipe p = sp.openPipe();
        ServerSocketChannel ssc = ServerSocketChannel.open();

        test1();
        test2();
        test3();
        test4();
    }

}
