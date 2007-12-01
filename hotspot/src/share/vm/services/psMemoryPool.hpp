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

class PSGenerationPool : public CollectedMemoryPool {
private:
  PSOldGen* _gen;

public:
  PSGenerationPool(PSOldGen* pool, const char* name, PoolType type, bool support_usage_threshold);
  PSGenerationPool(PSPermGen* pool, const char* name, PoolType type, bool support_usage_threshold);

  MemoryUsage get_memory_usage();
  size_t used_in_bytes()              { return _gen->used_in_bytes(); }
  size_t max_size() const             { return _gen->reserved().byte_size(); }
};

class EdenMutableSpacePool : public CollectedMemoryPool {
private:
  PSYoungGen*   _gen;
  MutableSpace* _space;

public:
  EdenMutableSpacePool(PSYoungGen* gen,
                       MutableSpace* space,
                       const char* name,
                       PoolType type,
                       bool support_usage_threshold);

  MutableSpace* space()                     { return _space; }
  MemoryUsage get_memory_usage();
  size_t used_in_bytes()                    { return space()->used_in_bytes(); }
  size_t max_size() const {
    // Eden's max_size = max_size of Young Gen - the current committed size of survivor spaces
    return _gen->max_size() - _gen->from_space()->capacity_in_bytes() - _gen->to_space()->capacity_in_bytes();
  }
};

class SurvivorMutableSpacePool : public CollectedMemoryPool {
private:
  PSYoungGen*   _gen;

public:
  SurvivorMutableSpacePool(PSYoungGen* gen,
                           const char* name,
                           PoolType type,
                           bool support_usage_threshold);

  MemoryUsage get_memory_usage();

  size_t used_in_bytes() {
    return _gen->from_space()->used_in_bytes();
  }
  size_t committed_in_bytes() {
    return _gen->from_space()->capacity_in_bytes();
  }
  size_t max_size() const {
    // Return current committed size of the from-space
    return _gen->from_space()->capacity_in_bytes();
  }
};
