/*
 * Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.security.action;

import java.security.Security;

/**
 * A convenience class for retrieving the boolean value of a security property
 * as a privileged action.
 *
 * <p>An instance of this class can be used as the argument of
 * <code>AccessController.doPrivileged</code>.
 *
 * <p>The following code retrieves the boolean value of the security
 * property named <code>"prop"</code> as a privileged action: <p>
 *
 * <pre>
 * boolean b = java.security.AccessController.doPrivileged
 *              (new GetBooleanSecurityPropertyAction("prop")).booleanValue();
 * </pre>
 *
 */
public class GetBooleanSecurityPropertyAction
        implements java.security.PrivilegedAction<Boolean> {
    private String theProp;

    /**
     * Constructor that takes the name of the security property whose boolean
     * value needs to be determined.
     *
     * @param theProp the name of the security property
     */
    public GetBooleanSecurityPropertyAction(String theProp) {
        this.theProp = theProp;
    }

    /**
     * Determines the boolean value of the security property whose name was
     * specified in the constructor.
     *
     * @return the <code>Boolean</code> value of the security property.
     */
    public Boolean run() {
        boolean b = false;
        try {
            String value = Security.getProperty(theProp);
            b = (value != null) && value.equalsIgnoreCase("true");
        } catch (NullPointerException e) {}
        return b;
    }
}
