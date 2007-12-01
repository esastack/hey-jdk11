/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.corba.se.impl.transport;

import java.io.IOException;

import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ReaderThread;
import com.sun.corba.se.pept.transport.Selector;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.threadpool.Work;

import com.sun.corba.se.impl.orbutil.ORBUtility;

public class ReaderThreadImpl
    implements
        ReaderThread,
        Work
{
    private ORB orb;
    private Connection connection;
    private Selector selector;
    private boolean keepRunning;
    private long enqueueTime;

    public ReaderThreadImpl(ORB orb,
                            Connection connection, Selector selector)
    {
        this.orb = orb;
        this.connection = connection;
        this.selector = selector;
        keepRunning = true;
    }

    ////////////////////////////////////////////////////
    //
    // ReaderThread methods.
    //

    public Connection getConnection()
    {
        return connection;
    }

    public void close()
    {
        if (orb.transportDebugFlag) {
            dprint(".close: " + connection);
        }

        keepRunning = false;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    // REVISIT - this needs alot more from previous ReaderThread.
    public void doWork()
    {
        try {
            if (orb.transportDebugFlag) {
                dprint(".doWork: Start ReaderThread: " + connection);
            }
            while (keepRunning) {
                try {

                    if (orb.transportDebugFlag) {
                        dprint(".doWork: Start ReaderThread cycle: "
                               + connection);
                    }

                    if (connection.read()) {
                        // REVISIT - put in pool;
                        return;
                    }

                    if (orb.transportDebugFlag) {
                        dprint(".doWork: End ReaderThread cycle: "
                               + connection);
                    }

                } catch (Throwable t) {
                    if (orb.transportDebugFlag) {
                        dprint(".doWork: exception in read: " + connection,t);
                    }
                    orb.getTransportManager().getSelector(0)
                        .unregisterForEvent(getConnection().getEventHandler());
                    getConnection().close();
                }
            }
        } finally {
            if (orb.transportDebugFlag) {
                dprint(".doWork: Terminated ReaderThread: " + connection);
            }
        }
    }

    public void setEnqueueTime(long timeInMillis)
    {
        enqueueTime = timeInMillis;
    }

    public long getEnqueueTime()
    {
        return enqueueTime;
    }

    public String getName() { return "ReaderThread"; }

    ////////////////////////////////////////////////////
    //
    // Implementation.
    //

    private void dprint(String msg)
    {
        ORBUtility.dprint("ReaderThreadImpl", msg);
    }

    protected void dprint(String msg, Throwable t)
    {
        dprint(msg);
        t.printStackTrace(System.out);
    }
}

// End of file.
