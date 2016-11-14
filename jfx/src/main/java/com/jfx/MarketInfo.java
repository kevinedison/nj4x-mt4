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
 * Market information identifiers
 */
public class MarketInfo {

    /**
     * Low day price.
     */
    public final static MarketInfo MODE_LOW = new MarketInfo(1);
    public final static int _MODE_LOW = 1;
    /**
     * High day price.
     */
    public final static MarketInfo MODE_HIGH = new MarketInfo(2);
    public final static int _MODE_HIGH = 2;
    /**
     * The last incoming tick time (last known server time).
     */
    public final static MarketInfo MODE_TIME = new MarketInfo(5);
    public final static int _MODE_TIME = 5;
    /**
     * Last incoming bid price. For the current symbol, it is stored in the predefined variable Bid
     */
    public final static MarketInfo MODE_BID = new MarketInfo(9);
    public final static int _MODE_BID = 9;
    /**
     * Last incoming ask price. For the current symbol, it is stored in the predefined variable Ask
     */
    public final static MarketInfo MODE_ASK = new MarketInfo(10);
    public final static int _MODE_ASK = 10;
    /**
     * Point size in the quote currency. For the current symbol, it is stored in the predefined variable Point
     */
    public final static MarketInfo MODE_POINT = new MarketInfo(11);
    public final static int _MODE_POINT = 11;
    /**
     * Count of digits after decimal point in the symbol prices. For the current symbol, it is stored in the predefined variable Digits
     */
    public final static MarketInfo MODE_DIGITS = new MarketInfo(12);
    public final static int _MODE_DIGITS = 12;
    /**
     * Spread value in points.
     */
    public final static MarketInfo MODE_SPREAD = new MarketInfo(13);
    public final static int _MODE_SPREAD = 13;
    /**
     * Stop level in points.
     */
    public final static MarketInfo MODE_STOPLEVEL = new MarketInfo(14);
    public final static int _MODE_STOPLEVEL = 14;
    /**
     * Lot size in the base currency.
     */
    public final static MarketInfo MODE_LOTSIZE = new MarketInfo(15);
    public final static int _MODE_LOTSIZE = 15;
    /**
     * Tick value in the deposit currency.
     */
    public final static MarketInfo MODE_TICKVALUE = new MarketInfo(16);
    public final static int _MODE_TICKVALUE = 16;
    /**
     * Tick size in the quote currency.
     */
    public final static MarketInfo MODE_TICKSIZE = new MarketInfo(17);
    public final static int _MODE_TICKSIZE = 17;
    /**
     * Swap of the long position.
     */
    public final static MarketInfo MODE_SWAPLONG = new MarketInfo(18);
    public final static int _MODE_SWAPLONG = 18;
    /**
     * Swap of the short position.
     */
    public final static MarketInfo MODE_SWAPSHORT = new MarketInfo(19);
    public final static int _MODE_SWAPSHORT = 19;
    /**
     * Market starting date (usually used for futures).
     */
    public final static MarketInfo MODE_STARTING = new MarketInfo(20);
    public final static int _MODE_STARTING = 20;
    /**
     * Market expiration date (usually used for futures).
     */
    public final static MarketInfo MODE_EXPIRATION = new MarketInfo(21);
    public final static int _MODE_EXPIRATION = 21;
    /**
     * Trade is allowed for the symbol.
     */
    public final static MarketInfo MODE_TRADEALLOWED = new MarketInfo(22);
    public final static int _MODE_TRADEALLOWED = 22;
    /**
     * Minimum permitted amount of a lot.
     */
    public final static MarketInfo MODE_MINLOT = new MarketInfo(23);
    public final static int _MODE_MINLOT = 23;
    /**
     * Step for changing lots.
     */
    public final static MarketInfo MODE_LOTSTEP = new MarketInfo(24);
    public final static int _MODE_LOTSTEP = 24;
    /**
     * Maximum permitted amount of a lot.
     */
    public final static MarketInfo MODE_MAXLOT = new MarketInfo(25);
    public final static int _MODE_MAXLOT = 25;
    /**
     * Swap calculation method. 0 - in points; 1 - in the symbol base currency; 2 - by interest; 3 - in the margin currency.
     */
    public final static MarketInfo MODE_SWAPTYPE = new MarketInfo(26);
    public final static int _MODE_SWAPTYPE = 26;
    /**
     * Profit calculation mode. 0 - Forex; 1 - CFD; 2 - Futures.
     */
    public final static MarketInfo MODE_PROFITCALCMODE = new MarketInfo(27);
    public final static int _MODE_PROFITCALCMODE = 27;
    /**
     * Margin calculation mode. 0 - Forex; 1 - CFD; 2 - Futures; 3 - CFD for indices.
     */
    public final static MarketInfo MODE_MARGINCALCMODE = new MarketInfo(28);
    public final static int _MODE_MARGINCALCMODE = 28;
    /**
     * Initial margin requirements for 1 lot.
     */
    public final static MarketInfo MODE_MARGININIT = new MarketInfo(29);
    public final static int _MODE_MARGININIT = 29;
    /**
     * Margin to maintain open positions calculated for 1 lot.
     */
    public final static MarketInfo MODE_MARGINMAINTENANCE = new MarketInfo(30);
    public final static int _MODE_MARGINMAINTENANCE = 30;
    /**
     * Hedged margin calculated for 1 lot.
     */
    public final static MarketInfo MODE_MARGINHEDGED = new MarketInfo(31);
    public final static int _MODE_MARGINHEDGED = 31;
    /**
     * Free margin required to open 1 lot for buying.
     */
    public final static MarketInfo MODE_MARGINREQUIRED = new MarketInfo(32);
    public final static int _MODE_MARGINREQUIRED = 32;
    /**
     * Order freeze level in points. If the execution price lies within the range defined by the freeze level, the order cannot be modified, cancelled or closed.
     */
    public final static MarketInfo MODE_FREEZELEVEL = new MarketInfo(33);
    public final static int _MODE_FREEZELEVEL = 33;
    public int val;

