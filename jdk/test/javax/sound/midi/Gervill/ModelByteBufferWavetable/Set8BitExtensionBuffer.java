/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

/* @test
   @summary Test ModelByteBufferWavetable set8BitExtensionBuffer method */

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.*;

import com.sun.media.sound.*;

public class Set8BitExtensionBuffer {

    static float[] testarray;
    static byte[] test_byte_array;
    static byte[] test_byte_array_8ext;
    static AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
    static AudioFormat format24 = new AudioFormat(44100, 24, 1, true, false);
    static ModelByteBuffer buffer;
    static ModelByteBuffer buffer_wave;
    static ModelByteBuffer buffer8;
    static ModelByteBuffer buffer16_8;
    static ModelByteBuffer buffer24;

    static void setUp() throws Exception {
        testarray = new float[1024];
        for (int i = 0; i < 1024; i++) {
            double ii = i / 1024.0;
            ii = ii * ii;
            testarray[i] = (float)Math.sin(10*ii*2*Math.PI);
            testarray[i] += (float)Math.sin(1.731 + 2*ii*2*Math.PI);
            testarray[i] += (float)Math.sin(0.231 + 6.3*ii*2*Math.PI);
            testarray[i] *= 0.3;
        }
        test_byte_array = new byte[testarray.length*2];
        AudioFloatConverter.getConverter(format).toByteArray(testarray, test_byte_array);
        buffer = new ModelByteBuffer(test_byte_array);

        byte[] test_byte_array2 = new byte[testarray.length*3];
        buffer24 = new ModelByteBuffer(test_byte_array2);
        test_byte_array_8ext = new byte[testarray.length];
        byte[] test_byte_array_8_16 = new byte[testarray.length*2];
        AudioFloatConverter.getConverter(format24).toByteArray(testarray, test_byte_array2);
        int ix = 0;
        int x = 0;
        for (int i = 0; i < test_byte_array_8ext.length; i++) {
            test_byte_array_8ext[i] = test_byte_array2[ix++];
            test_byte_array_8_16[x++] = test_byte_array2[ix++];
            test_byte_array_8_16[x++] = test_byte_array2[ix++];
        }
        buffer16_8 = new ModelByteBuffer(test_byte_array_8_16);
        buffer8 = new ModelByteBuffer(test_byte_array_8ext);

        AudioInputStream ais = new AudioInputStream(buffer.getInputStream(), format, testarray.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, baos);
        buffer_wave = new ModelByteBuffer(baos.toByteArray());
    }

    static float compare(float[] a, float[] b)
    {
        float ac_error = 0;
        int counter = 0;
        for (int i = 0; i < a.length; i++) {
            ac_error += Math.abs(a[i] - b[i]);
            counter++;
        }
        return ac_error / ((float)counter);

    }

    public static void main(String[] args) throws Exception {

        setUp();

        ModelByteBufferWavetable wavetable = new ModelByteBufferWavetable(buffer16_8,format,10f);
        float[] f1 = new float[testarray.length];
        float[] f2 = new float[testarray.length];
        wavetable.openStream().read(f1);
        wavetable.set8BitExtensionBuffer(buffer8);
        if(wavetable.get8BitExtensionBuffer() != buffer8)
            throw new RuntimeException("wavetable.get8BitExtensionBuffer() incorrect!");
        wavetable.openStream().read(f2);
        // f2 should have more accurity than f1,
        //    about 256 times more, or 8 bits
        float spec1 = compare(f1, testarray);
        float spec2 = compare(f2, testarray);
        if((spec1/spec2) <= 200)
            throw new RuntimeException("(spec1/spec2) <= 200!");


    }



}
