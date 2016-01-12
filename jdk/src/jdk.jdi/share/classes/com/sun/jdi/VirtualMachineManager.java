/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.jdi;

import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.Connection;
import java.util.List;
import java.io.IOException;

/**
 * A manager of connections to target virtual machines. The
 * VirtualMachineManager allows one application to debug
 * multiple target VMs. (Note that the converse is not
 * supported; a target VM can be debugged by only one
 * debugger application.) This interface
 * contains methods to manage connections
 * to remote target VMs and to obtain the {@link VirtualMachine}
 * mirror for available target VMs.
 * <p>
 * Connections can be made using one of several different
 * {@link com.sun.jdi.connect.Connector} objects. Each connector encapsulates
 * a different way of connecting the debugger with a target VM.
 * <p>
 * The VirtualMachineManager supports many different scenarios for
 * connecting a debugger to a virtual machine. Four examples
 * are presented in the table below. The
 * examples use the command line syntax in Sun's implementation.
 * Some {@link com.sun.jdi.connect.Connector} implementations may require slightly
 * different handling than presented below.
 *
 * <TABLE BORDER WIDTH="75%" SUMMARY="Four scenarios for connecting a debugger
 *  to a virtual machine">
 * <TR>
 * <TH scope=col>Scenario</TH>
 * <TH scope=col>Description</TH>
 * <TR>
 * <TD>Debugger launches target VM (simplest, most-common scenario)</TD>
 *
 * <TD>Debugger calls the
 * {@link com.sun.jdi.connect.LaunchingConnector#launch(java.util.Map)}
 * method of the default connector, obtained with {@link #defaultConnector}. The
 * target VM is launched, and a connection between that VM and the
 * debugger is established. A {@link VirtualMachine} mirror is returned.
 * <P>Or, for more control
 * <UL>
 * <LI>
 * Debugger selects a connector from the list returned by
 * {@link #launchingConnectors} with desired characteristics
 * (for example, transport type, etc.).
 * <LI>
 * Debugger calls the
 * {@link com.sun.jdi.connect.LaunchingConnector#launch(java.util.Map)}
 * method of the selected connector. The
 * target VM is launched, and a connection between that VM and the
 * debugger is established. A {@link VirtualMachine} mirror is returned.
 * </UL>
 * </TD>
 * </TR>
 * <TR>
 * <TD>Debugger attaches to previously-running VM</TD>
 * <TD>
 * <UL>
 * <LI>
 * Target VM is launched using the options
 * {@code -agentlib:jdwp=transport=xxx,server=y}
 * </LI>
 * <LI>
 * Target VM generates and outputs the tranport-specific address at which it will
 * listen for a connection.</LI>
 * <LI>
 * Debugger is launched. Debugger selects a connector in the list
 * returned by {@link #attachingConnectors} matching the transport with
 * the name "xxx".
 * <LI>
 * Debugger presents the default connector parameters (obtained through
 * {@link com.sun.jdi.connect.Connector#defaultArguments()}) to the end user,
 * allowing the user to
 * fill in the transport-specific address generated by the target VM.
 * <LI>
 * Debugger calls the {@link com.sun.jdi.connect.AttachingConnector#attach(java.util.Map)} method
 * of the selected to attach to the target VM. A {@link VirtualMachine}
 * mirror is returned.
 * </UL>
 * </TD>
 * </TR>
 *
 * <TR>
 * <TD>Target VM attaches to previously-running debugger</TD>
 * <TD>
 * <UL>
 * <LI>
 * At startup, debugger selects one or more connectors from
 * the list returned by {@link #listeningConnectors} for one or more
 * transports.</LI>
 * <LI>
 * Debugger calls the {@link com.sun.jdi.connect.ListeningConnector#startListening(java.util.Map)} method for each selected
 * connector. For each call, a transport-specific address string is
 * generated and returned. The debugger makes the transport names and
 * corresponding address strings available to the end user.
 * <LI>
 * Debugger calls
 * {@link com.sun.jdi.connect.ListeningConnector#accept(java.util.Map)}
 * for each selected connector to wait for
 * a target VM to connect.</LI>
 * <LI>
 * Later, target VM is launched by end user with the options
 * {@code -agentlib:jdwp=transport=xxx,address=yyy}
 * where "xxx" the transport for one of the connectors selected by the
 * the debugger and "yyy"
 * is the address generated by
 * {@link com.sun.jdi.connect.ListeningConnector#accept(java.util.Map)} for that
 * transport.</LI>
 * <LI>
 * Debugger's call to {@link com.sun.jdi.connect.ListeningConnector#accept(java.util.Map)} returns
 * a {@link VirtualMachine} mirror.</LI>
 * </UL>
 * </TD>
 * </TR>
 *
 * <TR>
 * <TD>Target VM launches debugger (sometimes called "Just-In-Time" debugging)</TD>
 * <TD>
 * <UL>
 * <LI>
 * Target VM is launched with the options
 * {@code -agentlib:jdwp=launch=cmdline,onuncaught=y,transport=xxx,server=y}
 * </LI>
 * <LI>
 * Later, an uncaught exception is thrown in the target VM. The target
 * VM generates the tranport-specific address at which it will
 * listen for a connection.
 * <LI>Target VM launches the debugger with the following items concatenated
 * together (separated by spaces) to form the command line:
 * <UL>
 * <LI> The launch= value
 * <LI> The transport= value
 * <LI> The generated transport-specific address at which VM is listening for
 * debugger connection.
 * </UL>
 * <LI>
 * Upon launch, debugger selects a connector in the list
 * returned by {@link #attachingConnectors} matching the transport with
 * the name "xxx".
 * <LI>
 * Debugger changes the default connector parameters (obtained through
 * {@link com.sun.jdi.connect.Connector#defaultArguments()}) to specify
 * the transport specific address at which the VM is listenig. Optionally,
 * other connector arguments can be presented to the user.
 * <LI>
 * Debugger calls the
 * {@link com.sun.jdi.connect.AttachingConnector#attach(java.util.Map)} method
 * of the selected to attach to the target VM. A {@link VirtualMachine}
 * mirror is returned.
 * </UL>
 * </TD>
 * </TR>
 * </TABLE>
 *
 * <p> Connectors are created at start-up time. That is, they
 * are created the first time that {@link
 * com.sun.jdi.Bootstrap#virtualMachineManager()} is invoked.
 * The list of all Connectors created at start-up time can be
 * obtained from the VirtualMachineManager by invoking the
 * {@link #allConnectors allConnectors} method.
 *
 * <p> Connectors are created at start-up time if they are
 * installed on the platform. In addition, Connectors are created
 * automatically by the VirtualMachineManager to encapsulate any
 * {@link com.sun.jdi.connect.spi.TransportService} implementations
 * that are installed on the platform. These two mechanisms for
 * creating Connectors are described here.
 *
 * <p> A Connector is installed on the platform if it is installed
 * in a jar file that is visible to the defining class loader of
 * the {@link com.sun.jdi.connect.Connector} type,
 * and that jar file contains a provider configuration file named
 * {@code com.sun.jdi.connect.Connector} in the resource directory
 * {@code META-INF/services}, and the provider configuration file
 * lists the full-qualified class name of the Connector
 * implementation. A Connector is a class that implements the
 * {@link com.sun.jdi.connect.Connector Connector} interface. More
 * appropriately the class implements one of the specific Connector
 * types, namely {@link com.sun.jdi.connect.AttachingConnector
 * AttachingConnector}, {@link com.sun.jdi.connect.ListeningConnector
 * ListeningConnector}, or {@link com.sun.jdi.connect.LaunchingConnector
 * LaunchingConnector}. The format of the provider configuration file
 * is one fully-qualified class name per line. Space and tab characters
 * surrounding each class, as well as blank lines are ignored. The
 * comment character is {@code '#'} ({@code 0x23}), and on each
 * line all characters following the first comment character are
 * ignored. The file must be encoded in UTF-8.
 *
 * <p> At start-up time the VirtualMachineManager attempts to load
 * and instantiate (using the no-arg constructor) each class listed
 * in the provider configuration file. Exceptions thrown when loading
 * or creating the Connector are caught and ignored. In other words,
 * the start-up process continues despite of errors.
 *
 * <p> In addition to Connectors installed on the platform the
 * VirtualMachineManager will also create Connectors to encapsulate
 * any {@link com.sun.jdi.connect.spi.TransportService} implementations
 * that are installed on the platform. A TransportService is
 * installed on the platform if it installed in a jar file that is
 * visible to the defining class loader for the
 * {@link com.sun.jdi.connect.spi.TransportService} type, and that jar
 * file contains a provider configuration file named
 * {@code com.sun.jdi.connect.spi.TransportService} in the resource
 * directory {@code META-INF/services}, and the provider
 * configuration file lists the full-qualified class name of the
 * TransportService implementation. A TransportService is a concrete
 * sub-class of {@link com.sun.jdi.connect.spi.TransportService
 * TransportService}. The format of the provider configuration file
 * is the same as the provider configuration file for Connectors
 * except that each class listed must be the fully-qualified class
 * name of a class that implements the TransportService interface.
 *
 * <p> For each TransportService installed on the platform, the
 * VirtualMachineManager creates a corresponding
 * {@link com.sun.jdi.connect.AttachingConnector} and
 * {@link com.sun.jdi.connect.ListeningConnector}. These
 * Connectors are created to encapsulate a {@link
 * com.sun.jdi.connect.Transport Transport} that in turn
 * encapsulates the TransportService.
 * The AttachingConnector will be named based on the name of the
 * transport service concatenated with the string {@code Attach}.
 * For example, if the transport service {@link
 * com.sun.jdi.connect.spi.TransportService#name() name()} method
 * returns {@code telepathic} then the AttachingConnector will
 * be named {@code telepathicAttach}. Similiarly the ListeningConnector
 * will be named with the string {@code Listen} tagged onto the
 * name of the transport service. The {@link
 * com.sun.jdi.connect.Connector#description() description()} method
 * of both the AttachingConnector, and the ListeningConnector, will
 * delegate to the {@link com.sun.jdi.connect.spi.TransportService#description()
 * description()} method of the underlying transport service. Both
 * the AttachingConnector and the ListeningConnector will have two
 * Connector {@link com.sun.jdi.connect.Connector$Argument Arguments}.
 * A {@link com.sun.jdi.connect.Connector$StringArgument StringArgument}
 * named {@code address} is the connector argument to specify the
 * address to attach too, or to listen on. A
 * {@link com.sun.jdi.connect.Connector$IntegerArgument IntegerArgument}
 * named {@code timeout} is the connector argument to specify the
 * timeout when attaching, or accepting. The timeout connector may be
 * ignored depending on if the transport service supports an attach
 * timeout or accept timeout.
 *
 * <p> Initialization of the virtual machine manager will fail, that is
 * {@link com.sun.jdi.Bootstrap#virtualMachineManager()} will throw an
 * error if the virtual machine manager is unable to create any
 * connectors.
 *
 * @author Gordon Hirsch
 * @since  1.3
 */
