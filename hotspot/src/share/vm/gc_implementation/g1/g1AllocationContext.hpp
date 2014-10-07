/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_GC_IMPLEMENTATION_G1_G1ALLOCATIONCONTEXT_HPP
#define SHARE_VM_GC_IMPLEMENTATION_G1_G1ALLOCATIONCONTEXT_HPP

#include "memory/allocation.hpp"

typedef unsigned char AllocationContext_t;

class AllocationContext : AllStatic {
public:
  // Currently used context
  static AllocationContext_t current() {
    return 0;
  }
  // System wide default context
  static AllocationContext_t system() {
    return 0;
  }
};

class AllocationContextStats: public StackObj {
public:
  inline void clear() { }
  inline void update(bool full_gc) { }
  inline void update_at_remark() { }
  inline bool available() { return false; }
};

#endif // SHARE_VM_GC_IMPLEMENTATION_G1_G1ALLOCATIONCONTEXT_HPP
