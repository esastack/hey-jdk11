/*
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
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

#include "precompiled.hpp"
#include "code/codeBlob.hpp"
#include "code/codeCache.hpp"
#include "code/compiledIC.hpp"
#include "code/dependencies.hpp"
#include "code/icBuffer.hpp"
#include "code/nmethod.hpp"
#include "code/pcDesc.hpp"
#include "compiler/compileBroker.hpp"
#include "gc_implementation/shared/markSweep.hpp"
#include "memory/allocation.inline.hpp"
#include "memory/gcLocker.hpp"
#include "memory/iterator.hpp"
#include "memory/resourceArea.hpp"
#include "oops/method.hpp"
#include "oops/objArrayOop.hpp"
#include "oops/oop.inline.hpp"
#include "runtime/handles.inline.hpp"
#include "runtime/arguments.hpp"
#include "runtime/icache.hpp"
#include "runtime/java.hpp"
#include "runtime/mutexLocker.hpp"
#include "services/memoryService.hpp"
#include "trace/tracing.hpp"
#include "utilities/xmlstream.hpp"

// Helper class for printing in CodeCache

class CodeBlob_sizes {
 private:
  int count;
  int total_size;
  int header_size;
  int code_size;
  int stub_size;
  int relocation_size;
  int scopes_oop_size;
  int scopes_metadata_size;
  int scopes_data_size;
  int scopes_pcs_size;

 public:
  CodeBlob_sizes() {
    count            = 0;
    total_size       = 0;
    header_size      = 0;
    code_size        = 0;
    stub_size        = 0;
    relocation_size  = 0;
    scopes_oop_size  = 0;
    scopes_metadata_size  = 0;
    scopes_data_size = 0;
    scopes_pcs_size  = 0;
  }

  int total()                                    { return total_size; }
  bool is_empty()                                { return count == 0; }

  void print(const char* title) {
    tty->print_cr(" #%d %s = %dK (hdr %d%%,  loc %d%%, code %d%%, stub %d%%, [oops %d%%, metadata %d%%, data %d%%, pcs %d%%])",
                  count,
                  title,
                  (int)(total() / K),
                  header_size             * 100 / total_size,
                  relocation_size         * 100 / total_size,
                  code_size               * 100 / total_size,
                  stub_size               * 100 / total_size,
                  scopes_oop_size         * 100 / total_size,
                  scopes_metadata_size    * 100 / total_size,
                  scopes_data_size        * 100 / total_size,
                  scopes_pcs_size         * 100 / total_size);
  }

  void add(CodeBlob* cb) {
    count++;
    total_size       += cb->size();
    header_size      += cb->header_size();
    relocation_size  += cb->relocation_size();
    if (cb->is_nmethod()) {
      nmethod* nm = cb->as_nmethod_or_null();
      code_size        += nm->insts_size();
      stub_size        += nm->stub_size();

      scopes_oop_size  += nm->oops_size();
      scopes_metadata_size  += nm->metadata_size();
      scopes_data_size += nm->scopes_data_size();
      scopes_pcs_size  += nm->scopes_pcs_size();
    } else {
      code_size        += cb->code_size();
    }
  }
};

// CodeCache implementation

CodeHeap * CodeCache::_heap = new CodeHeap();
int CodeCache::_number_of_blobs = 0;
int CodeCache::_number_of_adapters = 0;
int CodeCache::_number_of_nmethods = 0;
int CodeCache::_number_of_nmethods_with_dependencies = 0;
bool CodeCache::_needs_cache_clean = false;
nmethod* CodeCache::_scavenge_root_nmethods = NULL;

int CodeCache::_codemem_full_count = 0;

CodeBlob* CodeCache::first() {
  assert_locked_or_safepoint(CodeCache_lock);
  return (CodeBlob*)_heap->first();
}


CodeBlob* CodeCache::next(CodeBlob* cb) {
  assert_locked_or_safepoint(CodeCache_lock);
  return (CodeBlob*)_heap->next(cb);
}


CodeBlob* CodeCache::alive(CodeBlob *cb) {
  assert_locked_or_safepoint(CodeCache_lock);
  while (cb != NULL && !cb->is_alive()) cb = next(cb);
  return cb;
}


nmethod* CodeCache::alive_nmethod(CodeBlob* cb) {
  assert_locked_or_safepoint(CodeCache_lock);
  while (cb != NULL && (!cb->is_alive() || !cb->is_nmethod())) cb = next(cb);
  return (nmethod*)cb;
}

nmethod* CodeCache::first_nmethod() {
  assert_locked_or_safepoint(CodeCache_lock);
  CodeBlob* cb = first();
  while (cb != NULL && !cb->is_nmethod()) {
    cb = next(cb);
  }
  return (nmethod*)cb;
}

nmethod* CodeCache::next_nmethod (CodeBlob* cb) {
  assert_locked_or_safepoint(CodeCache_lock);
  cb = next(cb);
  while (cb != NULL && !cb->is_nmethod()) {
    cb = next(cb);
  }
  return (nmethod*)cb;
}

static size_t maxCodeCacheUsed = 0;

CodeBlob* CodeCache::allocate(int size, bool is_critical) {
  // Do not seize the CodeCache lock here--if the caller has not
  // already done so, we are going to lose bigtime, since the code
  // cache will contain a garbage CodeBlob until the caller can
  // run the constructor for the CodeBlob subclass he is busy
  // instantiating.
  assert_locked_or_safepoint(CodeCache_lock);
  assert(size > 0, "allocation request must be reasonable");
  if (size <= 0) {
    return NULL;
  }
  CodeBlob* cb = NULL;
  while (true) {
    cb = (CodeBlob*)_heap->allocate(size, is_critical);
    if (cb != NULL) break;
    if (!_heap->expand_by(CodeCacheExpansionSize)) {
      // Expansion failed
      return NULL;
    }
    if (PrintCodeCacheExtension) {
      ResourceMark rm;
      tty->print_cr("code cache extended to [" INTPTR_FORMAT ", " INTPTR_FORMAT "] (" SSIZE_FORMAT " bytes)",
                    (intptr_t)_heap->low_boundary(), (intptr_t)_heap->high(),
                    (address)_heap->high() - (address)_heap->low_boundary());
    }
  }
  maxCodeCacheUsed = MAX2(maxCodeCacheUsed, ((address)_heap->high_boundary() -
                          (address)_heap->low_boundary()) - unallocated_capacity());
  print_trace("allocation", cb, size);
  _number_of_blobs++;
  return cb;
}

void CodeCache::free(CodeBlob* cb) {
  assert_locked_or_safepoint(CodeCache_lock);

  print_trace("free", cb);
  if (cb->is_nmethod()) {
    _number_of_nmethods--;
    if (((nmethod *)cb)->has_dependencies()) {
      _number_of_nmethods_with_dependencies--;
    }
  }
  if (cb->is_adapter_blob()) {
    _number_of_adapters--;
  }
  _number_of_blobs--;

  _heap->deallocate(cb);

  assert(_number_of_blobs >= 0, "sanity check");
}


void CodeCache::commit(CodeBlob* cb) {
  // this is called by nmethod::nmethod, which must already own CodeCache_lock
  assert_locked_or_safepoint(CodeCache_lock);
  if (cb->is_nmethod()) {
    _number_of_nmethods++;
    if (((nmethod *)cb)->has_dependencies()) {
      _number_of_nmethods_with_dependencies++;
    }
  }
  if (cb->is_adapter_blob()) {
    _number_of_adapters++;
  }

  // flush the hardware I-cache
  ICache::invalidate_range(cb->content_begin(), cb->content_size());
}


// Iteration over CodeBlobs

#define FOR_ALL_BLOBS(var)       for (CodeBlob *var =       first() ; var != NULL; var =       next(var) )
#define FOR_ALL_ALIVE_BLOBS(var) for (CodeBlob *var = alive(first()); var != NULL; var = alive(next(var)))
#define FOR_ALL_ALIVE_NMETHODS(var) for (nmethod *var = alive_nmethod(first()); var != NULL; var = alive_nmethod(next(var)))


bool CodeCache::contains(void *p) {
  // It should be ok to call contains without holding a lock
  return _heap->contains(p);
}


// This method is safe to call without holding the CodeCache_lock, as long as a dead codeblob is not
// looked up (i.e., one that has been marked for deletion). It only dependes on the _segmap to contain
// valid indices, which it will always do, as long as the CodeBlob is not in the process of being recycled.
CodeBlob* CodeCache::find_blob(void* start) {
  CodeBlob* result = find_blob_unsafe(start);
  if (result == NULL) return NULL;
  // We could potentially look up non_entrant methods
  guarantee(!result->is_zombie() || result->is_locked_by_vm() || is_error_reported(), "unsafe access to zombie method");
  return result;
}

nmethod* CodeCache::find_nmethod(void* start) {
  CodeBlob *cb = find_blob(start);
  assert(cb == NULL || cb->is_nmethod(), "did not find an nmethod");
  return (nmethod*)cb;
}


void CodeCache::blobs_do(void f(CodeBlob* nm)) {
  assert_locked_or_safepoint(CodeCache_lock);
  FOR_ALL_BLOBS(p) {
    f(p);
  }
}


void CodeCache::nmethods_do(void f(nmethod* nm)) {
  assert_locked_or_safepoint(CodeCache_lock);
  FOR_ALL_BLOBS(nm) {
    if (nm->is_nmethod()) f((nmethod*)nm);
  }
}

void CodeCache::alive_nmethods_do(void f(nmethod* nm)) {
  assert_locked_or_safepoint(CodeCache_lock);
  FOR_ALL_ALIVE_NMETHODS(nm) {
    f(nm);
  }
}

int CodeCache::alignment_unit() {
  return (int)_heap->alignment_unit();
}


int CodeCache::alignment_offset() {
  return (int)_heap->alignment_offset();
}


// Mark nmethods for unloading if they contain otherwise unreachable
// oops.
void CodeCache::do_unloading(BoolObjectClosure* is_alive, bool unloading_occurred) {
  assert_locked_or_safepoint(CodeCache_lock);
  FOR_ALL_ALIVE_NMETHODS(nm) {
    nm->do_unloading(is_alive, unloading_occurred);
  }
}

void CodeCache::blobs_do(CodeBlobClosure* f) {
  assert_locked_or_safepoint(CodeCache_lock);
  FOR_ALL_ALIVE_BLOBS(cb) {
    f->do_code_blob(cb);

#ifdef ASSERT
    if (cb->is_nmethod())
      ((nmethod*)cb)->verify_scavenge_root_oops();
#endif //ASSERT
  }
}

// Walk the list of methods which might contain non-perm oops.
void CodeCache::scavenge_root_nmethods_do(CodeBlobClosure* f) {
  assert_locked_or_safepoint(CodeCache_lock);
  debug_only(mark_scavenge_root_nmethods());

  for (nmethod* cur = scavenge_root_nmethods(); cur != NULL; cur = cur->scavenge_root_link()) {
    debug_only(cur->clear_scavenge_root_marked());
    assert(cur->scavenge_root_not_marked(), "");
    assert(cur->on_scavenge_root_list(), "else shouldn't be on this list");

    bool is_live = (!cur->is_zombie() && !cur->is_unloaded());
#ifndef PRODUCT
    if (TraceScavenge) {
      cur->print_on(tty, is_live ? "scavenge root" : "dead scavenge root"); tty->cr();
    }
#endif //PRODUCT
    if (is_live) {
      // Perform cur->oops_do(f), maybe just once per nmethod.
      f->do_code_blob(cur);
    }
  }

  // Check for stray marks.
  debug_only(verify_perm_nmethods(NULL));
}

void CodeCache::add_scavenge_root_nmethod(nmethod* nm) {
  assert_locked_or_safepoint(CodeCache_lock);
  nm->set_on_scavenge_root_list();
  nm->set_scavenge_root_link(_scavenge_root_nmethods);
  set_scavenge_root_nmethods(nm);
  print_trace("add_scavenge_root", nm);
}

void CodeCache::drop_scavenge_root_nmethod(nmethod* nm) {
  assert_locked_or_safepoint(CodeCache_lock);
  print_trace("drop_scavenge_root", nm);
  nmethod* last = NULL;
  nmethod* cur = scavenge_root_nmethods();
  while (cur != NULL) {
    nmethod* next = cur->scavenge_root_link();
    if (cur == nm) {
      if (last != NULL)
            last->set_scavenge_root_link(next);
      else  set_scavenge_root_nmethods(next);
      nm->set_scavenge_root_link(NULL);
      nm->clear_on_scavenge_root_list();
      return;
    }
    last = cur;
    cur = next;
  }
  assert(false, "should have been on list");
}

void CodeCache::prune_scavenge_root_nmethods() {
  assert_locked_or_safepoint(CodeCache_lock);
  debug_only(mark_scavenge_root_nmethods());

  nmethod* last = NULL;
  nmethod* cur = scavenge_root_nmethods();
  while (cur != NULL) {
    nmethod* next = cur->scavenge_root_link();
    debug_only(cur->clear_scavenge_root_marked());
    assert(cur->scavenge_root_not_marked(), "");
    assert(cur->on_scavenge_root_list(), "else shouldn't be on this list");

    if (!cur->is_zombie() && !cur->is_unloaded()
        && cur->detect_scavenge_root_oops()) {
      // Keep it.  Advance 'last' to prevent deletion.
      last = cur;
    } else {
      // Prune it from the list, so we don't have to look at it any more.
      print_trace("prune_scavenge_root", cur);
      cur->set_scavenge_root_link(NULL);
      cur->clear_on_scavenge_root_list();
      if (last != NULL)
            last->set_scavenge_root_link(next);
      else  set_scavenge_root_nmethods(next);
    }
    cur = next;
  }

  // Check for stray marks.
  debug_only(verify_perm_nmethods(NULL));
}

#ifndef PRODUCT
void CodeCache::asserted_non_scavengable_nmethods_do(CodeBlobClosure* f) {
  // While we are here, verify the integrity of the list.
  mark_scavenge_root_nmethods();
  for (nmethod* cur = scavenge_root_nmethods(); cur != NULL; cur = cur->scavenge_root_link()) {
    assert(cur->on_scavenge_root_list(), "else shouldn't be on this list");
    cur->clear_scavenge_root_marked();
  }
  verify_perm_nmethods(f);
}

// Temporarily mark nmethods that are claimed to be on the non-perm list.
void CodeCache::mark_scavenge_root_nmethods() {
  FOR_ALL_ALIVE_BLOBS(cb) {
    if (cb->is_nmethod()) {
      nmethod *nm = (nmethod*)cb;
      assert(nm->scavenge_root_not_marked(), "clean state");
      if (nm->on_scavenge_root_list())
        nm->set_scavenge_root_marked();
    }
  }
}

// If the closure is given, run it on the unlisted nmethods.
// Also make sure that the effects of mark_scavenge_root_nmethods is gone.
void CodeCache::verify_perm_nmethods(CodeBlobClosure* f_or_null) {
  FOR_ALL_ALIVE_BLOBS(cb) {
    bool call_f = (f_or_null != NULL);
    if (cb->is_nmethod()) {
      nmethod *nm = (nmethod*)cb;
      assert(nm->scavenge_root_not_marked(), "must be already processed");
      if (nm->on_scavenge_root_list())
        call_f = false;  // don't show this one to the client
      nm->verify_scavenge_root_oops();
    } else {
      call_f = false;   // not an nmethod
    }
    if (call_f)  f_or_null->do_code_blob(cb);
  }
}
#endif //PRODUCT


void CodeCache::gc_prologue() {
  assert(!nmethod::oops_do_marking_is_active(), "oops_do_marking_epilogue must be called");
}

void CodeCache::gc_epilogue() {
  assert_locked_or_safepoint(CodeCache_lock);
  FOR_ALL_ALIVE_BLOBS(cb) {
    if (cb->is_nmethod()) {
      nmethod *nm = (nmethod*)cb;
      assert(!nm->is_unloaded(), "Tautology");
      if (needs_cache_clean()) {
        nm->cleanup_inline_caches();
      }
      DEBUG_ONLY(nm->verify());
      nm->fix_oop_relocations();
    }
  }
  set_needs_cache_clean(false);
  prune_scavenge_root_nmethods();
  assert(!nmethod::oops_do_marking_is_active(), "oops_do_marking_prologue must be called");

#ifdef ASSERT
  // make sure that we aren't leaking icholders
  int count = 0;
  FOR_ALL_BLOBS(cb) {
    if (cb->is_nmethod()) {
      RelocIterator iter((nmethod*)cb);
      while(iter.next()) {
        if (iter.type() == relocInfo::virtual_call_type) {
          if (CompiledIC::is_icholder_call_site(iter.virtual_call_reloc())) {
            CompiledIC *ic = CompiledIC_at(iter.reloc());
            if (TraceCompiledIC) {
              tty->print("noticed icholder " INTPTR_FORMAT " ", p2i(ic->cached_icholder()));
              ic->print();
            }
            assert(ic->cached_icholder() != NULL, "must be non-NULL");
            count++;
          }
        }
      }
    }
  }

  assert(count + InlineCacheBuffer::pending_icholder_count() + CompiledICHolder::live_not_claimed_count() ==
         CompiledICHolder::live_count(), "must agree");
#endif
}


void CodeCache::verify_oops() {
  MutexLockerEx mu(CodeCache_lock, Mutex::_no_safepoint_check_flag);
  VerifyOopClosure voc;
  FOR_ALL_ALIVE_BLOBS(cb) {
    if (cb->is_nmethod()) {
      nmethod *nm = (nmethod*)cb;
      nm->oops_do(&voc);
      nm->verify_oop_relocations();
    }
  }
}


address CodeCache::first_address() {
  assert_locked_or_safepoint(CodeCache_lock);
  return (address)_heap->low_boundary();
}


address CodeCache::last_address() {
  assert_locked_or_safepoint(CodeCache_lock);
  return (address)_heap->high();
}

/**
 * Returns the reverse free ratio. E.g., if 25% (1/4) of the code cache
 * is free, reverse_free_ratio() returns 4.
 */
