/*
 * Copyright 2002-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.interpreter;

import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.utilities.*;

public class BytecodeLoadConstant extends BytecodeWithCPIndex {
  BytecodeLoadConstant(Method method, int bci) {
    super(method, bci);
  }

  public int index() {
    return javaCode() == Bytecodes._ldc ?
                 (int) (0xFF & javaByteAt(1))
               : (int) (0xFFFF & javaShortAt(1));
  }

  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check load constant");
    }
  }

  public boolean isValid() {
    int jcode = javaCode();
    boolean codeOk = jcode == Bytecodes._ldc || jcode == Bytecodes._ldc_w ||
           jcode == Bytecodes._ldc2_w;
    if (! codeOk) return false;

    ConstantTag ctag = method().getConstants().getTagAt(index());
    if (jcode == Bytecodes._ldc2_w) {
       // has to be double or long
       return (ctag.isDouble() || ctag.isLong()) ? true: false;
    } else {
       // has to be int or float or String or Klass
       return (ctag.isUnresolvedString() || ctag.isString()
               || ctag.isUnresolvedKlass() || ctag.isKlass()
               || ctag.isInt() || ctag.isFloat())? true: false;
    }
  }

  public boolean isKlassConstant() {
    int jcode = javaCode();
    if (jcode == Bytecodes._ldc2_w) {
       return false;
    }

    ConstantTag ctag = method().getConstants().getTagAt(index());
    return ctag.isKlass() || ctag.isUnresolvedKlass();
  }

  // return Symbol (if unresolved) or Klass (if resolved)
  public Oop getKlass() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isKlassConstant(), "not a klass literal");
    }
    // tag change from 'unresolved' to 'klass' does not happen atomically.
    // We just look at the object at the corresponding index and
    // decide based on the oop type.
    ConstantPool cpool = method().getConstants();
    int cpIndex = index();
    Oop oop = cpool.getObjAt(cpIndex);
    if (oop.isKlass()) {
       return (Klass) oop;
    } else if (oop.isSymbol()) {
       return (Symbol) oop;
    } else {
       throw new RuntimeException("should not reach here");
    }
  }

  public static BytecodeLoadConstant at(Method method, int bci) {
    BytecodeLoadConstant b = new BytecodeLoadConstant(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }

  /** Like at, but returns null if the BCI is not at ldc or ldc_w or ldc2_w  */
  public static BytecodeLoadConstant atCheck(Method method, int bci) {
    BytecodeLoadConstant b = new BytecodeLoadConstant(method, bci);
    return (b.isValid() ? b : null);
  }

  public static BytecodeLoadConstant at(BytecodeStream bcs) {
    return new BytecodeLoadConstant(bcs.method(), bcs.bci());
  }

  public String getConstantValue() {
    ConstantPool cpool = method().getConstants();
    int cpIndex = index();
    ConstantTag ctag = cpool.getTagAt(cpIndex);
    if (ctag.isInt()) {
       return "<int " + Integer.toString(cpool.getIntAt(cpIndex)) +">";
    } else if (ctag.isLong()) {
       return "<long " + Long.toString(cpool.getLongAt(cpIndex)) + "L>";
    } else if (ctag.isFloat()) {
       return "<float " + Float.toString(cpool.getFloatAt(cpIndex)) + "F>";
    } else if (ctag.isDouble()) {
       return "<double " + Double.toString(cpool.getDoubleAt(cpIndex)) + "D>";
    } else if (ctag.isString() || ctag.isUnresolvedString()) {
       // tag change from 'unresolved' to 'string' does not happen atomically.
       // We just look at the object at the corresponding index and
       // decide based on the oop type.
       Oop obj = cpool.getObjAt(cpIndex);
       if (obj.isSymbol()) {
          Symbol sym = (Symbol) obj;
          return "<String \"" + sym.asString() + "\">";
       } else if (obj.isInstance()) {
          return "<String \"" + OopUtilities.stringOopToString(obj) + "\">";
       } else {
          throw new RuntimeException("should not reach here");
       }
    } else if (ctag.isKlass() || ctag.isUnresolvedKlass()) {
       // tag change from 'unresolved' to 'klass' does not happen atomically.
       // We just look at the object at the corresponding index and
       // decide based on the oop type.
       Oop obj = cpool.getObjAt(cpIndex);
       if (obj.isKlass()) {
          Klass k = (Klass) obj;
          return "<Class " + k.getName().asString() + "@" + k.getHandle() + ">";
       } else if (obj.isSymbol()) {
          Symbol sym = (Symbol) obj;
          return "<Class " + sym.asString() + ">";
       } else {
          throw new RuntimeException("should not reach here");
       }
    } else {
       if (Assert.ASSERTS_ENABLED) {
         Assert.that(false, "invalid load constant type");
       }
       return null;
    }
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(getJavaBytecodeName());
    buf.append(spaces);
    buf.append('#');
    buf.append(Integer.toString(index()));
    buf.append(spaces);
    buf.append(getConstantValue());
    if (code() != javaCode()) {
       buf.append(spaces);
       buf.append('[');
       buf.append(getBytecodeName());
       buf.append(']');
    }
    return buf.toString();
  }
}
