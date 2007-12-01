/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.asm.x86;

import sun.jvm.hotspot.asm.*;

public class X86InstructionFactoryImpl implements X86InstructionFactory {

   public X86Instruction newCallInstruction(String name, Address addr, int size, int prefixes) {
      return new X86CallInstruction(name, addr, size, prefixes);
   }

   public X86Instruction newJmpInstruction(String name, Address addr, int size, int prefixes) {
      return new X86JmpInstruction(name, addr, size, prefixes);
   }

   public X86Instruction newCondJmpInstruction(String name, X86PCRelativeAddress addr, int size, int prefixes) {
      return new X86CondJmpInstruction(name, addr, size, prefixes);
   }

   public X86Instruction newMoveInstruction(String name, X86Register rd, ImmediateOrRegister oSrc, int size, int prefixes) {
      return new X86MoveInstruction(name, rd, oSrc, size, prefixes);
   }

   public X86Instruction newMoveLoadInstruction(String name, X86Register op1, Address op2, int dataType, int size, int prefixes) {
      return new X86MoveLoadInstruction(name, op1, op2, dataType, size, prefixes);
   }

   public X86Instruction newMoveStoreInstruction(String name, Address op1, X86Register op2, int dataType, int size, int prefixes) {
      return new X86MoveStoreInstruction(name, op1, op2, dataType, size, prefixes);
   }

   public X86Instruction newArithmeticInstruction(String name, int rtlOperation, Operand op1, Operand op2,  Operand op3, int size, int prefixes) {
      return new X86ArithmeticInstruction(name, rtlOperation, op1, op2, op3, size, prefixes);
   }

   public X86Instruction newArithmeticInstruction(String name, int rtlOperation, Operand op1, Operand op2, int size, int prefixes) {
      return new X86ArithmeticInstruction(name, rtlOperation, op1, op2, size, prefixes);
   }


   public X86Instruction newLogicInstruction(String name, int rtlOperation, Operand op1, Operand op2, int size, int prefixes) {
      return new X86LogicInstruction(name, rtlOperation, op1, op2, size, prefixes);
   }

   public X86Instruction newBranchInstruction(String name, X86PCRelativeAddress addr, int size, int prefixes) {
      return new X86BranchInstruction(name, addr, size, prefixes);
   }

   public X86Instruction newShiftInstruction(String name, int rtlOperation, Operand op1, ImmediateOrRegister op2, int size, int prefixes) {
      return new X86ShiftInstruction(name, rtlOperation, op1, op2, size, prefixes);
   }

   public X86Instruction newRotateInstruction(String name, Operand op1, ImmediateOrRegister op2, int size, int prefixes) {
      return new X86RotateInstruction(name, op1, op2, size, prefixes);
   }

   public X86Instruction newFPLoadInstruction(String name, Operand op, int size, int prefixes) {
      return new X86FPLoadInstruction(name, op, size, prefixes);
   }

   public X86Instruction newFPStoreInstruction(String name, Operand op, int size, int prefixes) {
      return new X86FPStoreInstruction(name, op, size, prefixes);
   }

   public X86Instruction newFPArithmeticInstruction(String name, int rtlOperation, Operand op1, Operand op2, int size, int prefixes) {
      return new X86FPArithmeticInstruction(name, rtlOperation, op1, op2, size, prefixes);
   }

   public X86Instruction newGeneralInstruction(String name, Operand op1, Operand op2, Operand op3, int size, int prefixes) {
      return new X86GeneralInstruction(name, op1, op2, op3, size, prefixes);
   }

   public X86Instruction newGeneralInstruction(String name, Operand op1, Operand op2, int size, int prefixes) {
      return new X86GeneralInstruction(name, op1, op2, size, prefixes);
   }

   public X86Instruction newGeneralInstruction(String name, Operand op1, int size, int prefixes) {
      return new X86GeneralInstruction(name, op1, size, prefixes);
   }

   public X86Instruction newIllegalInstruction() {
      return new X86IllegalInstruction();
   }

}
