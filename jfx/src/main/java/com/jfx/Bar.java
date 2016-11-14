package com.jfx;

import java.util.Date;

/**
 * This structure stores OHLC information, time plus current bid/ask prices.
 */
public class Bar {
    /**
     * Current Ask price.
     */
    public double ask;

    /**
     * Current Bid price.
     */
    public double bid;

    /**
     * Close price
     */
    public double close;

    /**
     * The highest price of the period
     */
    public double high;

    /**
     * The lowest price of the period
     */
    public double low;

    /**
     * Open price
     */
    public double open;

    /**
     * Period start time
     */
    public Date time;

    Bar(Rate r, double bid, double ask) {
        time = r.time;
        open = r.open;
        high = r.high;
        low = r.low;
        close = r.close;
        this.bid = bid;
        this.ask = ask;
    }

    @Override
    public String toString() {
        return "" + time + " " + bid + "/" + ask + " OHLC=" + open + " " + high + " " + low + " " + close;
    }
}
