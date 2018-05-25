/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
 *
 * @summary converted from VM Testbase nsk/jvmti/RedefineClasses/redefclass027.
 * VM Testbase keywords: [quick, jpda, jvmti, noras, redefine]
 * VM Testbase readme:
 * DESCRIPTION
 *     The test exercises JVMTI function RedefineClasses(classCount, classDefs).
 *     The test creates a child thread, sets a breakpoint into method run
 *     and access/modification watchs on StaticInt and InstanceInt fields.
 *     Then the test starts the thread which does some nesting calls,
 *     accesses/modifies class fields, and throws/catches an exception.
 *     Catching breakpoint, single step, wathchpoint, exception
 *     and frame pop events the test test checks their class,
 *     current line number, names and values of local varaibles, and
 *     then redefines the thread's class by identical class.
 * COMMENTS
 *     Ported from JVMDI.
 *     Fixed according to 4960493 bug.
 *
 * @library /vmTestbase
 *          /test/lib
 * @run driver jdk.test.lib.FileInstaller . .
 *
 * @comment make sure redefclass027 is compiled with full debug info
 * @build nsk.jvmti.RedefineClasses.redefclass027
 * @clean nsk.jvmti.RedefineClasses.redefclass027
 * @compile -g:lines,source,vars ../redefclass027.java
 *
 * @run main/othervm/native -agentlib:redefclass027 nsk.jvmti.RedefineClasses.redefclass027
 */