double CodeCache::reverse_free_ratio() {
  double unallocated_capacity = (double)(CodeCache::unallocated_capacity() - CodeCacheMinimumFreeSpace);
  double max_capacity = (double)CodeCache::max_capacity();
  return max_capacity / unallocated_capacity;
}

void icache_init();

void CodeCache::initialize() {
  assert(CodeCacheSegmentSize >= (uintx)CodeEntryAlignment, "CodeCacheSegmentSize must be large enough to align entry points");
#ifdef COMPILER2
  assert(CodeCacheSegmentSize >= (uintx)OptoLoopAlignment,  "CodeCacheSegmentSize must be large enough to align inner loops");
#endif
  assert(CodeCacheSegmentSize >= sizeof(jdouble),    "CodeCacheSegmentSize must be large enough to align constants");
  // This was originally just a check of the alignment, causing failure, instead, round
  // the code cache to the page size.  In particular, Solaris is moving to a larger
  // default page size.
  CodeCacheExpansionSize = round_to(CodeCacheExpansionSize, os::vm_page_size());
  InitialCodeCacheSize = round_to(InitialCodeCacheSize, os::vm_page_size());
  ReservedCodeCacheSize = round_to(ReservedCodeCacheSize, os::vm_page_size());
  if (!_heap->reserve(ReservedCodeCacheSize, InitialCodeCacheSize, CodeCacheSegmentSize)) {
    vm_exit_during_initialization("Could not reserve enough space for code cache");
  }

  MemoryService::add_code_heap_memory_pool(_heap);

  // Initialize ICache flush mechanism
  // This service is needed for os::register_code_area
  icache_init();

  // Give OS a chance to register generated code area.
  // This is used on Windows 64 bit platforms to register
  // Structured Exception Handlers for our generated code.
  os::register_code_area(_heap->low_boundary(), _heap->high_boundary());
}


