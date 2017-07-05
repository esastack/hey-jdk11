/*
 * Copyright 1997-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

class Block;
class LRG_List;
class PhaseCFG;
class VectorSet;
class IndexSet;

//------------------------------PhaseLive--------------------------------------
// Compute live-in/live-out
class PhaseLive : public Phase {
  // Array of Sets of values live at the start of a block.
  // Indexed by block pre-order number.
  IndexSet *_live;

  // Array of Sets of values defined locally in the block
  // Indexed by block pre-order number.
  IndexSet *_defs;

  // Array of delta-set pointers, indexed by block pre-order number
  IndexSet **_deltas;
  IndexSet *_free_IndexSet;     // Free list of same

  Block_List *_worklist;        // Worklist for iterative solution

  const PhaseCFG &_cfg;         // Basic blocks
  LRG_List &_names;             // Mapping from Nodes to live ranges
  uint _maxlrg;                 // Largest live-range number
  Arena *_arena;

  IndexSet *getset( Block *p );
  IndexSet *getfreeset( );
  void freeset( const Block *p );
  void add_liveout( Block *p, uint r, VectorSet &first_pass );
  void add_liveout( Block *p, IndexSet *lo, VectorSet &first_pass );

public:
  PhaseLive( const PhaseCFG &cfg, LRG_List &names, Arena *arena );
  ~PhaseLive() {}
  // Compute liveness info
  void compute(uint maxlrg);
  // Reset arena storage
  void reset() { _live = NULL; }

  // Return the live-out set for this block
  IndexSet *live( const Block * b ) { return &_live[b->_pre_order-1]; }

#ifndef PRODUCT
  void dump( const Block *b ) const;
  void stats(uint iters) const;
#endif
};
