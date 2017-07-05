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

package sun.jvm.hotspot.interpreter;

import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.utilities.*;

public class BytecodeFastIAccess0 extends BytecodeGetPut {
  BytecodeFastIAccess0(Method method, int bci) {
    super(method, bci);
  }

  public int index() {
    return (int) (0xFF & javaShortAt(2));
  }

  public boolean isStatic() {
    return false;
  }

  public void verify() {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(isValid(), "check fast_iaccess_0");
    }
  }

  public boolean isValid() {
    return code() == Bytecodes._fast_iaccess_0;
  }

  public static BytecodeFastIAccess0 at(Method method, int bci) {
    BytecodeFastIAccess0 b = new BytecodeFastIAccess0(method, bci);
    if (Assert.ASSERTS_ENABLED) {
      b.verify();
    }
    return b;
  }

  /** Like at, but returns null if the BCI is not at fast_iaccess_0  */
  public static BytecodeFastIAccess0 atCheck(Method method, int bci) {
    BytecodeFastIAccess0 b = new BytecodeFastIAccess0(method, bci);
    return (b.isValid() ? b : null);
  }

  public static BytecodeFastIAccess0 at(BytecodeStream bcs) {
    return new BytecodeFastIAccess0(bcs.method(), bcs.bci());
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("aload_0");
    buf.append(spaces);
    buf.append(super.toString());
    return buf.toString();
  }
}