void codeCache_init() {
  CodeCache::initialize();
}

//------------------------------------------------------------------------------------------------

int CodeCache::number_of_nmethods_with_dependencies() {
  return _number_of_nmethods_with_dependencies;
}

void CodeCache::clear_inline_caches() {
  assert_locked_or_safepoint(CodeCache_lock);
  FOR_ALL_ALIVE_NMETHODS(nm) {
    nm->clear_inline_caches();
  }
}

// Keeps track of time spent for checking dependencies
NOT_PRODUCT(static elapsedTimer dependentCheckTime;)

int CodeCache::mark_for_deoptimization(DepChange& changes) {
  MutexLockerEx mu(CodeCache_lock, Mutex::_no_safepoint_check_flag);
  int number_of_marked_CodeBlobs = 0;

  // search the hierarchy looking for nmethods which are affected by the loading of this class

  // then search the interfaces this class implements looking for nmethods
  // which might be dependent of the fact that an interface only had one
  // implementor.
  // nmethod::check_all_dependencies works only correctly, if no safepoint
  // can happen
  No_Safepoint_Verifier nsv;
  for (DepChange::ContextStream str(changes, nsv); str.next(); ) {
    Klass* d = str.klass();
    number_of_marked_CodeBlobs += InstanceKlass::cast(d)->mark_dependent_nmethods(changes);
  }

#ifndef PRODUCT
  if (VerifyDependencies) {
    // Object pointers are used as unique identifiers for dependency arguments. This
    // is only possible if no safepoint, i.e., GC occurs during the verification code.
    dependentCheckTime.start();
    nmethod::check_all_dependencies(changes);
    dependentCheckTime.stop();
  }
#endif

  return number_of_marked_CodeBlobs;
}


