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

package sun.jvm.hotspot.asm.sparc;

import sun.jvm.hotspot.asm.*;
import sun.jvm.hotspot.utilities.Assert;

class FP2RegisterDecoder extends FloatDecoder {

    FP2RegisterDecoder(int opf, String name, int srcType, int resultType) {
        super(opf, name, srcType, resultType);
    }

    Instruction decodeFloatInstruction(int instruction,
                                       SPARCRegister rs1, SPARCRegister rs2,
                                       SPARCRegister rd,
                                       SPARCInstructionFactory factory) {
        if (Assert.ASSERTS_ENABLED)
            Assert.that(rs2.isFloat() && rd.isFloat(), "rs2, rd have to be float registers");

        return factory.newFP2RegisterInstruction(name, opf, (SPARCFloatRegister)rs2, (SPARCFloatRegister)rd);
    }
}
