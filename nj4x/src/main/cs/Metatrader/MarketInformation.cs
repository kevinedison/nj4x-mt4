// Copyright (c) 2008-2014 by Gerasimenko Roman
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 
// 1. Redistribution of source code must retain the above copyright
//     notice, this list of conditions and the following disclaimer.
// 
// 2. Redistribution in binary form must reproduce the above copyright
//     notice, this list of conditions and the following disclaimer in
//     the documentation and/or other materials provided with the
//     distribution.
// 
// 3. The name "NJ4X" must not be used to endorse or promote
//     products derived from this software without prior written
//     permission.
//     For written permission, please contact roman.gerasimenko@nj4x.com
// 
// 4. Products derived from this software may not be called "NJ4X",
//     nor may "NJ4X" appear in their name, without prior written
//     permission of Gerasimenko Roman.
// 
// THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
// OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
// BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
// USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

using System;

namespace nj4x.Metatrader
{
    /// <summary>
    /// Market information variables collection
    /// </summary>
    public class MarketInformation
    {
        /// <summary>
        ///Last incoming ask price. For the current symbol, it is stored in the predefined variable Ask
        /// </summary>
        public readonly double ASK;

        /// <summary>
        ///Last incoming bid price. For the current symbol, it is stored in the predefined variable Bid
        /// </summary>
        public readonly double BID;

        /// <summary>
        ///Count of digits after decimal point in the symbol prices. For the current symbol, it is stored in the predefined variable Digits
        /// </summary>
        public readonly double DIGITS;

        /// <summary>
        ///Market expiration date (usually used for futures).
        /// </summary>
        public readonly double EXPIRATION;

        /// <summary>
        ///Order freeze level in points. If the execution price lies within the range defined by the freeze level, the order cannot be modified, cancelled or closed.
        /// </summary>
        public readonly double FREEZELEVEL;

        /// <summary>
        ///High day price.
        /// </summary>
        public readonly double HIGH;

        /// <summary>
        ///Lot size in the base currency.
        /// </summary>
        public readonly double LOTSIZE;

        /// <summary>
        ///Step for changing lots.
        /// </summary>
        public readonly double LOTSTEP;

        /// <summary>
        ///Low day price.
        /// </summary>
        public readonly double LOW;

        /// <summary>
        ///Margin calculation mode. 0 - Forex; 1 - CFD; 2 - Futures; 3 - CFD for indices.
        /// </summary>
        public readonly double MARGINCALCMODE;

        /// <summary>
        ///Hedged margin calculated for 1 lot.
        /// </summary>
        public readonly double MARGINHEDGED;

        /// <summary>
        ///Initial margin requirements for 1 lot.
        /// </summary>
        public readonly double MARGININIT;

        /// <summary>
        ///Margin to maintain open positions calculated for 1 lot.
        /// </summary>
        public readonly double MARGINMAINTENANCE;

        /// <summary>
        ///Free margin required to open 1 lot for buying.
        /// </summary>
        public readonly double MARGINREQUIRED;

        /// <summary>
        ///Maximum permitted amount of a lot.
        /// </summary>
        public readonly double MAXLOT;

        /// <summary>
        ///Minimum permitted amount of a lot.
        /// </summary>
        public readonly double MINLOT;

        /// <summary>
        ///Point size in the quote currency. For the current symbol, it is stored in the predefined variable Point
        /// </summary>
        public readonly double POINT;

        /// <summary>
        ///Profit calculation mode. 0 - Forex; 1 - CFD; 2 - Futures.
        /// </summary>
        public readonly double PROFITCALCMODE;

        /// <summary>
        ///Spread value in points.
        /// </summary>
        public readonly double SPREAD;

        /// <summary>
        ///Market starting date (usually used for futures).
        /// </summary>
        public readonly double STARTING;

        /// <summary>
        ///Stop level in points.
        /// </summary>
        public readonly double STOPLEVEL;

        /// <summary>
        ///Swap of the long position.
        /// </summary>
        public readonly double SWAPLONG;

        /// <summary>
        ///Swap of the short position.
        /// </summary>
        public readonly double SWAPSHORT;

        /// <summary>
        ///Swap calculation method. 0 - in points; 1 - in the symbol base currency; 2 - by interest; 3 - in the margin currency.
        /// </summary>
        public readonly double SWAPTYPE;

        /// <summary>
        ///Tick size in the quote currency.
        /// </summary>
        public readonly double TICKSIZE;

        /// <summary>
        ///Tick value in the deposit currency.
        /// </summary>
        public readonly double TICKVALUE;

        /// <summary>
        ///The last incoming tick time (last known server time).
        /// </summary>
        public readonly DateTime TIME;

        /// <summary>
        ///Trade is allowed for the symbol.
        /// </summary>
        public readonly double TRADEALLOWED;

        internal MarketInformation(double LOW, double HIGH, DateTime TIME, double BID, double ASK, double POINT,
                                   double DIGITS,
                                   double SPREAD, double STOPLEVEL, double LOTSIZE, double TICKVALUE, double TICKSIZE,
                                   double SWAPLONG, double SWAPSHORT, double STARTING, double EXPIRATION,
                                   double TRADEALLOWED, double MINLOT, double LOTSTEP, double MAXLOT, double SWAPTYPE,
                                   double PROFITCALCMODE, double MARGINCALCMODE, double MARGININIT,
                                   double MARGINMAINTENANCE, double MARGINHEDGED, double MARGINREQUIRED,
                                   double FREEZELEVEL)
        {
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
}