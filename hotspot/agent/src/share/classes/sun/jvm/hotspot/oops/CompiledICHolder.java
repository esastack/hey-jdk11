/*
 * Copyright 2000-2001 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class CompiledICHolder extends Oop {
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }

  private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
    Type type    = db.lookupType("compiledICHolderOopDesc");
    holderMethod = new OopField(type.getOopField("_holder_method"), 0);
    holderKlass  = new OopField(type.getOopField("_holder_klass"), 0);
    headerSize   = type.getSize();
  }

  CompiledICHolder(OopHandle handle, ObjectHeap heap) {
    super(handle, heap);
  }

  public boolean isCompiledICHolder()  { return true; }

  private static long headerSize;

  // Fields
  private static OopField holderMethod;
  private static OopField holderKlass;

  // Accessors for declared fields
  public Method getHolderMethod() { return (Method) holderMethod.getValue(this); }
  public Klass  getHolderKlass()  { return (Klass)  holderKlass.getValue(this); }

  public void printValueOn(PrintStream tty) {
    tty.print("CompiledICHolder");
  }

  public long getObjectSize() {
    return alignObjectSize(headerSize);
  }

  void iterateFields(OopVisitor visitor, boolean doVMFields) {
    super.iterateFields(visitor, doVMFields);
    if (doVMFields) {
      visitor.doOop(holderMethod, true);
      visitor.doOop(holderKlass, true);
    }
  }
}
