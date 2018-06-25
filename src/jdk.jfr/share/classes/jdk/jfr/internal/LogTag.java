/*
 * Copyright (c) 2016, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package jdk.jfr.internal;

/* Mapped against c++ enum in jfrLogTagSet.hpp */
public enum LogTag {
    /**
     * Covers
     * <ul>
     * <li>Initialization of Flight Recorder
     * <li> recording life cycle (start, stop and dump)
     * <li> repository life cycle
     * <li>loading of configuration files.
     * </ul>
     * Target audience: operations
     */
    JFR(0),
    /**
     * Covers general implementation aspects of JFR (for Hotspot developers)
     */
    JFR_SYSTEM(1),
    /**
     * Covers JVM/JDK events (for Hotspot developers)
     */
    JFR_SYSTEM_EVENT(2),
    /**
     * Covers setting for the JVM/JDK  (for Hotspot developers)
     */
    JFR_SYSTEM_SETTING(3),
    /**
     * Covers generated bytecode (for Hotspot developers)
     */
    JFR_SYSTEM_BYTECODE(4),
    /**
     * Covers XML parsing (for Hotspot developers)
     */
    JFR_SYSTEM_PARSER(5),
    /**
     * Covers metadata for JVM/JDK (for Hotspot developers)
     */
    JFR_SYSTEM_METADATA(6),
    /**
     *  Covers metadata for Java user (for Hotspot developers)
     */
    JFR_METADATA(7),
    /**
     * Covers events (for users of the JDK)
     */
    JFR_EVENT(8),
    /**
     * Covers setting (for users of the JDK)
     */
    JFR_SETTING(9),
    /**
     * Covers usage of jcmd with JFR
     */
    JFR_DCMD(10);

    /* set from native side */
    private volatile int tagSetLevel = 100; // prevent logging if JVM log system has not been initialized

    final int id;

    LogTag(int tagId) {
        id = tagId;
    }

    public boolean shouldLog(int level) {
        return level >= tagSetLevel;
    }

    public boolean shouldLog(LogLevel logLevel) {
        return shouldLog(logLevel.level);
    }
}
