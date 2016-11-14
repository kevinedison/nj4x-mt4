package com.jfx;

import java.util.ArrayList;
import java.util.Date;

/**
 * Symbol's information.
 */
public class SymbolInfo {
    private MT4 conn;

    SymbolInfo(MT4 conn, String data) {
        this.conn = conn;
        SDParser p = new SDParser(data, '|');
        //
        isSelected = p.popBoolean();
        isFloatingSpread = p.popBoolean();
        //
        sessionDeals = p.popLong();
        sessionBuyOrders = p.popLong();
        sessionSellOrders = p.popLong();
        volume = p.popLong();
        volumeHigh = p.popLong();
        volumeLow = p.popLong();
        //
        digits = p.popInt();
        spread = p.popInt();
        stopsLevel = p.popInt();
        freezeLevel = p.popInt();
        //
        contractPriceCalculationMode = PriceCalculationMode.values()[p.popInt()];
        tradeMode = TradeMode.values()[p.popInt()];
        swapMode = SwapMode.values()[p.popInt()];
        swapRollover3Days = DayOfWeek.values()[p.popInt()];
        tradeExecutionMode = TradeExecutionMode.values()[p.popInt()];
        //
        time = conn.toDate(p.popDouble());
        startTime = conn.toDate(p.popDouble());
        expirationTime = conn.toDate(p.popDouble());
        //
        expirationModes = ExpirationMode.values(p.popInt());
        fillingModes = FillingMode.values(p.popInt());
        orderModes = OrderMode.values(p.popInt());
        //
        baseCurrency = p.pop();
        profitCurrency = p.pop();
        marginCurrency = p.pop();
        description = p.pop();
        path = p.pop();
        //
        bid = p.popDouble();
        bidHigh = p.popDouble();
        bidLow = p.popDouble();
        ask = p.popDouble();
        askHigh = p.popDouble();
        askLow = p.popDouble();
        last = p.popDouble();
        lastHigh = p.popDouble();
        lastLow = p.popDouble();
        point = p.popDouble();
        tradeTickValue = p.popDouble();
        tradeTickValueProfit = p.popDouble();
        tradeTickValueLoss = p.popDouble();
        tradeTickSize = p.popDouble();
        tradeContractSize = p.popDouble();
        minVolume = p.popDouble();
        maxVolume = p.popDouble();
        stepVolume = p.popDouble();
        limitVolume = p.popDouble();
        longSwap = p.popDouble();
        shortSwap = p.popDouble();
        initialMargin = p.popDouble();
        maintenanceMargin = p.popDouble();
        longMargin = p.popDouble();
        shortMargin = p.popDouble();
        limitMargin = p.popDouble();
        stopMargin = p.popDouble();
        stopLimitMargin = p.popDouble();
        sessionVolume = p.popDouble();
        sessionTurnover = p.popDouble();
        sessionInterest = p.popDouble();
        sessionBuyOrdersVolume = p.popDouble();
        sessionSellOrdersVolume = p.popDouble();
        sessionOpenPrice = p.popDouble();
        sessionClosePrice = p.popDouble();
        sessionAverageWeightedPrice = p.popDouble();
        sessionSettlementPrice = p.popDouble();
        sessionMinLimitPrice = p.popDouble();
        sessionMaxLimitPrice = p.popDouble();
    }

    public boolean isSelected, isFloatingSpread;
    public long sessionDeals, sessionBuyOrders, sessionSellOrders, volume, volumeHigh, volumeLow;
    public int digits, spread, stopsLevel, freezeLevel;
    public PriceCalculationMode contractPriceCalculationMode;
    public TradeMode tradeMode;
    public SwapMode swapMode;
    public DayOfWeek swapRollover3Days;
    public TradeExecutionMode tradeExecutionMode;
    public Date time, startTime, expirationTime;
    public ExpirationMode[] expirationModes;
    public FillingMode[] fillingModes;
    public OrderMode[] orderModes;
    public String baseCurrency, profitCurrency, marginCurrency, path, description;
    public double bid, bidHigh, bidLow, ask, askHigh, askLow, last, lastHigh, lastLow, point,
            tradeTickValue, tradeTickValueProfit, tradeTickValueLoss, tradeTickSize, tradeContractSize,
            minVolume, maxVolume, stepVolume, limitVolume, longSwap, shortSwap,
            initialMargin, maintenanceMargin, longMargin, shortMargin, limitMargin, stopMargin, stopLimitMargin,
            sessionVolume, sessionTurnover, sessionInterest, sessionBuyOrdersVolume, sessionSellOrdersVolume,
            sessionOpenPrice, sessionClosePrice, sessionAverageWeightedPrice,
            sessionSettlementPrice, sessionMinLimitPrice, sessionMaxLimitPrice;

