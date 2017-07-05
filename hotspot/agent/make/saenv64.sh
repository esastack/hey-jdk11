#!/bin/sh
#
# Copyright 2003-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

# This file sets common environment variables for all 64-bit Solaris [sparcv9,
# amd64] SA scripts. Please note that for 64-bit Linux use saenv.sh.

OS=`uname`
STARTDIR=`dirname $0`

CPU=`isainfo | grep sparcv9`

if [ "x$CPU" != "x" ]; then
  CPU=sparcv9
else 
  CPU=`isainfo | grep amd64`
  if [ "x$CPU" != "x" ]; then
     CPU=amd64
  else
     echo "unknown CPU, only sparcv9, amd64 are supported!"
     exit 1
  fi
fi

SA_LIBPATH=$STARTDIR/../src/os/solaris/proc/$CPU:$STARTDIR/solaris/$CPU

OPTIONS="-Dsa.library.path=$SA_LIBPATH -Dsun.jvm.hotspot.debugger.useProcDebugger"

if [ "x$SA_JAVA" = "x" ]; then
   SA_JAVA=java
fi

if [ "x$SA_DISABLE_VERS_CHK" != "x" ]; then
   OPTIONS="-Dsun.jvm.hotspot.runtime.VM.disableVersionCheck ${OPTIONS}"
fi

SA_CLASSPATH=$STARTDIR/../build/classes:$STARTDIR/../src/share/lib/maf-1_0.jar:$STARTDIR/../src/share/lib/jlfgr-1_0.jar:$STARTDIR/../src/share/lib/js.jar:$STARTDIR/sa.jar:$STARTDIR/lib/maf-1_0.jar:$STARTDIR/lib/jlfgr-1_0.jar:$STARTDIR/lib/js.jar

OPTIONS="-Djava.system.class.loader=sun.jvm.hotspot.SALauncherLoader ${OPTIONS}"

SA_JAVA_CMD="$SA_PREFIX_CMD $SA_JAVA -d64 -showversion ${OPTIONS} -cp $SA_CLASSPATH $SA_OPTIONS"
