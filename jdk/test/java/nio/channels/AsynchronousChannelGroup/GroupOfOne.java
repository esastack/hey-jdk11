/*
 * Copyright 2008-2009 Sun Microsystems, Inc.  All Rights Reserved.
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
 * @bug 4607272
 * @summary Unit test for AsynchronousChannelGroup
 */

import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.net.*;
import java.util.concurrent.*;
import java.io.IOException;

/**
 * This test verifies that a channel or channel group can be closed from a
 * completion handler when there are no threads available to handle I/O events.
 */

public class GroupOfOne {

    public static void main(String[] args) throws Exception {
        // create listener to accept connections
        final AsynchronousServerSocketChannel listener =
            AsynchronousServerSocketChannel.open()
                .bind(new InetSocketAddress(0));
        listener.accept(null, new CompletionHandler<AsynchronousSocketChannel,Void>() {
            public void completed(AsynchronousSocketChannel ch, Void att) {
                listener.accept(null, this);
            }
            public void failed(Throwable exc, Void att) {
            }
            public void cancelled(Void att) {
            }
        });

        int port = ((InetSocketAddress)(listener.getLocalAddress())).getPort();
        SocketAddress sa = new InetSocketAddress(InetAddress.getLocalHost(), port);

        test(sa, true, false);
        test(sa, false, true);
        test(sa, true, true);
    }

    static void test(SocketAddress sa,
                     final boolean closeChannel,
                     final boolean shutdownGroup)
        throws Exception
    {
        // group with 1 thread
        final AsynchronousChannelGroup group = AsynchronousChannelGroup
            .withFixedThreadPool(1, new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable r) {
                    return new Thread(r);
                }});
        final AsynchronousSocketChannel ch = AsynchronousSocketChannel.open(group);

        // the latch counts down when:
        // 1. The read operation fails (expected)
        // 2. the close/shutdown completes
        final CountDownLatch latch = new CountDownLatch(2);

        ch.connect(sa, null, new CompletionHandler<Void,Void>() {
            public void completed(Void result, Void att)  {
                System.out.println("Connected");

                // initiate I/O operation that does not complete (successfully)
                ByteBuffer buf = ByteBuffer.allocate(100);
                ch.read(buf, null, new CompletionHandler<Integer,Void>() {
                    public void completed(Integer bytesRead, Void att)  {
                        throw new RuntimeException();
                    }
                    public void failed(Throwable exc, Void att) {
                        if (!(exc instanceof AsynchronousCloseException))
                            throw new RuntimeException(exc);
                        System.out.println("Read failed (expected)");
                        latch.countDown();
                    }
                    public void cancelled(Void att) {
                        throw new RuntimeException();
                    }
                });

                // close channel or shutdown group
                try {
                    if (closeChannel) {
                        System.out.print("Close channel ...");
                        ch.close();
                        System.out.println(" done.");
                    }
                    if (shutdownGroup) {
                        System.out.print("Shutdown group ...");
                        group.shutdownNow();
                        System.out.println(" done.");
                    }
                    latch.countDown();
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }
            public void failed(Throwable exc, Void att) {
                throw new RuntimeException(exc);
            }
            public void cancelled(Void att) {
                throw new RuntimeException();
            }
        });

        latch.await();

        // clean-up
        group.shutdown();
        boolean terminated = group.awaitTermination(5, TimeUnit.SECONDS);
        if (!terminated)
            throw new RuntimeException("Group did not terminate");

        System.out.println("TEST OKAY");
    }
}
