/*
 * Copyright 2002-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 */

package sun.nio.ch;

import java.io.IOException;
import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.*;

/**
 * Pipe.SourceChannel implementation based on socket connection.
 */

class SourceChannelImpl
    extends Pipe.SourceChannel
    implements SelChImpl
{
    // The SocketChannel assoicated with this pipe
    SocketChannel sc;

    public FileDescriptor getFD() {
        return ((SocketChannelImpl) sc).getFD();
    }

    public int getFDVal() {
        return ((SocketChannelImpl) sc).getFDVal();
    }

    SourceChannelImpl(SelectorProvider sp, SocketChannel sc) {
        super(sp);
        this.sc = sc;
    }

    protected void implCloseSelectableChannel() throws IOException {
        if (!isRegistered())
            kill();
    }

    public void kill() throws IOException {
        sc.close();
    }

    protected void implConfigureBlocking(boolean block) throws IOException {
        sc.configureBlocking(block);
    }

    public boolean translateReadyOps(int ops, int initialOps,
                                     SelectionKeyImpl sk) {
        int intOps = sk.nioInterestOps(); // Do this just once, it synchronizes
        int oldOps = sk.nioReadyOps();
        int newOps = initialOps;

        if ((ops & PollArrayWrapper.POLLNVAL) != 0)
            throw new Error("POLLNVAL detected");

        if ((ops & (PollArrayWrapper.POLLERR
                    | PollArrayWrapper.POLLHUP)) != 0) {
            newOps = intOps;
            sk.nioReadyOps(newOps);
            return (newOps & ~oldOps) != 0;
        }

        if (((ops & PollArrayWrapper.POLLIN) != 0) &&
            ((intOps & SelectionKey.OP_READ) != 0))
            newOps |= SelectionKey.OP_READ;

        sk.nioReadyOps(newOps);
        return (newOps & ~oldOps) != 0;
    }

    public boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, sk.nioReadyOps(), sk);
    }

    public boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
        return translateReadyOps(ops, 0, sk);
    }

    public void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
        if ((ops & SelectionKey.OP_READ) != 0)
            ops = PollArrayWrapper.POLLIN;
        sk.selector.putEventOps(sk, ops);
    }

    public int read(ByteBuffer dst) throws IOException {
        try {
            return sc.read(dst);
        } catch (AsynchronousCloseException x) {
            close();
            throw x;
        }
    }

    public long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException
    {
        if ((offset < 0) || (length < 0) || (offset > dsts.length - length))
           throw new IndexOutOfBoundsException();
        try {
            return read(Util.subsequence(dsts, offset, length));
        } catch (AsynchronousCloseException x) {
            close();
            throw x;
        }
    }

    public long read(ByteBuffer[] dsts) throws IOException {
        try {
            return sc.read(dsts);
        } catch (AsynchronousCloseException x) {
            close();
            throw x;
        }
    }

}
