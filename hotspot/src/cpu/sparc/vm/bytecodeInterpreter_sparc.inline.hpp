/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
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

// Inline interpreter functions for sparc

inline jfloat BytecodeInterpreter::VMfloatAdd(jfloat op1, jfloat op2) { return op1 + op2; }
inline jfloat BytecodeInterpreter::VMfloatSub(jfloat op1, jfloat op2) { return op1 - op2; }
inline jfloat BytecodeInterpreter::VMfloatMul(jfloat op1, jfloat op2) { return op1 * op2; }
inline jfloat BytecodeInterpreter::VMfloatDiv(jfloat op1, jfloat op2) { return op1 / op2; }
inline jfloat BytecodeInterpreter::VMfloatRem(jfloat op1, jfloat op2) { return fmod(op1, op2); }

inline jfloat BytecodeInterpreter::VMfloatNeg(jfloat op) { return -op; }

inline int32_t BytecodeInterpreter::VMfloatCompare(jfloat op1, jfloat op2, int32_t direction) {
  return ( op1 < op2 ? -1 :
               op1 > op2 ? 1 :
                   op1 == op2 ? 0 :
                       (direction == -1 || direction == 1) ? direction : 0);

}

inline void BytecodeInterpreter::VMmemCopy64(uint32_t to[2], const uint32_t from[2]) {
  // x86 can do unaligned copies but not 64bits at a time
  to[0] = from[0]; to[1] = from[1];
}

// The long operations depend on compiler support for "long long" on x86

inline jlong BytecodeInterpreter::VMlongAdd(jlong op1, jlong op2) {
  return op1 + op2;
}

inline jlong BytecodeInterpreter::VMlongAnd(jlong op1, jlong op2) {
  return op1 & op2;
}

inline jlong BytecodeInterpreter::VMlongDiv(jlong op1, jlong op2) {
  // QQQ what about check and throw...
  return op1 / op2;
}

inline jlong BytecodeInterpreter::VMlongMul(jlong op1, jlong op2) {
  return op1 * op2;
}

inline jlong BytecodeInterpreter::VMlongOr(jlong op1, jlong op2) {
  return op1 | op2;
}

inline jlong BytecodeInterpreter::VMlongSub(jlong op1, jlong op2) {
  return op1 - op2;
}

inline jlong BytecodeInterpreter::VMlongXor(jlong op1, jlong op2) {
  return op1 ^ op2;
}

inline jlong BytecodeInterpreter::VMlongRem(jlong op1, jlong op2) {
  return op1 % op2;
}

inline jlong BytecodeInterpreter::VMlongUshr(jlong op1, jint op2) {
  // CVM did this 0x3f mask, is the really needed??? QQQ
  return ((unsigned long long) op1) >> (op2 & 0x3F);
}

inline jlong BytecodeInterpreter::VMlongShr(jlong op1, jint op2) {
  return op1 >> (op2 & 0x3F);
}

inline jlong BytecodeInterpreter::VMlongShl(jlong op1, jint op2) {
  return op1 << (op2 & 0x3F);
}

inline jlong BytecodeInterpreter::VMlongNeg(jlong op) {
  return -op;
}

inline jlong BytecodeInterpreter::VMlongNot(jlong op) {
  return ~op;
}

inline int32_t BytecodeInterpreter::VMlongLtz(jlong op) {
  return (op <= 0);
}

inline int32_t BytecodeInterpreter::VMlongGez(jlong op) {
  return (op >= 0);
}

inline int32_t BytecodeInterpreter::VMlongEqz(jlong op) {
  return (op == 0);
}

inline int32_t BytecodeInterpreter::VMlongEq(jlong op1, jlong op2) {
  return (op1 == op2);
}

inline int32_t BytecodeInterpreter::VMlongNe(jlong op1, jlong op2) {
  return (op1 != op2);
}

inline int32_t BytecodeInterpreter::VMlongGe(jlong op1, jlong op2) {
  return (op1 >= op2);
}

inline int32_t BytecodeInterpreter::VMlongLe(jlong op1, jlong op2) {
  return (op1 <= op2);
}

inline int32_t BytecodeInterpreter::VMlongLt(jlong op1, jlong op2) {
  return (op1 < op2);
}

inline int32_t BytecodeInterpreter::VMlongGt(jlong op1, jlong op2) {
  return (op1 > op2);
}

inline int32_t BytecodeInterpreter::VMlongCompare(jlong op1, jlong op2) {
  return (VMlongLt(op1, op2) ? -1 : VMlongGt(op1, op2) ? 1 : 0);
}

// Long conversions

inline jdouble BytecodeInterpreter::VMlong2Double(jlong val) {
  return (jdouble) val;
}

inline jfloat BytecodeInterpreter::VMlong2Float(jlong val) {
  return (jfloat) val;
}

inline jint BytecodeInterpreter::VMlong2Int(jlong val) {
  return (jint) val;
}

// Double Arithmetic

inline jdouble BytecodeInterpreter::VMdoubleAdd(jdouble op1, jdouble op2) {
  return op1 + op2;
}

inline jdouble BytecodeInterpreter::VMdoubleDiv(jdouble op1, jdouble op2) {
  // Divide by zero... QQQ
  return op1 / op2;
}

