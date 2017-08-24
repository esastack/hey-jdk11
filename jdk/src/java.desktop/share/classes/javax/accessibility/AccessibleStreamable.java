/*
 * Copyright (c) 2003, 2017, Oracle and/or its affiliates. All rights reserved.
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

package javax.accessibility;

import java.awt.datatransfer.DataFlavor;
import java.io.InputStream;

/**
 * The {@code AccessibleStreamable} interface should be implemented by the
 * {@code AccessibleContext} of any component that presents the raw stream
 * behind a component on the display screen. Examples of such components are
 * HTML, bitmap images and MathML. An object that implements
 * {@code AccessibleStreamable} provides two things: a list of MIME types
 * supported by the object and a streaming interface for each MIME type to get
 * the data.
 *
 * @author Lynn Monsanto
 * @author Peter Korn
 * @see AccessibleContext
 * @since 1.5
 */
public interface AccessibleStreamable {

    /**
     * Returns an array of {@code DataFlavor} objects for the MIME types this
     * object supports.
     *
     * @return an array of {@code DataFlavor} objects for the MIME types this
     *         object supports
     */
    DataFlavor[] getMimeTypes();

    /**
     * Returns an {@code InputStream} for a {@code DataFlavor}.
     *
     * @param  flavor the {@code DataFlavor}
     * @return an {@code ImputStream} if an input stream for this
     *         {@code DataFlavor} exists. Otherwise, {@code null} is returned.
     */
    InputStream getStream(DataFlavor flavor);
}
