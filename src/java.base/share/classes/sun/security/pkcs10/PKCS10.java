/*
 * Copyright (c) 1996, 2019, Oracle and/or its affiliates. All rights reserved.
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


package sun.security.pkcs10;

import java.io.PrintStream;
import java.io.IOException;
import java.math.BigInteger;

import java.security.cert.CertificateException;
import java.security.*;

import java.util.Base64;

import sun.security.util.*;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X509Key;
import sun.security.x509.X500Name;
import sun.security.util.SignatureUtil;


/**
 * A PKCS #10 certificate request is created and sent to a Certificate
 * Authority, which then creates an X.509 certificate and returns it to
 * the entity that requested it. A certificate request basically consists
 * of the subject's X.500 name, public key, and optionally some attributes,
 * signed using the corresponding private key.
 *
 * The ASN.1 syntax for a Certification Request is:
 * <pre>
 * CertificationRequest ::= SEQUENCE {
 *    certificationRequestInfo CertificationRequestInfo,
 *    signatureAlgorithm       SignatureAlgorithmIdentifier,
 *    signature                Signature
 *  }
 *
 * SignatureAlgorithmIdentifier ::= AlgorithmIdentifier
 * Signature ::= BIT STRING
 *
 * CertificationRequestInfo ::= SEQUENCE {
 *    version                 Version,
 *    subject                 Name,
 *    subjectPublicKeyInfo    SubjectPublicKeyInfo,
 *    attributes [0] IMPLICIT Attributes
 * }
 * Attributes ::= SET OF Attribute
 * </pre>
 *
 * @author David Brownell
 * @author Amit Kapoor
 * @author Hemma Prafullchandra
 */
public class PKCS10 {
    /**
     * Constructs an unsigned PKCS #10 certificate request.  Before this
     * request may be used, it must be encoded and signed.  Then it
     * must be retrieved in some conventional format (e.g. string).
     *
     * @param publicKey the public key that should be placed
     *          into the certificate generated by the CA.
     */
    public PKCS10(PublicKey publicKey) {
        subjectPublicKeyInfo = publicKey;
        attributeSet = new PKCS10Attributes();
    }

    /**
     * Constructs an unsigned PKCS #10 certificate request.  Before this
     * request may be used, it must be encoded and signed.  Then it
     * must be retrieved in some conventional format (e.g. string).
     *
     * @param publicKey the public key that should be placed
     *          into the certificate generated by the CA.
     * @param attributes additonal set of PKCS10 attributes requested
     *          for in the certificate.
     */
    public PKCS10(PublicKey publicKey, PKCS10Attributes attributes) {
        subjectPublicKeyInfo = publicKey;
        attributeSet = attributes;
    }

    /**
     * Parses an encoded, signed PKCS #10 certificate request, verifying
     * the request's signature as it does so.  This constructor would
     * typically be used by a Certificate Authority, from which a new
     * certificate would then be constructed.
     *
     * @param data the DER-encoded PKCS #10 request.
     * @exception IOException for low level errors reading the data
     * @exception SignatureException when the signature is invalid
     * @exception NoSuchAlgorithmException when the signature
     *  algorithm is not supported in this environment
     */
    public PKCS10(byte[] data)
    throws IOException, SignatureException, NoSuchAlgorithmException {
        DerInputStream  in;
        DerValue[]      seq;
        AlgorithmId     id;
        byte[]          sigData;
        Signature       sig;

        encoded = data;

        //
        // Outer sequence:  request, signature algorithm, signature.
        // Parse, and prepare to verify later.
        //
        in = new DerInputStream(data);
        seq = in.getSequence(3);

        if (seq.length != 3)
            throw new IllegalArgumentException("not a PKCS #10 request");

        data = seq[0].toByteArray();            // reusing this variable
        id = AlgorithmId.parse(seq[1]);
        sigData = seq[2].getBitString();

        //
        // Inner sequence:  version, name, key, attributes
        //
        BigInteger      serial;
        DerValue        val;

        serial = seq[0].data.getBigInteger();
        if (!serial.equals(BigInteger.ZERO))
            throw new IllegalArgumentException("not PKCS #10 v1");

        subject = new X500Name(seq[0].data);
        subjectPublicKeyInfo = X509Key.parse(seq[0].data.getDerValue());

        // Cope with a somewhat common illegal PKCS #10 format
        if (seq[0].data.available() != 0)
            attributeSet = new PKCS10Attributes(seq[0].data);
        else
            attributeSet = new PKCS10Attributes();

        if (seq[0].data.available() != 0)
            throw new IllegalArgumentException("illegal PKCS #10 data");

        //
        // OK, we parsed it all ... validate the signature using the
        // key and signature algorithm we found.
        //
        try {
            sigAlg = id.getName();
            sig = Signature.getInstance(sigAlg);

            sig.initVerify(subjectPublicKeyInfo);

            // set parameters after Signature.initSign/initVerify call,
            // so the deferred provider selections occur when key is set
            SignatureUtil.specialSetParameter(sig, id.getParameters());

            sig.update(data);
            if (!sig.verify(sigData)) {
                throw new SignatureException("Invalid PKCS #10 signature");
            }
        } catch (InvalidKeyException e) {
            throw new SignatureException("Invalid key");
        } catch (InvalidAlgorithmParameterException e) {
            throw new SignatureException("Invalid signature parameters", e);
        } catch (ProviderException e) {
            throw new SignatureException("Error parsing signature parameters",
                e.getCause());
        }
    }

