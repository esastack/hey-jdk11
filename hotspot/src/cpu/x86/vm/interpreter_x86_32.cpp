/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

#include "incls/_precompiled.incl"
#include "incls/_interpreter_x86_32.cpp.incl"

#define __ _masm->

// Initialize the sentinel used to distinguish an interpreter return address.
const int Interpreter::return_sentinel = 0xfeedbeed;

//------------------------------------------------------------------------------------------------------------------------

address AbstractInterpreterGenerator::generate_slow_signature_handler() {
  address entry = __ pc();
  // rbx,: method
  // rcx: temporary
  // rdi: pointer to locals
  // rsp: end of copied parameters area
  __ movl(rcx, rsp);
  __ call_VM(noreg, CAST_FROM_FN_PTR(address, InterpreterRuntime::slow_signature_handler), rbx, rdi, rcx);
  __ ret(0);
  return entry;
}


//
// Various method entries (that c++ and asm interpreter agree upon)
//------------------------------------------------------------------------------------------------------------------------
//
//

// Empty method, generate a very fast return.

address InterpreterGenerator::generate_empty_entry(void) {

  // rbx,: methodOop
  // rcx: receiver (unused)
  // rsi: previous interpreter state (C++ interpreter) must preserve
  // rsi: sender sp must set sp to this value on return

  if (!UseFastEmptyMethods) return NULL;

  address entry_point = __ pc();

  // If we need a safepoint check, generate full interpreter entry.
  Label slow_path;
  ExternalAddress state(SafepointSynchronize::address_of_state());
  __ cmp32(ExternalAddress(SafepointSynchronize::address_of_state()),
           SafepointSynchronize::_not_synchronized);
  __ jcc(Assembler::notEqual, slow_path);

  // do nothing for empty methods (do not even increment invocation counter)
  // Code: _return
  // _return
  // return w/o popping parameters
  __ popl(rax);
  __ movl(rsp, rsi);
  __ jmp(rax);

  __ bind(slow_path);
  (void) generate_normal_entry(false);
  return entry_point;
}

