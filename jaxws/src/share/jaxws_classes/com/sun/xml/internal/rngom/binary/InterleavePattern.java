/*
 * Copyright (c) 2005, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * Copyright (C) 2004-2011
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sun.xml.internal.rngom.binary;

import com.sun.xml.internal.rngom.binary.visitor.PatternFunction;
import com.sun.xml.internal.rngom.binary.visitor.PatternVisitor;

public class InterleavePattern extends BinaryPattern {
  InterleavePattern(Pattern p1, Pattern p2) {
    super(p1.isNullable() && p2.isNullable(),
          combineHashCode(INTERLEAVE_HASH_CODE, p1.hashCode(), p2.hashCode()),
          p1,
          p2);
  }
    @Override
  Pattern expand(SchemaPatternBuilder b) {
    Pattern ep1 = p1.expand(b);
    Pattern ep2 = p2.expand(b);
    if (ep1 != p1 || ep2 != p2)
      return b.makeInterleave(ep1, ep2);
    else
      return this;
  }
    @Override
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_interleave");
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_interleave");
    case LIST_CONTEXT:
      throw new RestrictionViolationException("list_contains_interleave");
    }
    if (context == ELEMENT_REPEAT_CONTEXT)
      context = ELEMENT_REPEAT_INTERLEAVE_CONTEXT;
    Alphabet a1;
    if (alpha != null && alpha.isEmpty())
      a1 = alpha;
    else
      a1 = new Alphabet();
    p1.checkRestrictions(context, dad, a1);
    if (a1.isEmpty())
      p2.checkRestrictions(context, dad, a1);
    else {
      Alphabet a2 = new Alphabet();
      p2.checkRestrictions(context, dad, a2);
      a1.checkOverlap(a2);
      if (alpha != null) {
        if (alpha != a1)
          alpha.addAlphabet(a1);
        alpha.addAlphabet(a2);
      }
    }
    if (context != LIST_CONTEXT
        && !contentTypeGroupable(p1.getContentType(), p2.getContentType()))
      throw new RestrictionViolationException("interleave_string");
    if (p1.getContentType() == MIXED_CONTENT_TYPE
        && p2.getContentType() == MIXED_CONTENT_TYPE)
      throw new RestrictionViolationException("interleave_text_overlap");
  }

  public void accept(PatternVisitor visitor) {
    visitor.visitInterleave(p1, p2);
  }
  public Object apply(PatternFunction f) {
    return f.caseInterleave(this);
  }
}