#ifdef HOTSWAP
int CodeCache::mark_for_evol_deoptimization(instanceKlassHandle dependee) {
  MutexLockerEx mu(CodeCache_lock, Mutex::_no_safepoint_check_flag);
  int number_of_marked_CodeBlobs = 0;

  // Deoptimize all methods of the evolving class itself
  Array<Method*>* old_methods = dependee->methods();
  for (int i = 0; i < old_methods->length(); i++) {
    ResourceMark rm;
    Method* old_method = old_methods->at(i);
    nmethod *nm = old_method->code();
    if (nm != NULL) {
      nm->mark_for_deoptimization();
      number_of_marked_CodeBlobs++;
    }
  }

  FOR_ALL_ALIVE_NMETHODS(nm) {
    if (nm->is_marked_for_deoptimization()) {
      // ...Already marked in the previous pass; don't count it again.
    } else if (nm->is_evol_dependent_on(dependee())) {
      ResourceMark rm;
      nm->mark_for_deoptimization();
      number_of_marked_CodeBlobs++;
    } else  {
      // flush caches in case they refer to a redefined Method*
      nm->clear_inline_caches();
    }
  }

  return number_of_marked_CodeBlobs;
}
#endif // HOTSWAP


// Deoptimize all methods
void CodeCache::mark_all_nmethods_for_deoptimization() {
  MutexLockerEx mu(CodeCache_lock, Mutex::_no_safepoint_check_flag);
  FOR_ALL_ALIVE_NMETHODS(nm) {
    nm->mark_for_deoptimization();
  }
}


