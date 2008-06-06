/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.classfile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class Attributes implements Iterable<Attribute> {
    Attributes(ClassReader cr) throws IOException {
        map = new HashMap<String,Attribute>();
        int attrs_count = cr.readUnsignedShort();
        attrs = new Attribute[attrs_count];
        for (int i = 0; i < attrs_count; i++) {
            Attribute attr = Attribute.read(cr);
            attrs[i] = attr;
            try {
                map.put(attr.getName(cr.getConstantPool()), attr);
            } catch (ConstantPoolException e) {
                // don't enter invalid names in map
            }
        }
    }

    public Attributes(ConstantPool constant_pool, Attribute[] attrs) {
        this.attrs = attrs;
        map = new HashMap<String,Attribute>();
        for (int i = 0; i < attrs.length; i++) {
            Attribute attr = attrs[i];
            try {
                map.put(attr.getName(constant_pool), attr);
            } catch (ConstantPoolException e) {
                // don't enter invalid names in map
            }
        }
    }

    public Iterator<Attribute> iterator() {
        return Arrays.asList(attrs).iterator();
    }

    public Attribute get(int index) {
        return attrs[index];
    }

    public Attribute get(String name) {
        return map.get(name);
    }

    public int size() {
        return attrs.length;
    }

    public final Attribute[] attrs;
    public final Map<String, Attribute> map;
}
