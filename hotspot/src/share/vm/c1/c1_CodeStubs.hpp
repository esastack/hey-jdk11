/*
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
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

class CodeEmitInfo;
class LIR_Assembler;
class LIR_OpVisitState;

// CodeStubs are little 'out-of-line' pieces of code that
// usually handle slow cases of operations. All code stubs
// are collected and code is emitted at the end of the
// nmethod.

class CodeStub: public CompilationResourceObj {
 protected:
  Label _entry;                                  // label at the stub entry point
  Label _continuation;                           // label where stub continues, if any

 public:
  CodeStub() {}

  // code generation
  void assert_no_unbound_labels()                { assert(!_entry.is_unbound() && !_continuation.is_unbound(), "unbound label"); }
  virtual void emit_code(LIR_Assembler* e) = 0;
  virtual CodeEmitInfo* info() const             { return NULL; }
  virtual bool is_exception_throw_stub() const   { return false; }
  virtual bool is_range_check_stub() const       { return false; }
  virtual bool is_divbyzero_stub() const         { return false; }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const = 0;
#endif

  // label access
  Label* entry()                                 { return &_entry; }
  Label* continuation()                          { return &_continuation; }
  // for LIR
  virtual void visit(LIR_OpVisitState* visit) {
#ifndef PRODUCT
    if (LIRTracePeephole && Verbose) {
      tty->print("no visitor for ");
      print_name(tty);
      tty->cr();
    }
#endif
  }
};


define_array(CodeStubArray, CodeStub*)
define_stack(_CodeStubList, CodeStubArray)

class CodeStubList: public _CodeStubList {
 public:
  CodeStubList(): _CodeStubList() {}

  void append(CodeStub* stub) {
    if (!contains(stub)) {
      _CodeStubList::append(stub);
    }
  }
};

#ifdef TIERED
class CounterOverflowStub: public CodeStub {
 private:
  CodeEmitInfo* _info;
  int           _bci;

public:
  CounterOverflowStub(CodeEmitInfo* info, int bci) : _info(info), _bci(bci) {
  }

  virtual void emit_code(LIR_Assembler* e);

  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
  }

#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("CounterOverflowStub"); }
#endif // PRODUCT

};
#endif // TIERED

class ConversionStub: public CodeStub {
 private:
  Bytecodes::Code _bytecode;
  LIR_Opr         _input;
  LIR_Opr         _result;

  static float float_zero;
  static double double_zero;
 public:
  ConversionStub(Bytecodes::Code bytecode, LIR_Opr input, LIR_Opr result)
    : _bytecode(bytecode), _input(input), _result(result) {
  }

  Bytecodes::Code bytecode() { return _bytecode; }
  LIR_Opr         input()    { return _input; }
  LIR_Opr         result()   { return _result; }

  virtual void emit_code(LIR_Assembler* e);
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case();
    visitor->do_input(_input);
    visitor->do_output(_result);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("ConversionStub"); }
#endif // PRODUCT
};


// Throws ArrayIndexOutOfBoundsException by default but can be
// configured to throw IndexOutOfBoundsException in constructor
class RangeCheckStub: public CodeStub {
 private:
  CodeEmitInfo* _info;
  LIR_Opr       _index;
  bool          _throw_index_out_of_bounds_exception;

 public:
  RangeCheckStub(CodeEmitInfo* info, LIR_Opr index, bool throw_index_out_of_bounds_exception = false);
  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual bool is_exception_throw_stub() const   { return true; }
  virtual bool is_range_check_stub() const       { return true; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
    visitor->do_input(_index);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("RangeCheckStub"); }
#endif // PRODUCT
};


class DivByZeroStub: public CodeStub {
 private:
  CodeEmitInfo* _info;
  int           _offset;

 public:
  DivByZeroStub(CodeEmitInfo* info)
    : _info(info), _offset(-1) {
  }
  DivByZeroStub(int offset, CodeEmitInfo* info)
    : _info(info), _offset(offset) {
  }
  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual bool is_exception_throw_stub() const   { return true; }
  virtual bool is_divbyzero_stub() const         { return true; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("DivByZeroStub"); }
#endif // PRODUCT
};


class ImplicitNullCheckStub: public CodeStub {
 private:
  CodeEmitInfo* _info;
  int           _offset;

 public:
  ImplicitNullCheckStub(int offset, CodeEmitInfo* info)
    : _offset(offset), _info(info) {
  }
  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual bool is_exception_throw_stub() const   { return true; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("ImplicitNullCheckStub"); }
#endif // PRODUCT
};


class NewInstanceStub: public CodeStub {
 private:
  ciInstanceKlass* _klass;
  LIR_Opr          _klass_reg;
  LIR_Opr          _result;
  CodeEmitInfo*    _info;
  Runtime1::StubID _stub_id;

 public:
  NewInstanceStub(LIR_Opr klass_reg, LIR_Opr result, ciInstanceKlass* klass, CodeEmitInfo* info, Runtime1::StubID stub_id);
  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
    visitor->do_input(_klass_reg);
    visitor->do_output(_result);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("NewInstanceStub"); }
#endif // PRODUCT
};


class NewTypeArrayStub: public CodeStub {
 private:
  LIR_Opr       _klass_reg;
  LIR_Opr       _length;
  LIR_Opr       _result;
  CodeEmitInfo* _info;

 public:
  NewTypeArrayStub(LIR_Opr klass_reg, LIR_Opr length, LIR_Opr result, CodeEmitInfo* info);
  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
    visitor->do_input(_klass_reg);
    visitor->do_input(_length);
    assert(_result->is_valid(), "must be valid"); visitor->do_output(_result);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("NewTypeArrayStub"); }
#endif // PRODUCT
};


class NewObjectArrayStub: public CodeStub {
 private:
  LIR_Opr        _klass_reg;
  LIR_Opr        _length;
  LIR_Opr        _result;
  CodeEmitInfo*  _info;

 public:
  NewObjectArrayStub(LIR_Opr klass_reg, LIR_Opr length, LIR_Opr result, CodeEmitInfo* info);
  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
    visitor->do_input(_klass_reg);
    visitor->do_input(_length);
    assert(_result->is_valid(), "must be valid"); visitor->do_output(_result);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("NewObjectArrayStub"); }
#endif // PRODUCT
};


class MonitorAccessStub: public CodeStub {
 protected:
  LIR_Opr _obj_reg;
  LIR_Opr _lock_reg;

 public:
  MonitorAccessStub(LIR_Opr obj_reg, LIR_Opr lock_reg) {
    _obj_reg  = obj_reg;
    _lock_reg  = lock_reg;
  }

#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("MonitorAccessStub"); }
#endif // PRODUCT
};


class MonitorEnterStub: public MonitorAccessStub {
 private:
  CodeEmitInfo* _info;

 public:
  MonitorEnterStub(LIR_Opr obj_reg, LIR_Opr lock_reg, CodeEmitInfo* info);

  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_input(_obj_reg);
    visitor->do_input(_lock_reg);
    visitor->do_slow_case(_info);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("MonitorEnterStub"); }
#endif // PRODUCT
};


class MonitorExitStub: public MonitorAccessStub {
 private:
  bool _compute_lock;
  int  _monitor_ix;

 public:
  MonitorExitStub(LIR_Opr lock_reg, bool compute_lock, int monitor_ix)
    : MonitorAccessStub(LIR_OprFact::illegalOpr, lock_reg),
      _compute_lock(compute_lock), _monitor_ix(monitor_ix) { }
  virtual void emit_code(LIR_Assembler* e);
  virtual void visit(LIR_OpVisitState* visitor) {
    assert(_obj_reg->is_illegal(), "unused");
    if (_compute_lock) {
      visitor->do_temp(_lock_reg);
    } else {
      visitor->do_input(_lock_reg);
    }
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("MonitorExitStub"); }
#endif // PRODUCT
};


class PatchingStub: public CodeStub {
 public:
  enum PatchID {
    access_field_id,
    load_klass_id
  };
  enum constants {
    patch_info_size = 3
  };
 private:
  PatchID       _id;
  address       _pc_start;
  int           _bytes_to_copy;
  Label         _patched_code_entry;
  Label         _patch_site_entry;
  Label         _patch_site_continuation;
  Register      _obj;
  CodeEmitInfo* _info;
  int           _oop_index;  // index of the patchable oop in nmethod oop table if needed
  static int    _patch_info_offset;

  void align_patch_site(MacroAssembler* masm);

 public:
  static int patch_info_offset() { return _patch_info_offset; }

  PatchingStub(MacroAssembler* masm, PatchID id, int oop_index = -1):
      _id(id)
    , _info(NULL)
    , _oop_index(oop_index) {
    if (os::is_MP()) {
      // force alignment of patch sites on MP hardware so we
      // can guarantee atomic writes to the patch site.
      align_patch_site(masm);
    }
    _pc_start = masm->pc();
    masm->bind(_patch_site_entry);
  }

  void install(MacroAssembler* masm, LIR_PatchCode patch_code, Register obj, CodeEmitInfo* info) {
    _info = info;
    _obj = obj;
    masm->bind(_patch_site_continuation);
    _bytes_to_copy = masm->pc() - pc_start();
    if (_id == PatchingStub::access_field_id) {
      // embed a fixed offset to handle long patches which need to be offset by a word.
      // the patching code will just add the field offset field to this offset so
      // that we can refernce either the high or low word of a double word field.
      int field_offset = 0;
      switch (patch_code) {
      case lir_patch_low:         field_offset = lo_word_offset_in_bytes; break;
      case lir_patch_high:        field_offset = hi_word_offset_in_bytes; break;
      case lir_patch_normal:      field_offset = 0;                       break;
      default: ShouldNotReachHere();
      }
      NativeMovRegMem* n_move = nativeMovRegMem_at(pc_start());
      n_move->set_offset(field_offset);
    } else if (_id == load_klass_id) {
      assert(_obj != noreg, "must have register object for load_klass");
#ifdef ASSERT
      // verify that we're pointing at a NativeMovConstReg
      nativeMovConstReg_at(pc_start());
#endif
    } else {
      ShouldNotReachHere();
    }
    assert(_bytes_to_copy <= (masm->pc() - pc_start()), "not enough bytes");
  }

  address pc_start() const                       { return _pc_start; }
  PatchID id() const                             { return _id; }

  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("PatchingStub"); }
#endif // PRODUCT
};


//------------------------------------------------------------------------------
// DeoptimizeStub
//
class DeoptimizeStub : public CodeStub {
private:
  CodeEmitInfo* _info;

public:
  DeoptimizeStub(CodeEmitInfo* info) : _info(new CodeEmitInfo(info)) {}

  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const           { return _info; }
  virtual bool is_exception_throw_stub() const { return true; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("DeoptimizeStub"); }
#endif // PRODUCT
};


class SimpleExceptionStub: public CodeStub {
 private:
  LIR_Opr          _obj;
  Runtime1::StubID _stub;
  CodeEmitInfo*    _info;

 public:
  SimpleExceptionStub(Runtime1::StubID stub, LIR_Opr obj, CodeEmitInfo* info):
    _obj(obj), _info(info), _stub(stub) {
  }

  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual bool is_exception_throw_stub() const   { return true; }
  virtual void visit(LIR_OpVisitState* visitor) {
    if (_obj->is_valid()) visitor->do_input(_obj);
    visitor->do_slow_case(_info);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("SimpleExceptionStub"); }
#endif // PRODUCT
};



class ArrayStoreExceptionStub: public CodeStub {
 private:
  CodeEmitInfo* _info;

 public:
  ArrayStoreExceptionStub(CodeEmitInfo* info);
  virtual void emit_code(LIR_Assembler* emit);
  virtual CodeEmitInfo* info() const             { return _info; }
  virtual bool is_exception_throw_stub() const   { return true; }
  virtual void visit(LIR_OpVisitState* visitor) {
    visitor->do_slow_case(_info);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("ArrayStoreExceptionStub"); }
#endif // PRODUCT
};


class ArrayCopyStub: public CodeStub {
 private:
  LIR_OpArrayCopy* _op;

 public:
  ArrayCopyStub(LIR_OpArrayCopy* op): _op(op) { }

  LIR_Opr src() const                         { return _op->src(); }
  LIR_Opr src_pos() const                     { return _op->src_pos(); }
  LIR_Opr dst() const                         { return _op->dst(); }
  LIR_Opr dst_pos() const                     { return _op->dst_pos(); }
  LIR_Opr length() const                      { return _op->length(); }
  LIR_Opr tmp() const                         { return _op->tmp(); }

  virtual void emit_code(LIR_Assembler* e);
  virtual CodeEmitInfo* info() const          { return _op->info(); }
  virtual void visit(LIR_OpVisitState* visitor) {
    // don't pass in the code emit info since it's processed in the fast path
    visitor->do_slow_case();
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("ArrayCopyStub"); }
#endif // PRODUCT
};

//////////////////////////////////////////////////////////////////////////////////////////
#ifndef SERIALGC

// Code stubs for Garbage-First barriers.
class G1PreBarrierStub: public CodeStub {
 private:
  LIR_Opr _addr;
  LIR_Opr _pre_val;
  LIR_PatchCode _patch_code;
  CodeEmitInfo* _info;

 public:
  // pre_val (a temporary register) must be a register;
  // addr (the address of the field to be read) must be a LIR_Address
  G1PreBarrierStub(LIR_Opr addr, LIR_Opr pre_val, LIR_PatchCode patch_code, CodeEmitInfo* info) :
    _addr(addr), _pre_val(pre_val), _patch_code(patch_code), _info(info)
  {
    assert(_pre_val->is_register(), "should be temporary register");
    assert(_addr->is_address(), "should be the address of the field");
  }

  LIR_Opr addr() const { return _addr; }
  LIR_Opr pre_val() const { return _pre_val; }
  LIR_PatchCode patch_code() const { return _patch_code; }
  CodeEmitInfo* info() const { return _info; }

  virtual void emit_code(LIR_Assembler* e);
  virtual void visit(LIR_OpVisitState* visitor) {
    // don't pass in the code emit info since it's processed in the fast
    // path
    if (_info != NULL)
      visitor->do_slow_case(_info);
    else
      visitor->do_slow_case();
    visitor->do_input(_addr);
    visitor->do_temp(_pre_val);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("G1PreBarrierStub"); }
#endif // PRODUCT
};

class G1PostBarrierStub: public CodeStub {
 private:
  LIR_Opr _addr;
  LIR_Opr _new_val;

  static jbyte* _byte_map_base;
  static jbyte* byte_map_base_slow();
  static jbyte* byte_map_base() {
    if (_byte_map_base == NULL) {
      _byte_map_base = byte_map_base_slow();
    }
    return _byte_map_base;
  }

 public:
  // addr (the address of the object head) and new_val must be registers.
  G1PostBarrierStub(LIR_Opr addr, LIR_Opr new_val): _addr(addr), _new_val(new_val) { }

  LIR_Opr addr() const { return _addr; }
  LIR_Opr new_val() const { return _new_val; }

  virtual void emit_code(LIR_Assembler* e);
  virtual void visit(LIR_OpVisitState* visitor) {
    // don't pass in the code emit info since it's processed in the fast path
    visitor->do_slow_case();
    visitor->do_input(_addr);
    visitor->do_input(_new_val);
  }
#ifndef PRODUCT
  virtual void print_name(outputStream* out) const { out->print("G1PostBarrierStub"); }
#endif // PRODUCT
};

#endif // SERIALGC
//////////////////////////////////////////////////////////////////////////////////////////
