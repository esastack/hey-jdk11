/*
 * Copyright (c) 2002, 2018, Oracle and/or its affiliates. All rights reserved.
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

package nsk.jdi.EventRequestManager.deleteEventRequest;

import nsk.share.*;
import nsk.share.jpda.*;
import nsk.share.jdi.*;

import java.io.*;

/**
 *  <code>delevtreq003b</code> is deugee's part of the delevtreq003.
 */

public class delevtreq003b {
    public final static int INITIAL_VALUE        = 0;
    public final static int BEFORE_REDEFINITION = 1;
    public final static int AFTER_REDEFINITION  = 2;

    public static boolean loadClass = false;
    public static int flag = INITIAL_VALUE;

    public static void runIt(boolean doWait) {

        flag = AFTER_REDEFINITION;
//             ^^^^^^^^^^^^^^^^^^^ it will be redefined

    }
}
