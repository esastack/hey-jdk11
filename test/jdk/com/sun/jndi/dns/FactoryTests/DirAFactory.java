/*
 * Copyright (c) 2001, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.spi.DirObjectFactory;
import java.util.Hashtable;

public class DirAFactory implements DirObjectFactory {
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable<?, ?> environment, Attributes attrs) {
        System.out.println(this.getClass().getName()
                + "(DirObjectFactory).getObjectInstance(): " + name + ": "
                + attrs);

        if (attrs != null) {
            Attribute txt = attrs.get("A");
            if (txt != null) {
                return new TestDnsObject(txt);
            }
        }

        // return null to indicate other factories should be tried
        return null;
    }

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
            Hashtable<?, ?> environment) {
        System.out.println(this.getClass().getName()
                + "(ObjectFactory).getObjectInstance(): " + name);

        return null;
    }
}
