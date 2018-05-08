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
 * @modules jdk.jdi/com.sun.tools.jdi:+open java.base/jdk.internal.misc:+open
 *
 * @summary converted from VM Testbase nsk/jdi/MonitorWaitRequest/MonitorWaitRequest002.
 * VM Testbase keywords: [jpda, jdi, feature_jdk6_jpda, vm6]
 * VM Testbase readme:
 * DESCRIPTION
 *         This is stress test for MonitorWaitRequest and MonitorWaitEvent, debugger force
 *         debuggee start several threads which simultaneously generate MonitorWaitEvents and checks that
 *         all events was received and contain     correct information.
 *         Test executes class nsk.share.jdi.EventTestTemplates$StressTestTemplate which uses JDI events testing
 *         framework based on classes from package nsk.share.jdi.*.
 *         This framework uses following scenario:
 *                 - debugger VM forces debuggee VM to create number of objects which should generate events during test
 *                 - debuggee performs event generation and stop at breakpoint
 *                 - debugger reads data saved by debuggee's event generators and checks is only expected events was generated
 *         Stress test template allows to specify number of events which should be generated during test execution(parameter -eventsNumber)
 *         and number of threads which simultaneously generate events (parameter -threadsNumber).
 *         This test set eventsNumber to 25 and threadsNumber to 5, but because of framework design generation of MonitorWait
 *         and MonitorWaited events is performed for 4 cases: thread exits from wait() after timeout, waiting thread is waked up with
 *         notify() or notifyAll(), or waiting thread is interrupted with interrupt().
 *         Also events are generated by 3 different threads, so actual number of generated events is (eventsNumber * 12),
 *         and actual number of threads generating events is (threadsNumber * 3).
 *
 * @library /vmTestbase
 *          /test/lib
 * @run driver jdk.test.lib.FileInstaller . .
 * @build nsk.share.jdi.EventTestTemplates
 *        nsk.share.jdi.JDIEventsDebuggee
 *        nsk.share.jdi.MonitorEventsDebuggee
 * @run main/othervm PropertyResolvingWrapper
 *      nsk.share.jdi.EventTestTemplates$StressTestTemplate
 *      -verbose
 *      -arch=${os.family}-${os.simpleArch}
 *      -waittime=5
 *      -debugee.vmkind=java
 *      -transport.address=dynamic
 *      "-debugee.vmkeys=${test.vm.opts} ${test.java.opts}"
 *      -eventTypes MONITOR_WAIT
 *      -debuggeeClassName nsk.share.jdi.MonitorEventsDebuggee
 *      -eventsNumber 10
 *      -threadsNumber 2
 */

