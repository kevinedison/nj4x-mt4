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
 * Timeframes where object may be shown. Used in ObjectSet() function to set OBJPROP_TIMEFRAMES property.
 */
public class ObjectVisibility {

    /**
     * Object shown is only on 1-minute charts.
     */
    public final static ObjectVisibility OBJ_PERIOD_M1 = new ObjectVisibility(1);
    public final static int _OBJ_PERIOD_M1 = 1;
    /**
     * Object shown is only on 5-minute charts.
     */
    public final static ObjectVisibility OBJ_PERIOD_M5 = new ObjectVisibility(2);
    public final static int _OBJ_PERIOD_M5 = 2;
    /**
     * Object shown is only on 15-minute charts.
     */
    public final static ObjectVisibility OBJ_PERIOD_M15 = new ObjectVisibility(4);
    public final static int _OBJ_PERIOD_M15 = 4;
    /**
     * Object shown is only on 30-minute charts.
     */
    public final static ObjectVisibility OBJ_PERIOD_M30 = new ObjectVisibility(8);
    public final static int _OBJ_PERIOD_M30 = 8;
    /**
     * Object shown is only on 1-hour charts.
     */
    public final static ObjectVisibility OBJ_PERIOD_H1 = new ObjectVisibility(16);
    public final static int _OBJ_PERIOD_H1 = 16;
    /**
     * Object shown is only on 4-hour charts.
     */
    public final static ObjectVisibility OBJ_PERIOD_H4 = new ObjectVisibility(32);
    public final static int _OBJ_PERIOD_H4 = 32;
    /**
     * Object shown is only on daily charts.
     */
    public final static ObjectVisibility OBJ_PERIOD_D1 = new ObjectVisibility(64);
    public final static int _OBJ_PERIOD_D1 = 64;
    /**
     * Object shown is only on weekly charts.
     */
    public final static ObjectVisibility OBJ_PERIOD_W1 = new ObjectVisibility(128);
    public final static int _OBJ_PERIOD_W1 = 128;
    /**
     * Object shown is only on monthly charts.
     */
    public final static ObjectVisibility OBJ_PERIOD_MN1 = new ObjectVisibility(256);
    public final static int _OBJ_PERIOD_MN1 = 256;
    /**
     * Object shown is on all timeframes.
     */
    public final static ObjectVisibility OBJ_ALL_PERIODS = new ObjectVisibility(511);
    public final static int _OBJ_ALL_PERIODS = 511;
    /**
     * Hidden object on all timeframes.
     */
    public final static ObjectVisibility OBJ_HIDDEN = new ObjectVisibility(-1);
    public final static int _OBJ_HIDDEN = -1;
    public int val;
    private ObjectVisibility(int val) {
        this.val = val;
    }
    public static ObjectVisibility getObjectVisibility(int val) {
        switch (val) {

            case 1: return OBJ_PERIOD_M1;
            case 2: return OBJ_PERIOD_M5;
            case 4: return OBJ_PERIOD_M15;
            case 8: return OBJ_PERIOD_M30;
            case 16: return OBJ_PERIOD_H1;
            case 32: return OBJ_PERIOD_H4;
            case 64: return OBJ_PERIOD_D1;
            case 128: return OBJ_PERIOD_W1;
            case 256: return OBJ_PERIOD_MN1;
            case 511: return OBJ_ALL_PERIODS;
            case -1: return OBJ_HIDDEN;
            default: return null;
        }
    }
}
