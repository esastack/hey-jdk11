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


package javax.naming;

import javax.naming.Name;

/**
  * This exception is thrown when a method
  * produces a result that exceeds a size-related limit.
  * This can happen, for example, if the result contains
  * more objects than the user requested, or when the size
  * of the result exceeds some implementation-specific limit.
  * <p>
  * Synchronization and serialization issues that apply to NamingException
  * apply directly here.
  *
  * @author Rosanna Lee
  * @author Scott Seligman
  *
  * @since 1.3
  */
public class SizeLimitExceededException extends LimitExceededException {
    /**
     * Constructs a new instance of SizeLimitExceededException.
     * All fields default to null.
     */
    public SizeLimitExceededException() {
        super();
    }

    /**
     * Constructs a new instance of SizeLimitExceededException using an
     * explanation. All other fields default to null.
     *
     * @param explanation Possibly null detail about this exception.
     */
    public SizeLimitExceededException(String explanation) {
        super(explanation);
    }

    /**
     * Use serialVersionUID from JNDI 1.1.1 for interoperability
     */
    private static final long serialVersionUID = 7129289564879168579L;
}
