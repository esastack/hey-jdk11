/*
 * Copyright 1997-2001 Sun Microsystems, Inc.  All Rights Reserved.
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
package org.omg.CORBA;


/**
* Interfaces derived from the <tt>Current</tt> interface enable ORB and CORBA
* services to provide access to information (context) associated with
* the thread of execution in which they are running. This information
* is accessed in a structured manner using interfaces derived from the
* <tt>Current</tt> interface defined in the CORBA module.
*
* <P>Each ORB or CORBA service that needs its own context derives an
* interface from the CORBA module's <tt>Current</tt>. Users of the
* service can obtain an instance of the appropriate <tt>Current</tt>
* interface by invoking <tt>ORB::resolve_initial_references</tt>.<P>
*
* org/omg/CORBA/Current.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from ../../../../../src/share/classes/org/omg/PortableServer/corba.idl
* Saturday, July 17, 1999 12:26:21 AM PDT.
*/

public interface Current extends CurrentOperations, org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity
{
} // interface Current
