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
 * Series array identifier used with ArrayCopySeries(), iHighest() and iLowest() functions.
 */
public class Series {

    /**
     * Open price.
     */
    public final static Series MODE_OPEN = new Series(0);
    public final static int _MODE_OPEN = 0;
    /**
     * Low price.
     */
    public final static Series MODE_LOW = new Series(1);
    public final static int _MODE_LOW = 1;
    /**
     * High price.
     */
    public final static Series MODE_HIGH = new Series(2);
    public final static int _MODE_HIGH = 2;
    /**
     * Close price.
     */
    public final static Series MODE_CLOSE = new Series(3);
    public final static int _MODE_CLOSE = 3;
    /**
     * Volume, used in iLowest() and iHighest() functions.
     */
    public final static Series MODE_VOLUME = new Series(4);
    public final static int _MODE_VOLUME = 4;
    /**
     * Bar open time, used in ArrayCopySeries() function.
     */
    public final static Series MODE_TIME = new Series(5);
    public final static int _MODE_TIME = 5;
    public int val;
    private Series(int val) {
        this.val = val;
    }
    public static Series getSeries(int val) {
        switch (val) {

            case 0: return MODE_OPEN;
            case 1: return MODE_LOW;
            case 2: return MODE_HIGH;
            case 3: return MODE_CLOSE;
            case 4: return MODE_VOLUME;
            case 5: return MODE_TIME;
            default: return null;
        }
    }

    public int getVal(int mType) {
        return val;
    }
}
