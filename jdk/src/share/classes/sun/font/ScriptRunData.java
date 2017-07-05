/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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

/*
 *******************************************************************************
 * Copyright (C) 2003, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package sun.font;

public final class ScriptRunData {
    private ScriptRunData() {}

    private static final int CHAR_START = 0;
    private static final int CHAR_LIMIT = 0x110000;

    private static int cache = 0;
    public static final int getScript(int cp) {
        // optimize for runs of characters in the same script
        if (cp >= data[cache] && cp < data[cache+2]) {
            return data[cache+1];
        }
        if (cp >= CHAR_START & cp < CHAR_LIMIT) {
            int probe = dataPower;
            int index = 0;

            if (cp >= data[dataExtra]) {
                index = dataExtra;
            }

            while (probe > 2) {
                probe >>= 1;
                if (cp >= data[index + probe]) {
                    index += probe;
                }
            }

            cache = index;
            return data[index+1];
        }

        throw new IllegalArgumentException(Integer.toString(cp));
    }

    private static final int[] data = {
        0x000000, 0x00,
        0x000041, 0x19, // 'latn' latin
        0x00005B, 0x00,
        0x000061, 0x19, // 'latn' latin
        0x00007B, 0x00,
        0x0000AA, 0x19, // 'latn' latin
        0x0000AB, 0x00,
        0x0000B5, 0x0E, // 'grek' greek
        0x0000B6, 0x00,
        0x0000BA, 0x19, // 'latn' latin
        0x0000BB, 0x00,
        0x0000C0, 0x19, // 'latn' latin
        0x0000D7, 0x00,
        0x0000D8, 0x19, // 'latn' latin
        0x0000F7, 0x00,
        0x0000F8, 0x19, // 'latn' latin
        0x000221, 0x00,
        0x000222, 0x19, // 'latn' latin
        0x000234, 0x00,
        0x000250, 0x19, // 'latn' latin
        0x0002AE, 0x00,
        0x0002B0, 0x19, // 'latn' latin
        0x0002B9, 0x00,
        0x0002E0, 0x19, // 'latn' latin
        0x0002E5, 0x00,
        0x000300, 0x01, // 'qaai' inherited
        0x000350, 0x00,
        0x000360, 0x01, // 'qaai' inherited
        0x000370, 0x00,
        0x00037A, 0x0E, // 'grek' greek
        0x00037B, 0x00,
        0x000386, 0x0E, // 'grek' greek
        0x000387, 0x00,
        0x000388, 0x0E, // 'grek' greek
        0x00038B, 0x00,
        0x00038C, 0x0E, // 'grek' greek
        0x00038D, 0x00,
        0x00038E, 0x0E, // 'grek' greek
        0x0003A2, 0x00,
        0x0003A3, 0x0E, // 'grek' greek
        0x0003CF, 0x00,
        0x0003D0, 0x0E, // 'grek' greek
        0x0003F6, 0x00,
        0x000400, 0x08, // 'cyrl' cyrillic
        0x000482, 0x00,
        0x000483, 0x08, // 'cyrl' cyrillic
        0x000487, 0x00,
        0x000488, 0x01, // 'qaai' inherited
        0x00048A, 0x08, // 'cyrl' cyrillic
        0x0004CF, 0x00,
        0x0004D0, 0x08, // 'cyrl' cyrillic
        0x0004F6, 0x00,
        0x0004F8, 0x08, // 'cyrl' cyrillic
        0x0004FA, 0x00,
        0x000500, 0x08, // 'cyrl' cyrillic
        0x000510, 0x00,
        0x000531, 0x03, // 'armn' armenian
        0x000557, 0x00,
        0x000559, 0x03, // 'armn' armenian
        0x00055A, 0x00,
        0x000561, 0x03, // 'armn' armenian
        0x000588, 0x00,
        0x000591, 0x01, // 'qaai' inherited
        0x0005A2, 0x00,
        0x0005A3, 0x01, // 'qaai' inherited
        0x0005BA, 0x00,
        0x0005BB, 0x01, // 'qaai' inherited
        0x0005BE, 0x00,
        0x0005BF, 0x01, // 'qaai' inherited
        0x0005C0, 0x00,
        0x0005C1, 0x01, // 'qaai' inherited
        0x0005C3, 0x00,
        0x0005C4, 0x01, // 'qaai' inherited
        0x0005C5, 0x00,
        0x0005D0, 0x13, // 'hebr' hebrew
        0x0005EB, 0x00,
        0x0005F0, 0x13, // 'hebr' hebrew
        0x0005F3, 0x00,
        0x000621, 0x02, // 'arab' arabic
        0x00063B, 0x00,
        0x000641, 0x02, // 'arab' arabic
        0x00064B, 0x01, // 'qaai' inherited
        0x000656, 0x00,
        0x00066E, 0x02, // 'arab' arabic
        0x000670, 0x01, // 'qaai' inherited
        0x000671, 0x02, // 'arab' arabic
        0x0006D4, 0x00,
        0x0006D5, 0x02, // 'arab' arabic
        0x0006D6, 0x01, // 'qaai' inherited
        0x0006E5, 0x02, // 'arab' arabic
        0x0006E7, 0x01, // 'qaai' inherited
        0x0006E9, 0x00,
        0x0006EA, 0x01, // 'qaai' inherited
        0x0006EE, 0x00,
        0x0006FA, 0x02, // 'arab' arabic
        0x0006FD, 0x00,
        0x000710, 0x22, // 'syrc' syriac
        0x00072D, 0x00,
        0x000730, 0x22, // 'syrc' syriac
        0x00074B, 0x00,
        0x000780, 0x25, // 'thaa' thaana
        0x0007B2, 0x00,
        0x000901, 0x0A, // 'deva' devanagari
        0x000904, 0x00,
        0x000905, 0x0A, // 'deva' devanagari
        0x00093A, 0x00,
        0x00093C, 0x0A, // 'deva' devanagari
        0x00094E, 0x00,
        0x000950, 0x0A, // 'deva' devanagari
        0x000955, 0x00,
        0x000958, 0x0A, // 'deva' devanagari
        0x000964, 0x00,
        0x000966, 0x0A, // 'deva' devanagari
        0x000970, 0x00,
        0x000981, 0x04, // 'beng' bengali
        0x000984, 0x00,
        0x000985, 0x04, // 'beng' bengali
        0x00098D, 0x00,
        0x00098F, 0x04, // 'beng' bengali
        0x000991, 0x00,
        0x000993, 0x04, // 'beng' bengali
        0x0009A9, 0x00,
        0x0009AA, 0x04, // 'beng' bengali
        0x0009B1, 0x00,
        0x0009B2, 0x04, // 'beng' bengali
        0x0009B3, 0x00,
        0x0009B6, 0x04, // 'beng' bengali
        0x0009BA, 0x00,
        0x0009BC, 0x04, // 'beng' bengali
        0x0009BD, 0x00,
        0x0009BE, 0x04, // 'beng' bengali
        0x0009C5, 0x00,
        0x0009C7, 0x04, // 'beng' bengali
        0x0009C9, 0x00,
        0x0009CB, 0x04, // 'beng' bengali
        0x0009CE, 0x00,
        0x0009D7, 0x04, // 'beng' bengali
        0x0009D8, 0x00,
        0x0009DC, 0x04, // 'beng' bengali
        0x0009DE, 0x00,
        0x0009DF, 0x04, // 'beng' bengali
        0x0009E4, 0x00,
        0x0009E6, 0x04, // 'beng' bengali
        0x0009F2, 0x00,
        0x000A02, 0x10, // 'guru' gurmukhi
        0x000A03, 0x00,
        0x000A05, 0x10, // 'guru' gurmukhi
        0x000A0B, 0x00,
        0x000A0F, 0x10, // 'guru' gurmukhi
        0x000A11, 0x00,
        0x000A13, 0x10, // 'guru' gurmukhi
        0x000A29, 0x00,
        0x000A2A, 0x10, // 'guru' gurmukhi
        0x000A31, 0x00,
        0x000A32, 0x10, // 'guru' gurmukhi
        0x000A34, 0x00,
        0x000A35, 0x10, // 'guru' gurmukhi
        0x000A37, 0x00,
        0x000A38, 0x10, // 'guru' gurmukhi
        0x000A3A, 0x00,
        0x000A3C, 0x10, // 'guru' gurmukhi
        0x000A3D, 0x00,
        0x000A3E, 0x10, // 'guru' gurmukhi
        0x000A43, 0x00,
        0x000A47, 0x10, // 'guru' gurmukhi
        0x000A49, 0x00,
        0x000A4B, 0x10, // 'guru' gurmukhi
        0x000A4E, 0x00,
        0x000A59, 0x10, // 'guru' gurmukhi
        0x000A5D, 0x00,
        0x000A5E, 0x10, // 'guru' gurmukhi
        0x000A5F, 0x00,
        0x000A66, 0x10, // 'guru' gurmukhi
        0x000A75, 0x00,
        0x000A81, 0x0F, // 'gujr' gujarati
        0x000A84, 0x00,
        0x000A85, 0x0F, // 'gujr' gujarati
        0x000A8C, 0x00,
        0x000A8D, 0x0F, // 'gujr' gujarati
        0x000A8E, 0x00,
        0x000A8F, 0x0F, // 'gujr' gujarati
        0x000A92, 0x00,
        0x000A93, 0x0F, // 'gujr' gujarati
        0x000AA9, 0x00,
        0x000AAA, 0x0F, // 'gujr' gujarati
        0x000AB1, 0x00,
        0x000AB2, 0x0F, // 'gujr' gujarati
        0x000AB4, 0x00,
        0x000AB5, 0x0F, // 'gujr' gujarati
        0x000ABA, 0x00,
        0x000ABC, 0x0F, // 'gujr' gujarati
        0x000AC6, 0x00,
        0x000AC7, 0x0F, // 'gujr' gujarati
        0x000ACA, 0x00,
        0x000ACB, 0x0F, // 'gujr' gujarati
        0x000ACE, 0x00,
        0x000AD0, 0x0F, // 'gujr' gujarati
        0x000AD1, 0x00,
        0x000AE0, 0x0F, // 'gujr' gujarati
        0x000AE1, 0x00,
        0x000AE6, 0x0F, // 'gujr' gujarati
        0x000AF0, 0x00,
        0x000B01, 0x1F, // 'orya' oriya
        0x000B04, 0x00,
        0x000B05, 0x1F, // 'orya' oriya
        0x000B0D, 0x00,
        0x000B0F, 0x1F, // 'orya' oriya
        0x000B11, 0x00,
        0x000B13, 0x1F, // 'orya' oriya
        0x000B29, 0x00,
        0x000B2A, 0x1F, // 'orya' oriya
        0x000B31, 0x00,
        0x000B32, 0x1F, // 'orya' oriya
        0x000B34, 0x00,
        0x000B36, 0x1F, // 'orya' oriya
        0x000B3A, 0x00,
        0x000B3C, 0x1F, // 'orya' oriya
        0x000B44, 0x00,
        0x000B47, 0x1F, // 'orya' oriya
        0x000B49, 0x00,
        0x000B4B, 0x1F, // 'orya' oriya
        0x000B4E, 0x00,
        0x000B56, 0x1F, // 'orya' oriya
        0x000B58, 0x00,
        0x000B5C, 0x1F, // 'orya' oriya
        0x000B5E, 0x00,
        0x000B5F, 0x1F, // 'orya' oriya
        0x000B62, 0x00,
        0x000B66, 0x1F, // 'orya' oriya
        0x000B70, 0x00,
        0x000B82, 0x23, // 'taml' tamil
        0x000B84, 0x00,
        0x000B85, 0x23, // 'taml' tamil
        0x000B8B, 0x00,
        0x000B8E, 0x23, // 'taml' tamil
        0x000B91, 0x00,
        0x000B92, 0x23, // 'taml' tamil
        0x000B96, 0x00,
        0x000B99, 0x23, // 'taml' tamil
        0x000B9B, 0x00,
        0x000B9C, 0x23, // 'taml' tamil
        0x000B9D, 0x00,
        0x000B9E, 0x23, // 'taml' tamil
        0x000BA0, 0x00,
        0x000BA3, 0x23, // 'taml' tamil
        0x000BA5, 0x00,
        0x000BA8, 0x23, // 'taml' tamil
        0x000BAB, 0x00,
        0x000BAE, 0x23, // 'taml' tamil
        0x000BB6, 0x00,
        0x000BB7, 0x23, // 'taml' tamil
        0x000BBA, 0x00,
        0x000BBE, 0x23, // 'taml' tamil
        0x000BC3, 0x00,
        0x000BC6, 0x23, // 'taml' tamil
        0x000BC9, 0x00,
        0x000BCA, 0x23, // 'taml' tamil
        0x000BCE, 0x00,
        0x000BD7, 0x23, // 'taml' tamil
        0x000BD8, 0x00,
        0x000BE7, 0x23, // 'taml' tamil
        0x000BF3, 0x00,
        0x000C01, 0x24, // 'telu' telugu
        0x000C04, 0x00,
        0x000C05, 0x24, // 'telu' telugu
        0x000C0D, 0x00,
        0x000C0E, 0x24, // 'telu' telugu
        0x000C11, 0x00,
        0x000C12, 0x24, // 'telu' telugu
        0x000C29, 0x00,
        0x000C2A, 0x24, // 'telu' telugu
        0x000C34, 0x00,
        0x000C35, 0x24, // 'telu' telugu
        0x000C3A, 0x00,
        0x000C3E, 0x24, // 'telu' telugu
        0x000C45, 0x00,
        0x000C46, 0x24, // 'telu' telugu
        0x000C49, 0x00,
        0x000C4A, 0x24, // 'telu' telugu
        0x000C4E, 0x00,
        0x000C55, 0x24, // 'telu' telugu
        0x000C57, 0x00,
        0x000C60, 0x24, // 'telu' telugu
        0x000C62, 0x00,
        0x000C66, 0x24, // 'telu' telugu
        0x000C70, 0x00,
        0x000C82, 0x15, // 'knda' kannada
        0x000C84, 0x00,
        0x000C85, 0x15, // 'knda' kannada
        0x000C8D, 0x00,
        0x000C8E, 0x15, // 'knda' kannada
        0x000C91, 0x00,
        0x000C92, 0x15, // 'knda' kannada
        0x000CA9, 0x00,
        0x000CAA, 0x15, // 'knda' kannada
        0x000CB4, 0x00,
        0x000CB5, 0x15, // 'knda' kannada
        0x000CBA, 0x00,
        0x000CBE, 0x15, // 'knda' kannada
        0x000CC5, 0x00,
        0x000CC6, 0x15, // 'knda' kannada
        0x000CC9, 0x00,
        0x000CCA, 0x15, // 'knda' kannada
        0x000CCE, 0x00,
        0x000CD5, 0x15, // 'knda' kannada
        0x000CD7, 0x00,
        0x000CDE, 0x15, // 'knda' kannada
        0x000CDF, 0x00,
        0x000CE0, 0x15, // 'knda' kannada
        0x000CE2, 0x00,
        0x000CE6, 0x15, // 'knda' kannada
        0x000CF0, 0x00,
        0x000D02, 0x1A, // 'mlym' malayalam
        0x000D04, 0x00,
        0x000D05, 0x1A, // 'mlym' malayalam
        0x000D0D, 0x00,
        0x000D0E, 0x1A, // 'mlym' malayalam
        0x000D11, 0x00,
        0x000D12, 0x1A, // 'mlym' malayalam
        0x000D29, 0x00,
        0x000D2A, 0x1A, // 'mlym' malayalam
        0x000D3A, 0x00,
        0x000D3E, 0x1A, // 'mlym' malayalam
        0x000D44, 0x00,
        0x000D46, 0x1A, // 'mlym' malayalam
        0x000D49, 0x00,
        0x000D4A, 0x1A, // 'mlym' malayalam
        0x000D4E, 0x00,
        0x000D57, 0x1A, // 'mlym' malayalam
        0x000D58, 0x00,
        0x000D60, 0x1A, // 'mlym' malayalam
        0x000D62, 0x00,
        0x000D66, 0x1A, // 'mlym' malayalam
        0x000D70, 0x00,
        0x000D82, 0x21, // 'sinh' sinhala
        0x000D84, 0x00,
        0x000D85, 0x21, // 'sinh' sinhala
        0x000D97, 0x00,
        0x000D9A, 0x21, // 'sinh' sinhala
        0x000DB2, 0x00,
        0x000DB3, 0x21, // 'sinh' sinhala
        0x000DBC, 0x00,
        0x000DBD, 0x21, // 'sinh' sinhala
        0x000DBE, 0x00,
        0x000DC0, 0x21, // 'sinh' sinhala
        0x000DC7, 0x00,
        0x000DCA, 0x21, // 'sinh' sinhala
        0x000DCB, 0x00,
        0x000DCF, 0x21, // 'sinh' sinhala
        0x000DD5, 0x00,
        0x000DD6, 0x21, // 'sinh' sinhala
        0x000DD7, 0x00,
        0x000DD8, 0x21, // 'sinh' sinhala
        0x000DE0, 0x00,
        0x000DF2, 0x21, // 'sinh' sinhala
        0x000DF4, 0x00,
        0x000E01, 0x26, // 'thai' thai
        0x000E3B, 0x00,
        0x000E40, 0x26, // 'thai' thai
        0x000E4F, 0x00,
        0x000E50, 0x26, // 'thai' thai
        0x000E5A, 0x00,
        0x000E81, 0x18, // 'laoo' lao
        0x000E83, 0x00,
        0x000E84, 0x18, // 'laoo' lao
        0x000E85, 0x00,
        0x000E87, 0x18, // 'laoo' lao
        0x000E89, 0x00,
        0x000E8A, 0x18, // 'laoo' lao
        0x000E8B, 0x00,
        0x000E8D, 0x18, // 'laoo' lao
        0x000E8E, 0x00,
        0x000E94, 0x18, // 'laoo' lao
        0x000E98, 0x00,
        0x000E99, 0x18, // 'laoo' lao
        0x000EA0, 0x00,
        0x000EA1, 0x18, // 'laoo' lao
        0x000EA4, 0x00,
        0x000EA5, 0x18, // 'laoo' lao
        0x000EA6, 0x00,
        0x000EA7, 0x18, // 'laoo' lao
        0x000EA8, 0x00,
        0x000EAA, 0x18, // 'laoo' lao
        0x000EAC, 0x00,
        0x000EAD, 0x18, // 'laoo' lao
        0x000EBA, 0x00,
        0x000EBB, 0x18, // 'laoo' lao
        0x000EBE, 0x00,
        0x000EC0, 0x18, // 'laoo' lao
        0x000EC5, 0x00,
        0x000EC6, 0x18, // 'laoo' lao
        0x000EC7, 0x00,
        0x000EC8, 0x18, // 'laoo' lao
        0x000ECE, 0x00,
        0x000ED0, 0x18, // 'laoo' lao
        0x000EDA, 0x00,
        0x000EDC, 0x18, // 'laoo' lao
        0x000EDE, 0x00,
        0x000F00, 0x27, // 'tibt' tibetan
        0x000F01, 0x00,
        0x000F18, 0x27, // 'tibt' tibetan
        0x000F1A, 0x00,
        0x000F20, 0x27, // 'tibt' tibetan
        0x000F34, 0x00,
        0x000F35, 0x27, // 'tibt' tibetan
        0x000F36, 0x00,
        0x000F37, 0x27, // 'tibt' tibetan
        0x000F38, 0x00,
        0x000F39, 0x27, // 'tibt' tibetan
        0x000F3A, 0x00,
        0x000F40, 0x27, // 'tibt' tibetan
        0x000F48, 0x00,
        0x000F49, 0x27, // 'tibt' tibetan
        0x000F6B, 0x00,
        0x000F71, 0x27, // 'tibt' tibetan
        0x000F85, 0x00,
        0x000F86, 0x27, // 'tibt' tibetan
        0x000F8C, 0x00,
        0x000F90, 0x27, // 'tibt' tibetan
        0x000F98, 0x00,
        0x000F99, 0x27, // 'tibt' tibetan
        0x000FBD, 0x00,
        0x000FC6, 0x27, // 'tibt' tibetan
        0x000FC7, 0x00,
        0x001000, 0x1C, // 'mymr' myanmar
        0x001022, 0x00,
        0x001023, 0x1C, // 'mymr' myanmar
        0x001028, 0x00,
        0x001029, 0x1C, // 'mymr' myanmar
        0x00102B, 0x00,
        0x00102C, 0x1C, // 'mymr' myanmar
        0x001033, 0x00,
        0x001036, 0x1C, // 'mymr' myanmar
        0x00103A, 0x00,
        0x001040, 0x1C, // 'mymr' myanmar
        0x00104A, 0x00,
        0x001050, 0x1C, // 'mymr' myanmar
        0x00105A, 0x00,
        0x0010A0, 0x0C, // 'geor' georgian
        0x0010C6, 0x00,
        0x0010D0, 0x0C, // 'geor' georgian
        0x0010F9, 0x00,
        0x001100, 0x12, // 'hang' hangul
        0x00115A, 0x00,
        0x00115F, 0x12, // 'hang' hangul
        0x0011A3, 0x00,
        0x0011A8, 0x12, // 'hang' hangul
        0x0011FA, 0x00,
        0x001200, 0x0B, // 'ethi' ethiopic
        0x001207, 0x00,
        0x001208, 0x0B, // 'ethi' ethiopic
        0x001247, 0x00,
        0x001248, 0x0B, // 'ethi' ethiopic
        0x001249, 0x00,
        0x00124A, 0x0B, // 'ethi' ethiopic
        0x00124E, 0x00,
        0x001250, 0x0B, // 'ethi' ethiopic
        0x001257, 0x00,
        0x001258, 0x0B, // 'ethi' ethiopic
        0x001259, 0x00,
        0x00125A, 0x0B, // 'ethi' ethiopic
        0x00125E, 0x00,
        0x001260, 0x0B, // 'ethi' ethiopic
        0x001287, 0x00,
        0x001288, 0x0B, // 'ethi' ethiopic
        0x001289, 0x00,
        0x00128A, 0x0B, // 'ethi' ethiopic
        0x00128E, 0x00,
        0x001290, 0x0B, // 'ethi' ethiopic
        0x0012AF, 0x00,
        0x0012B0, 0x0B, // 'ethi' ethiopic
        0x0012B1, 0x00,
        0x0012B2, 0x0B, // 'ethi' ethiopic
        0x0012B6, 0x00,
        0x0012B8, 0x0B, // 'ethi' ethiopic
        0x0012BF, 0x00,
        0x0012C0, 0x0B, // 'ethi' ethiopic
        0x0012C1, 0x00,
        0x0012C2, 0x0B, // 'ethi' ethiopic
        0x0012C6, 0x00,
        0x0012C8, 0x0B, // 'ethi' ethiopic
        0x0012CF, 0x00,
        0x0012D0, 0x0B, // 'ethi' ethiopic
        0x0012D7, 0x00,
        0x0012D8, 0x0B, // 'ethi' ethiopic
        0x0012EF, 0x00,
        0x0012F0, 0x0B, // 'ethi' ethiopic
        0x00130F, 0x00,
        0x001310, 0x0B, // 'ethi' ethiopic
        0x001311, 0x00,
        0x001312, 0x0B, // 'ethi' ethiopic
        0x001316, 0x00,
        0x001318, 0x0B, // 'ethi' ethiopic
        0x00131F, 0x00,
        0x001320, 0x0B, // 'ethi' ethiopic
        0x001347, 0x00,
        0x001348, 0x0B, // 'ethi' ethiopic
        0x00135B, 0x00,
        0x001369, 0x0B, // 'ethi' ethiopic
        0x00137D, 0x00,
        0x0013A0, 0x06, // 'cher' cherokee
        0x0013F5, 0x00,
        0x001401, 0x28, // 'cans' canadian_aboriginal
        0x00166D, 0x00,
        0x00166F, 0x28, // 'cans' canadian_aboriginal
        0x001677, 0x00,
        0x001681, 0x1D, // 'ogam' ogham
        0x00169B, 0x00,
        0x0016A0, 0x20, // 'runr' runic
        0x0016EB, 0x00,
        0x0016EE, 0x20, // 'runr' runic
        0x0016F1, 0x00,
        0x001700, 0x2A, // 'tglg' tagalog
        0x00170D, 0x00,
        0x00170E, 0x2A, // 'tglg' tagalog
        0x001715, 0x00,
        0x001720, 0x2B, // 'hano' hanunoo
        0x001735, 0x00,
        0x001740, 0x2C, // 'buhd' buhid
        0x001754, 0x00,
        0x001760, 0x2D, // 'tagb' tagbanwa
        0x00176D, 0x00,
        0x00176E, 0x2D, // 'tagb' tagbanwa
        0x001771, 0x00,
        0x001772, 0x2D, // 'tagb' tagbanwa
        0x001774, 0x00,
        0x001780, 0x17, // 'khmr' khmer
        0x0017D4, 0x00,
        0x0017E0, 0x17, // 'khmr' khmer
        0x0017EA, 0x00,
        0x00180B, 0x01, // 'qaai' inherited
        0x00180E, 0x00,
        0x001810, 0x1B, // 'mong' mongolian
        0x00181A, 0x00,
        0x001820, 0x1B, // 'mong' mongolian
        0x001878, 0x00,
        0x001880, 0x1B, // 'mong' mongolian
        0x0018AA, 0x00,
        0x001E00, 0x19, // 'latn' latin
        0x001E9C, 0x00,
        0x001EA0, 0x19, // 'latn' latin
        0x001EFA, 0x00,
        0x001F00, 0x0E, // 'grek' greek
        0x001F16, 0x00,
        0x001F18, 0x0E, // 'grek' greek
        0x001F1E, 0x00,
        0x001F20, 0x0E, // 'grek' greek
        0x001F46, 0x00,
        0x001F48, 0x0E, // 'grek' greek
        0x001F4E, 0x00,
        0x001F50, 0x0E, // 'grek' greek
        0x001F58, 0x00,
        0x001F59, 0x0E, // 'grek' greek
        0x001F5A, 0x00,
        0x001F5B, 0x0E, // 'grek' greek
        0x001F5C, 0x00,
        0x001F5D, 0x0E, // 'grek' greek
        0x001F5E, 0x00,
        0x001F5F, 0x0E, // 'grek' greek
        0x001F7E, 0x00,
        0x001F80, 0x0E, // 'grek' greek
        0x001FB5, 0x00,
        0x001FB6, 0x0E, // 'grek' greek
        0x001FBD, 0x00,
        0x001FBE, 0x0E, // 'grek' greek
        0x001FBF, 0x00,
        0x001FC2, 0x0E, // 'grek' greek
        0x001FC5, 0x00,
        0x001FC6, 0x0E, // 'grek' greek
        0x001FCD, 0x00,
        0x001FD0, 0x0E, // 'grek' greek
        0x001FD4, 0x00,
        0x001FD6, 0x0E, // 'grek' greek
        0x001FDC, 0x00,
        0x001FE0, 0x0E, // 'grek' greek
        0x001FED, 0x00,
        0x001FF2, 0x0E, // 'grek' greek
        0x001FF5, 0x00,
        0x001FF6, 0x0E, // 'grek' greek
        0x001FFD, 0x00,
        0x002071, 0x19, // 'latn' latin
        0x002072, 0x00,
        0x00207F, 0x19, // 'latn' latin
        0x002080, 0x00,
        0x0020D0, 0x01, // 'qaai' inherited
        0x0020EB, 0x00,
        0x002126, 0x0E, // 'grek' greek
        0x002127, 0x00,
        0x00212A, 0x19, // 'latn' latin
        0x00212C, 0x00,
        0x002E80, 0x11, // 'hani' han
        0x002E9A, 0x00,
        0x002E9B, 0x11, // 'hani' han
        0x002EF4, 0x00,
        0x002F00, 0x11, // 'hani' han
        0x002FD6, 0x00,
        0x003005, 0x11, // 'hani' han
        0x003006, 0x00,
        0x003007, 0x11, // 'hani' han
        0x003008, 0x00,
        0x003021, 0x11, // 'hani' han
        0x00302A, 0x01, // 'qaai' inherited
        0x003030, 0x00,
        0x003038, 0x11, // 'hani' han
        0x00303C, 0x00,
        0x003041, 0x14, // 'hira' hiragana
        0x003097, 0x00,
        0x003099, 0x01, // 'qaai' inherited
        0x00309B, 0x00,
        0x00309D, 0x14, // 'hira' hiragana
        0x0030A0, 0x00,
        0x0030A1, 0x16, // 'kana' katakana
        0x0030FB, 0x00,
        0x0030FD, 0x16, // 'kana' katakana
        0x003100, 0x00,
        0x003105, 0x05, // 'bopo' bopomofo
        0x00312D, 0x00,
        0x003131, 0x12, // 'hang' hangul
        0x00318F, 0x00,
        0x0031A0, 0x05, // 'bopo' bopomofo
        0x0031B8, 0x00,
        0x0031F0, 0x16, // 'kana' katakana
        0x003200, 0x00,
        0x003400, 0x11, // 'hani' han
        0x004DB6, 0x00,
        0x004E00, 0x11, // 'hani' han
        0x009FA6, 0x00,
        0x00A000, 0x29, // 'yiii' yi
        0x00A48D, 0x00,
        0x00A490, 0x29, // 'yiii' yi
        0x00A4A2, 0x00,
        0x00A4A4, 0x29, // 'yiii' yi
        0x00A4B4, 0x00,
        0x00A4B5, 0x29, // 'yiii' yi
        0x00A4C1, 0x00,
        0x00A4C2, 0x29, // 'yiii' yi
        0x00A4C5, 0x00,
        0x00A4C6, 0x29, // 'yiii' yi
        0x00A4C7, 0x00,
        0x00AC00, 0x12, // 'hang' hangul
        0x00D7A4, 0x00,
        0x00F900, 0x11, // 'hani' han
        0x00FA2E, 0x00,
        0x00FA30, 0x11, // 'hani' han
        0x00FA6B, 0x00,
        0x00FB00, 0x19, // 'latn' latin
        0x00FB07, 0x00,
        0x00FB13, 0x03, // 'armn' armenian
        0x00FB18, 0x00,
        0x00FB1D, 0x13, // 'hebr' hebrew
        0x00FB1E, 0x01, // 'qaai' inherited
        0x00FB1F, 0x13, // 'hebr' hebrew
        0x00FB29, 0x00,
        0x00FB2A, 0x13, // 'hebr' hebrew
        0x00FB37, 0x00,
        0x00FB38, 0x13, // 'hebr' hebrew
        0x00FB3D, 0x00,
        0x00FB3E, 0x13, // 'hebr' hebrew
        0x00FB3F, 0x00,
        0x00FB40, 0x13, // 'hebr' hebrew
        0x00FB42, 0x00,
        0x00FB43, 0x13, // 'hebr' hebrew
        0x00FB45, 0x00,
        0x00FB46, 0x13, // 'hebr' hebrew
        0x00FB50, 0x02, // 'arab' arabic
        0x00FBB2, 0x00,
        0x00FBD3, 0x02, // 'arab' arabic
        0x00FD3E, 0x00,
        0x00FD50, 0x02, // 'arab' arabic
        0x00FD90, 0x00,
        0x00FD92, 0x02, // 'arab' arabic
        0x00FDC8, 0x00,
        0x00FDF0, 0x02, // 'arab' arabic
        0x00FDFC, 0x00,
        0x00FE00, 0x01, // 'qaai' inherited
        0x00FE10, 0x00,
        0x00FE20, 0x01, // 'qaai' inherited
        0x00FE24, 0x00,
        0x00FE70, 0x02, // 'arab' arabic
        0x00FE75, 0x00,
        0x00FE76, 0x02, // 'arab' arabic
        0x00FEFD, 0x00,
        0x00FF21, 0x19, // 'latn' latin
        0x00FF3B, 0x00,
        0x00FF41, 0x19, // 'latn' latin
        0x00FF5B, 0x00,
        0x00FF66, 0x16, // 'kana' katakana
        0x00FF70, 0x00,
        0x00FF71, 0x16, // 'kana' katakana
        0x00FF9E, 0x00,
        0x00FFA0, 0x12, // 'hang' hangul
        0x00FFBF, 0x00,
        0x00FFC2, 0x12, // 'hang' hangul
        0x00FFC8, 0x00,
        0x00FFCA, 0x12, // 'hang' hangul
        0x00FFD0, 0x00,
        0x00FFD2, 0x12, // 'hang' hangul
        0x00FFD8, 0x00,
        0x00FFDA, 0x12, // 'hang' hangul
        0x00FFDD, 0x00,
        0x010300, 0x1E, // 'ital' old_italic
        0x01031F, 0x00,
        0x010330, 0x0D, // 'goth' gothic
        0x01034B, 0x00,
        0x010400, 0x09, // 'dsrt' deseret
        0x010426, 0x00,
        0x010428, 0x09, // 'dsrt' deseret
        0x01044E, 0x00,
        0x01D167, 0x01, // 'qaai' inherited
        0x01D16A, 0x00,
        0x01D17B, 0x01, // 'qaai' inherited
        0x01D183, 0x00,
        0x01D185, 0x01, // 'qaai' inherited
        0x01D18C, 0x00,
        0x01D1AA, 0x01, // 'qaai' inherited
        0x01D1AE, 0x00,
        0x020000, 0x11, // 'hani' han
        0x02A6D7, 0x00,
        0x02F800, 0x11, // 'hani' han
        0x02FA1E, 0x00,
        0x110000, -1, // (NO NAME)
    };

    private static final int dataPower = 1 << 10;
    private static final int dataExtra = data.length - dataPower;
}
