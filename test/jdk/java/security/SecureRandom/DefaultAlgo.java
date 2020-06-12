/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
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

import static java.lang.System.out;
import java.security.Provider;
import java.security.Security;
import java.security.SecureRandom;
import java.security.Provider.Service;
import java.util.Objects;
import java.util.Arrays;
import sun.security.provider.SunEntries;

/**
 * @test
 * @bug 8228613 8246613
 * @summary Ensure that the default SecureRandom algo used is based
 *     on the registration ordering, and falls to next provider
 *     if none are found
 * @modules java.base/sun.security.provider
 */
public class DefaultAlgo {

    public static void main(String[] args) throws Exception {
        String[] algos = { "A", "B", "C" };
        test3rdParty(algos);
        // reverse the order and re-check
        String[] algosReversed = { "C", "B", "A" };
        test3rdParty(algosReversed);
    }

    private static void test3rdParty(String[] algos) {
        Provider[] provs = {
            new SampleLegacyProvider(algos),
            new SampleServiceProvider(algos)
        };
        for (Provider p : provs) {
            checkDefault(p, algos);
        }
    }

    // validate the specified SecureRandom obj to be from the specified
    // provider and matches the specified algorithm
    private static void validate(SecureRandom sr, String pName, String algo) {
        if (!sr.getProvider().getName().equals(pName)) {
            throw new RuntimeException("Failed provider check, exp: " +
                    pName + ", got " + sr.getProvider().getName());
        }
        if (!sr.getAlgorithm().equals(algo)) {
            throw new RuntimeException("Failed algo check, exp: " +
                    algo + ", got " + sr.getAlgorithm());
        }
    }

    private static void checkDefault(Provider p, String ... algos) {
        out.println(p.getName() + " with " + Arrays.toString(algos));
        int pos = Security.insertProviderAt(p, 1);
        String pName = p.getName();
        boolean isLegacy = pName.equals("SampleLegacy");
        try {
            if (isLegacy) {
                for (String s : algos) {
                    validate(new SecureRandom(), pName, s);
                    p.remove("SecureRandom." + s);
                    out.println("removed "  + s);
                }
                validate(new SecureRandom(), "SUN",
                        SunEntries.DEF_SECURE_RANDOM_ALGO);
            } else {
                validate(new SecureRandom(), pName, algos[0]);
            }
            out.println("=> Test Passed");
        } finally {
            if (pos != -1) {
                Security.removeProvider(p.getName());
            }
        }
    }

    private static class SampleLegacyProvider extends Provider {
        SampleLegacyProvider(String[] listOfSupportedRNGs) {
            super("SampleLegacy", "1.0", "test provider using legacy put");
            for (String s : listOfSupportedRNGs) {
                put("SecureRandom." + s, "sun.security.provider.SecureRandom");
            }
        }
    }

    private static class SampleServiceProvider extends Provider {
        SampleServiceProvider(String[] listOfSupportedRNGs) {
            super("SampleService", "1.0", "test provider using putService");
            for (String s : listOfSupportedRNGs) {
                putService(new Provider.Service(this, "SecureRandom", s,
                        "sun.security.provider.SecureRandom", null, null));
            }
        }
    }
}
