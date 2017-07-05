/*
 * Copyright 2001 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.reflect;

class ByteVectorImpl implements ByteVector {
    private byte[] data;
    private int pos;

    public ByteVectorImpl() {
        this(100);
    }

    public ByteVectorImpl(int sz) {
        data = new byte[sz];
        pos = -1;
    }

    public int getLength() {
        return pos + 1;
    }

    public byte get(int index) {
        if (index >= data.length) {
            resize(index);
            pos = index;
        }
        return data[index];
    }

    public void put(int index, byte value) {
        if (index >= data.length) {
            resize(index);
            pos = index;
        }
        data[index] = value;
    }

    public void add(byte value) {
        if (++pos >= data.length) {
            resize(pos);
        }
        data[pos] = value;
    }

    public void trim() {
        if (pos != data.length - 1) {
            byte[] newData = new byte[pos + 1];
            System.arraycopy(data, 0, newData, 0, pos + 1);
            data = newData;
        }
    }

    public byte[] getData() {
        return data;
    }

    private void resize(int minSize) {
        if (minSize <= 2 * data.length) {
            minSize = 2 * data.length;
        }
        byte[] newData = new byte[minSize];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }
}
