/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 6403933
 * @summary POLLHUP or POLLERR on "idle" key can cause Selector to spin
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.nio.channels.*;

public class CloseWhenKeyIdle {

    // indicates if the wakeup has happened
    static volatile boolean wakeupDone = false;

    // Wakes up a Selector after a given delay
    static class Waker implements Runnable {
        private Selector sel;
        private long delay;

        Waker(Selector sel, long delay) {
            this.sel = sel;
            this.delay = delay;
        }

        public void run() {
            try {
                Thread.sleep(delay);
                wakeupDone = true;
                sel.wakeup();
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws Exception {

        // Skip test on pre-2.6 kernels until the poll SelectorProvider
        // is updated
        String osname = System.getProperty("os.name");
        if (osname.equals("Linux")) {
            String[] ver = System.getProperty("os.version").split("\\.", 0);
            if (ver.length >=2 ) {
                int major = Integer.parseInt(ver[0]);
                int minor = Integer.parseInt(ver[1]);
                if (major < 2 || (major == 2 && minor < 6)) {
                    System.out.println("Test passing on pre-2.6 kernel");
                    return;
                }
            }
        }


        // establish loopback connection

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(0));

        SocketAddress remote = new InetSocketAddress(InetAddress.getLocalHost(),
            ssc.socket().getLocalPort());

        SocketChannel sc1 = SocketChannel.open(remote);
        SocketChannel sc2 = ssc.accept();

        // register channel for one end with a Selector, interest set = 0

        Selector sel = Selector.open();

        sc1.configureBlocking(false);
        SelectionKey k = sc1.register(sel, 0);
        sel.selectNow();

        // hard close to provoke POLLHUP

        sc2.socket().setSoLinger(true, 0);
        sc2.close();

        // schedule wakeup after a few seconds

        Thread t = new Thread(new Waker(sel, 5000));
        t.setDaemon(true);
        t.start();

        // select should block

        int spinCount = 0;
        for (;;) {
            int n = sel.select();
            if (n > 0)
                throw new RuntimeException("channel should not be selected");

            // wakeup
            if (wakeupDone)
                break;

            // wakeup for no reason - if it happens a few times then we have a
            // problem
            spinCount++;
            if (spinCount >= 3)
                throw new RuntimeException("Selector appears to be spinning");
        }

        System.out.println("PASS");
    }

}
