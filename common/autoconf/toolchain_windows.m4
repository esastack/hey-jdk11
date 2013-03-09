#
# Copyright (c) 2011, 2012, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the LICENSE file that accompanied this code.
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

AC_DEFUN([TOOLCHAIN_CHECK_POSSIBLE_VISUAL_STUDIO_ROOT],
[
  if test "x$VS_ENV_CMD" = x; then
    VS100BASE="$1"
    METHOD="$2"
    BASIC_WINDOWS_REWRITE_AS_UNIX_PATH(VS100BASE)
    if test -d "$VS100BASE"; then
      if test -f "$VS100BASE/$VCVARSFILE"; then
        AC_MSG_NOTICE([Found Visual Studio installation at $VS100BASE using $METHOD])
        VS_ENV_CMD="$VS100BASE/$VCVARSFILE"
      else
        AC_MSG_NOTICE([Found Visual Studio installation at $VS100BASE using $METHOD])
        AC_MSG_NOTICE([Warning: $VCVARSFILE is missing, this is probably Visual Studio Express. Ignoring])
      fi
    fi
  fi
])

AC_DEFUN([TOOLCHAIN_CHECK_POSSIBLE_WIN_SDK_ROOT],
[
  if test "x$VS_ENV_CMD" = x; then
    WIN_SDK_BASE="$1"
    METHOD="$2"
    BASIC_WINDOWS_REWRITE_AS_UNIX_PATH(WIN_SDK_BASE)
    if test -d "$WIN_SDK_BASE"; then
      # There have been cases of partial or broken SDK installations. A missing
      # lib dir is not going to work.
      if test ! -d "$WIN_SDK_BASE/../lib"; then
        AC_MSG_NOTICE([Found Windows SDK installation at $WIN_SDK_BASE using $METHOD])
        AC_MSG_NOTICE([Warning: Installation is broken, lib dir is missing. Ignoring])
      elif test -f "$WIN_SDK_BASE/SetEnv.Cmd"; then
        AC_MSG_NOTICE([Found Windows SDK installation at $WIN_SDK_BASE using $METHOD])
        VS_ENV_CMD="$WIN_SDK_BASE/SetEnv.Cmd"
        if test "x$OPENJDK_TARGET_CPU_BITS" = x32; then
          VS_ENV_ARGS="/x86"
        else
          VS_ENV_ARGS="/x64"
        fi
      else
        AC_MSG_NOTICE([Found Windows SDK installation at $WIN_SDK_BASE using $METHOD])
        AC_MSG_NOTICE([Warning: Installation is broken, SetEnv.Cmd is missing. Ignoring])
      fi
    fi
  fi
])

