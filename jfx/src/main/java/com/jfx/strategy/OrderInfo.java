package com.jfx.strategy;

import com.jfx.TradeOperation;

import java.util.Date;

/**
 * Provides information about an order.
 * User: roman
 * Date: 27/09/13
 * Time: 09:47
 */
public interface OrderInfo {
    /**
     * Returns order's ticket.
     * @since 2.1.6
     */
    long ticket();

    /**
     * Returns order's ticket.
     * @deprecated - ticket's data type should be 'long', please use {@link #ticket ticket()} method instead.
     */
    int getTicket();

    TradeOperation getType();

    Date getOpenTime();

    Date getCloseTime();

    int getMagic();

    Date getExpiration();

    double getLots();

    double getOpenPrice();

    double getClosePrice();

    double getSl();

    double getTp();

    double getProfit();

    double getCommission();

    double getSwap();

    String getSymbol();

    String getComment();

    boolean isTypeChanged();

    boolean isOpenTimeChanged();

    boolean isCloseTimeChanged();

    boolean isExpirationTimeChanged();

    boolean isLotsChanged();

    boolean isOpenPriceChanged();

    boolean isClosePriceChanged();

    boolean isStopLossChanged();

    boolean isTakeProfitChanged();

    boolean isProfitChanged();

    boolean isCommissionChanged();

    boolean isSwapChanged();

    boolean isModified();

    OrderInfo merge(OrderInfo from);

}
