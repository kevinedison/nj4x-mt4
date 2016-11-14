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

import java.util.Date;

/**
 * Market information variables
 */
public class MarketInformation {

    /**
     * Low day price.
     */
    public final double LOW;
    /**
     * High day price.
     */
    public final double HIGH;
    /**
     * The last incoming tick time (last known server time).
     */
    public final Date TIME;
    /**
     * Last incoming bid price. For the current symbol, it is stored in the predefined variable Bid
     */
    public final double BID;
    /**
     * Last incoming ask price. For the current symbol, it is stored in the predefined variable Ask
     */
    public final double ASK;
    /**
     * Point size in the quote currency. For the current symbol, it is stored in the predefined variable Point
     */
    public final double POINT;
    /**
     * Count of digits after decimal point in the symbol prices. For the current symbol, it is stored in the predefined variable Digits
     */
    public final double DIGITS;
    /**
     * Spread value in points.
     */
    public final double SPREAD;
    /**
     * Stop level in points.
     */
    public final double STOPLEVEL;
    /**
     * Lot size in the base currency.
     */
    public final double LOTSIZE;
    /**
     * Tick value in the deposit currency.
     */
    public final double TICKVALUE;
    /**
     * Tick size in the quote currency.
     */
    public final double TICKSIZE;
    /**
     * Swap of the long position.
     */
    public final double SWAPLONG;
    /**
     * Swap of the short position.
     */
    public final double SWAPSHORT;
    /**
     * Market starting date (usually used for futures).
     */
    public final double STARTING;
    /**
     * Market expiration date (usually used for futures).
     */
    public final double EXPIRATION;
    /**
     * Trade is allowed for the symbol.
     */
    public final double TRADEALLOWED;
    /**
     * Minimum permitted amount of a lot.
     */
    public final double MINLOT;
    /**
     * Step for changing lots.
     */
    public final double LOTSTEP;
    /**
     * Maximum permitted amount of a lot.
     */
    public final double MAXLOT;
    /**
     * Swap calculation method. 0 - in points; 1 - in the symbol base currency; 2 - by interest; 3 - in the margin currency.
     */
    public final double SWAPTYPE;
    /**
     * Profit calculation mode. 0 - Forex; 1 - CFD; 2 - Futures.
     */
    public final double PROFITCALCMODE;
    /**
     * Margin calculation mode. 0 - Forex; 1 - CFD; 2 - Futures; 3 - CFD for indices.
     */
    public final double MARGINCALCMODE;
    /**
     * Initial margin requirements for 1 lot.
     */
    public final double MARGININIT;
    /**
     * Margin to maintain open positions calculated for 1 lot.
     */
    public final double MARGINMAINTENANCE;
    /**
     * Hedged margin calculated for 1 lot.
     */
    public final double MARGINHEDGED;
    /**
     * Free margin required to open 1 lot for buying.
     */
    public final double MARGINREQUIRED;
    /**
     * Order freeze level in points. If the execution price lies within the range defined by the freeze level, the order cannot be modified, cancelled or closed.
     */
    public final double FREEZELEVEL;

    public MarketInformation(double LOW, double HIGH, Date TIME, double BID, double ASK, double POINT, double DIGITS, double SPREAD, double STOPLEVEL, double LOTSIZE, double TICKVALUE, double TICKSIZE, double SWAPLONG, double SWAPSHORT, double STARTING, double EXPIRATION, double TRADEALLOWED, double MINLOT, double LOTSTEP, double MAXLOT, double SWAPTYPE, double PROFITCALCMODE, double MARGINCALCMODE, double MARGININIT, double MARGINMAINTENANCE, double MARGINHEDGED, double MARGINREQUIRED, double FREEZELEVEL) {
        this.LOW = LOW;
        this.HIGH = HIGH;
        this.TIME = TIME;
        this.BID = BID;
        this.ASK = ASK;
        this.POINT = POINT;
        this.DIGITS = DIGITS;
        this.SPREAD = SPREAD;
        this.STOPLEVEL = STOPLEVEL;
        this.LOTSIZE = LOTSIZE;
        this.TICKVALUE = TICKVALUE;
        this.TICKSIZE = TICKSIZE;
        this.SWAPLONG = SWAPLONG;
        this.SWAPSHORT = SWAPSHORT;
        this.STARTING = STARTING;
        this.EXPIRATION = EXPIRATION;
        this.TRADEALLOWED = TRADEALLOWED;
        this.MINLOT = MINLOT;
        this.LOTSTEP = LOTSTEP;
        this.MAXLOT = MAXLOT;
        this.SWAPTYPE = SWAPTYPE;
        this.PROFITCALCMODE = PROFITCALCMODE;
        this.MARGINCALCMODE = MARGINCALCMODE;
        this.MARGININIT = MARGININIT;
        this.MARGINMAINTENANCE = MARGINMAINTENANCE;
        this.MARGINHEDGED = MARGINHEDGED;
        this.MARGINREQUIRED = MARGINREQUIRED;
        this.FREEZELEVEL = FREEZELEVEL;
    }

}
