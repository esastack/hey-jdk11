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
 * @modules java.base/jdk.internal.org.objectweb.asm:+open java.base/jdk.internal.org.objectweb.asm.util:+open
 *
 * @summary converted from VM Testbase vm/runtime/defmeth/scenarios/SuperCall_v52_syncstrict_invoke_noredefine.
 * VM Testbase keywords: [defmeth, jdk8, quick]
 *
 * @library /vmTestbase /test/lib
 * @run driver jdk.test.lib.FileInstaller . .
 * @run main/othervm
 *      -XX:+IgnoreUnrecognizedVMOptions
 *      vm.runtime.defmeth.SuperCallTest
 *      -ver 52
 *      -flags 2080
 *      -mode invoke
 */

