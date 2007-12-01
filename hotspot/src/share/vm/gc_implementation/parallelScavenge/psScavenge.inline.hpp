/*
 * Copyright 2002-2006 Sun Microsystems, Inc.  All Rights Reserved.
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


inline void PSScavenge::save_to_space_top_before_gc() {
  ParallelScavengeHeap* heap = (ParallelScavengeHeap*)Universe::heap();
  _to_space_top_before_gc = heap->young_gen()->to_space()->top();
}

inline bool PSScavenge::should_scavenge(oop p) {
  return p == NULL ? false : PSScavenge::is_obj_in_young((HeapWord*) p);
}

inline bool PSScavenge::should_scavenge(oop p, MutableSpace* to_space) {
  if (should_scavenge(p)) {
    // Skip objects copied to to_space since the scavenge started.
    HeapWord* const addr = (HeapWord*) p;
    return addr < to_space_top_before_gc() || addr >= to_space->end();
  }
  return false;
}

inline bool PSScavenge::should_scavenge(oop p, bool check_to_space) {
  if (check_to_space) {
    ParallelScavengeHeap* heap = (ParallelScavengeHeap*) Universe::heap();
    return should_scavenge(p, heap->young_gen()->to_space());
  }
  return should_scavenge(p);
}

// Attempt to "claim" oop at p via CAS, push the new obj if successful
// This version tests the oop* to make sure it is within the heap before
// attempting marking.
inline void PSScavenge::copy_and_push_safe_barrier(PSPromotionManager* pm,
                                                   oop*                p) {
  assert(should_scavenge(*p, true), "revisiting object?");

  oop o = *p;
  if (o->is_forwarded()) {
    *p = o->forwardee();
  } else {
    *p = pm->copy_to_survivor_space(o, pm->depth_first());
  }

  // We cannot mark without test, as some code passes us pointers
  // that are outside the heap.
  if ((!PSScavenge::is_obj_in_young((HeapWord*) p)) &&
      Universe::heap()->is_in_reserved(p)) {
    o = *p;
    if (PSScavenge::is_obj_in_young((HeapWord*) o)) {
      card_table()->inline_write_ref_field_gc(p, o);
    }
  }
}
