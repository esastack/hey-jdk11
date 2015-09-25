/*
 * Copyright (c) 1999, 2001, Oracle and/or its affiliates. All rights reserved.
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
package org.omg.CORBA;


/**
 * The Helper for {@code Current}. For more information on
 * Helper files, see <a href="doc-files/generatedfiles.html#helper">
 * "Generated Files: Helper Files"</a>.<P>
 * org/omg/CORBA/CurrentHelper.java
 * Generated by the IDL-to-Java compiler (portable), version "3.0"
 * from ../../../../../src/share/classes/org/omg/PortableServer/corba.idl
 * Saturday, July 17, 1999 12:26:21 AM PDT
 */

abstract public class CurrentHelper
{
  private static String  _id = "IDL:omg.org/CORBA/Current:1.0";

  public static void insert (org.omg.CORBA.Any a, org.omg.CORBA.Current that)
  {
    throw new org.omg.CORBA.MARSHAL() ;
  }

  public static org.omg.CORBA.Current extract (org.omg.CORBA.Any a)
  {
    throw new org.omg.CORBA.MARSHAL() ;
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (org.omg.CORBA.CurrentHelper.id (), "Current");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static org.omg.CORBA.Current read (org.omg.CORBA.portable.InputStream istream)
  {
    throw new org.omg.CORBA.MARSHAL() ;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.omg.CORBA.Current value)
  {
    throw new org.omg.CORBA.MARSHAL() ;
  }

  public static org.omg.CORBA.Current narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof org.omg.CORBA.Current)
      return (org.omg.CORBA.Current)obj;
    else
      throw new org.omg.CORBA.BAD_PARAM ();
  }

}
