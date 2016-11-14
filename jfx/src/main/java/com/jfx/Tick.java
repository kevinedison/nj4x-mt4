package com.jfx;

import java.util.Date;

/**
 * Represents symbol's tick.
 * User: roman
 * Date: 23/06/2014
 * Time: 14:24
 */
public class Tick {
    public String symbol;
    public Date time;
    public double bid, ask;

    public Tick(String symbol, Date time, double bid, double ask) {
        this.symbol = symbol;
        this.time = time;
        this.bid = bid;
        this.ask = ask;
    }

    @Override
    public String toString() {
        return symbol + "(" + bid + "," + ask + " @ " + time + ")";
    }
}
