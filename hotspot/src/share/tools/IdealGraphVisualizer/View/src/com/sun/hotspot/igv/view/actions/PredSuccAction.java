/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.hotspot.igv.view.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 *
 * @author Thomas Wuerthinger
 */
public class PredSuccAction extends AbstractAction {

    private boolean state;
    public static final String STATE = "state";

    public PredSuccAction() {
        state = true;
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(org.openide.util.Utilities.loadImage(iconResource())));
        putValue(STATE, true);
        putValue(Action.SHORT_DESCRIPTION, "Show neighboring nodes of fully visible nodes semi-transparent");
    }

    public void actionPerformed(ActionEvent ev) {
        this.state = !state;
        this.putValue(STATE, state);
    }

    protected String iconResource() {
        return "com/sun/hotspot/igv/view/images/predsucc.gif";
    }
}
