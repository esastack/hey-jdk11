/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.jmx.mbeanserver;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.JMX;

public class DescriptorCache {
    private DescriptorCache() {
    }

    static DescriptorCache getInstance() {
        return instance;
    }

    public static DescriptorCache getInstance(JMX proof) {
        if (proof != null)
            return instance;
        else
            return null;
    }

    public ImmutableDescriptor get(ImmutableDescriptor descriptor) {
        WeakReference<ImmutableDescriptor> wr = map.get(descriptor);
        ImmutableDescriptor got = (wr == null) ? null : wr.get();
        if (got != null)
            return got;
        map.put(descriptor, new WeakReference<ImmutableDescriptor>(descriptor));
        return descriptor;
    }

    public ImmutableDescriptor union(Descriptor... descriptors) {
        return get(ImmutableDescriptor.union(descriptors));
    }

    private final static DescriptorCache instance = new DescriptorCache();
    private final WeakHashMap<ImmutableDescriptor,
                              WeakReference<ImmutableDescriptor>>
        map = new WeakHashMap<ImmutableDescriptor,
                              WeakReference<ImmutableDescriptor>>();
}
