/*
 * Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.code;

import java.util.*;

import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class VMRegImpl {

  private static VMReg stack0;
  private static int stack0Val;
  private static Address stack0Addr;
  private static AddressField regNameField;

  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }

  private static void initialize(TypeDataBase db) {
    Type type = db.lookupType("VMRegImpl");
    AddressField stack0Field = type.getAddressField("stack0");
    stack0Addr = stack0Field.getValue();
    stack0Val = (int) stack0Addr.hashCode();
    stack0 = new VMReg(stack0Val);
    regNameField = type.getAddressField("regName[0]");
  }

  public static VMReg getStack0() {
    return stack0;
  }

  public static String getRegisterName(int index) {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(index >= 0 && index < stack0Val, "invalid index : " + index);
    }
    Address regName = regNameField.getStaticFieldAddress();
    long addrSize = VM.getVM().getAddressSize();
    return CStringUtilities.getString(regName.getAddressAt(index * addrSize));
  }
}
