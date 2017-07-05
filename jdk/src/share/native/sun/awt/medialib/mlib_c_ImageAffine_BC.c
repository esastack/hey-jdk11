/*
 * Copyright 1998-2003 Sun Microsystems, Inc.  All Rights Reserved.
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


/*
 * FUNCTION
 *      Image affine transformation with Bicubic filtering
 * SYNOPSIS
 *      mlib_status mlib_ImageAffine_[u8|s16|u16]_?ch_bc(mlib_s32 *leftEdges,
 *                                                       mlib_s32 *rightEdges,
 *                                                       mlib_s32 *xStarts,
 *                                                       mlib_s32 *yStarts,
 *                                                       mlib_s32 *sides,
 *                                                       mlib_u8  *dstData,
 *                                                       mlib_u8  **lineAddr,
 *                                                       mlib_s32 dstYStride,
 *                                                       mlib_s32 is_affine,
 *                                                       mlib_s32 srcYStride,
 *                                                       mlib_filter filter)
 *
 *
 * ARGUMENTS
 *      leftEdges  array[dstHeight] of xLeft coordinates
 *      RightEdges array[dstHeight] of xRight coordinates
 *      xStarts    array[dstHeight] of xStart * 65536 coordinates
 *      yStarts    array[dstHeight] of yStart * 65536 coordinates
 *      sides      output array[4]. sides[0] is yStart, sides[1] is yFinish,
 *                 sides[2] is dx * 65536, sides[3] is dy * 65536
 *      dstData    pointer to the first pixel on (yStart - 1) line
 *      lineAddr   array[srcHeight] of pointers to the first pixel on
 *                 the corresponding lines
 *      dstYStride stride of destination image
 *      is_affine  indicator (Affine - GridWarp)
 *      srcYStride stride of source image
 *      filter     type of resampling filter
 *
 * DESCRIPTION
 *      The functions step along the lines from xLeft to xRight and apply
 *      the bicubic filtering.
 *
 */

#include "mlib_ImageAffine.h"

#define DTYPE  mlib_u8

#define FUN_NAME(CHAN) mlib_ImageAffine_u8_##CHAN##_bc

#define FILTER_BITS   8

/***************************************************************/
#ifdef __sparc /* for SPARC, using floating-point multiplies is faster */

#undef  FILTER_ELEM_BITS
#define FILTER_ELEM_BITS  4

#ifdef MLIB_USE_FTOI_CLAMPING

#define SAT8(DST)                                               \
  DST = ((mlib_s32)(val0 - sat) >> 24) ^ 0x80

#else

#define SAT8(DST)                                               \
  val0 -= sat;                                                  \
  if (val0 >= MLIB_S32_MAX)                                     \
    DST = MLIB_U8_MAX;                                          \
  else if (val0 <= MLIB_S32_MIN)                                \
    DST = MLIB_U8_MIN;                                          \
  else                                                          \
    DST = ((mlib_s32)val0 >> 24) ^ 0x80

#endif /* MLIB_USE_FTOI_CLAMPING */

