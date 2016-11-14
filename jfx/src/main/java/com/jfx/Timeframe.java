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
 * Timeframe of the chart (chart period).
 */
public class Timeframe {

    // PERIOD_M1=1
    // PERIOD_M2=2
    // PERIOD_M3=3
    // PERIOD_M4=4
    // PERIOD_M5=5
    // PERIOD_M6=6
    // PERIOD_M10=10
    // PERIOD_M12=12
    // PERIOD_M15=15
    // PERIOD_M20=20
    // PERIOD_M30=30

    /**
     * 1 minute
     */
    public final static int _PERIOD_M1 = 1;
    public final static Timeframe PERIOD_M1 = new Timeframe(_PERIOD_M1);
    /**
     * 5 minutes
     */
    public final static int _PERIOD_M5 = 5;
    public final static Timeframe PERIOD_M5 = new Timeframe(_PERIOD_M5);
    /**
     * 15 minutes
     */
    public final static int _PERIOD_M15 = 15;
    public final static Timeframe PERIOD_M15 = new Timeframe(_PERIOD_M15);
    /**
     * 30 minutes
     */
    public final static int _PERIOD_M30 = 30;
    public final static Timeframe PERIOD_M30 = new Timeframe(_PERIOD_M30);

    // PERIOD_H1=16385
    // PERIOD_H2=16386
    // PERIOD_H3=16387
    // PERIOD_H4=16388
    // PERIOD_H6=16390
    // PERIOD_H8=16392
    // PERIOD_H12=16396
    // PERIOD_D1=16408
    // PERIOD_W1=32769
    // PERIOD_MN1=49153
    /**
     * 1 hour
     */
    /*~MT4~*/public final static int _PERIOD_H1 = 60;
    //~MT5~*/public final static int _PERIOD_H1 = 16385;
    public final static Timeframe PERIOD_H1 = new Timeframe(_PERIOD_H1);
    /**
     * 4 hour
     */
    /*~MT4~*/public final static int _PERIOD_H4 = 240;
    //~MT5~*/public final static int _PERIOD_H4 = 16388;
    public final static Timeframe PERIOD_H4 = new Timeframe(_PERIOD_H4);
    /**
     * 1 Daily
     */
    /*~MT4~*/public final static int _PERIOD_D1 = 1440;
    //~MT5~*/public final static int _PERIOD_D1 = 16408;
    public final static Timeframe PERIOD_D1 = new Timeframe(_PERIOD_D1);
    /**
     * Weekly
     */
    /*~MT4~*/public final static int _PERIOD_W1 = 10080;
    //~MT5~*/public final static int _PERIOD_W1 = 32769;
    public final static Timeframe PERIOD_W1 = new Timeframe(_PERIOD_W1);
    /**
     * Monthly
     */
    /*~MT4~*/public final static int _PERIOD_MN1 = 43200;
    //~MT5~*/public final static int _PERIOD_MN1 = 49153;
    public final static Timeframe PERIOD_MN1 = new Timeframe(_PERIOD_MN1);
    /**
     * Timeframe used on the chart.
     */
    public final static Timeframe PERIOD_DEFAULT = new Timeframe(0);
    public final static int _PERIOD_DEFAULT = 0;
    public int val;

    protected Timeframe(int val) {
        this.val = val;
    }

    public static Timeframe getTimeframe(int val) {
        switch (val) {

            case _PERIOD_M1:
                return PERIOD_M1;
            case _PERIOD_M5:
                return PERIOD_M5;
            case _PERIOD_M15:
                return PERIOD_M15;
            case _PERIOD_M30:
                return PERIOD_M30;
            case _PERIOD_H1:
                return PERIOD_H1;
            case _PERIOD_H4:
                return PERIOD_H4;
            case _PERIOD_D1:
                return PERIOD_D1;
            case _PERIOD_W1:
                return PERIOD_W1;
            case _PERIOD_MN1:
                return PERIOD_MN1;
            case 0:
                return PERIOD_DEFAULT;
            default:
                return null;
        }
    }

    public int getVal(int mType) {
        return val;
    }
}