AC_DEFUN([TOOLCHAIN_FIND_VISUAL_STUDIO_BAT_FILE],
[
  if test "x$OPENJDK_TARGET_CPU_BITS" = x32; then
    VCVARSFILE="vc/bin/vcvars32.bat"
  else
    VCVARSFILE="vc/bin/amd64/vcvars64.bat"
  fi 

  VS_ENV_CMD=""
  VS_ENV_ARGS=""
  if test "x$with_toolsdir" != x; then
    TOOLCHAIN_CHECK_POSSIBLE_VISUAL_STUDIO_ROOT([$with_toolsdir/../..], [--with-tools-dir])
  fi

  if test "x$with_toolsdir" != x && test "x$VS_ENV_CMD" = x; then
    # Having specified an argument which is incorrect will produce an instant failure;
    # we should not go on looking
    AC_MSG_NOTICE([The path given by --with-tools-dir does not contain a valid Visual Studio installation])
    AC_MSG_NOTICE([Please point to the VC/bin directory within the Visual Studio installation])
    AC_MSG_ERROR([Cannot locate a valid Visual Studio installation])
  fi  

  if test "x$ProgramW6432" != x; then
    TOOLCHAIN_CHECK_POSSIBLE_WIN_SDK_ROOT([$ProgramW6432/Microsoft SDKs/Windows/v7.1/Bin], [well-known name])
  fi
  if test "x$PROGRAMW6432" != x; then
    TOOLCHAIN_CHECK_POSSIBLE_WIN_SDK_ROOT([$PROGRAMW6432/Microsoft SDKs/Windows/v7.1/Bin], [well-known name])
  fi
  if test "x$PROGRAMFILES" != x; then
    TOOLCHAIN_CHECK_POSSIBLE_WIN_SDK_ROOT([$PROGRAMFILES/Microsoft SDKs/Windows/v7.1/Bin], [well-known name])
  fi
  TOOLCHAIN_CHECK_POSSIBLE_WIN_SDK_ROOT([C:/Program Files/Microsoft SDKs/Windows/v7.1/Bin], [well-known name])
  TOOLCHAIN_CHECK_POSSIBLE_WIN_SDK_ROOT([C:/Program Files (x86)/Microsoft SDKs/Windows/v7.1/Bin], [well-known name])

  if test "x$VS100COMNTOOLS" != x; then
    TOOLCHAIN_CHECK_POSSIBLE_VISUAL_STUDIO_ROOT([$VS100COMNTOOLS/../..], [VS100COMNTOOLS variable])
  fi
  if test "x$PROGRAMFILES" != x; then
    TOOLCHAIN_CHECK_POSSIBLE_VISUAL_STUDIO_ROOT([$PROGRAMFILES/Microsoft Visual Studio 10.0], [well-known name])
  fi
  TOOLCHAIN_CHECK_POSSIBLE_VISUAL_STUDIO_ROOT([C:/Program Files/Microsoft Visual Studio 10.0], [well-known name])
  TOOLCHAIN_CHECK_POSSIBLE_VISUAL_STUDIO_ROOT([C:/Program Files (x86)/Microsoft Visual Studio 10.0], [well-known name])
])