    private MarketInfo(int val) {
        this.val = val;
    }

    public static MarketInfo getMarketInfo(int val) {
        switch (val) {

            case 1:
                return MODE_LOW;
            case 2:
                return MODE_HIGH;
            case 5:
                return MODE_TIME;
            case 9:
                return MODE_BID;
            case 10:
                return MODE_ASK;
            case 11:
                return MODE_POINT;
            case 12:
                return MODE_DIGITS;
            case 13:
                return MODE_SPREAD;
            case 14:
                return MODE_STOPLEVEL;
            case 15:
                return MODE_LOTSIZE;
            case 16:
                return MODE_TICKVALUE;
            case 17:
                return MODE_TICKSIZE;
            case 18:
                return MODE_SWAPLONG;
            case 19:
                return MODE_SWAPSHORT;
            case 20:
                return MODE_STARTING;
            case 21:
                return MODE_EXPIRATION;
            case 22:
                return MODE_TRADEALLOWED;
            case 23:
                return MODE_MINLOT;
            case 24:
                return MODE_LOTSTEP;
            case 25:
                return MODE_MAXLOT;
            case 26:
                return MODE_SWAPTYPE;
            case 27:
                return MODE_PROFITCALCMODE;
            case 28:
                return MODE_MARGINCALCMODE;
            case 29:
                return MODE_MARGININIT;
            case 30:
                return MODE_MARGINMAINTENANCE;
            case 31:
                return MODE_MARGINHEDGED;
            case 32:
                return MODE_MARGINREQUIRED;
            case 33:
                return MODE_FREEZELEVEL;
            default:
                return null;
        }
    }

    public int getVal(int mType) {
        return val;
    }
}
