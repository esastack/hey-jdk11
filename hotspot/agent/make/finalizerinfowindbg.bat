@echo off
REM
REM Copyright 2004 Sun Microsystems, Inc.  All Rights Reserved.
REM DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
REM
REM This code is free software; you can redistribute it and/or modify it
REM under the terms of the GNU General Public License version 2 only, as
REM published by the Free Software Foundation.
REM
REM This code is distributed in the hope that it will be useful, but WITHOUT
REM ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
REM FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
REM version 2 for more details (a copy is included in the LICENSE file that
REM accompanied this code).
REM
REM You should have received a copy of the GNU General Public License version
REM 2 along with this work; if not, write to the Free Software Foundation,
REM Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
REM
REM Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
REM CA 95054 USA or visit www.sun.com if you need additional information or
REM have any questions.
REM  
REM

call saenv.bat

%SA_JAVA_CMD% sun.jvm.hotspot.tools.FinalizerInfo %1 %2