int CodeCache::mark_for_deoptimization(Method* dependee) {
  MutexLockerEx mu(CodeCache_lock, Mutex::_no_safepoint_check_flag);
  int number_of_marked_CodeBlobs = 0;

  FOR_ALL_ALIVE_NMETHODS(nm) {
    if (nm->is_dependent_on_method(dependee)) {
      ResourceMark rm;
      nm->mark_for_deoptimization();
      number_of_marked_CodeBlobs++;
    }
  }

  return number_of_marked_CodeBlobs;
}

void CodeCache::make_marked_nmethods_zombies() {
  assert(SafepointSynchronize::is_at_safepoint(), "must be at a safepoint");
  FOR_ALL_ALIVE_NMETHODS(nm) {
    if (nm->is_marked_for_deoptimization()) {

      // If the nmethod has already been made non-entrant and it can be converted
      // then zombie it now. Otherwise make it non-entrant and it will eventually
      // be zombied when it is no longer seen on the stack. Note that the nmethod
      // might be "entrant" and not on the stack and so could be zombied immediately
      // but we can't tell because we don't track it on stack until it becomes
      // non-entrant.

      if (nm->is_not_entrant() && nm->can_not_entrant_be_converted()) {
        nm->make_zombie();
      } else {
        nm->make_not_entrant();
      }
    }
  }
}