public interface VirtualMachineManager {

    /**
     * Identifies the default connector. This connector should
     * be used as the launching connector when selection of a
     * connector with specific characteristics is unnecessary.
     *
     * @return the default {@link com.sun.jdi.connect.LaunchingConnector}
     */
    LaunchingConnector defaultConnector();

    /**
     * Returns the list of known {@link com.sun.jdi.connect.LaunchingConnector} objects.
     * Any of the returned objects can be used to launch a new target
     * VM and immediately create a {@link VirtualMachine} mirror for it.
     *
     * Note that a target VM launched by a launching connector is not
     * guaranteed to be stable until after the {@link com.sun.jdi.event.VMStartEvent} has been
     * received.
     * @return a list of {@link com.sun.jdi.connect.LaunchingConnector} objects.
     */
    List<LaunchingConnector> launchingConnectors();

    /**
     * Returns the list of known {@link com.sun.jdi.connect.AttachingConnector} objects.
     * Any of the returned objects can be used to attach to an existing target
     * VM and create a {@link VirtualMachine} mirror for it.
     *
     * @return a list of {@link com.sun.jdi.connect.AttachingConnector} objects.
     */
    List<AttachingConnector> attachingConnectors();

    /**
     * Returns the list of known {@link com.sun.jdi.connect.ListeningConnector} objects.
     * Any of the returned objects can be used to listen for a
     * connection initiated by a target VM
     * and create a {@link VirtualMachine} mirror for it.
     *
     * @return a list of {@link com.sun.jdi.connect.ListeningConnector} objects.
     */
    List<ListeningConnector> listeningConnectors();

