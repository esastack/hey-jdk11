/*
 * Copyright (c) 2007, 2018, Oracle and/or its affiliates. All rights reserved.
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

/*
 * A Simple AttachingConnector with default arguments of all types used by
 * nsk/jdi/PlugConnectors/MultiConnectors/plugMultiConnect002 test
 */

package nsk.jdi.PlugConnectors.MultiConnectors.plugMultiConnect002.connectors;

import nsk.share.jdi.*;
import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import java.util.*;

public class PlugAttachConnector002_02 extends PlugConnectors implements AttachingConnector {

    static String plugAttachConnectorName
        = "PlugAttachConnector002_02_Name";
    static String plugAttachConnectorDescription
        = "PlugAttachConnector002_02_Description";
    static Transport plugAttachConnectorTransport
        = new PlugConnectorsTransport("PlugAttachConnector002_02_Transport");
    static Map<String, Connector.Argument> plugAttachConnectorDefaultArguments
        = new HashMap<String, Connector.Argument>();

    static Map<String, Connector.Argument> prepareConnectorDefaultArguments() {
        String plugAttachConnectorStringArgumentKey = "PlugAttachConnector002_02_StringArgument_Key";
        Connector.StringArgument testStringArgument = new TestStringArgument(
            "PlugAttachConnector002_02_StringArgument_Name",
            "PlugAttachConnector002_02_StringArgument_Label",
            "PlugAttachConnector002_02_StringArgument_Description",
            "PlugAttachConnector002_02_StringArgument_Value",
            true  // mustSpecify
            );
        plugAttachConnectorDefaultArguments.put(plugAttachConnectorStringArgumentKey, testStringArgument);

        String plugAttachConnectorIntegerArgumentKey = "PlugAttachConnector002_02_IntegerArgument_Key";
        Connector.IntegerArgument testIntegerArgument = new TestIntegerArgument(
            "PlugAttachConnector002_02_IntegerArgument_Name",
            "PlugAttachConnector002_02_IntegerArgument_Label",
            "PlugAttachConnector002_02_IntegerArgument_Description",
            555555, // IntegerArgument_Value",
            111111, // IntegerArgument_Min",
            999999, // IntegerArgument_Max",
            true    // mustSpecify
            );
        plugAttachConnectorDefaultArguments.put(plugAttachConnectorIntegerArgumentKey, testIntegerArgument);

        String plugAttachConnectorBooleanArgumentKey = "PlugAttachConnector002_02_BooleanArgument_Key";
        Connector.BooleanArgument testBooleanArgument = new TestBooleanArgument(
            "PlugAttachConnector002_02_BooleanArgument_Name",
            "PlugAttachConnector002_02_BooleanArgument_Label",
            "PlugAttachConnector002_02_BooleanArgument_Description",
            true, // BooleanArgument_Value",
            true    // mustSpecify
            );
        plugAttachConnectorDefaultArguments.put(plugAttachConnectorBooleanArgumentKey, testBooleanArgument);

        String plugAttachConnectorSelectedArgumentKey = "PlugAttachConnector002_02_SelectedArgument_Key";
        List<String> selectedArgumentChoices = new ArrayList<String>();
        selectedArgumentChoices.add("PlugAttachConnector002_02_SelectedArgument_Value_0");
        selectedArgumentChoices.add("PlugAttachConnector002_02_SelectedArgument_Value");
        selectedArgumentChoices.add("PlugAttachConnector002_02_SelectedArgument_Value_1");

        Connector.SelectedArgument testSelectedArgument = new TestSelectedArgument(
            "PlugAttachConnector002_02_SelectedArgument_Name",
            "PlugAttachConnector002_02_SelectedArgument_Label",
            "PlugAttachConnector002_02_SelectedArgument_Description",
            "PlugAttachConnector002_02_SelectedArgument_Value",
            selectedArgumentChoices, // List of choices,
            true    // mustSpecify
            );
        plugAttachConnectorDefaultArguments.put(plugAttachConnectorSelectedArgumentKey, testSelectedArgument);

        return plugAttachConnectorDefaultArguments;
    }  // end of prepareConnectorDefaultArguments() method


    public PlugAttachConnector002_02() {

        super(plugAttachConnectorName,
            plugAttachConnectorDescription,
            plugAttachConnectorTransport,
            prepareConnectorDefaultArguments());
    }

} // end of PlugAttachConnector002_02 class