void CodeCache::make_marked_nmethods_not_entrant() {
  assert_locked_or_safepoint(CodeCache_lock);
  FOR_ALL_ALIVE_NMETHODS(nm) {
    if (nm->is_marked_for_deoptimization()) {
      nm->make_not_entrant();
    }
  }
}

void CodeCache::verify() {
  _heap->verify();
  FOR_ALL_ALIVE_BLOBS(p) {
    p->verify();
  }
}

void CodeCache::report_codemem_full() {
  _codemem_full_count++;
  EventCodeCacheFull event;
  if (event.should_commit()) {
    event.set_startAddress((u8)low_bound());
    event.set_commitedTopAddress((u8)high());
    event.set_reservedTopAddress((u8)high_bound());
    event.set_entryCount(nof_blobs());
    event.set_methodCount(nof_nmethods());
    event.set_adaptorCount(nof_adapters());
    event.set_unallocatedCapacity(unallocated_capacity()/K);
    event.set_fullCount(_codemem_full_count);
    event.commit();
  }
}

void CodeCache::print_memory_overhead() {
  size_t wasted_bytes = 0;
  CodeBlob *cb;
  for (cb = first(); cb != NULL; cb = next(cb)) {
    HeapBlock* heap_block = ((HeapBlock*)cb) - 1;
    wasted_bytes += heap_block->length() * CodeCacheSegmentSize - cb->size();
  }
  // Print bytes that are allocated in the freelist
  ttyLocker ttl;
  tty->print_cr("Number of elements in freelist: " SSIZE_FORMAT,    freelist_length());
  tty->print_cr("Allocated in freelist:          " SSIZE_FORMAT "kB",  bytes_allocated_in_freelist()/K);
  tty->print_cr("Unused bytes in CodeBlobs:      " SSIZE_FORMAT "kB",  (wasted_bytes/K));
  tty->print_cr("Segment map size:               " SSIZE_FORMAT "kB",  allocated_segments()/K); // 1 byte per segment
}

