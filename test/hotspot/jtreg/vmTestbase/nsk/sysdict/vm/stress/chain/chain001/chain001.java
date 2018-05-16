/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates. All rights reserved.
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


/*
 * @test
 * @key stress
 *
 * @summary converted from VM Testbase nsk/sysdict/vm/stress/chain/chain001.
 * VM Testbase keywords: [stress, sysdict, stressopt, nonconcurrent]
 * VM Testbase readme:
 * DESCRIPTION
 *     Single thread loads a long chain of lean classes
 *     with a short chain of loaders (NxM load attempts).
 *     The test is deemed failed if loading attempt fails.
 *     The test repeats until the given number of iterations,
 *     or until EndOfMemoryError.
 *
 * @library /vmTestbase /test/lib
 * @comment build fats.jar
 * @run driver nsk.sysdict.share.GenClassesBuilder fats
 * @comment build leans.jar
 * @run driver nsk.sysdict.share.GenClassesBuilder leans
 * @run driver jdk.test.lib.FileInstaller . .
 * @build nsk.sysdict.share.ChainTest
 * @run main/othervm
 *      -XX:-UseGCOverheadLimit
 *      PropertyResolvingWrapper
 *      nsk.sysdict.share.ChainTest
 *      -t 1
 *      -jarpath leans.jar${path.separator}fats.jar
 */

