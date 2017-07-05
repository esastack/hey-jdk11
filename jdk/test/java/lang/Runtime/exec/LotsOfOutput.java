/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/**
 * @test
 * @bug 4369826
 * @summary Process with lots of output should not crash VM
 * @author kladko
 */

import java.io.File;

public class LotsOfOutput {
    static final String CAT = "/usr/bin/cat";

    public static void main(String[] args) throws Exception{
        if (File.separatorChar == '\\' ||                // Windows
                                !new File(CAT).exists()) // no cat
            return;
        Process p = Runtime.getRuntime().exec(CAT + " /dev/zero");
        long initMemory = Runtime.getRuntime().totalMemory();
        for (int i=1; i< 10; i++) {
            Thread.sleep(100);
            if (Runtime.getRuntime().totalMemory() > initMemory + 1000000)
                throw new Exception("Process consumes memory.");
        }

    }
}
