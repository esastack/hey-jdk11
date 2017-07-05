/*
 * Copyright 2000-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.font;

import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import sun.awt.SunHints;
import sun.awt.SunToolkit;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.GlyphListPipe;
import sun.java2d.pipe.Region;
import sun.java2d.loops.FontInfo;
import sun.java2d.loops.GraphicsPrimitive;
import sun.java2d.x11.X11SurfaceData;

/**
 * A delegate pipe of SG2D for drawing text with
 * a solid source colour to an X11 drawable destination.
 */
public class X11TextRenderer extends GlyphListPipe {
    /*
     * Override super class method to call the AA pipe if
     * AA is specified in the GlyphVector's FontRenderContext
     */
    public void drawGlyphVector(SunGraphics2D sg2d, GlyphVector g,
                                float x, float y)
    {
        FontRenderContext frc = g.getFontRenderContext();
        FontInfo info = sg2d.getGVFontInfo(g.getFont(), frc);
        switch (info.aaHint) {
        case SunHints.INTVAL_TEXT_ANTIALIAS_OFF:
            super.drawGlyphVector(sg2d, g, x, y);
            return;
        case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
             sg2d.surfaceData.aaTextRenderer.drawGlyphVector(sg2d, g, x, y);
            return;
        case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB:
        case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VRGB:
             sg2d.surfaceData.lcdTextRenderer.drawGlyphVector(sg2d, g, x, y);
            return;
        default:
        }
    }

    native void doDrawGlyphList(long dstData, long xgc,
                                Region clip, GlyphList gl);

    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
        SunToolkit.awtLock();
        try {
            X11SurfaceData x11sd = (X11SurfaceData)sg2d.surfaceData;
            Region clip = sg2d.getCompClip();
            long xgc = x11sd.getRenderGC(clip, SunGraphics2D.COMP_ISCOPY,
                                         null, sg2d.pixel);
            doDrawGlyphList(x11sd.getNativeOps(), xgc, clip, gl);
        } finally {
            SunToolkit.awtUnlock();
        }
    }

    public X11TextRenderer traceWrap() {
        return new Tracer();
    }

    public static class Tracer extends X11TextRenderer {
        void doDrawGlyphList(long dstData, long xgc,
                             Region clip, GlyphList gl)
        {
            GraphicsPrimitive.tracePrimitive("X11DrawGlyphs");
            super.doDrawGlyphList(dstData, xgc, clip, gl);
        }
    }
}
