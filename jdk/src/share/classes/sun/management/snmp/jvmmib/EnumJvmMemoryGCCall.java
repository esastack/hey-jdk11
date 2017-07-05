/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.management.snmp.jvmmib;

//
// Generated by mibgen version 5.0 (06/02/03) when compiling JVM-MANAGEMENT-MIB.
//

// java imports
//
import java.io.Serializable;
import java.util.Hashtable;

// RI imports
//
import com.sun.jmx.snmp.Enumerated;

/**
 * The class is used for representing "JvmMemoryGCCall".
 */
public class EnumJvmMemoryGCCall extends Enumerated implements Serializable {

    protected static Hashtable<Integer, String> intTable =
            new Hashtable<Integer, String>();
    protected static Hashtable<String, Integer> stringTable =
            new Hashtable<String, Integer>();
    static  {
        intTable.put(new Integer(2), "supported");
        intTable.put(new Integer(5), "failed");
        intTable.put(new Integer(4), "started");
        intTable.put(new Integer(1), "unsupported");
        intTable.put(new Integer(3), "start");
        stringTable.put("supported", new Integer(2));
        stringTable.put("failed", new Integer(5));
        stringTable.put("started", new Integer(4));
        stringTable.put("unsupported", new Integer(1));
        stringTable.put("start", new Integer(3));
    }

    public EnumJvmMemoryGCCall(int valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }

    public EnumJvmMemoryGCCall(Integer valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }

    public EnumJvmMemoryGCCall() throws IllegalArgumentException {
        super();
    }

    public EnumJvmMemoryGCCall(String x) throws IllegalArgumentException {
        super(x);
    }

    protected Hashtable getIntTable() {
        return intTable ;
    }

    protected Hashtable getStringTable() {
        return stringTable ;
    }

}
