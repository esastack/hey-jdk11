/*
 * Copyright 1996-2002 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.rmi.server;

import java.io.*;
import java.net.*;

/**
 * An <code>RMISocketFactory</code> instance is used by the RMI runtime
 * in order to obtain client and server sockets for RMI calls.  An
 * application may use the <code>setSocketFactory</code> method to
 * request that the RMI runtime use its socket factory instance
 * instead of the default implementation.<p>
 *
 * The default socket factory implementation used goes through a
 * three-tiered approach to creating client sockets. First, a direct
 * socket connection to the remote VM is attempted.  If that fails
 * (due to a firewall), the runtime uses HTTP with the explicit port
 * number of the server.  If the firewall does not allow this type of
 * communication, then HTTP to a cgi-bin script on the server is used
 * to POST the RMI call.<p>
 *
 * @author  Ann Wollrath
 * @author  Peter Jones
 * @since   JDK1.1
 */
public abstract class RMISocketFactory
        implements RMIClientSocketFactory, RMIServerSocketFactory
{

    /** Client/server socket factory to be used by RMI runtime */
    private static RMISocketFactory factory = null;
    /** default socket factory used by this RMI implementation */
    private static RMISocketFactory defaultSocketFactory;
    /** Handler for socket creation failure */
    private static RMIFailureHandler handler = null;

    /**
     * Constructs an <code>RMISocketFactory</code>.
     * @since JDK1.1
     */
    public RMISocketFactory() {
        super();
    }

    /**
     * Creates a client socket connected to the specified host and port.
     * @param  host   the host name
     * @param  port   the port number
     * @return a socket connected to the specified host and port.
     * @exception IOException if an I/O error occurs during socket creation
     * @since JDK1.1
     */
    public abstract Socket createSocket(String host, int port)
        throws IOException;

    /**
     * Create a server socket on the specified port (port 0 indicates
     * an anonymous port).
     * @param  port the port number
     * @return the server socket on the specified port
     * @exception IOException if an I/O error occurs during server socket
     * creation
     * @since JDK1.1
     */
    public abstract ServerSocket createServerSocket(int port)
        throws IOException;

    /**
     * Set the global socket factory from which RMI gets sockets (if the
     * remote object is not associated with a specific client and/or server
     * socket factory). The RMI socket factory can only be set once. Note: The
     * RMISocketFactory may only be set if the current security manager allows
     * setting a socket factory; if disallowed, a SecurityException will be
     * thrown.
     * @param fac the socket factory
     * @exception IOException if the RMI socket factory is already set
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkSetFactory</code> method doesn't allow the operation.
     * @see #getSocketFactory
     * @see java.lang.SecurityManager#checkSetFactory()
     * @since JDK1.1
     */
    public synchronized static void setSocketFactory(RMISocketFactory fac)
        throws IOException
    {
        if (factory != null) {
            throw new SocketException("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }

    /**
     * Returns the socket factory set by the <code>setSocketFactory</code>
     * method. Returns <code>null</code> if no socket factory has been
     * set.
     * @return the socket factory
     * @see #setSocketFactory(RMISocketFactory)
     * @since JDK1.1
     */
    public synchronized static RMISocketFactory getSocketFactory()
    {
        return factory;
    }

    /**
     * Returns a reference to the default socket factory used
     * by this RMI implementation.  This will be the factory used
     * by the RMI runtime when <code>getSocketFactory</code>
     * returns <code>null</code>.
     * @return the default RMI socket factory
     * @since JDK1.1
     */
    public synchronized static RMISocketFactory getDefaultSocketFactory() {
        if (defaultSocketFactory == null) {
            defaultSocketFactory =
                new sun.rmi.transport.proxy.RMIMasterSocketFactory();
        }
        return defaultSocketFactory;
    }

    /**
     * Sets the failure handler to be called by the RMI runtime if server
     * socket creation fails.  By default, if no failure handler is installed
     * and server socket creation fails, the RMI runtime does attempt to
     * recreate the server socket.
     *
     * <p>If there is a security manager, this method first calls
     * the security manager's <code>checkSetFactory</code> method
     * to ensure the operation is allowed.
     * This could result in a <code>SecurityException</code>.
     *
     * @param fh the failure handler
     * @throws  SecurityException  if a security manager exists and its
     *          <code>checkSetFactory</code> method doesn't allow the
     *          operation.
     * @see #getFailureHandler
     * @see java.rmi.server.RMIFailureHandler#failure(Exception)
     * @since JDK1.1
     */
    public synchronized static void setFailureHandler(RMIFailureHandler fh)
    {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        handler = fh;
    }

    /**
     * Returns the handler for socket creation failure set by the
     * <code>setFailureHandler</code> method.
     * @return the failure handler
     * @see #setFailureHandler(RMIFailureHandler)
     * @since JDK1.1
     */
    public synchronized static RMIFailureHandler getFailureHandler()
    {
        return handler;
    }
}
