/*
 * Copyright (c) 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

#define VM_STRUCTS_CMS(nonstatic_field, \
                   static_field) \
  nonstatic_field(CompactibleFreeListSpace,    _collector,                                    CMSCollector*)                         \
  nonstatic_field(CompactibleFreeListSpace,    _bt,                                           BlockOffsetArrayNonContigSpace)        \
                                                                                                                                     \
  nonstatic_field(CMSPermGen,                  _gen,                                          ConcurrentMarkSweepGeneration*)        \
  nonstatic_field(CMSBitMap,                   _bmStartWord,                                  HeapWord*)                             \
  nonstatic_field(CMSBitMap,                   _bmWordSize,                                   size_t)                                \
  nonstatic_field(CMSBitMap,                   _shifter,                                      const int)                            \
  nonstatic_field(CMSBitMap,                      _bm,                                           BitMap)                            \
  nonstatic_field(CMSBitMap,                   _virtual_space,                                VirtualSpace)                         \
  nonstatic_field(CMSCollector,                _markBitMap,                                   CMSBitMap)                             \
  nonstatic_field(ConcurrentMarkSweepGeneration, _cmsSpace,                                   CompactibleFreeListSpace*)             \
     static_field(ConcurrentMarkSweepThread,   _collector,                                    CMSCollector*)                         \
  nonstatic_field(FreeChunk,                   _next,                                         FreeChunk*)                            \
  nonstatic_field(FreeChunk,                   _prev,                                         FreeChunk*)                            \
  nonstatic_field(FreeChunk,                   _size,                                         size_t)

#define VM_TYPES_CMS(declare_type,                                        \
                     declare_toplevel_type)                               \
                                                                          \
           declare_type(ConcurrentMarkSweepGeneration,CardGeneration)     \
           declare_type(CompactibleFreeListSpace,     CompactibleSpace)   \
           declare_type(CMSPermGenGen,                ConcurrentMarkSweepGeneration) \
           declare_type(CMSPermGen,                   PermGen)            \
           declare_type(ConcurrentMarkSweepThread,    NamedThread)        \
           declare_type(SurrogateLockerThread, JavaThread)                \
  declare_toplevel_type(CMSCollector)                                     \
  declare_toplevel_type(CMSBitMap)                                        \
  declare_toplevel_type(FreeChunk)                                        \
  declare_toplevel_type(ConcurrentMarkSweepThread*)                       \
  declare_toplevel_type(ConcurrentMarkSweepGeneration*)                   \
  declare_toplevel_type(SurrogateLockerThread*)                           \
  declare_toplevel_type(CompactibleFreeListSpace*)                        \
  declare_toplevel_type(CMSCollector*)                                    \
  declare_toplevel_type(FreeChunk*)

#define VM_INT_CONSTANTS_CMS(declare_constant)                            \
  declare_constant(Generation::ConcurrentMarkSweep)                       \
  declare_constant(PermGen::ConcurrentMarkSweep)
