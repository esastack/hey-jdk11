/*
 * Copyright 2003-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

# include "incls/_precompiled.incl"
# include "incls/_jvmtiUtil.cpp.incl"
//
// class JvmtiUtil
//

ResourceArea* JvmtiUtil::_single_threaded_resource_area = NULL;

ResourceArea* JvmtiUtil::single_threaded_resource_area() {
  if (_single_threaded_resource_area == NULL) {
    // lazily create the single threaded resource area
    // pick a size which is not a standard since the pools don't exist yet
    _single_threaded_resource_area = new ResourceArea(Chunk::non_pool_size);
  }
  return _single_threaded_resource_area;
}
