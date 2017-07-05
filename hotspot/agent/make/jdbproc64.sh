#!/bin/sh
#
# Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#  
#

usage()
{
  echo "usage:   $0 <java process ID>"
  exit 1
}
#
if [ $# -lt 1 ]; then
    usage
else
    PID="${1}"
    echo "$0 attaching to PID=${PID}"
fi

. `dirname $0`/saenv64.sh

$JAVA_HOME/bin/jdb -J-d64 -J-Xbootclasspath/a:$SA_CLASSPATH:$JAVA_HOME/lib/tools.jar \
 -J-Dsun.boot.library.path=$JAVA_HOME/jre/lib/$CPU:$SA_LIBPATH \
 -connect sun.jvm.hotspot.jdi.SAPIDAttachingConnector:pid=${PID}

