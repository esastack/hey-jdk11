/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 *
 * THIS FILE WAS MODIFIED BY SUN MICROSYSTEMS, INC.
 */

/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 *
 * THIS FILE WAS MODIFIED BY SUN MICROSYSTEMS, INC.
 *
 */


package com.sun.xml.internal.fastinfoset;

import javax.xml.namespace.QName;

public class QualifiedName {
    public final String prefix;
    public final String namespaceName;
    public final String localName;
    public String qName;
    public final int index;
    public final int prefixIndex;
    public final int namespaceNameIndex;
    public final int localNameIndex;
    public int attributeId;
    public int attributeHash;
    private QName qNameObject;

    public QualifiedName(String prefix, String namespaceName, String localName, String qName) {
        this.prefix = prefix;
        this.namespaceName = namespaceName;
        this.localName = localName;
        this.qName = qName;
        this.index = -1;
        this.prefixIndex = 0;
        this.namespaceNameIndex = 0;
        this.localNameIndex = -1;
    }

    public QualifiedName(String prefix, String namespaceName, String localName, String qName, int index) {
        this.prefix = prefix;
        this.namespaceName = namespaceName;
        this.localName = localName;
        this.qName = qName;
        this.index = index;
        this.prefixIndex = 0;
        this.namespaceNameIndex = 0;
        this.localNameIndex = -1;
    }

    public QualifiedName(String prefix, String namespaceName, String localName, String qName, int index,
            int prefixIndex, int namespaceNameIndex, int localNameIndex) {
        this.prefix = prefix;
        this.namespaceName = namespaceName;
        this.localName = localName;
        this.qName = qName;
        this.index = index;
        this.prefixIndex = prefixIndex + 1;
        this.namespaceNameIndex = namespaceNameIndex + 1;
        this.localNameIndex = localNameIndex;
    }

    public QualifiedName(String prefix, String namespaceName, String localName) {
        this.prefix = prefix;
        this.namespaceName = namespaceName;
        this.localName = localName;
        this.qName = createQNameString(prefix, localName);
        this.index = -1;
        this.prefixIndex = 0;
        this.namespaceNameIndex = 0;
        this.localNameIndex = -1;
    }

    public QualifiedName(String prefix, String namespaceName, String localName,
            int prefixIndex, int namespaceNameIndex, int localNameIndex,
            char[] charBuffer) {
        this.prefix = prefix;
        this.namespaceName = namespaceName;
        this.localName = localName;

        if (charBuffer != null) {
            final int l1 = prefix.length();
            final int l2 = localName.length();
            final int total = l1 + l2 + 1;
            if (total < charBuffer.length) {
                prefix.getChars(0, l1, charBuffer, 0);
                charBuffer[l1] = ':';
                localName.getChars(0, l2, charBuffer, l1 + 1);
                this.qName = new String(charBuffer, 0, total);
            } else {
                this.qName = createQNameString(prefix, localName);
            }
        } else {
            this.qName = this.localName;
        }

        this.prefixIndex = prefixIndex + 1;
        this.namespaceNameIndex = namespaceNameIndex + 1;
        this.localNameIndex = localNameIndex;
        this.index = -1;
    }

    public QualifiedName(String prefix, String namespaceName, String localName, int index) {
        this.prefix = prefix;
        this.namespaceName = namespaceName;
        this.localName = localName;
        this.qName = createQNameString(prefix, localName);
        this.index = index;
        this.prefixIndex = 0;
        this.namespaceNameIndex = 0;
        this.localNameIndex = -1;
    }

    public QualifiedName(String prefix, String namespaceName, String localName, int index,
            int prefixIndex, int namespaceNameIndex, int localNameIndex) {
        this.prefix = prefix;
        this.namespaceName = namespaceName;
        this.localName = localName;
        this.qName = createQNameString(prefix, localName);
        this.index = index;
        this.prefixIndex = prefixIndex + 1;
        this.namespaceNameIndex = namespaceNameIndex + 1;
        this.localNameIndex = localNameIndex;
    }

    // Qualified Name as a Namespace Name
    public QualifiedName(String prefix, String namespaceName) {
        this.prefix = prefix;
        this.namespaceName = namespaceName;
        this.localName = "";
        this.qName = "";
        this.index = -1;
        this.prefixIndex = 0;
        this.namespaceNameIndex = 0;
        this.localNameIndex = -1;
    }

    public final QName getQName() {
        if (qNameObject == null) {
            qNameObject = new QName(namespaceName, localName, prefix);
        }

        return qNameObject;
    }

    public final String getQNameString() {
        if (this.qName != "") {
            return this.qName;
        }

        return this.qName = createQNameString(prefix, localName);
    }

    public final void createAttributeValues(int size) {
        attributeId = localNameIndex | (namespaceNameIndex << 20);
        attributeHash = localNameIndex % size;
    }

    private final String createQNameString(String p, String l) {
        if (p != null && p != "") {
            final StringBuffer b = new StringBuffer(p);
            b.append(':');
            b.append(l);
            return b.toString();
        } else {
            return l;
        }
    }
}
