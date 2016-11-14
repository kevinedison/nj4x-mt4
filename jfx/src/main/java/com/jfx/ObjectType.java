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
 * Object type identifier constants used with ObjectCreate(), ObjectsDeleteAll() and ObjectType() functions
 */
public class ObjectType {

    /**
     * Represents all object types.
     */
    public final static ObjectType ALL = new ObjectType(-1);
    public final static int _ALL = -1;

    /**
     * Vertical line. Uses time part of first coordinate.
     */
    public final static ObjectType OBJ_VLINE = new ObjectType(0);
    public final static int _OBJ_VLINE = 0;
    /**
     * Horizontal line. Uses price part of first coordinate.
     */
    public final static ObjectType OBJ_HLINE = new ObjectType(1);
    public final static int _OBJ_HLINE = 1;
    /**
     * Trend line. Uses 2 coordinates.
     */
    public final static ObjectType OBJ_TREND = new ObjectType(2);
    public final static int _OBJ_TREND = 2;
    /**
     * Trend by angle. Uses 1 coordinate. To set angle of line use ObjectSet() function.
     */
    public final static ObjectType OBJ_TRENDBYANGLE = new ObjectType(3);
    public final static int _OBJ_TRENDBYANGLE = 3;
    /**
     * Regression. Uses time parts of first two coordinates.
     */
    public final static ObjectType OBJ_REGRESSION = new ObjectType(4);
    public final static int _OBJ_REGRESSION = 4;
    /**
     * Channel. Uses 3 coordinates.
     */
    public final static ObjectType OBJ_CHANNEL = new ObjectType(5);
    public final static int _OBJ_CHANNEL = 5;
    /**
     * Standard deviation channel. Uses time parts of first two coordinates.
     */
    public final static ObjectType OBJ_STDDEVCHANNEL = new ObjectType(6);
    public final static int _OBJ_STDDEVCHANNEL = 6;
    /**
     * Gann line. Uses 2 coordinate, but price part of second coordinate ignored.
     */
    public final static ObjectType OBJ_GANNLINE = new ObjectType(7);
    public final static int _OBJ_GANNLINE = 7;
    /**
     * Gann fan. Uses 2 coordinate, but price part of second coordinate ignored.
     */
    public final static ObjectType OBJ_GANNFAN = new ObjectType(8);
    public final static int _OBJ_GANNFAN = 8;
    /**
     * Gann grid. Uses 2 coordinate, but price part of second coordinate ignored.
     */
    public final static ObjectType OBJ_GANNGRID = new ObjectType(9);
    public final static int _OBJ_GANNGRID = 9;
    /**
     * Fibonacci retracement. Uses 2 coordinates.
     */
    public final static ObjectType OBJ_FIBO = new ObjectType(10);
    public final static int _OBJ_FIBO = 10;
    /**
     * Fibonacci time zones. Uses 2 coordinates.
     */
    public final static ObjectType OBJ_FIBOTIMES = new ObjectType(11);
    public final static int _OBJ_FIBOTIMES = 11;
    /**
     * Fibonacci fan. Uses 2 coordinates.
     */
    public final static ObjectType OBJ_FIBOFAN = new ObjectType(12);
    public final static int _OBJ_FIBOFAN = 12;
    /**
     * Fibonacci arcs. Uses 2 coordinates.
     */
    public final static ObjectType OBJ_FIBOARC = new ObjectType(13);
    public final static int _OBJ_FIBOARC = 13;
    /**
     * Fibonacci expansions. Uses 3 coordinates.
     */
    public final static ObjectType OBJ_EXPANSION = new ObjectType(14);
    public final static int _OBJ_EXPANSION = 14;
    /**
     * Fibonacci channel. Uses 3 coordinates.
     */
    public final static ObjectType OBJ_FIBOCHANNEL = new ObjectType(15);
    public final static int _OBJ_FIBOCHANNEL = 15;
    /**
     * Rectangle. Uses 2 coordinates.
     */
    public final static ObjectType OBJ_RECTANGLE = new ObjectType(16);
    public final static int _OBJ_RECTANGLE = 16;
    /**
     * Triangle. Uses 3 coordinates.
     */
    public final static ObjectType OBJ_TRIANGLE = new ObjectType(17);
    public final static int _OBJ_TRIANGLE = 17;
    /**
     * Ellipse. Uses 2 coordinates.
     */
    public final static ObjectType OBJ_ELLIPSE = new ObjectType(18);
    public final static int _OBJ_ELLIPSE = 18;
    /**
     * Andrews pitchfork. Uses 3 coordinates.
     */
    public final static ObjectType OBJ_PITCHFORK = new ObjectType(19);
    public final static int _OBJ_PITCHFORK = 19;
    /**
     * Cycles. Uses 2 coordinates.
     */
    public final static ObjectType OBJ_CYCLES = new ObjectType(20);
    public final static int _OBJ_CYCLES = 20;
    /**
     * Text. Uses 1 coordinate.
     */
    public final static ObjectType OBJ_TEXT = new ObjectType(21);
    public final static int _OBJ_TEXT = 21;
    /**
     * Arrows. Uses 1 coordinate.
     */
    public final static ObjectType OBJ_ARROW = new ObjectType(22);
    public final static int _OBJ_ARROW = 22;
    /**
     * Text label. Uses 1 coordinate in pixels.
     */
    public final static ObjectType OBJ_LABEL = new ObjectType(23);
    public final static int _OBJ_LABEL = 23;
    public int val;

    protected ObjectType(int val) {
        this.val = val;
    }

    public static ObjectType getObjectType(int val) {
        switch (val) {
            case 0:
                return OBJ_VLINE;
            case 1:
                return OBJ_HLINE;
            case 2:
                return OBJ_TREND;
            case 3:
                return OBJ_TRENDBYANGLE;
            case 4:
                return OBJ_REGRESSION;
            case 5:
                return OBJ_CHANNEL;
            case 6:
                return OBJ_STDDEVCHANNEL;
            case 7:
                return OBJ_GANNLINE;
            case 8:
                return OBJ_GANNFAN;
            case 9:
                return OBJ_GANNGRID;
            case 10:
                return OBJ_FIBO;
            case 11:
                return OBJ_FIBOTIMES;
            case 12:
                return OBJ_FIBOFAN;
            case 13:
                return OBJ_FIBOARC;
            case 14:
                return OBJ_EXPANSION;
            case 15:
                return OBJ_FIBOCHANNEL;
            case 16:
                return OBJ_RECTANGLE;
            case 17:
                return OBJ_TRIANGLE;
            case 18:
                return OBJ_ELLIPSE;
            case 19:
                return OBJ_PITCHFORK;
            case 20:
                return OBJ_CYCLES;
            case 21:
                return OBJ_TEXT;
            case 22:
                return OBJ_ARROW;
            case 23:
                return OBJ_LABEL;
            default:
                return null;
        }
    }

    public int getVal(int mType) {
        return val; // mType == 4
    }
}
