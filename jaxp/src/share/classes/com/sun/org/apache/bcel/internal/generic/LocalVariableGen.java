/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
package com.sun.org.apache.bcel.internal.generic;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.*;

/**
 * This class represents a local variable within a method. It contains its
 * scope, name and type. The generated LocalVariable object can be obtained
 * with getLocalVariable which needs the instruction list and the constant
 * pool as parameters.
 *
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see     LocalVariable
 * @see     MethodGen
 */
public class LocalVariableGen
  implements InstructionTargeter, NamedAndTyped, Cloneable,
             java.io.Serializable
{
  private int         index;
  private String      name;
  private Type        type;
  private InstructionHandle start, end;

  /**
   * Generate a local variable that with index `index'. Note that double and long
   * variables need two indexs. Index indices have to be provided by the user.
   *
   * @param index index of local variable
   * @param name its name
   * @param type its type
   * @param start from where the instruction is valid (null means from the start)
   * @param end until where the instruction is valid (null means to the end)
   */
  public LocalVariableGen(int index, String name, Type type,
                          InstructionHandle start, InstructionHandle end) {
    if((index < 0) || (index > Constants.MAX_SHORT))
      throw new ClassGenException("Invalid index index: " + index);

    this.name  = name;
    this.type  = type;
    this.index  = index;
    setStart(start);
    setEnd(end);
  }

  /**
   * Get LocalVariable object.
   *
   * This relies on that the instruction list has already been dumped to byte code or
   * or that the `setPositions' methods has been called for the instruction list.
   *
   * Note that for local variables whose scope end at the last
   * instruction of the method's code, the JVM specification is ambiguous:
   * both a start_pc+length ending at the last instruction and
   * start_pc+length ending at first index beyond the end of the code are
   * valid.
   *
   * @param il instruction list (byte code) which this variable belongs to
   * @param cp constant pool
   */
  public LocalVariable getLocalVariable(ConstantPoolGen cp) {
    int start_pc        = start.getPosition();
    int length          = end.getPosition() - start_pc;

    if(length > 0)
      length += end.getInstruction().getLength();

    int name_index      = cp.addUtf8(name);
    int signature_index = cp.addUtf8(type.getSignature());

    return new LocalVariable(start_pc, length, name_index,
                             signature_index, index, cp.getConstantPool());
  }

  public void        setIndex(int index)           { this.index = index; }
  public int         getIndex()                   { return index; }
  public void        setName(String name)        { this.name = name; }
  public String      getName()                   { return name; }
  public void        setType(Type type)          { this.type = type; }
  public Type        getType()                   { return type; }

  public InstructionHandle getStart()                  { return start; }
  public InstructionHandle getEnd()                    { return end; }

  public void setStart(InstructionHandle start) {
    BranchInstruction.notifyTarget(this.start, start, this);
    this.start = start;
  }

  public void setEnd(InstructionHandle end) {
    BranchInstruction.notifyTarget(this.end, end, this);
    this.end = end;
  }

  /**
   * @param old_ih old target, either start or end
   * @param new_ih new target
   */
  public void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih) {
    boolean targeted = false;

    if(start == old_ih) {
      targeted = true;
      setStart(new_ih);
    }

    if(end == old_ih) {
      targeted = true;
      setEnd(new_ih);
    }

    if(!targeted)
      throw new ClassGenException("Not targeting " + old_ih + ", but {" + start + ", " +
                                  end + "}");
  }

  /**
   * @return true, if ih is target of this variable
   */
  public boolean containsTarget(InstructionHandle ih) {
    return (start == ih) || (end == ih);
  }

  /**
   * We consider to local variables to be equal, if the use the same index and
   * are valid in the same range.
   */
  public boolean equals(Object o) {
    if(!(o instanceof LocalVariableGen))
      return false;

    LocalVariableGen l = (LocalVariableGen)o;
    return (l.index == index) && (l.start == start) && (l.end == end);
  }

  public String toString() {
    return "LocalVariableGen(" + name +  ", " + type +  ", " + start + ", " + end + ")";
  }

  public Object clone() {
    try {
      return super.clone();
    } catch(CloneNotSupportedException e) {
      System.err.println(e);
      return null;
    }
  }
}
