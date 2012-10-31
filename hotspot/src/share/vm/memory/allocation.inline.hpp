/*
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_MEMORY_ALLOCATION_INLINE_HPP
#define SHARE_VM_MEMORY_ALLOCATION_INLINE_HPP

#include "runtime/os.hpp"

// Explicit C-heap memory management

void trace_heap_malloc(size_t size, const char* name, void *p);
void trace_heap_free(void *p);

#ifndef PRODUCT
// Increments unsigned long value for statistics (not atomic on MP).
inline void inc_stat_counter(volatile julong* dest, julong add_value) {
#if defined(SPARC) || defined(X86)
  // Sparc and X86 have atomic jlong (8 bytes) instructions
  julong value = Atomic::load((volatile jlong*)dest);
  value += add_value;
  Atomic::store((jlong)value, (volatile jlong*)dest);
#else
  // possible word-tearing during load/store
  *dest += add_value;
#endif
}
#endif

// allocate using malloc; will fail if no memory available
inline char* AllocateHeap(size_t size, MEMFLAGS flags, address pc = 0,
    AllocFailType alloc_failmode = AllocFailStrategy::EXIT_OOM) {
  if (pc == 0) {
    pc = CURRENT_PC;
  }
  char* p = (char*) os::malloc(size, flags, pc);
  #ifdef ASSERT
  if (PrintMallocFree) trace_heap_malloc(size, "AllocateHeap", p);
  #endif
  if (p == NULL && alloc_failmode == AllocFailStrategy::EXIT_OOM) vm_exit_out_of_memory(size, "AllocateHeap");
  return p;
}

inline char* ReallocateHeap(char *old, size_t size, MEMFLAGS flags,
    AllocFailType alloc_failmode = AllocFailStrategy::EXIT_OOM) {
  char* p = (char*) os::realloc(old, size, flags, CURRENT_PC);
  #ifdef ASSERT
  if (PrintMallocFree) trace_heap_malloc(size, "ReallocateHeap", p);
  #endif
  if (p == NULL && alloc_failmode == AllocFailStrategy::EXIT_OOM) vm_exit_out_of_memory(size, "ReallocateHeap");
  return p;
}

inline void FreeHeap(void* p, MEMFLAGS memflags = mtInternal) {
  #ifdef ASSERT
  if (PrintMallocFree) trace_heap_free(p);
  #endif
  os::free(p, memflags);
}


template <MEMFLAGS F> void* CHeapObj<F>::operator new(size_t size,
      address caller_pc){
#ifdef ASSERT
    void* p = (void*)AllocateHeap(size, F, (caller_pc != 0 ? caller_pc : CALLER_PC));
    if (PrintMallocFree) trace_heap_malloc(size, "CHeapObj-new", p);
    return p;
#else
    return (void *) AllocateHeap(size, F, (caller_pc != 0 ? caller_pc : CALLER_PC));
#endif
  }

template <MEMFLAGS F> void* CHeapObj<F>::operator new (size_t size,
  const std::nothrow_t&  nothrow_constant, address caller_pc) {
#ifdef ASSERT
  void* p = (void*)AllocateHeap(size, F, (caller_pc != 0 ? caller_pc : CALLER_PC),
      AllocFailStrategy::RETURN_NULL);
    if (PrintMallocFree) trace_heap_malloc(size, "CHeapObj-new", p);
    return p;
#else
  return (void *) AllocateHeap(size, F, (caller_pc != 0 ? caller_pc : CALLER_PC),
      AllocFailStrategy::RETURN_NULL);
#endif
}

template <MEMFLAGS F> void CHeapObj<F>::operator delete(void* p){
   FreeHeap(p, F);
}


#endif // SHARE_VM_MEMORY_ALLOCATION_INLINE_HPP