/***************************************************************/
mlib_status FUN_NAME(1ch)(mlib_affine_param *param)
{
  DECLAREVAR_BC();
  DTYPE *dstLineEnd;
  mlib_d64 sat = (mlib_d64) 0x7F800000;
  const mlib_f32 *mlib_filters_table;

  if (filter == MLIB_BICUBIC) {
    mlib_filters_table = mlib_filters_u8f_bc;
  }
  else {
    mlib_filters_table = mlib_filters_u8f_bc2;
  }

  for (j = yStart; j <= yFinish; j++) {
    mlib_d64 xf0, xf1, xf2, xf3;
    mlib_d64 yf0, yf1, yf2, yf3;
    mlib_d64 c0, c1, c2, c3, val0;
    mlib_s32 filterpos;
    mlib_f32 *fptr;
    mlib_u8 s0, s1, s2, s3;

    CLIP(1);
    dstLineEnd = (DTYPE *) dstData + xRight;

    filterpos = (X >> FILTER_SHIFT) & FILTER_MASK;
    fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

    xf0 = fptr[0];
    xf1 = fptr[1];
    xf2 = fptr[2];
    xf3 = fptr[3];

    filterpos = (Y >> FILTER_SHIFT) & FILTER_MASK;
    fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

    yf0 = fptr[0];
    yf1 = fptr[1];
    yf2 = fptr[2];
    yf3 = fptr[3];

    xSrc = (X >> MLIB_SHIFT) - 1;
    ySrc = (Y >> MLIB_SHIFT) - 1;

    srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + xSrc;
    s0 = srcPixelPtr[0];
    s1 = srcPixelPtr[1];
    s2 = srcPixelPtr[2];
    s3 = srcPixelPtr[3];

#ifdef __SUNPRO_C
#pragma pipeloop(0)
#endif /* __SUNPRO_C */
    for (; dstPixelPtr <= (dstLineEnd - 1); dstPixelPtr++) {
      X += dX;
      Y += dY;

      c0 = (mlib_U82D64[s0] * xf0 + mlib_U82D64[s1] * xf1 +
            mlib_U82D64[s2] * xf2 + mlib_U82D64[s3] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c1 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[1]] * xf1 +
            mlib_U82D64[srcPixelPtr[2]] * xf2 + mlib_U82D64[srcPixelPtr[3]] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c2 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[1]] * xf1 +
            mlib_U82D64[srcPixelPtr[2]] * xf2 + mlib_U82D64[srcPixelPtr[3]] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c3 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[1]] * xf1 +
            mlib_U82D64[srcPixelPtr[2]] * xf2 + mlib_U82D64[srcPixelPtr[3]] * xf3);

      filterpos = (X >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      xf0 = fptr[0];
      xf1 = fptr[1];
      xf2 = fptr[2];
      xf3 = fptr[3];

      val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3);

      filterpos = (Y >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      yf0 = fptr[0];
      yf1 = fptr[1];
      yf2 = fptr[2];
      yf3 = fptr[3];

      SAT8(dstPixelPtr[0]);

      xSrc = (X >> MLIB_SHIFT) - 1;
      ySrc = (Y >> MLIB_SHIFT) - 1;

      srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + xSrc;
      s0 = srcPixelPtr[0];
      s1 = srcPixelPtr[1];
      s2 = srcPixelPtr[2];
      s3 = srcPixelPtr[3];
    }

    c0 = (mlib_U82D64[s0] * xf0 + mlib_U82D64[s1] * xf1 +
          mlib_U82D64[s2] * xf2 + mlib_U82D64[s3] * xf3);
    srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
    c1 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[1]] * xf1 +
          mlib_U82D64[srcPixelPtr[2]] * xf2 + mlib_U82D64[srcPixelPtr[3]] * xf3);
    srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
    c2 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[1]] * xf1 +
          mlib_U82D64[srcPixelPtr[2]] * xf2 + mlib_U82D64[srcPixelPtr[3]] * xf3);
    srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
    c3 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[1]] * xf1 +
          mlib_U82D64[srcPixelPtr[2]] * xf2 + mlib_U82D64[srcPixelPtr[3]] * xf3);

    val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3);

    SAT8(dstPixelPtr[0]);
  }

  return MLIB_SUCCESS;
}

/***************************************************************/
mlib_status FUN_NAME(2ch)(mlib_affine_param *param)
{
  DECLAREVAR_BC();
  DTYPE *dstLineEnd;
  mlib_d64 sat = (mlib_d64) 0x7F800000;
  const mlib_f32 *mlib_filters_table;

  if (filter == MLIB_BICUBIC) {
    mlib_filters_table = mlib_filters_u8f_bc;
  }
  else {
    mlib_filters_table = mlib_filters_u8f_bc2;
  }

  for (j = yStart; j <= yFinish; j++) {
    mlib_d64 xf0, xf1, xf2, xf3;
    mlib_d64 yf0, yf1, yf2, yf3;
    mlib_d64 c0, c1, c2, c3, val0;
    mlib_s32 filterpos, k;
    mlib_f32 *fptr;
    mlib_u8 s0, s1, s2, s3;

    CLIP(2);
    dstLineEnd = (DTYPE *) dstData + 2 * xRight;

    for (k = 0; k < 2; k++) {
      mlib_s32 X1 = X;
      mlib_s32 Y1 = Y;
      DTYPE *dPtr = dstPixelPtr + k;

      filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      xf0 = fptr[0];
      xf1 = fptr[1];
      xf2 = fptr[2];
      xf3 = fptr[3];

      filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      yf0 = fptr[0];
      yf1 = fptr[1];
      yf2 = fptr[2];
      yf3 = fptr[3];

      xSrc = (X1 >> MLIB_SHIFT) - 1;
      ySrc = (Y1 >> MLIB_SHIFT) - 1;

      srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 2 * xSrc + k;
      s0 = srcPixelPtr[0];
      s1 = srcPixelPtr[2];
      s2 = srcPixelPtr[4];
      s3 = srcPixelPtr[6];

#ifdef __SUNPRO_C
#pragma pipeloop(0)
#endif /* __SUNPRO_C */
      for (; dPtr <= (dstLineEnd - 1); dPtr += 2) {
        X1 += dX;
        Y1 += dY;

        c0 = (mlib_U82D64[s0] * xf0 + mlib_U82D64[s1] * xf1 +
              mlib_U82D64[s2] * xf2 + mlib_U82D64[s3] * xf3);
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c1 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[2]] * xf1 +
              mlib_U82D64[srcPixelPtr[4]] * xf2 + mlib_U82D64[srcPixelPtr[6]] * xf3);
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c2 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[2]] * xf1 +
              mlib_U82D64[srcPixelPtr[4]] * xf2 + mlib_U82D64[srcPixelPtr[6]] * xf3);
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c3 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[2]] * xf1 +
              mlib_U82D64[srcPixelPtr[4]] * xf2 + mlib_U82D64[srcPixelPtr[6]] * xf3);

        filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        xf0 = fptr[0];
        xf1 = fptr[1];
        xf2 = fptr[2];
        xf3 = fptr[3];

        val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3);

        filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        yf0 = fptr[0];
        yf1 = fptr[1];
        yf2 = fptr[2];
        yf3 = fptr[3];

        SAT8(dPtr[0]);

        xSrc = (X1 >> MLIB_SHIFT) - 1;
        ySrc = (Y1 >> MLIB_SHIFT) - 1;

        srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 2 * xSrc + k;
        s0 = srcPixelPtr[0];
        s1 = srcPixelPtr[2];
        s2 = srcPixelPtr[4];
        s3 = srcPixelPtr[6];
      }

      c0 = (mlib_U82D64[s0] * xf0 + mlib_U82D64[s1] * xf1 +
            mlib_U82D64[s2] * xf2 + mlib_U82D64[s3] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c1 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[2]] * xf1 +
            mlib_U82D64[srcPixelPtr[4]] * xf2 + mlib_U82D64[srcPixelPtr[6]] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c2 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[2]] * xf1 +
            mlib_U82D64[srcPixelPtr[4]] * xf2 + mlib_U82D64[srcPixelPtr[6]] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c3 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[2]] * xf1 +
            mlib_U82D64[srcPixelPtr[4]] * xf2 + mlib_U82D64[srcPixelPtr[6]] * xf3);

      val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3);

      SAT8(dPtr[0]);
    }
  }

  return MLIB_SUCCESS;
}