//------------------------------------------------------------------------------------------------
// Non-product version

#ifndef PRODUCT

void CodeCache::print_trace(const char* event, CodeBlob* cb, int size) {
  if (PrintCodeCache2) {  // Need to add a new flag
    ResourceMark rm;
    if (size == 0)  size = cb->size();
    tty->print_cr("CodeCache %s:  addr: " INTPTR_FORMAT ", size: 0x%x", event, p2i(cb), size);
  }
}

void CodeCache::print_internals() {
  int nmethodCount = 0;
  int runtimeStubCount = 0;
  int adapterCount = 0;
  int deoptimizationStubCount = 0;
  int uncommonTrapStubCount = 0;
  int bufferBlobCount = 0;
  int total = 0;
  int nmethodAlive = 0;
  int nmethodNotEntrant = 0;
  int nmethodZombie = 0;
  int nmethodUnloaded = 0;
  int nmethodJava = 0;
  int nmethodNative = 0;
  int max_nm_size = 0;
  ResourceMark rm;

  CodeBlob *cb;
  for (cb = first(); cb != NULL; cb = next(cb)) {
    total++;
    if (cb->is_nmethod()) {
      nmethod* nm = (nmethod*)cb;

      if (Verbose && nm->method() != NULL) {
        ResourceMark rm;
        char *method_name = nm->method()->name_and_sig_as_C_string();
        tty->print("%s", method_name);
        if(nm->is_alive()) { tty->print_cr(" alive"); }
        if(nm->is_not_entrant()) { tty->print_cr(" not-entrant"); }
        if(nm->is_zombie()) { tty->print_cr(" zombie"); }
      }

      nmethodCount++;

      if(nm->is_alive()) { nmethodAlive++; }
      if(nm->is_not_entrant()) { nmethodNotEntrant++; }
      if(nm->is_zombie()) { nmethodZombie++; }
      if(nm->is_unloaded()) { nmethodUnloaded++; }
      if(nm->method() != NULL && nm->is_native_method()) { nmethodNative++; }

      if(nm->method() != NULL && nm->is_java_method()) {
        nmethodJava++;
        max_nm_size = MAX2(max_nm_size, nm->size());
      }
    } else if (cb->is_runtime_stub()) {
      runtimeStubCount++;
    } else if (cb->is_deoptimization_stub()) {
      deoptimizationStubCount++;
    } else if (cb->is_uncommon_trap_stub()) {
      uncommonTrapStubCount++;
    } else if (cb->is_adapter_blob()) {
      adapterCount++;
    } else if (cb->is_buffer_blob()) {
      bufferBlobCount++;
    }
  }

  int bucketSize = 512;
  int bucketLimit = max_nm_size / bucketSize + 1;
  int *buckets = NEW_C_HEAP_ARRAY(int, bucketLimit, mtCode);
  memset(buckets, 0, sizeof(int) * bucketLimit);

  for (cb = first(); cb != NULL; cb = next(cb)) {
    if (cb->is_nmethod()) {
      nmethod* nm = (nmethod*)cb;
      if(nm->is_java_method()) {
        buckets[nm->size() / bucketSize]++;
       }
    }
  }

  tty->print_cr("Code Cache Entries (total of %d)",total);
  tty->print_cr("-------------------------------------------------");
  tty->print_cr("nmethods: %d",nmethodCount);
  tty->print_cr("\talive: %d",nmethodAlive);
  tty->print_cr("\tnot_entrant: %d",nmethodNotEntrant);
  tty->print_cr("\tzombie: %d",nmethodZombie);
  tty->print_cr("\tunloaded: %d",nmethodUnloaded);
  tty->print_cr("\tjava: %d",nmethodJava);
  tty->print_cr("\tnative: %d",nmethodNative);
  tty->print_cr("runtime_stubs: %d",runtimeStubCount);
  tty->print_cr("adapters: %d",adapterCount);
  tty->print_cr("buffer blobs: %d",bufferBlobCount);
  tty->print_cr("deoptimization_stubs: %d",deoptimizationStubCount);
  tty->print_cr("uncommon_traps: %d",uncommonTrapStubCount);
  tty->print_cr("\nnmethod size distribution (non-zombie java)");
  tty->print_cr("-------------------------------------------------");

  for(int i=0; i<bucketLimit; i++) {
    if(buckets[i] != 0) {
      tty->print("%d - %d bytes",i*bucketSize,(i+1)*bucketSize);
      tty->fill_to(40);
      tty->print_cr("%d",buckets[i]);
    }
  }

  FREE_C_HEAP_ARRAY(int, buckets, mtCode);
  print_memory_overhead();
}

