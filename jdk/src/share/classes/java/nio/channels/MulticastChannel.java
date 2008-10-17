/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.nio.channels;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.io.IOException;
import java.net.ProtocolFamily;             // javadoc
import java.net.StandardProtocolFamily;     // javadoc
import java.net.StandardSocketOption;       // javadoc

/**
 * A network channel that supports Internet Protocol (IP) multicasting.
 *
 * <p> IP multicasting is the transmission of IP datagrams to members of
 * a <em>group</em> that is zero or more hosts identified by a single destination
 * address.
 *
 * <p> In the case of a channel to an {@link StandardProtocolFamily#INET IPv4} socket,
 * the underlying operating system supports <a href="http://www.ietf.org/rfc/rfc2236.txt">
 * <i>RFC&nbsp;2236: Internet Group Management Protocol, Version 2 (IGMPv2)</i></a>.
 * It may optionally support source filtering as specified by <a
 * href="http://www.ietf.org/rfc/rfc3376.txt"> <i>RFC&nbsp;3376: Internet Group
 * Management Protocol, Version 3 (IGMPv3)</i></a>.
 * For channels to an {@link StandardProtocolFamily#INET6 IPv6} socket, the equivalent
 * standards are <a href="http://www.ietf.org/rfc/rfc2710.txt"> <i>RFC&nbsp;2710:
 * Multicast Listener Discovery (MLD) for IPv6</i></a> and <a
 * href="http://www.ietf.org/rfc/rfc3810.txt"> <i>RFC&nbsp;3810: Multicast Listener
 * Discovery Version 2 (MLDv2) for IPv6</i></a>.
 *
 * <p> The {@link #join(InetAddress,NetworkInterface)} method is used to
 * join a group and receive all multicast datagrams sent to the group. A channel
 * may join several multicast groups and may join the same group on several
 * {@link NetworkInterface interfaces}. Membership is dropped by invoking the {@link
 * MembershipKey#drop drop} method on the returned {@link MembershipKey}. If the
 * underlying platform supports source filtering then the {@link MembershipKey#block
 * block} and {@link MembershipKey#unblock unblock} methods can be used to block or
 * unblock multicast datagrams from particular source addresses.
 *
 * <p> The {@link #join(InetAddress,NetworkInterface,InetAddress)} method
 * is used to begin receiving datagrams sent to a group whose source address matches
 * a given source address. This method throws {@link UnsupportedOperationException}
 * if the underlying platform does not support source filtering.  Membership is
 * <em>cumulative</em> and this method may be invoked again with the same group
 * and interface to allow receiving datagrams from other source addresses. The
 * method returns a {@link MembershipKey} that represents membership to receive
 * datagrams from the given source address. Invoking the key's {@link
 * MembershipKey#drop drop} method drops membership so that datagrams from the
 * source address can no longer be received.
 *
 * <h4>Platform dependencies</h4>
 *
 * The multicast implementation is intended to map directly to the native
 * multicasting facility. Consequently, the following items should be considered
 * when developing an application that receives IP multicast datagrams:
 *
 * <ol>
 *
 * <li><p> The creation of the channel should specify the {@link ProtocolFamily}
 * that corresponds to the address type of the multicast groups that the channel
 * will join. There is no guarantee that a channel to a socket in one protocol
 * family can join and receive multicast datagrams when the address of the
 * multicast group corresponds to another protocol family. For example, it is
 * implementation specific if a channel to an {@link StandardProtocolFamily#INET6 IPv6}
 * socket can join an {@link StandardProtocolFamily#INET IPv4} multicast group and receive
 * multicast datagrams sent to the group. </p></li>
 *
 * <li><p> The channel's socket should be bound to the {@link
 * InetAddress#isAnyLocalAddress wildcard} address. If the socket is bound to
 * a specific address, rather than the wildcard address then it is implementation
 * specific if multicast datagrams are received by the socket. </p></li>
 *
 * <li><p> The {@link StandardSocketOption#SO_REUSEADDR SO_REUSEADDR} option should be
 * enabled prior to {@link NetworkChannel#bind binding} the socket. This is
 * required to allow multiple members of the group to bind to the same
 * address. </p></li>
 *
 * </ol>
 *
 * <p> <b>Usage Example:</b>
 * <pre>
 *     // join multicast group on this interface, and also use this
 *     // interface for outgoing multicast datagrams
 *     NetworkInterface ni = NetworkInterface.getByName("hme0");
 *
 *     DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
 *         .setOption(StandardSocketOption.SO_REUSEADDR, true)
 *         .bind(new InetSocketAddress(5000))
 *         .setOption(StandardSocketOption.IP_MULTICAST_IF, ni);
 *
 *     InetAddress group = InetAddress.getByName("225.4.5.6");
 *
 *     MembershipKey key = dc.join(group, ni);
 * </pre>
 *
 * @since 1.7
 */

