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
 * Moving Average calculation method used with iAlligator(), iEnvelopes(), iEnvelopesOnArray, iForce(), iGator(), iMA(), iMAOnArray(), iStdDev(), iStdDevOnArray(), iStochastic() indicators.
 */
public class MovingAverageMethod {

    /**
     * Simple moving average
     */
    public final static MovingAverageMethod MODE_SMA = new MovingAverageMethod(0);
    public final static int _MODE_SMA = 0;
    /**
     * Exponential moving average
     */
    public final static MovingAverageMethod MODE_EMA = new MovingAverageMethod(1);
    public final static int _MODE_EMA = 1;
    /**
     * Smoothed moving average
     */
    public final static MovingAverageMethod MODE_SMMA = new MovingAverageMethod(2);
    public final static int _MODE_SMMA = 2;
    /**
     * Linear weighted moving average
     */
    public final static MovingAverageMethod MODE_LWMA = new MovingAverageMethod(3);
    public final static int _MODE_LWMA = 3;
    public int val;
    private MovingAverageMethod(int val) {
        this.val = val;
    }
    public static MovingAverageMethod getMovingAverageMethod(int val) {
        switch (val) {

            case 0: return MODE_SMA;
            case 1: return MODE_EMA;
            case 2: return MODE_SMMA;
            case 3: return MODE_LWMA;
            default: return null;
        }
    }

    public int getVal(int mType) {
        return val;
    }
}
