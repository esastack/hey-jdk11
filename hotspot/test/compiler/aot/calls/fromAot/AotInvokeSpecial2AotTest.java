/*
 * Copyright (c) 2016, 2017, Oracle and/or its affiliates. All rights reserved.
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
 * @requires vm.aot
 * @library /test/lib /testlibrary /
 * @requires vm.bits == "64" & (os.arch == "amd64" | os.arch == "x86_64")
 * @modules java.base/jdk.internal.misc
 * @build compiler.calls.common.InvokeSpecial
 *        compiler.aot.AotCompiler
 * @run main ClassFileInstaller sun.hotspot.WhiteBox
 *      sun.hotspot.WhiteBox$WhiteBoxPermission
 * @run main compiler.aot.AotCompiler -libname AotInvokeSpecial2AotTest.so
 *      -class compiler.calls.common.InvokeSpecial
 *      -extraopt -XX:+UnlockDiagnosticVMOptions -extraopt -XX:+WhiteBoxAPI -extraopt -Xbootclasspath/a:.
 * @run main/othervm -XX:+UseAOT -XX:AOTLibrary=./AotInvokeSpecial2AotTest.so
 *      -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -Xbootclasspath/a:.
 *      compiler.calls.common.InvokeSpecial
 *      -checkCallerCompileLevel -1 -checkCalleeCompileLevel -1
 * @summary check calls from aot to aot code using invokespecial
 */
