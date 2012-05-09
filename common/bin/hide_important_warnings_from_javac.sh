#!/bin/sh
#
# Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

if [ -x /usr/bin/ggrep ] ; then
    # Gnu grep on Solaris
    # (reference configure and build/solaris-i586-clientANDserver-release/spec.gmk
    GREP=/usr/bin/ggrep
else
    GREP=grep
fi
#
EXP="Note: Some input files use or override a deprecated API."
EXP="${EXP}|Note: Recompile with -Xlint:deprecation for details."
EXP="${EXP}|Note: Some input files use unchecked or unsafe operations."
EXP="${EXP}|Note: Recompile with -Xlint:unchecked for details."
EXP="${EXP}| warning"
EXP="${EXP}|uses or overrides a deprecated API."
EXP="${EXP}|uses unchecked or unsafe operations."
#
${GREP} --line-buffered -v -E "${EXP}"
