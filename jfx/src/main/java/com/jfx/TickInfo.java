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
import java.util.HashMap;
import java.util.Map;

/**
 * Tick information, includes bid/ask prices, server time (truncated to seconds) and active orders ticket - profit/loss map.
 */
public class TickInfo {
    /**
     * Symbol's tick BID price.
     */
    public double bid;
    /**
     * Symbol's tick ASK price.
     */
    public double ask;
    /**
     * Time on server, truncated to a second.
     */
    public Date time;

    /**
     * Symbol-related orders map: ticket# - (profit/loss).
     * @deprecated - ticket's data type should be 'long', please use {@link #orderProfitLossMap orderProfitLossMap} instead.
     */
    public HashMap<Integer, Double> orderPlMap;

    /**
     * Symbol-related orders map: ticket# - (profit/loss).
     */
    public HashMap<Long, Double> orderProfitLossMap;

    /**
     * Construct dummy tick info to be used as a lastTickInfo in MT4.newTick() method.
     */
    public TickInfo() {
    }

    TickInfo(Date time, double bid, double ask, HashMap<Long, Double> orderPlMap) {
        this.time = time;
        this.bid = bid;
        this.ask = ask;
        this.orderProfitLossMap = orderPlMap;
    }

    void initPlMap() {
        this.orderPlMap = new HashMap<>();
        for (Map.Entry<Long, Double> e : orderProfitLossMap.entrySet()) {
            orderPlMap.put(e.getKey().intValue(), e.getValue());
        }
    }

    @Override
    public String toString() {
        return "bid=" + bid + ", ask=" + ask + ", orders=" + orderPlMap;
    }
}
