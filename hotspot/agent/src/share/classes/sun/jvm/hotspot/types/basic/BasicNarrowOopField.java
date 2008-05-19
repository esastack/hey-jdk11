/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.jvm.hotspot.types.basic;

import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;

/** A specialization of BasicField which represents a field containing
    an oop value and which adds typechecked getValue() routines
    returning OopHandles. */

public class BasicNarrowOopField extends BasicOopField implements NarrowOopField {

  private static final boolean DEBUG = false;

  public BasicNarrowOopField (OopField oopf) {
    super(oopf);
  }

  public BasicNarrowOopField(BasicTypeDataBase db, Type containingType, String name, Type type,
                       boolean isStatic, long offset, Address staticFieldAddress) {
    super(db, containingType, name, type, isStatic, offset, staticFieldAddress);

    if (DEBUG) {
      System.out.println(" name " + name + " type " + type + " isStatic " + isStatic + " offset " + offset + " static addr " + staticFieldAddress);
    }
    if (!type.isOopType()) {
      throw new WrongTypeException("Type of a BasicOopField must be an oop type");
    }
  }

  /** The field must be nonstatic and the type of the field must be a
      Java oop, or a WrongTypeException will be thrown. */
  public OopHandle getValue(Address addr) throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getNarrowOopHandle(addr);
  }

  /** The field must be static and the type of the field must be a
      Java oop, or a WrongTypeException will be thrown. */
  public OopHandle getValue() throws UnmappedAddressException, UnalignedAddressException, WrongTypeException {
    return getNarrowOopHandle();
  }
}
