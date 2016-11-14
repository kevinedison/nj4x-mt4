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
 * Ichimoku Kinko Hyo identifiers used in iIchimoku() indicator call as source of requested data.
 */
public class IchimokuSource {

    /**
     * Tenkan-sen.
     */
    public final static IchimokuSource MODE_TENKANSEN = new IchimokuSource(1);
    public final static int _MODE_TENKANSEN = 1;
    /**
     * Kijun-sen.
     */
    public final static IchimokuSource MODE_KIJUNSEN = new IchimokuSource(2);
    public final static int _MODE_KIJUNSEN = 2;
    /**
     * Senkou Span A.
     */
    public final static IchimokuSource MODE_SENKOUSPANA = new IchimokuSource(3);
    public final static int _MODE_SENKOUSPANA = 3;
    /**
     * Senkou Span B.
     */
    public final static IchimokuSource MODE_SENKOUSPANB = new IchimokuSource(4);
    public final static int _MODE_SENKOUSPANB = 4;
    /**
     * Chinkou Span.
     */
    public final static IchimokuSource MODE_CHINKOUSPAN = new IchimokuSource(5);
    public final static int _MODE_CHINKOUSPAN = 5;
    public int val;
    private IchimokuSource(int val) {
        this.val = val;
    }
    public static IchimokuSource getIchimokuSource(int val) {
        switch (val) {

            case 1: return MODE_TENKANSEN;
            case 2: return MODE_KIJUNSEN;
            case 3: return MODE_SENKOUSPANA;
            case 4: return MODE_SENKOUSPANB;
            case 5: return MODE_CHINKOUSPAN;
            default: return null;
        }
    }

    public int getVal(int mType) {
        return val;
    }
}