# Check if the VS env variables were setup prior to running configure.
# If not, then find vcvarsall.bat and run it automatically, and integrate
# the set env variables into the spec file.
AC_DEFUN([TOOLCHAIN_SETUP_VISUAL_STUDIO_ENV],
[
  # Store path to cygwin link.exe to help excluding it when searching for 
  # VS linker. This must be done before changing the PATH when looking for VS.
  AC_PATH_PROG(CYGWIN_LINK, link)
  if test "x$CYGWIN_LINK" != x; then
    AC_MSG_CHECKING([if the first found link.exe is actually the Cygwin link tool])
    "$CYGWIN_LINK" --version > /dev/null
    if test $? -eq 0 ; then
      AC_MSG_RESULT([yes])
    else
      AC_MSG_RESULT([no])
      # This might be the VS linker. Don't exclude it later on.
      CYGWIN_LINK=""
    fi
  fi

  # First-hand choice is to locate and run the vsvars bat file.
  TOOLCHAIN_FIND_VISUAL_STUDIO_BAT_FILE
  if test "x$VS_ENV_CMD" != x; then
    # We have found a Visual Studio environment on disk, let's extract variables from the vsvars bat file.
    BASIC_FIXUP_EXECUTABLE(VS_ENV_CMD)

    # Lets extract the variables that are set by vcvarsall.bat/vsvars32.bat/vsvars64.bat
    AC_MSG_NOTICE([Trying to extract Visual Studio environment variables])
    cd $OUTPUT_ROOT
    # FIXME: The code betweeen ---- was inlined from a separate script and is not properly adapted
    # to autoconf standards.

    #----

    # Cannot use the VS10 setup script directly (since it only updates the DOS subshell environment)
    # but calculate the difference in Cygwin environment before/after running it and then
    # apply the diff.

    if test "x$OPENJDK_BUILD_OS_ENV" = xwindows.cygwin; then
      _vs10varsall=`cygpath -a -m -s "$VS_ENV_CMD"`
      _dosvs10varsall=`cygpath -a -w -s $_vs10varsall`
      _dosbash=`cygpath -a -w -s \`which bash\`.*`
    else
      _dosvs10varsall=`cmd //c echo $VS_ENV_CMD`
      _dosbash=`cmd //c echo \`which bash\``
    fi

    # generate the set of exported vars before/after the vs10 setup
    $ECHO "@echo off"                                           >  localdevenvtmp.bat
    $ECHO "$_dosbash -c \"export -p\" > localdevenvtmp.export0" >> localdevenvtmp.bat
    $ECHO "call $_dosvs10varsall $VS_ENV_ARGS"                  >> localdevenvtmp.bat
    $ECHO "$_dosbash -c \"export -p\" > localdevenvtmp.export1" >> localdevenvtmp.bat

    # Now execute the newly created bat file.
    # The | cat is to stop SetEnv.Cmd to mess with system colors on msys
    cmd /c localdevenvtmp.bat | cat

    # apply the diff (less some non-vs10 vars named by "!")
    $SORT localdevenvtmp.export0 | $GREP -v "!" > localdevenvtmp.export0.sort
    $SORT localdevenvtmp.export1 | $GREP -v "!" > localdevenvtmp.export1.sort
    $COMM -1 -3 localdevenvtmp.export0.sort localdevenvtmp.export1.sort > localdevenv.sh

    # cleanup
    $RM localdevenvtmp*
    #----
    cd $CURDIR
    if test ! -s $OUTPUT_ROOT/localdevenv.sh; then
      AC_MSG_RESULT([no])
      AC_MSG_NOTICE([Could not succesfully extract the envionment variables needed for the VS setup.])
      AC_MSG_NOTICE([Try setting --with-tools-dir to the VC/bin directory within the VS installation])
      AC_MSG_NOTICE([or run "bash.exe -l" from a VS command prompt and then run configure from there.])
      AC_MSG_ERROR([Cannot continue])
    fi

    # Now set all paths and other env variables. This will allow the rest of 
    # the configure script to find and run the compiler in the proper way.
    AC_MSG_NOTICE([Setting extracted environment variables])
    . $OUTPUT_ROOT/localdevenv.sh
  else
    # We did not find a vsvars bat file, let's hope we are run from a VS command prompt.
    AC_MSG_NOTICE([Cannot locate a valid Visual Studio installation, checking current environment])
  fi

  # At this point, we should have corrent variables in the environment, or we can't continue.
  AC_MSG_CHECKING([for Visual Studio variables])
  
  if test "x$VCINSTALLDIR" != x || test "x$WindowsSDKDir" != x || test "x$WINDOWSSDKDIR" != x; then
    if test "x$INCLUDE" = x || test "x$LIB" = x; then
      AC_MSG_RESULT([present but broken])
      AC_MSG_ERROR([Your VC command prompt seems broken, INCLUDE and/or LIB is missing.])
    else
      AC_MSG_RESULT([ok])
      # Remove any trailing \ from INCLUDE and LIB to avoid trouble in spec.gmk.
      VS_INCLUDE=`$ECHO "$INCLUDE" | $SED 's/\\\\$//'`
      VS_LIB=`$ECHO "$LIB" | $SED 's/\\\\$//'`
      VS_PATH="$PATH"
      AC_SUBST(VS_INCLUDE)
      AC_SUBST(VS_LIB)
      AC_SUBST(VS_PATH)
    fi
  else
    AC_MSG_RESULT([not found])

    if test "x$VS_ENV_CMD" = x; then
      AC_MSG_NOTICE([Cannot locate a valid Visual Studio or Windows SDK installation on disk,])
      AC_MSG_NOTICE([nor is this script run from a Visual Studio command prompt.])
    else
      AC_MSG_NOTICE([Running the extraction script failed.])
    fi
    AC_MSG_NOTICE([Try setting --with-tools-dir to the VC/bin directory within the VS installation])
    AC_MSG_NOTICE([or run "bash.exe -l" from a VS command prompt and then run configure from there.])
    AC_MSG_ERROR([Cannot continue])
  fi
  
  AC_MSG_CHECKING([for msvcr100.dll])
  AC_ARG_WITH(msvcr-dll, [AS_HELP_STRING([--with-msvcr-dll],
      [copy this msvcr100.dll into the built JDK (Windows only) @<:@probed@:>@])])
  if test "x$with_msvcr_dll" != x; then
    MSVCR_DLL="$with_msvcr_dll"
  else
    if test "x$VCINSTALLDIR" != x; then
      if test "x$OPENJDK_TARGET_CPU_BITS" = x64; then
        MSVCR_DLL=`find "$VCINSTALLDIR" -name msvcr100.dll | grep x64 | head --lines 1`
      else
        MSVCR_DLL=`find "$VCINSTALLDIR" -name msvcr100.dll | grep x86 | grep -v ia64 | grep -v x64 | head --lines 1`
        if test "x$MSVCR_DLL" = x; then
          MSVCR_DLL=`find "$VCINSTALLDIR" -name msvcr100.dll | head --lines 1`
        fi
      fi
      if test "x$MSVCR_DLL" != x; then
        AC_MSG_NOTICE([msvcr100.dll found in VCINSTALLDIR: $VCINSTALLDIR])
      else
        AC_MSG_NOTICE([Warning: msvcr100.dll not found in VCINSTALLDIR: $VCINSTALLDIR])
      fi
    fi
    if test "x$MSVCR_DLL" = x; then
      if test -f "$SYSTEMROOT/system32/msvcr100.dll"; then
        AC_MSG_NOTICE([msvcr100.dll found in $SYSTEMROOT/system32])
        MSVCR_DLL="$SYSTEMROOT/system32/msvcr100.dll"
      fi
    fi
  fi
  if test "x$MSVCR_DLL" = x; then
    AC_MSG_RESULT([no])
    AC_MSG_ERROR([Could not find msvcr100.dll !])
  fi
  AC_MSG_RESULT([$MSVCR_DLL])
  BASIC_FIXUP_PATH(MSVCR_DLL)
])


