/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * NASHORN-89 :  Date functions and constructor should evaluate vararg arguments in the order of appearance.
 *
 * @test
 * @run
 */

function MyObj(val) {
    return {
        valueOf: function() { throw "value-" + val; }
    }
}

try {
    var d = new Date(1980, new MyObj(2), new MyObj(3), new MyObj(4));
} catch(e) {
    if (e != 'value-2') {
        fail("expecting 'value-2' got " + e);
    }
}

try {
    var d = new Date(1980, 2, new MyObj(3), new MyObj(4), new MyObj(5));
} catch(e) {
    if (e != 'value-3') {
        fail("expecting 'value-3' got " + e);
    }
}

try {
    var d = new Date(1980, 2, 1, new MyObj(4), new MyObj(5), new MyObj(6));
} catch(e) {
    if (e != 'value-4') {
        fail("expecting 'value-4' got " + e);
    }
}

try {
    var d = new Date(1980, 2, 1, 1, new MyObj(5), new MyObj(6), new MyObj(7));
} catch(e) {
    if (e != 'value-5') {
        fail("expecting 'value-5' got " + e);
    }
}
try {
    var d = new Date(1980, 2, 1, 1, 1, new MyObj(6), new MyObj(7));
} catch(e) {
    if (e != 'value-6') {
        fail("expecting 'value-6' got " + e);
    }
}
try {
    var d = new Date(1980, 2, 1, 1, 1, 1, new MyObj(7));
} catch(e) {
    if (e != 'value-7') {
        fail("expecting 'value-7' got " + e);
    }
}