/***************************************************************/
mlib_status FUN_NAME(3ch)(mlib_affine_param *param)
{
  DECLAREVAR_BC();
  DTYPE *dstLineEnd;
  mlib_d64 sat = (mlib_d64) 0x7F800000;
  const mlib_f32 *mlib_filters_table;

  if (filter == MLIB_BICUBIC) {
    mlib_filters_table = mlib_filters_u8f_bc;
  }
  else {
    mlib_filters_table = mlib_filters_u8f_bc2;
  }

  for (j = yStart; j <= yFinish; j++) {
    mlib_d64 xf0, xf1, xf2, xf3;
    mlib_d64 yf0, yf1, yf2, yf3;
    mlib_d64 c0, c1, c2, c3, val0;
    mlib_s32 filterpos, k;
    mlib_f32 *fptr;
    mlib_u8 s0, s1, s2, s3;

    CLIP(3);
    dstLineEnd = (DTYPE *) dstData + 3 * xRight;

    for (k = 0; k < 3; k++) {
      mlib_s32 X1 = X;
      mlib_s32 Y1 = Y;
      DTYPE *dPtr = dstPixelPtr + k;

      filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      xf0 = fptr[0];
      xf1 = fptr[1];
      xf2 = fptr[2];
      xf3 = fptr[3];

      filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      yf0 = fptr[0];
      yf1 = fptr[1];
      yf2 = fptr[2];
      yf3 = fptr[3];

      xSrc = (X1 >> MLIB_SHIFT) - 1;
      ySrc = (Y1 >> MLIB_SHIFT) - 1;

      srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 3 * xSrc + k;
      s0 = srcPixelPtr[0];
      s1 = srcPixelPtr[3];
      s2 = srcPixelPtr[6];
      s3 = srcPixelPtr[9];

#ifdef __SUNPRO_C
#pragma pipeloop(0)
#endif /* __SUNPRO_C */
      for (; dPtr <= (dstLineEnd - 1); dPtr += 3) {
        X1 += dX;
        Y1 += dY;

        c0 = (mlib_U82D64[s0] * xf0 + mlib_U82D64[s1] * xf1 +
              mlib_U82D64[s2] * xf2 + mlib_U82D64[s3] * xf3);
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c1 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[3]] * xf1 +
              mlib_U82D64[srcPixelPtr[6]] * xf2 + mlib_U82D64[srcPixelPtr[9]] * xf3);
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c2 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[3]] * xf1 +
              mlib_U82D64[srcPixelPtr[6]] * xf2 + mlib_U82D64[srcPixelPtr[9]] * xf3);
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c3 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[3]] * xf1 +
              mlib_U82D64[srcPixelPtr[6]] * xf2 + mlib_U82D64[srcPixelPtr[9]] * xf3);

        filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        xf0 = fptr[0];
        xf1 = fptr[1];
        xf2 = fptr[2];
        xf3 = fptr[3];

        val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3);

        filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        yf0 = fptr[0];
        yf1 = fptr[1];
        yf2 = fptr[2];
        yf3 = fptr[3];

        SAT8(dPtr[0]);

        xSrc = (X1 >> MLIB_SHIFT) - 1;
        ySrc = (Y1 >> MLIB_SHIFT) - 1;

        srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 3 * xSrc + k;
        s0 = srcPixelPtr[0];
        s1 = srcPixelPtr[3];
        s2 = srcPixelPtr[6];
        s3 = srcPixelPtr[9];
      }

      c0 = (mlib_U82D64[s0] * xf0 + mlib_U82D64[s1] * xf1 +
            mlib_U82D64[s2] * xf2 + mlib_U82D64[s3] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c1 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[3]] * xf1 +
            mlib_U82D64[srcPixelPtr[6]] * xf2 + mlib_U82D64[srcPixelPtr[9]] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c2 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[3]] * xf1 +
            mlib_U82D64[srcPixelPtr[6]] * xf2 + mlib_U82D64[srcPixelPtr[9]] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c3 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[3]] * xf1 +
            mlib_U82D64[srcPixelPtr[6]] * xf2 + mlib_U82D64[srcPixelPtr[9]] * xf3);

      val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3);

      SAT8(dPtr[0]);
    }
  }

  return MLIB_SUCCESS;
}

