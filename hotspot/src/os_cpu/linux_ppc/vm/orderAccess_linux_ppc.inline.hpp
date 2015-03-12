/*
 * Copyright (c) 1997, 2015, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2012, 2014, SAP AG. All rights reserved.
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
 *
 */

#ifndef OS_CPU_LINUX_PPC_VM_ORDERACCESS_LINUX_PPC_INLINE_HPP
#define OS_CPU_LINUX_PPC_VM_ORDERACCESS_LINUX_PPC_INLINE_HPP

#include "runtime/orderAccess.hpp"

#ifndef PPC64
#error "OrderAccess currently only implemented for PPC64"
#endif

// Compiler version last used for testing: gcc 4.1.2
// Please update this information when this file changes

// Implementation of class OrderAccess.

//
// Machine barrier instructions:
//
// - sync            Two-way memory barrier, aka fence.
// - lwsync          orders  Store|Store,
//                            Load|Store,
//                            Load|Load,
//                   but not Store|Load
// - eieio           orders  Store|Store
// - isync           Invalidates speculatively executed instructions,
//                   but isync may complete before storage accesses
//                   associated with instructions preceding isync have
//                   been performed.
//
// Semantic barrier instructions:
// (as defined in orderAccess.hpp)
//
// - release         orders Store|Store,       (maps to lwsync)
//                           Load|Store
// - acquire         orders  Load|Store,       (maps to lwsync)
//                           Load|Load
// - fence           orders Store|Store,       (maps to sync)
//                           Load|Store,
//                           Load|Load,
//                          Store|Load
//

#define inlasm_sync()     __asm__ __volatile__ ("sync"   : : : "memory");
#define inlasm_lwsync()   __asm__ __volatile__ ("lwsync" : : : "memory");
#define inlasm_eieio()    __asm__ __volatile__ ("eieio"  : : : "memory");
#define inlasm_isync()    __asm__ __volatile__ ("isync"  : : : "memory");
// Use twi-isync for load_acquire (faster than lwsync).
#define inlasm_acquire_reg(X) __asm__ __volatile__ ("twi 0,%0,0\n isync\n" : : "r" (X) : "memory");

inline void   OrderAccess::loadload()   { inlasm_lwsync(); }
inline void   OrderAccess::storestore() { inlasm_lwsync(); }
inline void   OrderAccess::loadstore()  { inlasm_lwsync(); }
inline void   OrderAccess::storeload()  { inlasm_sync();   }

inline void   OrderAccess::acquire()    { inlasm_lwsync(); }
inline void   OrderAccess::release()    { inlasm_lwsync(); }
inline void   OrderAccess::fence()      { inlasm_sync();   }

template<> inline jbyte  OrderAccess::specialized_load_acquire<jbyte> (volatile jbyte*  p) { register jbyte t = load(p);  inlasm_acquire_reg(t); return t; }
template<> inline jshort OrderAccess::specialized_load_acquire<jshort>(volatile jshort* p) { register jshort t = load(p); inlasm_acquire_reg(t); return t; }
template<> inline jint   OrderAccess::specialized_load_acquire<jint>  (volatile jint*   p) { register jint t = load(p);   inlasm_acquire_reg(t); return t; }
template<> inline jlong  OrderAccess::specialized_load_acquire<jlong> (volatile jlong*  p) { register jlong t = load(p);  inlasm_acquire_reg(t); return t; }

#undef inlasm_sync
#undef inlasm_lwsync
#undef inlasm_eieio
#undef inlasm_isync
#undef inlasm_acquire_reg

#define VM_HAS_GENERALIZED_ORDER_ACCESS 1

#endif // OS_CPU_LINUX_PPC_VM_ORDERACCESS_LINUX_PPC_INLINE_HPP