    /**
     * Create the signed certificate request.  This will later be
     * retrieved in either string or binary format.
     *
     * @param subject identifies the signer (by X.500 name).
     * @param signature private key and signing algorithm to use.
     * @exception IOException on errors.
     * @exception CertificateException on certificate handling errors.
     * @exception SignatureException on signature handling errors.
     */
    public void encodeAndSign(X500Name subject, Signature signature)
    throws CertificateException, IOException, SignatureException {
        DerOutputStream out, scratch;
        byte[]          certificateRequestInfo;
        byte[]          sig;

        if (encoded != null)
            throw new SignatureException("request is already signed");

        this.subject = subject;

        /*
         * Encode cert request info, wrap in a sequence for signing
         */
        scratch = new DerOutputStream();
        scratch.putInteger(BigInteger.ZERO);            // PKCS #10 v1.0
        subject.encode(scratch);                        // X.500 name
        scratch.write(subjectPublicKeyInfo.getEncoded()); // public key
        attributeSet.encode(scratch);

        out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, scratch);      // wrap it!
        certificateRequestInfo = out.toByteArray();
        scratch = out;

        /*
         * Sign it ...
         */
        signature.update(certificateRequestInfo, 0,
                certificateRequestInfo.length);
        sig = signature.sign();
        sigAlg = signature.getAlgorithm();

        /*
         * Build guts of SIGNED macro
         */
        AlgorithmId algId = null;
        try {
            AlgorithmParameters params = signature.getParameters();
            algId = params == null
                    ? AlgorithmId.get(signature.getAlgorithm())
                    : AlgorithmId.get(params);
        } catch (NoSuchAlgorithmException nsae) {
            throw new SignatureException(nsae);
        }

        algId.encode(scratch);     // sig algorithm
        scratch.putBitString(sig);                      // sig

        /*
         * Wrap those guts in a sequence
         */
        out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, scratch);
        encoded = out.toByteArray();
    }

    /**
     * Returns the subject's name.
     */
    public X500Name getSubjectName() { return subject; }

    /**
     * Returns the subject's public key.
     */
    public PublicKey getSubjectPublicKeyInfo()
        { return subjectPublicKeyInfo; }

    /**
     * Returns the signature algorithm.
     */
    public String getSigAlg() { return sigAlg; }

    /**
     * Returns the additional attributes requested.
     */
    public PKCS10Attributes getAttributes()
        { return attributeSet; }

    /**
     * Returns the encoded and signed certificate request as a
     * DER-encoded byte array.
     *
     * @return the certificate request, or null if encodeAndSign()
     *          has not yet been called.
     */
    public byte[] getEncoded() {
        if (encoded != null)
            return encoded.clone();
        else
            return null;
    }

    /**
     * Prints an E-Mailable version of the certificate request on the print
     * stream passed.  The format is a common base64 encoded one, supported
     * by most Certificate Authorities because Netscape web servers have
     * used this for some time.  Some certificate authorities expect some
     * more information, in particular contact information for the web
     * server administrator.
     *
     * @param out the print stream where the certificate request
     *  will be printed.
     * @exception IOException when an output operation failed
     * @exception SignatureException when the certificate request was
     *  not yet signed.
     */
    public void print(PrintStream out)
    throws IOException, SignatureException {
        if (encoded == null)
            throw new SignatureException("Cert request was not signed");


        byte[] CRLF = new byte[] {'\r', '\n'};
        out.println("-----BEGIN NEW CERTIFICATE REQUEST-----");
        out.println(Base64.getMimeEncoder(64, CRLF).encodeToString(encoded));
        out.println("-----END NEW CERTIFICATE REQUEST-----");
    }

    /**
     * Provides a short description of this request.
     */
    public String toString() {
        return "[PKCS #10 certificate request:\n"
            + subjectPublicKeyInfo.toString()
            + " subject: <" + subject + ">" + "\n"
            + " attributes: " + attributeSet.toString()
            + "\n]";
    }

    /**
     * Compares this object for equality with the specified
     * object. If the <code>other</code> object is an
     * <code>instanceof</code> <code>PKCS10</code>, then
     * its encoded form is retrieved and compared with the
     * encoded form of this certificate request.
     *
     * @param other the object to test for equality with this object.
     * @return true iff the encoded forms of the two certificate
     * requests match, false otherwise.
     */
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof PKCS10))
            return false;
        if (encoded == null) // not signed yet
            return false;
        byte[] otherEncoded = ((PKCS10)other).getEncoded();
        if (otherEncoded == null)
            return false;

        return java.util.Arrays.equals(encoded, otherEncoded);
    }

    /**
     * Returns a hashcode value for this certificate request from its
     * encoded form.
     *
     * @return the hashcode value.
     */
    public int hashCode() {
        int     retval = 0;
        if (encoded != null)
            for (int i = 1; i < encoded.length; i++)
             retval += encoded[i] * i;
        return(retval);
    }

    private X500Name            subject;
    private PublicKey           subjectPublicKeyInfo;
    private String              sigAlg;
    private PKCS10Attributes    attributeSet;
    private byte[]              encoded;        // signed
}