    /**
     * @return Indication of a floating spread.
     */
    public boolean isFloatingSpread() {
        return isFloatingSpread;
    }

    /**
     * @return true if symbol is selected in Market Watch.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Information about how the margin requirements for a symbol are calculated.
     */
    public enum PriceCalculationMode {
        FOREX, FUTURES, CFD, CFD_INDEX, CFD_LEVERAGE, EXCH_STOCKS, EXCH_FUTURES, EXCH_FUTURES_FORTS
    }

    /**
     * Methods of swap calculation at position transfer
     */
    public enum SwapMode {
        DISABLED, POINTS, SYMBOL_CURRENCY, MARGIN_CURRENCY, DEPOSIT_CURRENCY,
        CURRENT_INTEREST, OPEN_INTEREST,
        CURRENT_REOPEN, BID_REOPEN
    }

    public enum ExpirationMode {
        GTC, DAY, SPECIFIED, SPECIFIED_DAY;

        public static ExpirationMode[] values(int flags) {
            ArrayList<ExpirationMode> res = new ArrayList<>();
            int masks[] = {1, 2, 4, 8};
            ExpirationMode[] values = ExpirationMode.values();
            for (int i = 0; i < masks.length; i++) {
                int mask = masks[i];
                if ((flags & mask) != 0) {
                    res.add(values[i]);
                }
            }
            ExpirationMode[] result = new ExpirationMode[res.size()];
            res.toArray(result);
            return result;
        }
    }

    public enum FillingMode {
        FILL_OR_KILL, IMMEDIATE_OR_CANCEL, RETURN;

        public static FillingMode[] values(int flags) {
            ArrayList<FillingMode> res = new ArrayList<>();
            int masks[] = {1, 2, 4};
            FillingMode[] values = FillingMode.values();
            for (int i = 0; i < masks.length; i++) {
                int mask = masks[i];
                if ((flags & mask) != 0) {
                    res.add(values[i]);
                }
            }
            FillingMode[] result = new FillingMode[res.size()];
            res.toArray(result);
            return result;
        }
    }

    public enum OrderMode {
        MARKET, LIMIT, STOP, STOP_LIMIT, SL, TP;

        public static OrderMode[] values(int flags) {
            ArrayList<OrderMode> res = new ArrayList<>();
            int masks[] = {1, 2, 4, 8, 16, 32};
            OrderMode[] values = OrderMode.values();
            for (int i = 0; i < masks.length; i++) {
                int mask = masks[i];
                if ((flags & mask) != 0) {
                    res.add(values[i]);
                }
            }
            OrderMode[] result = new OrderMode[res.size()];
            res.toArray(result);
            return result;
        }
    }

    /**
     * There are several symbol trading modes.
     */
    public enum TradeMode {
        /**
         * Trade is disabled for the symbol
         */
        DISABLED,
        /**
         * Allowed only long positions (MT5 only)
         */
        LONG_ONLY,
        /**
         * Allowed only short positions (MT5 only)
         */
        SHORT_ONLY,
        /**
         * Allowed only position close operations
         */
        CLOSE_ONLY,
        /**
         * No trade restrictions
         */
        FULL
    }

    /**
     * Possible deal execution modes for a certain symbol
     */
    public enum TradeExecutionMode {
        /**
         * Execution by request
         */
        REQUEST,
        /**
         * Instant execution
         */
        INSTANT,
        /**
         * Market execution
         */
        MARKET,
        /**
         * Exchange execution (MT5 only)
         */
        EXCHANGE
    }

    /**
     * used for specifying weekdays
     */
    public enum DayOfWeek {
        SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    }

    /**
     * @return Basic currency of a symbol
     */
    public String getBaseCurrency() {
        return baseCurrency;
    }

