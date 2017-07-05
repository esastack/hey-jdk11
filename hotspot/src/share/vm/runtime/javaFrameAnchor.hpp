/*
 * Copyright 2002-2005 Sun Microsystems, Inc.  All Rights Reserved.
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
//
// An object for encapsulating the machine/os dependent part of a JavaThread frame state
//
class JavaThread;

class JavaFrameAnchor VALUE_OBJ_CLASS_SPEC {
// Too many friends...
friend class CallNativeDirectNode;
friend class OptoRuntime;
friend class Runtime1;
friend class StubAssembler;
friend class CallRuntimeDirectNode;
friend class MacroAssembler;
friend class InterpreterGenerator;
friend class LIR_Assembler;
friend class GraphKit;
friend class StubGenerator;
friend class JavaThread;
friend class frame;
friend class VMStructs;
friend class BytecodeInterpreter;
friend class JavaCallWrapper;

 private:
  //
  // Whenever _last_Java_sp != NULL other anchor fields MUST be valid!
  // The stack may not be walkable [check with walkable() ] but the values must be valid.
  // The profiler apparently depends on this.
  //
  intptr_t* volatile _last_Java_sp;

  // Whenever we call from Java to native we can not be assured that the return
  // address that composes the last_Java_frame will be in an accessible location
  // so calls from Java to native store that pc (or one good enough to locate
  // the oopmap) in the frame anchor. Since the frames that call from Java to
  // native are never deoptimized we never need to patch the pc and so this
  // is acceptable.
  volatile  address _last_Java_pc;

  // tells whether the last Java frame is set
  // It is important that when last_Java_sp != NULL that the rest of the frame
  // anchor (including platform specific) all be valid.

  bool has_last_Java_frame() const                   { return _last_Java_sp != NULL; }
  // This is very dangerous unless sp == NULL
  // Invalidate the anchor so that has_last_frame is false
  // and no one should look at the other fields.
  void zap(void)                                     { _last_Java_sp = NULL; }

#include "incls/_javaFrameAnchor_pd.hpp.incl"

public:
  JavaFrameAnchor()                              { clear(); }
  JavaFrameAnchor(JavaFrameAnchor *src)          { copy(src); }

  address last_Java_pc(void)                     { return _last_Java_pc; }
  void set_last_Java_pc(address pc)              { _last_Java_pc = pc; }

  // Assembly stub generation helpers

  static ByteSize last_Java_sp_offset()          { return byte_offset_of(JavaFrameAnchor, _last_Java_sp); }
  static ByteSize last_Java_pc_offset()          { return byte_offset_of(JavaFrameAnchor, _last_Java_pc); }

};