/***************************************************************/
mlib_status FUN_NAME(4ch)(mlib_affine_param *param)
{
  DECLAREVAR_BC();
  DTYPE *dstLineEnd;
  mlib_d64 sat = (mlib_d64) 0x7F800000;
  const mlib_f32 *mlib_filters_table;

  if (filter == MLIB_BICUBIC) {
    mlib_filters_table = mlib_filters_u8f_bc;
  }
  else {
    mlib_filters_table = mlib_filters_u8f_bc2;
  }

  for (j = yStart; j <= yFinish; j++) {
    mlib_d64 xf0, xf1, xf2, xf3;
    mlib_d64 yf0, yf1, yf2, yf3;
    mlib_d64 c0, c1, c2, c3, val0;
    mlib_s32 filterpos, k;
    mlib_f32 *fptr;
    mlib_u8 s0, s1, s2, s3;

    CLIP(4);
    dstLineEnd = (DTYPE *) dstData + 4 * xRight;

    for (k = 0; k < 4; k++) {
      mlib_s32 X1 = X;
      mlib_s32 Y1 = Y;
      DTYPE *dPtr = dstPixelPtr + k;

      filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      xf0 = fptr[0];
      xf1 = fptr[1];
      xf2 = fptr[2];
      xf3 = fptr[3];

      filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      yf0 = fptr[0];
      yf1 = fptr[1];
      yf2 = fptr[2];
      yf3 = fptr[3];

      xSrc = (X1 >> MLIB_SHIFT) - 1;
      ySrc = (Y1 >> MLIB_SHIFT) - 1;

      srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 4 * xSrc + k;
      s0 = srcPixelPtr[0];
      s1 = srcPixelPtr[4];
      s2 = srcPixelPtr[8];
      s3 = srcPixelPtr[12];

#ifdef __SUNPRO_C
#pragma pipeloop(0)
#endif /* __SUNPRO_C */
      for (; dPtr <= (dstLineEnd - 1); dPtr += 4) {
        X1 += dX;
        Y1 += dY;

        c0 = (mlib_U82D64[s0] * xf0 + mlib_U82D64[s1] * xf1 +
              mlib_U82D64[s2] * xf2 + mlib_U82D64[s3] * xf3);
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c1 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[4]] * xf1 +
              mlib_U82D64[srcPixelPtr[8]] * xf2 + mlib_U82D64[srcPixelPtr[12]] * xf3);
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c2 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[4]] * xf1 +
              mlib_U82D64[srcPixelPtr[8]] * xf2 + mlib_U82D64[srcPixelPtr[12]] * xf3);
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c3 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[4]] * xf1 +
              mlib_U82D64[srcPixelPtr[8]] * xf2 + mlib_U82D64[srcPixelPtr[12]] * xf3);

        filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        xf0 = fptr[0];
        xf1 = fptr[1];
        xf2 = fptr[2];
        xf3 = fptr[3];

        val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3);

        filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_f32 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        yf0 = fptr[0];
        yf1 = fptr[1];
        yf2 = fptr[2];
        yf3 = fptr[3];

        SAT8(dPtr[0]);

        xSrc = (X1 >> MLIB_SHIFT) - 1;
        ySrc = (Y1 >> MLIB_SHIFT) - 1;

        srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 4 * xSrc + k;
        s0 = srcPixelPtr[0];
        s1 = srcPixelPtr[4];
        s2 = srcPixelPtr[8];
        s3 = srcPixelPtr[12];
      }

      c0 = (mlib_U82D64[s0] * xf0 + mlib_U82D64[s1] * xf1 +
            mlib_U82D64[s2] * xf2 + mlib_U82D64[s3] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c1 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[4]] * xf1 +
            mlib_U82D64[srcPixelPtr[8]] * xf2 + mlib_U82D64[srcPixelPtr[12]] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c2 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[4]] * xf1 +
            mlib_U82D64[srcPixelPtr[8]] * xf2 + mlib_U82D64[srcPixelPtr[12]] * xf3);
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c3 = (mlib_U82D64[srcPixelPtr[0]] * xf0 + mlib_U82D64[srcPixelPtr[4]] * xf1 +
            mlib_U82D64[srcPixelPtr[8]] * xf2 + mlib_U82D64[srcPixelPtr[12]] * xf3);

      val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3);

      SAT8(dPtr[0]);
    }
  }

  return MLIB_SUCCESS;
}