address InterpreterGenerator::generate_math_entry(AbstractInterpreter::MethodKind kind) {

  // rbx,: methodOop
  // rcx: scratrch
  // rsi: sender sp

  if (!InlineIntrinsics) return NULL; // Generate a vanilla entry

  address entry_point = __ pc();

  // These don't need a safepoint check because they aren't virtually
  // callable. We won't enter these intrinsics from compiled code.
  // If in the future we added an intrinsic which was virtually callable
  // we'd have to worry about how to safepoint so that this code is used.

  // mathematical functions inlined by compiler
  // (interpreter must provide identical implementation
  // in order to avoid monotonicity bugs when switching
  // from interpreter to compiler in the middle of some
  // computation)
  //
  // stack: [ ret adr ] <-- rsp
  //        [ lo(arg) ]
  //        [ hi(arg) ]
  //

  // Note: For JDK 1.2 StrictMath doesn't exist and Math.sin/cos/sqrt are
  //       native methods. Interpreter::method_kind(...) does a check for
  //       native methods first before checking for intrinsic methods and
  //       thus will never select this entry point. Make sure it is not
  //       called accidentally since the SharedRuntime entry points will
  //       not work for JDK 1.2.
  //
  // We no longer need to check for JDK 1.2 since it's EOL'ed.
  // The following check existed in pre 1.6 implementation,
  //    if (Universe::is_jdk12x_version()) {
  //      __ should_not_reach_here();
  //    }
  // Universe::is_jdk12x_version() always returns false since
  // the JDK version is not yet determined when this method is called.
  // This method is called during interpreter_init() whereas
  // JDK version is only determined when universe2_init() is called.

  // Note: For JDK 1.3 StrictMath exists and Math.sin/cos/sqrt are
  //       java methods.  Interpreter::method_kind(...) will select
  //       this entry point for the corresponding methods in JDK 1.3.
  // get argument
  if (TaggedStackInterpreter) {
    __ pushl(Address(rsp, 3*wordSize));  // push hi (and note rsp -= wordSize)
    __ pushl(Address(rsp, 2*wordSize));  // push lo
    __ fld_d(Address(rsp, 0));           // get double in ST0
    __ addl(rsp, 2*wordSize);
  } else {
    __ fld_d(Address(rsp, 1*wordSize));
  }
  switch (kind) {
    case Interpreter::java_lang_math_sin :
        __ trigfunc('s');
        break;
    case Interpreter::java_lang_math_cos :
        __ trigfunc('c');
        break;
    case Interpreter::java_lang_math_tan :
        __ trigfunc('t');
        break;
    case Interpreter::java_lang_math_sqrt:
        __ fsqrt();
        break;
    case Interpreter::java_lang_math_abs:
        __ fabs();
        break;
    case Interpreter::java_lang_math_log:
        __ flog();
        // Store to stack to convert 80bit precision back to 64bits
        __ push_fTOS();
        __ pop_fTOS();
        break;
    case Interpreter::java_lang_math_log10:
        __ flog10();
        // Store to stack to convert 80bit precision back to 64bits
        __ push_fTOS();
        __ pop_fTOS();
        break;
    default                              :
        ShouldNotReachHere();
  }

  // return double result in xmm0 for interpreter and compilers.
  if (UseSSE >= 2) {
    __ subl(rsp, 2*wordSize);
    __ fstp_d(Address(rsp, 0));
    __ movdbl(xmm0, Address(rsp, 0));
    __ addl(rsp, 2*wordSize);
  }

  // done, result in FPU ST(0) or XMM0
  __ popl(rdi);                              // get return address
  __ movl(rsp, rsi);                         // set sp to sender sp
  __ jmp(rdi);

  return entry_point;
}


// Abstract method entry
// Attempt to execute abstract method. Throw exception
address InterpreterGenerator::generate_abstract_entry(void) {

  // rbx,: methodOop
  // rcx: receiver (unused)
  // rsi: previous interpreter state (C++ interpreter) must preserve

  // rsi: sender SP

  address entry_point = __ pc();

  // abstract method entry
  // remove return address. Not really needed, since exception handling throws away expression stack
  __ popl(rbx);

  // adjust stack to what a normal return would do
  __ movl(rsp, rsi);
  // throw exception
  __ call_VM(noreg, CAST_FROM_FN_PTR(address, InterpreterRuntime::throw_AbstractMethodError));
  // the call_VM checks for exception, so we should never return here.
  __ should_not_reach_here();

  return entry_point;
}

// This method tells the deoptimizer how big an interpreted frame must be:
int AbstractInterpreter::size_activation(methodOop method,
                                         int tempcount,
                                         int popframe_extra_args,
                                         int moncount,
                                         int callee_param_count,
                                         int callee_locals,
                                         bool is_top_frame) {
  return layout_activation(method,
                           tempcount,
                           popframe_extra_args,
                           moncount,
                           callee_param_count,
                           callee_locals,
                           (frame*) NULL,
                           (frame*) NULL,
                           is_top_frame);
}

void Deoptimization::unwind_callee_save_values(frame* f, vframeArray* vframe_array) {

  // This code is sort of the equivalent of C2IAdapter::setup_stack_frame back in
  // the days we had adapter frames. When we deoptimize a situation where a
  // compiled caller calls a compiled caller will have registers it expects
  // to survive the call to the callee. If we deoptimize the callee the only
  // way we can restore these registers is to have the oldest interpreter
  // frame that we create restore these values. That is what this routine
  // will accomplish.

  // At the moment we have modified c2 to not have any callee save registers
  // so this problem does not exist and this routine is just a place holder.

  assert(f->is_interpreted_frame(), "must be interpreted");
}