public interface MulticastChannel
    extends NetworkChannel
{
    /**
     * Joins a multicast group to begin receiving all datagrams sent to the group,
     * returning a membership key.
     *
     * <p> If this channel is currently a member of the group on the given
     * interface to receive all datagrams then the membership key, representing
     * that membership, is returned. Otherwise this channel joins the group and
     * the resulting new membership key is returned. The resulting membership key
     * is not {@link MembershipKey#getSourceAddress source-specific}.
     *
     * <p> A multicast channel may join several multicast groups, including
     * the same group on more than one interface. An implementation may impose a
     * limit on the number of groups that may be joined at the same time.
     *
     * @param   group
     *          The multicast address to join
     * @param   interf
     *          The network interface on which to join the group
     *
     * @return  The membership key
     *
     * @throws  IllegalArgumentException
     *          If the group parameter is not a {@link InetAddress#isMulticastAddress
     *          multicast} address, or the group parameter is an address type
     *          that is not supported by this channel
     * @throws  IllegalStateException
     *          If the channel already has source-specific membership of the
     *          group on the interface
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          If a security manager is set, and its
     *          {@link SecurityManager#checkMulticast(InetAddress) checkMulticast}
     *          method denies access to the multiast group
     */
    MembershipKey join(InetAddress group, NetworkInterface interf)
        throws IOException;

    /**
     * Joins a multicast group to begin receiving datagrams sent to the group
     * from a given source address.
     *
     * <p> If this channel is currently a member of the group on the given
     * interface to receive datagrams from the given source address then the
     * membership key, representing that membership, is returned. Otherwise this
     * channel joins the group and the resulting new membership key is returned.
     * The resulting membership key is {@link MembershipKey#getSourceAddress
     * source-specific}.
     *
     * <p> Membership is <em>cumulative</em> and this method may be invoked
     * again with the same group and interface to allow receiving datagrams sent
     * by other source addresses to the group.
     *
     * @param   group
     *          The multicast address to join
     * @param   interf
     *          The network interface on which to join the group
     * @param   source
     *          The source address
     *
     * @return  The membership key
     *
     * @throws  IllegalArgumentException
     *          If the group parameter is not a {@link
     *          InetAddress#isMulticastAddress multicast} address, the
     *          source parameter is not a unicast address, the group
     *          parameter is an address type that is not supported by this channel,
     *          or the source parameter is not the same address type as the group
     * @throws  IllegalStateException
     *          If the channel is currently a member of the group on the given
     *          interface to receive all datagrams
     * @throws  UnsupportedOperationException
     *          If the underlying operation system does not support source filtering
     * @throws  ClosedChannelException
     *          If this channel is closed
     * @throws  IOException
     *          If an I/O error occurs
     * @throws  SecurityException
     *          If a security manager is set, and its
     *          {@link SecurityManager#checkMulticast(InetAddress) checkMulticast}
     *          method denies access to the multiast group
     */
    MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source)
        throws IOException;
}
