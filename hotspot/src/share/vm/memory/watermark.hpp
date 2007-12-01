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

// A water mark points into a space and is used during GC to keep track of
// progress.

class Space;

class WaterMark VALUE_OBJ_CLASS_SPEC {
  friend class VMStructs;
 private:
  HeapWord* _point;
  Space*    _space;
 public:
  // Accessors
  Space* space() const        { return _space;  }
  void set_space(Space* s)    { _space = s;     }
  HeapWord* point() const     { return _point;  }
  void set_point(HeapWord* p) { _point = p;     }

  // Constructors
  WaterMark(Space* s, HeapWord* p) : _space(s), _point(p) {};
  WaterMark() : _space(NULL), _point(NULL) {};
};

inline bool operator==(const WaterMark& x, const WaterMark& y) {
  return (x.point() == y.point()) && (x.space() == y.space());
}

inline bool operator!=(const WaterMark& x, const WaterMark& y) {
  return !(x == y);
}
