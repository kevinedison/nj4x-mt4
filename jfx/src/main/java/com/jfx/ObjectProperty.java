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
 * Object value index used with ObjectGet() and ObjectSet() functions. 
 */
public class ObjectProperty {

    /**
     * datetime; Datetime value to set/get first coordinate time part.
     */
    public final static ObjectProperty OBJPROP_TIME1 = new ObjectProperty(0);
    public final static int _OBJPROP_TIME1 = 0;
    /**
     * double; Double value to set/get first coordinate price part.
     */
    public final static ObjectProperty OBJPROP_PRICE1 = new ObjectProperty(1);
    public final static int _OBJPROP_PRICE1 = 1;
    /**
     * datetime; Datetime value to set/get second coordinate time part.
     */
    public final static ObjectProperty OBJPROP_TIME2 = new ObjectProperty(2);
    public final static int _OBJPROP_TIME2 = 2;
    /**
     * double; Double value to set/get second coordinate price part.
     */
    public final static ObjectProperty OBJPROP_PRICE2 = new ObjectProperty(3);
    public final static int _OBJPROP_PRICE2 = 3;
    /**
     * datetime; Datetime value to set/get third coordinate time part.
     */
    public final static ObjectProperty OBJPROP_TIME3 = new ObjectProperty(4);
    public final static int _OBJPROP_TIME3 = 4;
    /**
     * double; Double value to set/get third coordinate price part.
     */
    public final static ObjectProperty OBJPROP_PRICE3 = new ObjectProperty(5);
    public final static int _OBJPROP_PRICE3 = 5;
    /**
     * color; Color value to set/get object color.
     */
    public final static ObjectProperty OBJPROP_COLOR = new ObjectProperty(6);
    public final static int _OBJPROP_COLOR = 6;
    /**
     * int/DrawingStyle; Value is one of STYLE_SOLID, STYLE_DASH, STYLE_DOT, STYLE_DASHDOT, STYLE_DASHDOTDOT constants to set/get object line style.
     */
    public final static ObjectProperty OBJPROP_STYLE = new ObjectProperty(7);
    public final static int _OBJPROP_STYLE = 7;
    /**
     * Integer value to set/get object line width. Can be from 1 to 5.
     */
    public final static ObjectProperty OBJPROP_WIDTH = new ObjectProperty(8);
    public final static int _OBJPROP_WIDTH = 8;
    /**
     * bool; Boolean value to set/get background drawing flag for object.
     */
    public final static ObjectProperty OBJPROP_BACK = new ObjectProperty(9);
    public final static int _OBJPROP_BACK = 9;
    /**
     * bool; Boolean value to set/get ray flag of object.
     */
    public final static ObjectProperty OBJPROP_RAY = new ObjectProperty(10);
    public final static int _OBJPROP_RAY = 10;
    /**
     * bool; Boolean value to set/get ellipse flag for fibo arcs.
     */
    public final static ObjectProperty OBJPROP_ELLIPSE = new ObjectProperty(11);
    public final static int _OBJPROP_ELLIPSE = 11;
    /**
     * double; Double value to set/get scale object property.
     */
    public final static ObjectProperty OBJPROP_SCALE = new ObjectProperty(12);
    public final static int _OBJPROP_SCALE = 12;
    /**
     * double; Double value to set/get angle object property in degrees.
     */
    public final static ObjectProperty OBJPROP_ANGLE = new ObjectProperty(13);
    public final static int _OBJPROP_ANGLE = 13;
    /**
     * int/ArrowCodes; Integer value or arrow enumeration to set/get arrow code object property.
     */
    public final static ObjectProperty OBJPROP_ARROWCODE = new ObjectProperty(14);
    public final static int _OBJPROP_ARROWCODE = 14;
    /**
     * int; Value can be one or combination (bitwise addition) of object visibility constants to set/get timeframe object property.
     */
    public final static ObjectProperty OBJPROP_TIMEFRAMES = new ObjectProperty(15);
    public final static int _OBJPROP_TIMEFRAMES = 15;
    /**
     * double; Double value to set/get deviation property for Standard deviation objects.
     */
    public final static ObjectProperty OBJPROP_DEVIATION = new ObjectProperty(16);
    public final static int _OBJPROP_DEVIATION = 16;
    /**
     * int; Integer value to set/get font size for text objects.
     */
    public final static ObjectProperty OBJPROP_FONTSIZE = new ObjectProperty(100);
    public final static int _OBJPROP_FONTSIZE = 100;
    /**
     * int; Integer value to set/get anchor corner property for label objects. Must be from 0-3.
     */
    public final static ObjectProperty OBJPROP_CORNER = new ObjectProperty(101);
    public final static int _OBJPROP_CORNER = 101;
    /**
     * int; Integer value to set/get anchor X distance object property in pixels.
     */
    public final static ObjectProperty OBJPROP_XDISTANCE = new ObjectProperty(102);
    public final static int _OBJPROP_XDISTANCE = 102;
    /**
     * int; Integer value is to set/get anchor Y distance object property in pixels.
     */
    public final static ObjectProperty OBJPROP_YDISTANCE = new ObjectProperty(103);
    public final static int _OBJPROP_YDISTANCE = 103;
    /**
     * int; Integer value to set/get Fibonacci object level count. Can be from 0 to 32.
     */
    public final static ObjectProperty OBJPROP_FIBOLEVELS = new ObjectProperty(200);
    public final static int _OBJPROP_FIBOLEVELS = 200;
    /**
     * color; Color value to set/get object level line color.
     */
    public final static ObjectProperty OBJPROP_LEVELCOLOR = new ObjectProperty(201);
    public final static int _OBJPROP_LEVELCOLOR = 201;
    /**
     * int/DrawingStyle; Value is one of STYLE_SOLID, STYLE_DASH, STYLE_DOT, STYLE_DASHDOT, STYLE_DASHDOTDOT constants to set/get object level line style.
     */
    public final static ObjectProperty OBJPROP_LEVELSTYLE = new ObjectProperty(202);
    public final static int _OBJPROP_LEVELSTYLE = 202;
    /**
     * int; Integer value to set/get object level line width. Can be from 1 to 5.
     */
    public final static ObjectProperty OBJPROP_LEVELWIDTH = new ObjectProperty(203);
    public final static int _OBJPROP_LEVELWIDTH = 203;
    /**
     * int, 210+n n in [0..31]; Fibonacci object level index, where n is level index to set/get. Can be from 0 to 31.
     */
    public final static ObjectProperty OBJPROP_FIRSTLEVEL = new ObjectProperty(210);
    public final static int _OBJPROP_FIRSTLEVEL = 210;
    public int val;
    protected ObjectProperty(int val) {
        this.val = val;
    }
    public static ObjectProperty getObjectProperty(int val) {
        switch (val) {

            case 0: return OBJPROP_TIME1;
            case 1: return OBJPROP_PRICE1;
            case 2: return OBJPROP_TIME2;
            case 3: return OBJPROP_PRICE2;
            case 4: return OBJPROP_TIME3;
            case 5: return OBJPROP_PRICE3;
            case 6: return OBJPROP_COLOR;
            case 7: return OBJPROP_STYLE;
            case 8: return OBJPROP_WIDTH;
            case 9: return OBJPROP_BACK;
            case 10: return OBJPROP_RAY;
            case 11: return OBJPROP_ELLIPSE;
            case 12: return OBJPROP_SCALE;
            case 13: return OBJPROP_ANGLE;
            case 14: return OBJPROP_ARROWCODE;
            case 15: return OBJPROP_TIMEFRAMES;
            case 16: return OBJPROP_DEVIATION;
            case 100: return OBJPROP_FONTSIZE;
            case 101: return OBJPROP_CORNER;
            case 102: return OBJPROP_XDISTANCE;
            case 103: return OBJPROP_YDISTANCE;
            case 200: return OBJPROP_FIBOLEVELS;
            case 201: return OBJPROP_LEVELCOLOR;
            case 202: return OBJPROP_LEVELSTYLE;
            case 203: return OBJPROP_LEVELWIDTH;
            case 210: return OBJPROP_FIRSTLEVEL;
            default: return null;
        }
    }

    public int getVal(int mType) {
        return val;
    }
}
