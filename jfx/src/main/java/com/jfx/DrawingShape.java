/*
 * Copyright (c) 2008-2014 by Gerasimenko Roman.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistribution of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * 3. The name "JFX" must not be used to endorse or promote
 *     products derived from this software without prior written
 *     permission.
 *     For written permission, please contact roman.gerasimenko@gmail.com
 *
 * 4. Products derived from this software may not be called "JFX",
 *     nor may "JFX" appear in their name, without prior written
 *     permission of Gerasimenko Roman.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 */

package com.jfx;

/**
 * Drawing shape style enumeration for SetIndexStyle() function.
 */
public class DrawingShape {

    /**
     * Drawing line.
     */
    public final static DrawingShape DRAW_LINE = new DrawingShape(0);
    public final static int _DRAW_LINE = 0;
    /**
     * Drawing sections.
     */
    public final static DrawingShape DRAW_SECTION = new DrawingShape(1);
    public final static int _DRAW_SECTION = 1;
    /**
     * Drawing histogram.
     */
    public final static DrawingShape DRAW_HISTOGRAM = new DrawingShape(2);
    public final static int _DRAW_HISTOGRAM = 2;
    /**
     * Drawing arrows (symbols).
     */
    public final static DrawingShape DRAW_ARROW = new DrawingShape(3);
    public final static int _DRAW_ARROW = 3;
    /**
     * Drawing sections between even and odd indicator buffers.
     */
    public final static DrawingShape DRAW_ZIGZAG = new DrawingShape(4);
    public final static int _DRAW_ZIGZAG = 4;
    /**
     * No drawing
     */
    public final static DrawingShape DRAW_NONE = new DrawingShape(12);
    public final static int _DRAW_NONE = 12;
    public int val;
    private DrawingShape(int val) {
        this.val = val;
    }
    public static DrawingShape getDrawingShape(int val) {
        switch (val) {

            case 0: return DRAW_LINE;
            case 1: return DRAW_SECTION;
            case 2: return DRAW_HISTOGRAM;
            case 3: return DRAW_ARROW;
            case 4: return DRAW_ZIGZAG;
            case 12: return DRAW_NONE;
            default: return null;
        }
    }
}