# Setup the DXSDK paths
AC_DEFUN([TOOLCHAIN_SETUP_DXSDK],
[
  AC_ARG_WITH(dxsdk, [AS_HELP_STRING([--with-dxsdk],
      [the DirectX SDK (Windows only) @<:@probed@:>@])])
  AC_ARG_WITH(dxsdk-lib, [AS_HELP_STRING([--with-dxsdk-lib],
      [the DirectX SDK lib directory (Windows only) @<:@probed@:>@])])
  AC_ARG_WITH(dxsdk-include, [AS_HELP_STRING([--with-dxsdk-include],
      [the DirectX SDK include directory (Windows only) @<:@probed@:>@])])

  AC_MSG_CHECKING([for DirectX SDK])

  if test "x$with_dxsdk" != x; then
    dxsdk_path="$with_dxsdk"
  elif test "x$DXSDK_DIR" != x; then
    dxsdk_path="$DXSDK_DIR"
  elif test -d "C:/DXSDK"; then
    dxsdk_path="C:/DXSDK"
  else
    AC_MSG_ERROR([Could not find the DirectX SDK])
  fi
  AC_MSG_RESULT([$dxsdk_path])
  BASIC_FIXUP_PATH(dxsdk_path)

  AC_MSG_CHECKING([for DirectX SDK lib dir])
  if test "x$with_dxsdk_lib" != x; then
    DXSDK_LIB_PATH="$with_dxsdk_lib"
  elif test "x$OPENJDK_TARGET_CPU" = "xx86_64"; then
    DXSDK_LIB_PATH="$dxsdk_path/Lib/x64"
  else
    DXSDK_LIB_PATH="$dxsdk_path/Lib"
  fi
  # dsound.lib is linked to in jsoundds
  if test ! -f "$DXSDK_LIB_PATH/dsound.lib"; then
    AC_MSG_ERROR([Invalid DirectX SDK lib dir])
  fi
  AC_MSG_RESULT([$DXSDK_LIB_PATH])
  BASIC_FIXUP_PATH(DXSDK_LIB_PATH)

  AC_MSG_CHECKING([for DirectX SDK include dir])
  if test "x$with_dxsdk_include" != x; then
    DXSDK_INCLUDE_PATH="$with_dxsdk_include"
  else
    DXSDK_INCLUDE_PATH="$dxsdk_path/Include"
  fi
  # dsound.h is included in jsoundds
  if test ! -f "$DXSDK_INCLUDE_PATH/dsound.h"; then
    AC_MSG_ERROR([Invalid DirectX SDK lib dir])
  fi
  AC_MSG_RESULT([$DXSDK_INCLUDE_PATH])
  BASIC_FIXUP_PATH(DXSDK_INCLUDE_PATH)

  AC_SUBST(DXSDK_LIB_PATH)
  AC_SUBST(DXSDK_INCLUDE_PATH)
  LDFLAGS_JDK="$LDFLAGS_JDK -libpath:$DXSDK_LIB_PATH"
])