inline jdouble BytecodeInterpreter::VMdoubleMul(jdouble op1, jdouble op2) {
  return op1 * op2;
}

inline jdouble BytecodeInterpreter::VMdoubleNeg(jdouble op) {
  return -op;
}

inline jdouble BytecodeInterpreter::VMdoubleRem(jdouble op1, jdouble op2) {
  return fmod(op1, op2);
}

inline jdouble BytecodeInterpreter::VMdoubleSub(jdouble op1, jdouble op2) {
  return op1 - op2;
}

inline int32_t BytecodeInterpreter::VMdoubleCompare(jdouble op1, jdouble op2, int32_t direction) {
  return ( op1 < op2 ? -1 :
               op1 > op2 ? 1 :
                   op1 == op2 ? 0 :
                       (direction == -1 || direction == 1) ? direction : 0);
}

// Double Conversions

inline jfloat BytecodeInterpreter::VMdouble2Float(jdouble val) {
  return (jfloat) val;
}

// Float Conversions

inline jdouble BytecodeInterpreter::VMfloat2Double(jfloat op) {
  return (jdouble) op;
}

// Integer Arithmetic

inline jint BytecodeInterpreter::VMintAdd(jint op1, jint op2) {
  return op1 + op2;
}

inline jint BytecodeInterpreter::VMintAnd(jint op1, jint op2) {
  return op1 & op2;
}

inline jint BytecodeInterpreter::VMintDiv(jint op1, jint op2) {
  /* it's possible we could catch this special case implicitly */
  if (op1 == 0x80000000 && op2 == -1) return op1;
  else return op1 / op2;
}

inline jint BytecodeInterpreter::VMintMul(jint op1, jint op2) {
  return op1 * op2;
}

inline jint BytecodeInterpreter::VMintNeg(jint op) {
  return -op;
}

inline jint BytecodeInterpreter::VMintOr(jint op1, jint op2) {
  return op1 | op2;
}

inline jint BytecodeInterpreter::VMintRem(jint op1, jint op2) {
  /* it's possible we could catch this special case implicitly */
  if (op1 == 0x80000000 && op2 == -1) return 0;
  else return op1 % op2;
}

inline jint BytecodeInterpreter::VMintShl(jint op1, jint op2) {
  return op1 <<  op2;
}

inline jint BytecodeInterpreter::VMintShr(jint op1, jint op2) {
  return op1 >>  op2; // QQ op2 & 0x1f??
}

inline jint BytecodeInterpreter::VMintSub(jint op1, jint op2) {
  return op1 - op2;
}

inline jint BytecodeInterpreter::VMintUshr(jint op1, jint op2) {
  return ((juint) op1) >> op2; // QQ op2 & 0x1f??
}

inline jint BytecodeInterpreter::VMintXor(jint op1, jint op2) {
  return op1 ^ op2;
}

inline jdouble BytecodeInterpreter::VMint2Double(jint val) {
  return (jdouble) val;
}

inline jfloat BytecodeInterpreter::VMint2Float(jint val) {
  return (jfloat) val;
}

inline jlong BytecodeInterpreter::VMint2Long(jint val) {
  return (jlong) val;
}

inline jchar BytecodeInterpreter::VMint2Char(jint val) {
  return (jchar) val;
}

inline jshort BytecodeInterpreter::VMint2Short(jint val) {
  return (jshort) val;
}

inline jbyte BytecodeInterpreter::VMint2Byte(jint val) {
  return (jbyte) val;
}

// The implementations are platform dependent. We have to worry about alignment
// issues on some machines which can change on the same platform depending on
// whether it is an LP64 machine also.

// We know that on LP32 mode that longs/doubles are the only thing that gives
// us alignment headaches. We also know that the worst we have is 32bit alignment
// so thing are not really too bad.
// (Also sparcworks compiler does the right thing for free if we don't use -arch..
// switches. Only gcc gives us a hard time. In LP64 mode I think we have no issue
// with alignment.

#ifdef _GNU_SOURCE
  #define ALIGN_CONVERTER        /* Needs alignment converter */
#else
  #undef ALIGN_CONVERTER        /* No alignment converter */
#endif /* _GNU_SOURCE */

#ifdef ALIGN_CONVERTER
class u8_converter {

  private:

  public:
  static jdouble get_jdouble(address p) {
    VMJavaVal64 tmp;
    tmp.v[0] = ((uint32_t*)p)[0];
    tmp.v[1] = ((uint32_t*)p)[1];
    return tmp.d;
  }

  static void put_jdouble(address p, jdouble d) {
    VMJavaVal64 tmp;
    tmp.d = d;
    ((uint32_t*)p)[0] = tmp.v[0];
    ((uint32_t*)p)[1] = tmp.v[1];
  }

  static jlong get_jlong(address p) {
    VMJavaVal64 tmp;
    tmp.v[0] = ((uint32_t*)p)[0];
    tmp.v[1] = ((uint32_t*)p)[1];
    return tmp.l;
  }

  static void put_jlong(address p, jlong l) {
    VMJavaVal64 tmp;
    tmp.l = l;
    ((uint32_t*)p)[0] = tmp.v[0];
    ((uint32_t*)p)[1] = tmp.v[1];
  }
};
#endif /* ALIGN_CONVERTER */