#endif // !PRODUCT

void CodeCache::print() {
  print_summary(tty);

#ifndef PRODUCT
  if (!Verbose) return;

  CodeBlob_sizes live;
  CodeBlob_sizes dead;

  FOR_ALL_BLOBS(p) {
    if (!p->is_alive()) {
      dead.add(p);
    } else {
      live.add(p);
    }
  }

  tty->print_cr("CodeCache:");
  tty->print_cr("nmethod dependency checking time %fs", dependentCheckTime.seconds());

  if (!live.is_empty()) {
    live.print("live");
  }
  if (!dead.is_empty()) {
    dead.print("dead");
  }


  if (WizardMode) {
     // print the oop_map usage
    int code_size = 0;
    int number_of_blobs = 0;
    int number_of_oop_maps = 0;
    int map_size = 0;
    FOR_ALL_BLOBS(p) {
      if (p->is_alive()) {
        number_of_blobs++;
        code_size += p->code_size();
        OopMapSet* set = p->oop_maps();
        if (set != NULL) {
          number_of_oop_maps += set->size();
          map_size           += set->heap_size();
        }
      }
    }
    tty->print_cr("OopMaps");
    tty->print_cr("  #blobs    = %d", number_of_blobs);
    tty->print_cr("  code size = %d", code_size);
    tty->print_cr("  #oop_maps = %d", number_of_oop_maps);
    tty->print_cr("  map size  = %d", map_size);
  }

#endif // !PRODUCT
}

void CodeCache::print_summary(outputStream* st, bool detailed) {
  size_t total = (_heap->high_boundary() - _heap->low_boundary());
  st->print_cr("CodeCache: size=" SIZE_FORMAT "Kb used=" SIZE_FORMAT
               "Kb max_used=" SIZE_FORMAT "Kb free=" SIZE_FORMAT "Kb",
               total/K, (total - unallocated_capacity())/K,
               maxCodeCacheUsed/K, unallocated_capacity()/K);

  if (detailed) {
    st->print_cr(" bounds [" INTPTR_FORMAT ", " INTPTR_FORMAT ", " INTPTR_FORMAT "]",
                 p2i(_heap->low_boundary()),
                 p2i(_heap->high()),
                 p2i(_heap->high_boundary()));
    st->print_cr(" total_blobs=" UINT32_FORMAT " nmethods=" UINT32_FORMAT
                 " adapters=" UINT32_FORMAT,
                 nof_blobs(), nof_nmethods(), nof_adapters());
    st->print_cr(" compilation: %s", CompileBroker::should_compile_new_jobs() ?
                 "enabled" : Arguments::mode() == Arguments::_int ?
                 "disabled (interpreter mode)" :
                 "disabled (not enough contiguous free space left)");
  }
}

void CodeCache::log_state(outputStream* st) {
  st->print(" total_blobs='" UINT32_FORMAT "' nmethods='" UINT32_FORMAT "'"
            " adapters='" UINT32_FORMAT "' free_code_cache='" SIZE_FORMAT "'",
            nof_blobs(), nof_nmethods(), nof_adapters(),
            unallocated_capacity());
}

