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

namespace nj4x.Metatrader
{
    /// <summary>Market information identifiers</summary>
    /// 
    public enum MarketInfo
    {
        /// <summary>Low day price</summary>
        /// 
        MODE_LOW = 1,

        /// <summary>High day price</summary>
        /// 
        MODE_HIGH = 2,

        /// <summary>The last incoming tick time (last known server time)</summary>
        /// 
        MODE_TIME = 5,

        /// <summary>Last incoming bid price</summary>
        /// <remarks>For the current symbol, it is stored in the predefined variable Bid</remarks>
        MODE_BID = 9,

        /// <summary>Last incoming ask price</summary>
        /// <remarks>For the current symbol, it is stored in the predefined variable Ask</remarks>
        MODE_ASK = 10,

        /// <summary>Point size in the quote currency</summary>
        /// <remarks>For the current symbol, it is stored in the predefined variable Point</remarks>
        MODE_POINT = 11,

        /// <summary>Count of digits after decimal point in the symbol prices</summary>
        /// <remarks>For the current symbol, it is stored in the predefined variable Digits</remarks>
        MODE_DIGITS = 12,

        /// <summary>Spread value in points</summary>
        /// 
        MODE_SPREAD = 13,

        /// <summary>Stop level in points</summary>
        /// 
        MODE_STOPLEVEL = 14,

        /// <summary>Lot size in the base currency</summary>
        /// 
        MODE_LOTSIZE = 15,

        /// <summary>Tick value in the deposit currency</summary>
        /// 
        MODE_TICKVALUE = 16,

        /// <summary>Tick size in the quote currency</summary>
        /// 
        MODE_TICKSIZE = 17,

        /// <summary>Swap of the long position</summary>
        /// 
        MODE_SWAPLONG = 18,

        /// <summary>Swap of the short position</summary>
        /// 
        MODE_SWAPSHORT = 19,

        /// <summary>Market starting date (usually used for futures)</summary>
        /// 
        MODE_STARTING = 20,

        /// <summary>Market expiration date (usually used for futures)</summary>
        /// 
        MODE_EXPIRATION = 21,

        /// <summary>Trade is allowed for the symbol</summary>
        /// 
        MODE_TRADEALLOWED = 22,

        /// <summary>Minimum permitted amount of a lot</summary>
        /// 
        MODE_MINLOT = 23,

        /// <summary>Step for changing lots</summary>
        /// 
        MODE_LOTSTEP = 24,

        /// <summary>Maximum permitted amount of a lot</summary>
        /// 
        MODE_MAXLOT = 25,

        /// <summary>Swap calculation method</summary>
        /// <remarks>0 - in points; 1 - in the symbol base currency; 2 - by interest; 3 - in the margin currency.</remarks>
        MODE_SWAPTYPE = 26,

        /// <summary>Profit calculation mode</summary>
        /// <remarks>0 - Forex; 1 - CFD; 2 - Futures.</remarks>
        MODE_PROFITCALCMODE = 27,

        /// <summary>Margin calculation mode</summary>
        /// <remarks>0 - Forex; 1 - CFD; 2 - Futures; 3 - CFD for indices.</remarks>
        MODE_MARGINCALCMODE = 28,

        /// <summary>Initial margin requirements for 1 lot</summary>
        /// 
        MODE_MARGININIT = 29,

        /// <summary>Margin to maintain open positions calculated for 1 lot</summary>
        /// 
        MODE_MARGINMAINTENANCE = 30,

        /// <summary>Hedged margin calculated for 1 lot</summary>
        /// 
        MODE_MARGINHEDGED = 31,

        /// <summary>Free margin required to open 1 lot for buying</summary>
        /// 
        MODE_MARGINREQUIRED = 32,

        /// <summary>Order freeze level in points</summary>
        /// <remarks>If the execution price lies within the range defined by the freeze level, the order cannot be modified, cancelled or closed.</remarks>
        MODE_FREEZELEVEL = 33
    }
}