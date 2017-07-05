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

package sun.java2d;

/**
 * Signals that some part of a graphics pipeline is not in an appropriate
 * state for the requested operation.  The most likely cause of this is
 * an asynchronous change to the configuration of the destination surface
 * since the current set of rendering loops was chosen.  Other potential
 * causes are the appearance or disappearance of overlapping opaque
 * windows which toggle the need to use platform graphics or direct
 * graphics access.
 */
public class InvalidPipeException extends IllegalStateException {
    /**
     * Constructs an InvalidPipeException with no detail message.
     * A detail message is a String that describes this particular exception.
     */
    public InvalidPipeException() {
        super();
    }

    /**
     * Constructs an InvalidPipeException with the specified detail
     * message.  A detail message is a String that describes this particular
     * exception.
     * @param s the String that contains a detailed message
     */
    public InvalidPipeException(String s) {
        super(s);
    }
}
