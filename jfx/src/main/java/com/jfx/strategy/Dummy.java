package com.jfx.strategy;

import com.jfx.ErrUnknownSymbol;
import com.jfx.MarketInfo;
import com.jfx.SelectionPool;

import java.io.IOException;
import java.util.Map;

/**
 * com.jfx.strategy.Dummy strategy for jvm inside client terminal testing.
 * User: roman
 * Date: 11/01/2015
 * Time: 11:51
 */
public class Dummy extends Strategy {
    @Override
    public void init(String symbol, int period, StrategyRunner strategyRunner) throws ErrUnknownSymbol, IOException {
        System.out.println("Dummy: init: " + symbol + ", " + period + ", " + strategyRunner);
    }

    @Override
    public long coordinationIntervalMillis() {
        return 1000;
    }

    @Override
    public void coordinate() {
        try {
            Map<Long, OrderInfo> longOrderInfoMap = orderGetAll(SelectionPool.MODE_HISTORY);
            System.out.println("Dummy: coordinate: " + marketInfo_MODE_TIME(symbol) + "> " + symbol + " bid=" + marketInfo(symbol, MarketInfo.MODE_BID) + " historyOrdersCount: " + longOrderInfoMap.size());
        } catch (ErrUnknownSymbol errUnknownSymbol) {
            errUnknownSymbol.printStackTrace();
        }
    }

    @Override
    public void deinit() {
        System.out.println("Dummy: deinit: " + symbol + ", " + period);
    }
}
