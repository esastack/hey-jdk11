/*
 * Copyright 2001 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Tests that a thread blocked in Socket.getOutputStream().write()
 * throws a SocketException if the socket is asynchronously closed.
 */
import java.net.*;
import java.io.*;

public class Socket_getOutputStream_write extends AsyncCloseTest implements Runnable {
    Socket s;

    public String description() {
        return "Socket.getOutputStream().write()";
    }

    public void run() {
        try {
            OutputStream out = s.getOutputStream();
            for (;;) {
                byte b[] = new byte[8192];
                out.write(b);
            }
        } catch (SocketException se) {
            closed();
        } catch (Exception e) {
            failed(e.getMessage());
        }
    }

    public boolean go() throws Exception {
        ServerSocket ss = new ServerSocket(0);

        InetAddress lh = InetAddress.getLocalHost();
        s = new Socket();
        s.connect( new InetSocketAddress(lh, ss.getLocalPort()) );

        Socket s2 = ss.accept();

        Thread thr = new Thread(this);
        thr.start();

        Thread.currentThread().sleep(2000);

        s.close();

        Thread.currentThread().sleep(2000);

        if (isClosed()) {
            return true;
        } else {
            failed("getOutputStream().write() wasn't preempted");
            return false;
        }
    }
}
