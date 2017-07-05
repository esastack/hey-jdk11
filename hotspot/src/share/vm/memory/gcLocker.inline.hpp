/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_VM_MEMORY_GCLOCKER_INLINE_HPP
#define SHARE_VM_MEMORY_GCLOCKER_INLINE_HPP

#include "memory/gcLocker.hpp"

inline bool GC_locker::is_active() {
  assert(_needs_gc || SafepointSynchronize::is_at_safepoint(), "only read at safepoint");
  verify_critical_count();
  return _lock_count > 0 || _jni_lock_count > 0;
}

inline void GC_locker::lock() {
  // cast away volatile
  Atomic::inc(&_lock_count);
  CHECK_UNHANDLED_OOPS_ONLY(
    if (CheckUnhandledOops) { Thread::current()->_gc_locked_out_count++; })
  assert(Universe::heap() == NULL ||
         !Universe::heap()->is_gc_active(), "locking failed");
}

inline void GC_locker::unlock() {
  // cast away volatile
  Atomic::dec(&_lock_count);
  CHECK_UNHANDLED_OOPS_ONLY(
    if (CheckUnhandledOops) { Thread::current()->_gc_locked_out_count--; })
}

inline void GC_locker::lock_critical(JavaThread* thread) {
  if (!thread->in_critical()) {
    if (needs_gc()) {
      // jni_lock call calls enter_critical under the lock so that the
      // global lock count and per thread count are in agreement.
      jni_lock(thread);
      return;
    }
    increment_debug_jni_lock_count();
  }
  thread->enter_critical();
}

inline void GC_locker::unlock_critical(JavaThread* thread) {
  if (thread->in_last_critical()) {
    if (needs_gc()) {
      // jni_unlock call calls exit_critical under the lock so that
      // the global lock count and per thread count are in agreement.
      jni_unlock(thread);
      return;
    }
    decrement_debug_jni_lock_count();
  }
  thread->exit_critical();
}

#endif // SHARE_VM_MEMORY_GCLOCKER_INLINE_HPP
