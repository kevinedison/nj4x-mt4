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
 * Applied price constants. 
 */
public class AppliedPrice {

    /**
     * Close price
     */
    public final static AppliedPrice PRICE_CLOSE = new AppliedPrice(0);
    public final static int _PRICE_CLOSE = 0;
    /**
     * Open price
     */
    public final static AppliedPrice PRICE_OPEN = new AppliedPrice(1);
    public final static int _PRICE_OPEN = 1;
    /**
     * High price
     */
    public final static AppliedPrice PRICE_HIGH = new AppliedPrice(2);
    public final static int _PRICE_HIGH = 2;
    /**
     * Low price
     */
    public final static AppliedPrice PRICE_LOW = new AppliedPrice(3);
    public final static int _PRICE_LOW = 3;
    /**
     * Median price, (high+low)/2.
     */
    public final static AppliedPrice PRICE_MEDIAN = new AppliedPrice(4);
    public final static int _PRICE_MEDIAN = 4;
    /**
     * Typical price, (high+low+close)/3.
     */
    public final static AppliedPrice PRICE_TYPICAL = new AppliedPrice(5);
    public final static int _PRICE_TYPICAL = 5;
    /**
     * Weighted close price, (high+low+close+close)/4.
     */
    public final static AppliedPrice PRICE_WEIGHTED = new AppliedPrice(6);
    public final static int _PRICE_WEIGHTED = 6;
    public int val;
    private AppliedPrice(int val) {
        this.val = val;
    }
    public static AppliedPrice getAppliedPrice(int val) {
        switch (val) {

            case 0: return PRICE_CLOSE;
            case 1: return PRICE_OPEN;
            case 2: return PRICE_HIGH;
            case 3: return PRICE_LOW;
            case 4: return PRICE_MEDIAN;
            case 5: return PRICE_TYPICAL;
            case 6: return PRICE_WEIGHTED;
            default: return null;
        }
    }

    public int getVal(int mType) {
        return val;
    }
}
