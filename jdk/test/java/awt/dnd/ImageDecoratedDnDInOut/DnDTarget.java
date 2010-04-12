/*
 * Copyright 2009 Sun Microsystems, Inc.  All Rights Reserved.
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

/*
* Panel is a DropTarget
*
*/

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;


class DnDTarget extends Panel implements DropTargetListener {
    private int dragOperation = DnDConstants.ACTION_COPY | DnDConstants.ACTION_MOVE;
    Color bgColor;
    Color htColor;

    DnDTarget(Color bgColor, Color htColor) {
        super();
        this.bgColor = bgColor;
        this.htColor = htColor;
        setBackground(bgColor);
        setDropTarget(new DropTarget(this, this));
    }


    public void dragEnter(DropTargetDragEvent e) {
        System.out.println("[Target] dragEnter");
        setBackground(htColor);
        repaint();
    }

    public void dragOver(DropTargetDragEvent e) {
        System.out.println("[Target] dragOver");
    }

    public void dragExit(DropTargetEvent e) {
        System.out.println("[Target] dragExit");
        setBackground(bgColor);
        repaint();
    }

    public void dragScroll(DropTargetDragEvent e) {
        System.out.println("[Target] dragScroll");
    }

    public void dropActionChanged(DropTargetDragEvent e) {
        System.out.println("[Target] dropActionChanged");
    }

    public void drop(DropTargetDropEvent dtde) {
        System.out.println("[Target] drop");
        boolean success = false;
        if ((dtde.getDropAction() & dragOperation) == 0) {
            dtde.rejectDrop();
            Label label = new Label("[no links here :) ]");
            label.setBackground(Color.cyan);
            add(label);
        } else {
            dtde.acceptDrop(dragOperation);
            DataFlavor[] dfs = dtde.getCurrentDataFlavors();
            if (dfs != null && dfs.length >= 1){
                Transferable transfer = dtde.getTransferable();
                try {
                    Button button = (Button)transfer.getTransferData(dfs[0]);
                    if( button != null ){
                        add(button);
                        success = true;
                    }
                } catch (IOException ioe) {
                    System.out.println(ioe.getMessage());
                    return;
                } catch (UnsupportedFlavorException ufe) {
                    System.out.println(ufe.getMessage());
                    return;
                }  catch (Exception e) {
                    System.out.println(e.getMessage());
                    return;
                }
            }
        }
        setBackground(bgColor);
        dtde.dropComplete(success);

        invalidate();
        validate();
        repaint();
    }
}
