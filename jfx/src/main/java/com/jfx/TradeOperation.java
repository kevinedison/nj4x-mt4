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
 * Operation type for the OrderSend() function
 */
public class TradeOperation {

    /**
     * Buying position.
     */
    public final static TradeOperation OP_BUY = new TradeOperation(0);
    public final static int _OP_BUY = 0;
    /**
     * Selling position.
     */
    public final static TradeOperation OP_SELL = new TradeOperation(1);
    public final static int _OP_SELL = 1;
    /**
     * Buy limit pending position.
     */
    public final static TradeOperation OP_BUYLIMIT = new TradeOperation(2);
    public final static int _OP_BUYLIMIT = 2;
    /**
     * Sell limit pending position.
     */
    public final static TradeOperation OP_SELLLIMIT = new TradeOperation(3);
    public final static int _OP_SELLLIMIT = 3;
    /**
     * Buy stop pending position.
     */
    public final static TradeOperation OP_BUYSTOP = new TradeOperation(4);
    public final static int _OP_BUYSTOP = 4;
    /**
     * Sell stop pending position.
     */
    public final static TradeOperation OP_SELLSTOP = new TradeOperation(5);
    public final static int _OP_SELLSTOP = 5;

    public final static TradeOperation OP_DEPOSIT = new TradeOperation(6);
    public final static int _OP_DEPOSIT = 6;

    public final static TradeOperation OP_CREDIT = new TradeOperation(7);
    public final static int _OP_CREDIT = 7;

    public int val;
    private TradeOperation(int val) {
        this.val = val;
    }
    public static TradeOperation getTradeOperation(int val) {
        switch (val) {
            case 0: return OP_BUY;
            case 1: return OP_SELL;
            case 2: return OP_BUYLIMIT;
            case 3: return OP_SELLLIMIT;
            case 4: return OP_BUYSTOP;
            case 5: return OP_SELLSTOP;
            case 6: return OP_DEPOSIT;
            case 7: return OP_CREDIT;
            default: return new TradeOperation(val);
        }
    }

    @Override
    public String toString() {
        switch (val) {
            case 0: return "BUY";
            case 1: return "SELL";
            case 2: return "BUYLIMIT";
            case 3: return "SELLLIMIT";
            case 4: return "BUYSTOP";
            case 5: return "SELLSTOP";
            case 6: return "DEPOSIT";
            case 7: return "CREDIT";
            default: return "TradeOperation #" + val;
        }
    }

    public int getVal(int mType) {
        return val;
    }
}
