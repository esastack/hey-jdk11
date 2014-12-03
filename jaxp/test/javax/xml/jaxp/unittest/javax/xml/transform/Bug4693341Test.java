/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

package javax.xml.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/*
 * @bug 4693341
 * @summary Test transform with external dtd.
 */
public class Bug4693341Test {

    @Test
    public void test() {
        boolean status = false;

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            String out = getClass().getResource("Bug4693341.out").getPath();
            StreamResult result = new StreamResult(new FileOutputStream(out));

            String in = getClass().getResource("Bug4693341.xml").getPath();
            File file = new File(in);
            StreamSource source = new StreamSource(new FileInputStream(file), ("file://" + in));

            transformer.transform(source, result);

            //URL inputsource = new URL("file", "", golden);
            URL output = new URL("file", "", out);

            // error happens when trying to parse output
            String systemId = output.toExternalForm();
            System.out.println("systemId: " + systemId);
            InputSource is = new InputSource(systemId);
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(is, new DefaultHandler());

        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

}