#else       /* for x86, using integer multiplies is faster */

#define SHIFT_X  12
#define ROUND_X  0 /* (1 << (SHIFT_X - 1)) */

#define SHIFT_Y  (14 + 14 - SHIFT_X)
#define ROUND_Y  (1 << (SHIFT_Y - 1))

/***************************************************************/
/* Test for the presence of any "1" bit in bits
   8 to 31 of val. If present, then val is either
   negative or >255. If over/underflows of 8 bits
   are uncommon, then this technique can be a win,
   since only a single test, rather than two, is
   necessary to determine if clamping is needed.
   On the other hand, if over/underflows are common,
   it adds an extra test.
*/
#define S32_TO_U8_SAT(DST)                                      \
  if (val0 & 0xffffff00) {                                      \
    if (val0 < MLIB_U8_MIN)                                     \
      DST = MLIB_U8_MIN;                                        \
    else                                                        \
      DST = MLIB_U8_MAX;                                        \
  } else {                                                      \
    DST = (mlib_u8)val0;                                        \
  }

/***************************************************************/
mlib_status FUN_NAME(1ch)(mlib_affine_param *param)
{
  DECLAREVAR_BC();
  DTYPE *dstLineEnd;
  const mlib_s16 *mlib_filters_table;

  if (filter == MLIB_BICUBIC) {
    mlib_filters_table = (mlib_s16 *) mlib_filters_u8_bc;
  }
  else {
    mlib_filters_table = (mlib_s16 *) mlib_filters_u8_bc2;
  }

  for (j = yStart; j <= yFinish; j++) {
    mlib_s32 xf0, xf1, xf2, xf3;
    mlib_s32 yf0, yf1, yf2, yf3;
    mlib_s32 c0, c1, c2, c3, val0;
    mlib_s32 filterpos;
    mlib_s16 *fptr;
    mlib_u8 s0, s1, s2, s3;

    CLIP(1);
    dstLineEnd = (DTYPE *) dstData + xRight;

    filterpos = (X >> FILTER_SHIFT) & FILTER_MASK;
    fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

    xf0 = fptr[0];
    xf1 = fptr[1];
    xf2 = fptr[2];
    xf3 = fptr[3];

    filterpos = (Y >> FILTER_SHIFT) & FILTER_MASK;
    fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

    yf0 = fptr[0];
    yf1 = fptr[1];
    yf2 = fptr[2];
    yf3 = fptr[3];

    xSrc = (X >> MLIB_SHIFT) - 1;
    ySrc = (Y >> MLIB_SHIFT) - 1;

    srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + xSrc;
    s0 = srcPixelPtr[0];
    s1 = srcPixelPtr[1];
    s2 = srcPixelPtr[2];
    s3 = srcPixelPtr[3];

#ifdef __SUNPRO_C
#pragma pipeloop(0)
#endif /* __SUNPRO_C */
    for (; dstPixelPtr <= (dstLineEnd - 1); dstPixelPtr++) {
      X += dX;
      Y += dY;

      c0 = (s0 * xf0 + s1 * xf1 + s2 * xf2 + s3 * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c1 = (srcPixelPtr[0] * xf0 + srcPixelPtr[1] * xf1 +
            srcPixelPtr[2] * xf2 + srcPixelPtr[3] * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c2 = (srcPixelPtr[0] * xf0 + srcPixelPtr[1] * xf1 +
            srcPixelPtr[2] * xf2 + srcPixelPtr[3] * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c3 = (srcPixelPtr[0] * xf0 + srcPixelPtr[1] * xf1 +
            srcPixelPtr[2] * xf2 + srcPixelPtr[3] * xf3 + ROUND_X) >> SHIFT_X;

      filterpos = (X >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      xf0 = fptr[0];
      xf1 = fptr[1];
      xf2 = fptr[2];
      xf3 = fptr[3];

      val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3 + ROUND_Y) >> SHIFT_Y;

      filterpos = (Y >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      yf0 = fptr[0];
      yf1 = fptr[1];
      yf2 = fptr[2];
      yf3 = fptr[3];

      S32_TO_U8_SAT(dstPixelPtr[0]);

      xSrc = (X >> MLIB_SHIFT) - 1;
      ySrc = (Y >> MLIB_SHIFT) - 1;

      srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + xSrc;
      s0 = srcPixelPtr[0];
      s1 = srcPixelPtr[1];
      s2 = srcPixelPtr[2];
      s3 = srcPixelPtr[3];
    }

    c0 = (s0 * xf0 + s1 * xf1 + s2 * xf2 + s3 * xf3 + ROUND_X) >> SHIFT_X;
    srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
    c1 = (srcPixelPtr[0] * xf0 + srcPixelPtr[1] * xf1 +
          srcPixelPtr[2] * xf2 + srcPixelPtr[3] * xf3 + ROUND_X) >> SHIFT_X;
    srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
    c2 = (srcPixelPtr[0] * xf0 + srcPixelPtr[1] * xf1 +
          srcPixelPtr[2] * xf2 + srcPixelPtr[3] * xf3 + ROUND_X) >> SHIFT_X;
    srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
    c3 = (srcPixelPtr[0] * xf0 + srcPixelPtr[1] * xf1 +
          srcPixelPtr[2] * xf2 + srcPixelPtr[3] * xf3 + ROUND_X) >> SHIFT_X;

    val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3 + ROUND_Y) >> SHIFT_Y;

    S32_TO_U8_SAT(dstPixelPtr[0]);
  }

  return MLIB_SUCCESS;
}

/***************************************************************/
mlib_status FUN_NAME(2ch)(mlib_affine_param *param)
{
  DECLAREVAR_BC();
  DTYPE *dstLineEnd;
  const mlib_s16 *mlib_filters_table;

  if (filter == MLIB_BICUBIC) {
    mlib_filters_table = (mlib_s16 *) mlib_filters_u8_bc;
  }
  else {
    mlib_filters_table = (mlib_s16 *) mlib_filters_u8_bc2;
  }

  for (j = yStart; j <= yFinish; j++) {
    mlib_s32 xf0, xf1, xf2, xf3;
    mlib_s32 yf0, yf1, yf2, yf3;
    mlib_s32 c0, c1, c2, c3, val0;
    mlib_s32 filterpos, k;
    mlib_s16 *fptr;
    mlib_u8 s0, s1, s2, s3;

    CLIP(2);
    dstLineEnd = (DTYPE *) dstData + 2 * xRight;

    for (k = 0; k < 2; k++) {
      mlib_s32 X1 = X;
      mlib_s32 Y1 = Y;
      DTYPE *dPtr = dstPixelPtr + k;

      filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      xf0 = fptr[0];
      xf1 = fptr[1];
      xf2 = fptr[2];
      xf3 = fptr[3];

      filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      yf0 = fptr[0];
      yf1 = fptr[1];
      yf2 = fptr[2];
      yf3 = fptr[3];

      xSrc = (X1 >> MLIB_SHIFT) - 1;
      ySrc = (Y1 >> MLIB_SHIFT) - 1;

      srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 2 * xSrc + k;
      s0 = srcPixelPtr[0];
      s1 = srcPixelPtr[2];
      s2 = srcPixelPtr[4];
      s3 = srcPixelPtr[6];

#ifdef __SUNPRO_C
#pragma pipeloop(0)
#endif /* __SUNPRO_C */
      for (; dPtr <= (dstLineEnd - 1); dPtr += 2) {
        X1 += dX;
        Y1 += dY;

        c0 = (s0 * xf0 + s1 * xf1 + s2 * xf2 + s3 * xf3 + ROUND_X) >> SHIFT_X;
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c1 = (srcPixelPtr[0] * xf0 + srcPixelPtr[2] * xf1 +
              srcPixelPtr[4] * xf2 + srcPixelPtr[6] * xf3 + ROUND_X) >> SHIFT_X;
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c2 = (srcPixelPtr[0] * xf0 + srcPixelPtr[2] * xf1 +
              srcPixelPtr[4] * xf2 + srcPixelPtr[6] * xf3 + ROUND_X) >> SHIFT_X;
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c3 = (srcPixelPtr[0] * xf0 + srcPixelPtr[2] * xf1 +
              srcPixelPtr[4] * xf2 + srcPixelPtr[6] * xf3 + ROUND_X) >> SHIFT_X;

        filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        xf0 = fptr[0];
        xf1 = fptr[1];
        xf2 = fptr[2];
        xf3 = fptr[3];

        val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3 + ROUND_Y) >> SHIFT_Y;

        filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        yf0 = fptr[0];
        yf1 = fptr[1];
        yf2 = fptr[2];
        yf3 = fptr[3];

        S32_TO_U8_SAT(dPtr[0]);

        xSrc = (X1 >> MLIB_SHIFT) - 1;
        ySrc = (Y1 >> MLIB_SHIFT) - 1;

        srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 2 * xSrc + k;
        s0 = srcPixelPtr[0];
        s1 = srcPixelPtr[2];
        s2 = srcPixelPtr[4];
        s3 = srcPixelPtr[6];
      }

      c0 = (s0 * xf0 + s1 * xf1 + s2 * xf2 + s3 * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c1 = (srcPixelPtr[0] * xf0 + srcPixelPtr[2] * xf1 +
            srcPixelPtr[4] * xf2 + srcPixelPtr[6] * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c2 = (srcPixelPtr[0] * xf0 + srcPixelPtr[2] * xf1 +
            srcPixelPtr[4] * xf2 + srcPixelPtr[6] * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c3 = (srcPixelPtr[0] * xf0 + srcPixelPtr[2] * xf1 +
            srcPixelPtr[4] * xf2 + srcPixelPtr[6] * xf3 + ROUND_X) >> SHIFT_X;

      val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3 + ROUND_Y) >> SHIFT_Y;

      S32_TO_U8_SAT(dPtr[0]);
    }
  }

  return MLIB_SUCCESS;
}

/***************************************************************/
mlib_status FUN_NAME(3ch)(mlib_affine_param *param)
{
  DECLAREVAR_BC();
  DTYPE *dstLineEnd;
  const mlib_s16 *mlib_filters_table;

  if (filter == MLIB_BICUBIC) {
    mlib_filters_table = (mlib_s16 *) mlib_filters_u8_bc;
  }
  else {
    mlib_filters_table = (mlib_s16 *) mlib_filters_u8_bc2;
  }

  for (j = yStart; j <= yFinish; j++) {
    mlib_s32 xf0, xf1, xf2, xf3;
    mlib_s32 yf0, yf1, yf2, yf3;
    mlib_s32 c0, c1, c2, c3, val0;
    mlib_s32 filterpos, k;
    mlib_s16 *fptr;
    mlib_u8 s0, s1, s2, s3;

    CLIP(3);
    dstLineEnd = (DTYPE *) dstData + 3 * xRight;

    for (k = 0; k < 3; k++) {
      mlib_s32 X1 = X;
      mlib_s32 Y1 = Y;
      DTYPE *dPtr = dstPixelPtr + k;

      filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      xf0 = fptr[0];
      xf1 = fptr[1];
      xf2 = fptr[2];
      xf3 = fptr[3];

      filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      yf0 = fptr[0];
      yf1 = fptr[1];
      yf2 = fptr[2];
      yf3 = fptr[3];

      xSrc = (X1 >> MLIB_SHIFT) - 1;
      ySrc = (Y1 >> MLIB_SHIFT) - 1;

      srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 3 * xSrc + k;
      s0 = srcPixelPtr[0];
      s1 = srcPixelPtr[3];
      s2 = srcPixelPtr[6];
      s3 = srcPixelPtr[9];

#ifdef __SUNPRO_C
#pragma pipeloop(0)
#endif /* __SUNPRO_C */
      for (; dPtr <= (dstLineEnd - 1); dPtr += 3) {
        X1 += dX;
        Y1 += dY;

        c0 = (s0 * xf0 + s1 * xf1 + s2 * xf2 + s3 * xf3 + ROUND_X) >> SHIFT_X;
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c1 = (srcPixelPtr[0] * xf0 + srcPixelPtr[3] * xf1 +
              srcPixelPtr[6] * xf2 + srcPixelPtr[9] * xf3 + ROUND_X) >> SHIFT_X;
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c2 = (srcPixelPtr[0] * xf0 + srcPixelPtr[3] * xf1 +
              srcPixelPtr[6] * xf2 + srcPixelPtr[9] * xf3 + ROUND_X) >> SHIFT_X;
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c3 = (srcPixelPtr[0] * xf0 + srcPixelPtr[3] * xf1 +
              srcPixelPtr[6] * xf2 + srcPixelPtr[9] * xf3 + ROUND_X) >> SHIFT_X;

        filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        xf0 = fptr[0];
        xf1 = fptr[1];
        xf2 = fptr[2];
        xf3 = fptr[3];

        val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3 + ROUND_Y) >> SHIFT_Y;

        filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        yf0 = fptr[0];
        yf1 = fptr[1];
        yf2 = fptr[2];
        yf3 = fptr[3];

        S32_TO_U8_SAT(dPtr[0]);

        xSrc = (X1 >> MLIB_SHIFT) - 1;
        ySrc = (Y1 >> MLIB_SHIFT) - 1;

        srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 3 * xSrc + k;
        s0 = srcPixelPtr[0];
        s1 = srcPixelPtr[3];
        s2 = srcPixelPtr[6];
        s3 = srcPixelPtr[9];
      }

      c0 = (s0 * xf0 + s1 * xf1 + s2 * xf2 + s3 * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c1 = (srcPixelPtr[0] * xf0 + srcPixelPtr[3] * xf1 +
            srcPixelPtr[6] * xf2 + srcPixelPtr[9] * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c2 = (srcPixelPtr[0] * xf0 + srcPixelPtr[3] * xf1 +
            srcPixelPtr[6] * xf2 + srcPixelPtr[9] * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c3 = (srcPixelPtr[0] * xf0 + srcPixelPtr[3] * xf1 +
            srcPixelPtr[6] * xf2 + srcPixelPtr[9] * xf3 + ROUND_X) >> SHIFT_X;

      val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3 + ROUND_Y) >> SHIFT_Y;

      S32_TO_U8_SAT(dPtr[0]);
    }
  }

  return MLIB_SUCCESS;
}

/***************************************************************/
mlib_status FUN_NAME(4ch)(mlib_affine_param *param)
{
  DECLAREVAR_BC();
  DTYPE *dstLineEnd;
  const mlib_s16 *mlib_filters_table;

  if (filter == MLIB_BICUBIC) {
    mlib_filters_table = (mlib_s16 *) mlib_filters_u8_bc;
  }
  else {
    mlib_filters_table = (mlib_s16 *) mlib_filters_u8_bc2;
  }

  for (j = yStart; j <= yFinish; j++) {
    mlib_s32 xf0, xf1, xf2, xf3;
    mlib_s32 yf0, yf1, yf2, yf3;
    mlib_s32 c0, c1, c2, c3, val0;
    mlib_s32 filterpos, k;
    mlib_s16 *fptr;
    mlib_u8 s0, s1, s2, s3;

    CLIP(4);
    dstLineEnd = (DTYPE *) dstData + 4 * xRight;

    for (k = 0; k < 4; k++) {
      mlib_s32 X1 = X;
      mlib_s32 Y1 = Y;
      DTYPE *dPtr = dstPixelPtr + k;

      filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      xf0 = fptr[0];
      xf1 = fptr[1];
      xf2 = fptr[2];
      xf3 = fptr[3];

      filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
      fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

      yf0 = fptr[0];
      yf1 = fptr[1];
      yf2 = fptr[2];
      yf3 = fptr[3];

      xSrc = (X1 >> MLIB_SHIFT) - 1;
      ySrc = (Y1 >> MLIB_SHIFT) - 1;

      srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 4 * xSrc + k;
      s0 = srcPixelPtr[0];
      s1 = srcPixelPtr[4];
      s2 = srcPixelPtr[8];
      s3 = srcPixelPtr[12];

#ifdef __SUNPRO_C
#pragma pipeloop(0)
#endif /* __SUNPRO_C */
      for (; dPtr <= (dstLineEnd - 1); dPtr += 4) {
        X1 += dX;
        Y1 += dY;

        c0 = (s0 * xf0 + s1 * xf1 + s2 * xf2 + s3 * xf3 + ROUND_X) >> SHIFT_X;
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c1 = (srcPixelPtr[0] * xf0 + srcPixelPtr[4] * xf1 +
              srcPixelPtr[8] * xf2 + srcPixelPtr[12] * xf3 + ROUND_X) >> SHIFT_X;
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c2 = (srcPixelPtr[0] * xf0 + srcPixelPtr[4] * xf1 +
              srcPixelPtr[8] * xf2 + srcPixelPtr[12] * xf3 + ROUND_X) >> SHIFT_X;
        srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
        c3 = (srcPixelPtr[0] * xf0 + srcPixelPtr[4] * xf1 +
              srcPixelPtr[8] * xf2 + srcPixelPtr[12] * xf3 + ROUND_X) >> SHIFT_X;

        filterpos = (X1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        xf0 = fptr[0];
        xf1 = fptr[1];
        xf2 = fptr[2];
        xf3 = fptr[3];

        val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3 + ROUND_Y) >> SHIFT_Y;

        filterpos = (Y1 >> FILTER_SHIFT) & FILTER_MASK;
        fptr = (mlib_s16 *) ((mlib_u8 *) mlib_filters_table + filterpos);

        yf0 = fptr[0];
        yf1 = fptr[1];
        yf2 = fptr[2];
        yf3 = fptr[3];

        S32_TO_U8_SAT(dPtr[0]);

        xSrc = (X1 >> MLIB_SHIFT) - 1;
        ySrc = (Y1 >> MLIB_SHIFT) - 1;

        srcPixelPtr = ((DTYPE **) lineAddr)[ySrc] + 4 * xSrc + k;
        s0 = srcPixelPtr[0];
        s1 = srcPixelPtr[4];
        s2 = srcPixelPtr[8];
        s3 = srcPixelPtr[12];
      }

      c0 = (s0 * xf0 + s1 * xf1 + s2 * xf2 + s3 * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c1 = (srcPixelPtr[0] * xf0 + srcPixelPtr[4] * xf1 +
            srcPixelPtr[8] * xf2 + srcPixelPtr[12] * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c2 = (srcPixelPtr[0] * xf0 + srcPixelPtr[4] * xf1 +
            srcPixelPtr[8] * xf2 + srcPixelPtr[12] * xf3 + ROUND_X) >> SHIFT_X;
      srcPixelPtr = (DTYPE *) ((mlib_addr) srcPixelPtr + srcYStride);
      c3 = (srcPixelPtr[0] * xf0 + srcPixelPtr[4] * xf1 +
            srcPixelPtr[8] * xf2 + srcPixelPtr[12] * xf3 + ROUND_X) >> SHIFT_X;

      val0 = (c0 * yf0 + c1 * yf1 + c2 * yf2 + c3 * yf3 + ROUND_Y) >> SHIFT_Y;

      S32_TO_U8_SAT(dPtr[0]);
    }
  }

  return MLIB_SUCCESS;
}

#endif /* __sparc ( for SPARC, using floating-point multiplies is faster ) */

/***************************************************************/