    /**
     * @return Profit currency of a symbol
     */
    public String getProfitCurrency() {
        return profitCurrency;
    }

    /**
     * @return Margin currency of a symbol
     */
    public String getMarginCurrency() {
        return marginCurrency;
    }

    /**
     * @return Path in the symbol tree.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return Symbol's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Bid - best sell offer
     */
    public double getBid() {
        return bid;
    }

    /**
     * @return Maximal Bid of the day
     */
    public double getBidHigh() {
        return bidHigh;
    }

    /**
     * @return Minimal Bid of the day
     */
    public double getBidLow() {
        return bidLow;
    }

    /**
     * @return Ask - best buy offer
     */
    public double getAsk() {
        return ask;
    }

    /**
     * @return Maximal Ask of the day
     */
    public double getAskHigh() {
        return askHigh;
    }

    /**
     * @return Minimal Ask of the day
     */
    public double getAskLow() {
        return askLow;
    }

    /**
     * @return Price of the last deal
     */
    public double getLast() {
        return last;
    }

    /**
     * @return Maximal Last of the day
     */
    public double getLastHigh() {
        return lastHigh;
    }

    /**
     * @return Minimal Last of the day
     */
    public double getLastLow() {
        return lastLow;
    }

    /**
     * @return Symbol point value
     */
    public double getPoint() {
        return point;
    }

    /**
     * @return Value of calculated tick price for a profitable order
     */
    public double getTradeTickValue() {
        return tradeTickValue;
    }

    /**
     * @return Calculated tick price for a profitable order
     */
    public double getTradeTickValueProfit() {
        return tradeTickValueProfit;
    }

    /**
     * @return Calculated tick price for a losing order
     */
    public double getTradeTickValueLoss() {
        return tradeTickValueLoss;
    }

    /**
     * @return minimal price change
     */
    public double getTradeTickSize() {
        return tradeTickSize;
    }

    /**
     * @return Trade contract size
     */
    public double getTradeContractSize() {
        return tradeContractSize;
    }

    /**
     * @return Minimal volume for a deal
     */
    public double getMinVolume() {
        return minVolume;
    }

    /**
     * @return Maximal volume for a deal
     */
    public double getMaxVolume() {
        return maxVolume;
    }

    /**
     * @return Minimal volume change step for deal execution
     */
    public double getStepVolume() {
        return stepVolume;
    }

    /**
     * Maximum allowed aggregate volume of an opened and pending orders in one direction (buy or sell) for the symbol.
     * <p/>
     * For example, with the limitation of 5 lots, you can have an open buy order with the volume of 5 lots and place a pending order Sell Limit with the volume of 5 lots. But in this case you cannot place a Buy Limit pending order (since the total volume in one direction will exceed the limitation) or place Sell Limit with the volume more than 5 lots.
     *
     * @return Maximum allowed aggregate volume of an opened and pending orders in one direction (buy or sell) for the symbol.
     */
    public double getLimitVolume() {
        return limitVolume;
    }

    /**
     * @return Buy order swap value
     */
    public double getLongSwap() {
        return longSwap;
    }

    /**
     * @return Sell order swap value
     */
    public double getShortSwap() {
        return shortSwap;
    }

    /**
     * Initial margin means the amount in the margin currency required for opening an order with the volume of one lot.
     * <p/>
     * It is used for checking a client's assets when he or she enters the market.
     *
     * @return Initial margin for a symbol.
     */
    public double getInitialMargin() {
        return initialMargin;
    }

    /**
     * If it is set, it sets the margin amount in the margin currency of the symbol, charged from one lot.
     * <p/>
     * It is used for checking a client's assets when his/her account state changes.
     * <p/>
     * If the maintenance margin is equal to 0, the initial margin is used.
     *
     * @return The maintenance margin for a symbol.
     */
    public double getMaintenanceMargin() {
        return maintenanceMargin;
    }

    /**
     * @return Rate of margin charging on buy orders
     */
    public double getLongMargin() {
        return longMargin;
    }

    /**
     * @return Rate of margin charging on sell orders
     */
    public double getShortMargin() {
        return shortMargin;
    }

    /**
     * @return Rate of margin charging on limit orders
     */
    public double getLimitMargin() {
        return limitMargin;
    }

    /**
     * @return Rate of margin charging on stop orders
     */
    public double getStopMargin() {
        return stopMargin;
    }

