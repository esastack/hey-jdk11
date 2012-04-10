/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.internal.ws.api;

import java.util.Set;

import com.sun.istack.internal.NotNull;

/**
 * Registry for component delegates.  It is expected that implementations of
 * ComponentRegistry will delegate to registered {@link Component}s in its own
 * implementation of {@link Component.getSPI}, either before or after it
 * considers its own SPI implementations.
 *
 * @since 2.2.6
 */
public interface ComponentRegistry extends Component {
        /**
         * Returns the set of {@link Component}s registered with this object
         * @return set of registered components
         */
    public @NotNull Set<Component> getComponents();
}
