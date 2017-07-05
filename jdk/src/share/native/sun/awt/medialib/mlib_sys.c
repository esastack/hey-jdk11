/*
 * Copyright 1997-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */


#include <stdlib.h>
#include <string.h>
#include <mlib_types.h>
#include <mlib_sys_proto.h>
#include "mlib_SysMath.h"

/***************************************************************/

#if ! defined ( __MEDIALIB_OLD_NAMES )
#if defined ( __SUNPRO_C )

#pragma weak mlib_memmove = __mlib_memmove
#pragma weak mlib_malloc = __mlib_malloc
#pragma weak mlib_realloc = __mlib_realloc
#pragma weak mlib_free = __mlib_free
#pragma weak mlib_memset = __mlib_memset
#pragma weak mlib_memcpy = __mlib_memcpy

#ifdef MLIB_NO_LIBSUNMATH
#pragma weak mlib_sincosf = __mlib_sincosf
#endif /* MLIB_NO_LIBSUNMATH */

#elif defined ( __GNUC__ ) /* defined ( __SUNPRO_C ) */

  __typeof__ ( __mlib_memmove) mlib_memmove
    __attribute__ ((weak,alias("__mlib_memmove")));
  __typeof__ ( __mlib_malloc) mlib_malloc
    __attribute__ ((weak,alias("__mlib_malloc")));
  __typeof__ ( __mlib_realloc) mlib_realloc
    __attribute__ ((weak,alias("__mlib_realloc")));
  __typeof__ ( __mlib_free) mlib_free
    __attribute__ ((weak,alias("__mlib_free")));
  __typeof__ ( __mlib_memset) mlib_memset
    __attribute__ ((weak,alias("__mlib_memset")));
  __typeof__ ( __mlib_memcpy) mlib_memcpy
    __attribute__ ((weak,alias("__mlib_memcpy")));

#ifdef MLIB_NO_LIBSUNMATH

void __mlib_sincosf (float x, float *s, float *c);

__typeof__ ( __mlib_sincosf) mlib_sincosf
    __attribute__ ((weak,alias("__mlib_sincosf")));
#endif /* MLIB_NO_LIBSUNMATH */

#else /* defined ( __SUNPRO_C ) */

#error  "unknown platform"

#endif /* defined ( __SUNPRO_C ) */
#endif /* ! defined ( __MEDIALIB_OLD_NAMES ) */

/***************************************************************/

void *__mlib_malloc(mlib_u32 size)
{
#ifdef _MSC_VER
  /*
   * Currently, all MS C compilers for Win32 platforms default to 8 byte
   * alignment. -- from stdlib.h of MS VC++5.0.
   */
  return (void *) malloc(size);
#else /* _MSC_VER */
  return (void *) memalign(8, size);
#endif /* _MSC_VER */
}

void *__mlib_realloc(void *ptr, mlib_u32 size)
{
  return realloc(ptr, size);
}

void __mlib_free(void *ptr)
{
  free(ptr);
}

void *__mlib_memset(void *s, mlib_s32 c, mlib_u32 n)
{
  return memset(s, c, n);
}

void *__mlib_memcpy(void *s1, void *s2, mlib_u32 n)
{
  return memcpy(s1, s2, n);
}

void *__mlib_memmove(void *s1, void *s2, mlib_u32 n)
{
  return memmove(s1, s2, n);
}

#ifdef MLIB_NO_LIBSUNMATH

void __mlib_sincosf (mlib_f32 x, mlib_f32 *s, mlib_f32 *c)
{
  *s = (mlib_f32)sin(x);
  *c = (mlib_f32)cos(x);
}

#endif /* MLIB_NO_LIBSUNMATH */
