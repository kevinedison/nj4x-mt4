package com.jfx.strategy;

import java.util.Map;

/**
 * Trader's position.
 */
public interface PositionInfo {
    /**
     * This positions' active trades
     */
    Map<Long, OrderInfo> liveOrders();

    /**
     * Historical trades of the PositionInfo
     */
    Map<Long, OrderInfo> historicalOrders();

    /**
     * This positions' active trades
     * @deprecated - ticket's data type should be 'long', please use {@link #liveOrders liveOrders()} method instead.
     */
    Map<Integer, OrderInfo> getLiveOrders();

    /**
     * Historical trades of the PositionInfo
     * @deprecated - ticket's data type should be 'long', please use {@link #historicalOrders historicalOrders()} method instead.
     */
    Map<Integer, OrderInfo> getHistoricalOrders();

    public PositionChangeInfo mergePosition(PositionInfo newPositionInfo);
}