    /**
     * @return Rate of margin charging on stop limit orders
     */
    public double getStopLimitMargin() {
        return stopLimitMargin;
    }

    /**
     * @return Summary volume of current session deals
     */
    public double getSessionVolume() {
        return sessionVolume;
    }

    /**
     * @return Summary turnover of the current session
     */
    public double getSessionTurnover() {
        return sessionTurnover;
    }

    /**
     * @return Summary open interest
     */
    public double getSessionInterest() {
        return sessionInterest;
    }

    /**
     * @return Current volume of Buy orders.
     */
    public double getSessionBuyOrdersVolume() {
        return sessionBuyOrdersVolume;
    }

    /**
     * @return Current volume of Sell orders.
     */
    public double getSessionSellOrdersVolume() {
        return sessionSellOrdersVolume;
    }

    /**
     * @return Open price of the current session
     */
    public double getSessionOpenPrice() {
        return sessionOpenPrice;
    }

    /**
     * @return Close price of the current session
     */
    public double getSessionClosePrice() {
        return sessionClosePrice;
    }

    /**
     * @return Average weighted price of the current session
     */
    public double getSessionAverageWeightedPrice() {
        return sessionAverageWeightedPrice;
    }

    /**
     * @return Settlement price of the current session
     */
    public double getSessionSettlementPrice() {
        return sessionSettlementPrice;
    }

    /**
     * @return Minimal price of the current session
     */
    public double getSessionMinLimitPrice() {
        return sessionMinLimitPrice;
    }

    /**
     * @return Maximal price of the current session
     */
    public double getSessionMaxLimitPrice() {
        return sessionMaxLimitPrice;
    }

    /**
     * @return Number of deals in the current session
     */
    public long getSessionDeals() {
        return sessionDeals;
    }

    /**
     * @return Number of Buy orders at the moment
     */
    public long getSessionBuyOrders() {
        return sessionBuyOrders;
    }

    /**
     * @return Number of Sell orders at the moment
     */
    public long getSessionSellOrders() {
        return sessionSellOrders;
    }

    /**
     * @return Volume of the last deal
     */
    public long getVolume() {
        return volume;
    }

    /**
     * @return Maximal day volume
     */
    public long getVolumeHigh() {
        return volumeHigh;
    }

    /**
     * @return Minimal day volume
     */
    public long getVolumeLow() {
        return volumeLow;
    }

    /**
     * @return Digits after a decimal point
     */
    public int getDigits() {
        return digits;
    }

    /**
     * @return Spread value in points
     */
    public int getSpread() {
        return spread;
    }

    /**
     * @return Minimal indention in points from the current close price to place Stop orders
     */
    public int getStopsLevel() {
        return stopsLevel;
    }

    /**
     * @return Distance to freeze trade operations in points
     */
    public int getFreezeLevel() {
        return freezeLevel;
    }

    /**
     * @return Contract price calculation mode
     */
    public PriceCalculationMode getContractPriceCalculationMode() {
        return contractPriceCalculationMode;
    }

    /**
     * @return Order execution type
     */
    public TradeMode getTradeMode() {
        return tradeMode;
    }

    /**
     * @return Swap calculation model
     */
    public SwapMode getSwapMode() {
        return swapMode;
    }

    /**
     * @return Weekday to charge 3 days swap rollover
     */
    public DayOfWeek getSwapRollover3Days() {
        return swapRollover3Days;
    }

    /**
     * @return Deal execution mode
     */
    public TradeExecutionMode getTradeExecutionMode() {
        return tradeExecutionMode;
    }

    /**
     * @return Time of the last quote
     */
    public Date getTime() {
        return time;
    }

    /**
     * @return Date of the symbol trade beginning (usually used for futures)
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @return Date of the symbol trade end (usually used for futures)
     */
    public Date getExpirationTime() {
        return expirationTime;
    }

    /**
     * @return allowed order expiration modes
     */
    public ExpirationMode[] getExpirationModes() {
        return expirationModes;
    }

    /**
     * @return allowed order filling modes
     */
    public FillingMode[] getFillingModes() {
        return fillingModes;
    }

    /**
     * @return allowed order types
     */
    public OrderMode[] getOrderModes() {
        return orderModes;
    }
}
