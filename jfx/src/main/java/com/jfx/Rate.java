package com.jfx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This structure stores information about the prices, volumes and spread.
 */
public class Rate {
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
     * Trade volume
     */
    public long realVolume;

    /**
     * Spread
     */
    public int spread;

    /**
     * Tick volume
     */
    public long tickVolume;

    /**
     * Period start time
     */
    public Date time;


    @Override
    public String toString() {
        return "" + time + " OHLC=" + open + " " + high + " " + low + " " + close;
    }

    Rate(double bid) {
        time = new Date();
        open = high = low = close = bid;
        tickVolume = 0;
        spread = 0;
        realVolume = 0;
    }

    Rate(String encodedRate, MT4 utils) {
        SDParser p = new SDParser(encodedRate, '|');
        time = utils.toDate(Integer.parseInt(p.pop()));
        open = Double.parseDouble(p.pop());
        high = Double.parseDouble(p.pop());
        low = Double.parseDouble(p.pop());
        close = Double.parseDouble(p.pop());
        tickVolume = Long.parseLong(p.pop());
        spread = Integer.parseInt(p.pop());
        realVolume = Long.parseLong(p.pop());
    }

    static List<Rate> Decode(String ratesEncoded, MT4 utils, double[] bid) {
        DDParser bp = new DDParser(ratesEncoded, MT4.ARG_BEGIN, MT4.ARG_END);
        int sz = Integer.parseInt(bp.pop());
        ArrayList<Rate> rates = new ArrayList<Rate>(sz);
        for (int i = 0; i < sz; i++) {
            Rate oi = new Rate(bp.pop(), utils);
            rates.add(oi);
        }
        String tail = bp.tail();
        if (tail == null || tail.length() == 0) {
            bid[0] = bid[1] = 0;
        } else {
            SDParser p = new SDParser(tail, '|');
            bid[0] = p.popDouble();
            bid[1] = p.popDouble();
        }

        return rates;
    }
}

