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

package sun.jvm.hotspot.gc_implementation.parallelScavenge;

import java.io.*;
import java.util.*;

import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.gc_implementation.shared.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class PSOldGen extends VMObject {
   static {
      VM.registerVMInitializedObserver(new Observer() {
         public void update(Observable o, Object data) {
            initialize(VM.getVM().getTypeDataBase());
         }
      });
   }

   private static synchronized void initialize(TypeDataBase db) {
      Type type = db.lookupType("PSOldGen");
      objectSpaceField = type.getAddressField("_object_space");
   }

   public PSOldGen(Address addr) {
      super(addr);
   }

   // Fields
   private static AddressField objectSpaceField;

   // Accessors
   public MutableSpace objectSpace() {
      return (MutableSpace) VMObjectFactory.newObject(MutableSpace.class, objectSpaceField.getValue(addr));
   }

   public long capacity() {
      return objectSpace().capacity();
   }

   public long used() {
      return objectSpace().used();
   }

   public boolean isIn(Address a) {
      return objectSpace().contains(a);
   }

   public void printOn(PrintStream tty) {
      tty.print("PSOldGen [ ");
      objectSpace().printOn(tty);
      tty.print(" ] ");
   }
}