    /**
     * Returns the list of all known {@link com.sun.jdi.connect.Connector} objects.
     *
     * @return a list of {@link com.sun.jdi.connect.Connector} objects.
     */
     List<Connector> allConnectors();

    /**
     * Lists all target VMs which are connected to the debugger.
     * The list includes {@link VirtualMachine} instances for
     * any target VMs which initiated a connection
     * and any
     * target VMs to which this manager has initiated a connection.
     * A target VM will remain in this list
     * until the VM is disconnected.
     * {@link com.sun.jdi.event.VMDisconnectEvent} is placed in the event queue
     * after the VM is removed from the list.
     *
     * @return a list of {@link VirtualMachine} objects, each mirroring
     * a target VM.
     */
     List<VirtualMachine> connectedVirtualMachines();

     /**
      * Returns the major version number of the JDI interface.
      * See {@link VirtualMachine#version} target VM version and
      * information and
      * {@link VirtualMachine#description} more version information.
      *
      * @return the integer major version number.
      */
     int majorInterfaceVersion();

     /**
      * Returns the minor version number of the JDI interface.
      * See {@link VirtualMachine#version} target VM version and
      * information and
      * {@link VirtualMachine#description} more version information.
      *
      * @return the integer minor version number
      */
     int minorInterfaceVersion();

