/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018, SAP SE. All rights reserved.
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

#ifndef CPU_PPC_GC_G1_G1BARRIERSETASSEMBLER_PPC_HPP
#define CPU_PPC_GC_G1_G1BARRIERSETASSEMBLER_PPC_HPP

#include "asm/macroAssembler.hpp"
#include "gc/shared/modRefBarrierSetAssembler.hpp"

class G1BarrierSetAssembler: public ModRefBarrierSetAssembler {
protected:
  virtual void gen_write_ref_array_pre_barrier(MacroAssembler* masm, DecoratorSet decorators, Register from, Register to, Register count,
                                               Register preserve1, Register preserve2);
  virtual void gen_write_ref_array_post_barrier(MacroAssembler* masm, DecoratorSet decorators, Register addr, Register count, Register preserve);

  void g1_write_barrier_pre(MacroAssembler* masm, DecoratorSet decorators, Register obj, RegisterOrConstant ind_or_offs, Register pre_val,
                            Register tmp1, Register tmp2, bool needs_frame);
  void g1_write_barrier_post(MacroAssembler* masm, DecoratorSet decorators, Register store_addr, Register new_val,
                             Register tmp1, Register tmp2, Register tmp3);

  virtual void oop_store_at(MacroAssembler* masm, DecoratorSet decorators, BasicType type,
                            Register base, RegisterOrConstant ind_or_offs, Register val,
                            Register tmp1, Register tmp2, Register tmp3, bool needs_frame);

public:
  virtual void load_at(MacroAssembler* masm, DecoratorSet decorators, BasicType type,
                       Register base, RegisterOrConstant ind_or_offs, Register dst,
                       Register tmp1, Register tmp2, bool needs_frame, Label *is_null = NULL);

  virtual void resolve_jobject(MacroAssembler* masm, Register value, Register tmp1, Register tmp2, bool needs_frame);
};

#endif // CPU_PPC_GC_G1_G1BARRIERSETASSEMBLER_PPC_HPP
