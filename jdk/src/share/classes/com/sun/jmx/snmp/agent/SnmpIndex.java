/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
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


package com.sun.jmx.snmp.agent;



// java imports
//
import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;

// jmx imports
//
import com.sun.jmx.snmp.SnmpOid;

/**
 * Represents a SNMP index.
 * An <CODE>SnmpIndex</CODE> is represented as a <CODE>Vector</CODE> of <CODE>SnmpOid</CODE>.
 * <P>
 * This class is used internally and by the classes generated by <CODE>mibgen</CODE>.
 * You should not need to use this class directly.
 *
 * <p><b>This API is a Sun Microsystems internal API  and is subject
 * to change without notice.</b></p>
 */

public class SnmpIndex implements Serializable {
    private static final long serialVersionUID = 8712159739982192146L;

    /**
     * Initializes an <CODE>SnmpIndex</CODE> using a vector of object identifiers.
     * <P>Following the RFC recommendations, every syntax that is used as a
     * table index should have an object identifier representation. There are
     * some guidelines on how to map the different syntaxes into an object identifier.
     * In the different <CODE>SnmpValue</CODE> classes provided, there is a <CODE>toOid</CODE> method to get
     * the object identifier of the value.
     *
     * @param oidList The list of Object Identifiers.
     */
    public SnmpIndex(SnmpOid[] oidList) {
        size= oidList.length;
        for(int i= 0; i <size; i++) {
            // The order is important ...
            //
            oids.addElement(oidList[i]);
        }
    }

    /**
     * Initializes an <CODE>SnmpIndex</CODE> using the specified Object Identifier.
     *
     * @param oid The Object Identifier.
     */
    public SnmpIndex(SnmpOid oid) {
        oids.addElement(oid);
        size= 1;
    }

    /**
     * Gets the number of Object Identifiers the index is made of.
     *
     * @return The number of Object Identifiers.
     */
    public int getNbComponents() {
        return size;
    }

    /**
     * Gets the index as a vector of Object Identifiers.
     *
     * @return The index as a vector.
     */
    public Vector<SnmpOid> getComponents() {
        return oids;
    }

    /**
     * Compares two indexes for equality.
     *
     * @param index The index to compare <CODE>this</CODE> with.
     *
     * @return <CODE>true</CODE> if the two indexes are equal, <CODE>false</CODE> otherwise.
     */
    public boolean equals(SnmpIndex index) {

        if (size != index.getNbComponents())
            return false;

        // The two vectors have the same length.
        // Compare each single element ...
        //
        SnmpOid oid1;
        SnmpOid oid2;
        Vector<SnmpOid> components= index.getComponents();
        for(int i=0; i <size; i++) {
            oid1= oids.elementAt(i);
            oid2= components.elementAt(i);
            if (oid1.equals(oid2) == false)
                return false;
        }
        return true;
    }

    /**
     * Compares two indexes.
     *
     * @param index The index to compare <CODE>this</CODE> with.
     *
     * @return The value 0 if the two OID vectors have the same elements, another value otherwise.
     */
    public int compareTo(SnmpIndex index) {

        int length= index.getNbComponents();
        Vector<SnmpOid> components= index.getComponents();
        SnmpOid oid1;
        SnmpOid oid2;
        int comp;
        for(int i=0; i < size; i++) {
            if ( i > length) {
                // There is no more element in the index
                //
                return 1;
            }
            // Access the element ...
            //
            oid1= oids.elementAt(i);
            oid2= components.elementAt(i);
            comp= oid1.compareTo(oid2);
            if (comp == 0)
                continue;
            return comp;
        }
        return 0;
    }

    /**
     * Returns a <CODE>String</CODE> representation of the index.
     * The different elements are separated by "//".
     *
     * @return A string representation of the index.
     */
    public String toString() {
        StringBuffer msg= new StringBuffer();
        for(Enumeration e= oids.elements(); e.hasMoreElements(); ) {
            SnmpOid val= (SnmpOid) e.nextElement();
            msg.append( "//" + val.toString());
        }
        return msg.toString();
    }

    // PRIVATE VARIABLES
    //------------------

    /**
     * The list of OIDs.
     * @serial
     */
    private Vector<SnmpOid> oids = new Vector<SnmpOid>();

    /**
     * The number of elements in the index.
     * @serial
     */
    private int size = 0;
}
