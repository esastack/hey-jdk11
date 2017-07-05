/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

/**
 * @test
 * @bug 4898484
 * @summary basic test for symmetric ciphers with no padding
 * @author Valerie Peng
 * @library ..
 */

import java.io.*;
import java.nio.*;
import java.util.*;

import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

public class TestSymmCiphersNoPad extends PKCS11Test {

    private static class CI { // class for holding Cipher Information
        String transformation;
        String keyAlgo;
        int dataSize;

        CI(String transformation, String keyAlgo, int dataSize) {
            this.transformation = transformation;
            this.keyAlgo = keyAlgo;
            this.dataSize = dataSize;
        }
    }

    private static final CI TEST_LIST[] = {
        new CI("ARCFOUR", "ARCFOUR", 400),
        new CI("RC4", "RC4", 401),
        new CI("DES/CBC/NoPadding", "DES", 400),
        new CI("DESede/CBC/NoPadding", "DESede", 160),
        new CI("AES/CBC/NoPadding", "AES", 4800),
        new CI("Blowfish/CBC/NoPadding", "Blowfish", 24)
    };

    private static StringBuffer debugBuf;

    public void main(Provider p) throws Exception {
        boolean status = true;
        Random random = new Random();
        try {
            for (int i = 0; i < TEST_LIST.length; i++) {
                CI currTest = TEST_LIST[i];
                System.out.println("===" + currTest.transformation + "===");
                try {
                    KeyGenerator kg =
                        KeyGenerator.getInstance(currTest.keyAlgo, p);
                    SecretKey key = kg.generateKey();
                    Cipher c1 = Cipher.getInstance(currTest.transformation, p);
                    Cipher c2 = Cipher.getInstance(currTest.transformation,
                                                   "SunJCE");

                    byte[] plainTxt = new byte[currTest.dataSize];
                    random.nextBytes(plainTxt);
                    System.out.println("Testing inLen = " + plainTxt.length);

                    c2.init(Cipher.ENCRYPT_MODE, key);
                    AlgorithmParameters params = c2.getParameters();
                    byte[] answer = c2.doFinal(plainTxt);
                    test(c1, Cipher.ENCRYPT_MODE, key, params,
                         plainTxt, answer);
                    System.out.println("Encryption tests: DONE");
                    c2.init(Cipher.DECRYPT_MODE, key, params);
                    byte[] answer2 = c2.doFinal(answer);
                    test(c1, Cipher.DECRYPT_MODE, key, params,
                         answer, answer2);
                    System.out.println("Decryption tests: DONE");
                } catch (NoSuchAlgorithmException nsae) {
                    System.out.println("Skipping unsupported algorithm: " +
                                       nsae);
                }
            }
        } catch (Exception ex) {
            // print out debug info when exception is encountered
            if (debugBuf != null) {
                System.out.println(debugBuf.toString());
            }
            throw ex;
        }
    }

    private static void test(Cipher cipher, int mode, SecretKey key,
                             AlgorithmParameters params,
                             byte[] in, byte[] answer) throws Exception {
        // test setup
        debugBuf = new StringBuffer();
        cipher.init(mode, key, params);
        int outLen = cipher.getOutputSize(in.length);
        debugBuf.append("Estimated output size = " + outLen + "\n");

        // test data preparation
        ByteBuffer inBuf = ByteBuffer.allocate(in.length);
        inBuf.put(in);
        inBuf.position(0);
        ByteBuffer inDirectBuf = ByteBuffer.allocateDirect(in.length);
        inDirectBuf.put(in);
        inDirectBuf.position(0);
        ByteBuffer outBuf = ByteBuffer.allocate(outLen);
        ByteBuffer outDirectBuf = ByteBuffer.allocateDirect(outLen);

        // test#1: byte[] in + byte[] out
        debugBuf.append("Test#1:\n");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] testOut1 = cipher.update(in, 0, 16);
        if (testOut1 != null) baos.write(testOut1, 0, testOut1.length);
        testOut1 = cipher.doFinal(in, 16, in.length-16);
        if (testOut1 != null) baos.write(testOut1, 0, testOut1.length);
        testOut1 = baos.toByteArray();
        match(testOut1, answer);

        // test#2: Non-direct Buffer in + non-direct Buffer out
        debugBuf.append("Test#2:\n");
        debugBuf.append("inputBuf: " + inBuf + "\n");
        debugBuf.append("outputBuf: " + outBuf + "\n");
        cipher.update(inBuf, outBuf);
        cipher.doFinal(inBuf, outBuf);
        match(outBuf, answer);

        // test#3: Direct Buffer in + direc Buffer out
        debugBuf.append("Test#3:\n");
        debugBuf.append("(pre) inputBuf: " + inDirectBuf + "\n");
        debugBuf.append("(pre) outputBuf: " + outDirectBuf + "\n");
        cipher.update(inDirectBuf, outDirectBuf);
        cipher.doFinal(inDirectBuf, outDirectBuf);

        debugBuf.append("(post) inputBuf: " + inDirectBuf + "\n");
        debugBuf.append("(post) outputBuf: " + outDirectBuf + "\n");
        match(outDirectBuf, answer);

        // test#4: Direct Buffer in + non-direct Buffer out
        debugBuf.append("Test#4:\n");
        inDirectBuf.position(0);
        outBuf.position(0);
        debugBuf.append("inputBuf: " + inDirectBuf + "\n");
        debugBuf.append("outputBuf: " + outBuf + "\n");
        cipher.update(inDirectBuf, outBuf);
        cipher.doFinal(inDirectBuf, outBuf);
        match(outBuf, answer);

        // test#5: Non-direct Buffer in + direct Buffer out
        debugBuf.append("Test#5:\n");
        inBuf.position(0);
        outDirectBuf.position(0);

        debugBuf.append("(pre) inputBuf: " + inBuf + "\n");
        debugBuf.append("(pre) outputBuf: " + outDirectBuf + "\n");

        cipher.update(inBuf, outDirectBuf);
        cipher.doFinal(inBuf, outDirectBuf);

        debugBuf.append("(post) inputBuf: " + inBuf + "\n");
        debugBuf.append("(post) outputBuf: " + outDirectBuf + "\n");
        match(outDirectBuf, answer);

        debugBuf = null;
    }

    private static void match(byte[] b1, byte[] b2) throws Exception {
        if (b1.length != b2.length) {
            debugBuf.append("got len   : " + b1.length + "\n");
            debugBuf.append("expect len: " + b2.length + "\n");
            throw new Exception("mismatch - different length!\n");
        } else {
            for (int i = 0; i < b1.length; i++) {
                if (b1[i] != b2[i]) {
                    debugBuf.append("got   : " + toString(b1) + "\n");
                    debugBuf.append("expect: " + toString(b2) + "\n");
                    throw new Exception("mismatch");
                }
            }
        }
    }

    private static void match(ByteBuffer bb, byte[] answer) throws Exception {
        byte[] bbTemp = new byte[bb.position()];
        bb.position(0);
        bb.get(bbTemp, 0, bbTemp.length);
        match(bbTemp, answer);
    }

    public static void main(String[] args) throws Exception {
        main(new TestSymmCiphersNoPad());
    }
}
