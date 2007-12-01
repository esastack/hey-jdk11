/*
 * Copyright 1999 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.org.omg.CORBA;


/**
* com/sun/org/omg/CORBA/IdentifierHelper.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from ir.idl
* Thursday, May 6, 1999 1:51:42 AM PDT
*/

public final class IdentifierHelper
{
    private static String  _id = "IDL:omg.org/CORBA/Identifier:1.0";

    public IdentifierHelper()
    {
    }

    public static void insert (org.omg.CORBA.Any a, String that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }

    public static String extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = org.omg.CORBA.ORB.init ().create_string_tc (0);
                __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.org.omg.CORBA.IdentifierHelper.id (), "Identifier", __typeCode);
            }
        return __typeCode;
    }

    public static String id ()
    {
        return _id;
    }

    public static String read (org.omg.CORBA.portable.InputStream istream)
    {
        String value = null;
        value = istream.read_string ();
        return value;
    }

    public static void write (org.omg.CORBA.portable.OutputStream ostream, String value)
    {
        ostream.write_string (value);
    }

}
