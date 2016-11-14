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
 * Uninitialize reason codes returned by UninitializeReason() function.
 */
public class UninitializeReason {

    /**
     * Script finished its execution independently.
     */
    public final static UninitializeReason REASON_NORMAL = new UninitializeReason(0);
    public final static int _REASON_NORMAL = 0;
    /**
     * Expert removed from chart.
     */
    public final static UninitializeReason REASON_REMOVE = new UninitializeReason(1);
    public final static int _REASON_REMOVE = 1;
    /**
     * Expert recompiled.
     */
    public final static UninitializeReason REASON_RECOMPILE = new UninitializeReason(2);
    public final static int _REASON_RECOMPILE = 2;
    /**
     * Symbol or timeframe changed on the chart.
     */
    public final static UninitializeReason REASON_CHARTCHANGE = new UninitializeReason(3);
    public final static int _REASON_CHARTCHANGE = 3;
    /**
     * Chart closed.
     */
    public final static UninitializeReason REASON_CHARTCLOSE = new UninitializeReason(4);
    public final static int _REASON_CHARTCLOSE = 4;
    /**
     * Inputs parameters was changed by user.
     */
    public final static UninitializeReason REASON_PARAMETERS = new UninitializeReason(5);
    public final static int _REASON_PARAMETERS = 5;
    /**
     * Other account activated.
     */
    public final static UninitializeReason REASON_ACCOUNT = new UninitializeReason(6);
    public final static int _REASON_ACCOUNT = 6;
    public int val;
    private UninitializeReason(int val) {
        this.val = val;
    }
    public static UninitializeReason getUninitializeReason(int val) {
        switch (val) {

            case 0: return REASON_NORMAL;
            case 1: return REASON_REMOVE;
            case 2: return REASON_RECOMPILE;
            case 3: return REASON_CHARTCHANGE;
            case 4: return REASON_CHARTCLOSE;
            case 5: return REASON_PARAMETERS;
            case 6: return REASON_ACCOUNT;
            default: return null;
        }
    }
}