     /**
      * Create a virtual machine mirror for a target VM.
      *
      * <p> Creates a virtual machine mirror for a target VM
      * for which a {@link com.sun.jdi.connect.spi.Connection Connection}
      * already exists. A Connection is created when a {@link
      * com.sun.jdi.connect.Connector Connector} establishes
      * a connection and successfully handshakes with a target VM.
      * A Connector can then use this method to create a virtual machine
      * mirror to represent the composite state of the target VM.
      *
      * <p> The {@code process} argument specifies the
      * {@link java.lang.Process} object for the taget VM. It may be
      * specified as {@code null}. If the target VM is launched
      * by a {@link com.sun.jdi.connect.LaunchingConnector
      * LaunchingConnector} the {@code process} argument should be
      * specified, otherwise calling {@link com.sun.jdi.VirtualMachine#process()}
      * on the created virtual machine will return {@code null}.
      *
      * <p> This method exists so that Connectors may create
      * a virtual machine mirror when a connection is established
      * to a target VM. Only developers creating new Connector
      * implementations should need to make direct use of this
      * method.
      *
      * @param  connection
      *         The open connection to the target VM.
      *
      * @param  process
      *         If launched, the {@link java.lang.Process} object for
      *         the target VM. {@code null} if not launched.
      *
      * @return new virtual machine representing the target VM.
      *
      * @throws IOException
      *         if an I/O error occurs
      *
      * @throws IllegalStateException
      *         if the connection is not open
      *
      * @see com.sun.jdi.connect.spi.Connection#isOpen()
      * @see com.sun.jdi.VirtualMachine#process()
      *
      * @since 1.5
      */
     VirtualMachine createVirtualMachine(Connection connection, Process process) throws IOException;

     /**
      * Creates a new virtual machine.
      *
      * <p> This convenience method works as if by invoking {@link
      * #createVirtualMachine(Connection, Process)} method and
      * specifying {@code null} as the {@code process} argument.
      *
      * <p> This method exists so that Connectors may create
      * a virtual machine mirror when a connection is established
      * to a target VM. Only developers creating new Connector
      * implementations should need to make direct use of this
      * method.
      *
      * @return the new virtual machine
      *
      * @throws IOException
      *         if an I/O error occurs
      *
      * @throws IllegalStateException
      *         if the connection is not open
      *
      * @since 1.5
      */
     VirtualMachine createVirtualMachine(Connection connection) throws IOException;
}
