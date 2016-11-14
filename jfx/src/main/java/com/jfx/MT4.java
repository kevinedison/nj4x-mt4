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

import com.jfx.net.ByteBufferBuilder;
import com.jfx.net.UbbArgsMapper;
import com.jfx.strategy.OrderInfo;
import com.jfx.strategy.PositionInfo;
import com.jfx.strategy.StrategyRunner;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
@SuppressWarnings({"UnusedAssignment", "ConstantConditions", "Convert2Diamond", "UnusedDeclaration", "ForLoopReplaceableByForEach"})
public abstract class MT4 {
    public static final char ARG_BEGIN = '\u0001';
    public static final char ARG_END = '\u0002';
    public static final int PROTO = 0;
    StrategyRunner strategyRunner;
    StrategyRunner ordersProcessingChannel;
    private static final String T1970_01_01 = "1970.01.01 00:00";
    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy.MM.dd HH:mm:ss";
    private static final String UNEXPECTED_ERROR_OCCURRED = "Unexpected error occurred: ";
    private static final String CHECK_HTTP_DOCS_MQL4_COM_RUNTIME_ERRORS_FOR_DETAILS = ", check http://docs.mql4.com/constants/errorswarnings/errorcodes for details";
    private static final String CHECK_HTTP_DOCS_MQL5_COM_RUNTIME_ERRORS_FOR_DETAILS = ", check http://www.mql5.com/en/docs/constants/errorswarnings/errorcodes for details";

    public boolean IsConnectedToTerminal() {
        return strategyRunner != null && strategyRunner.isConnected()
                && (ordersProcessingChannel == null || ordersProcessingChannel.isConnected());
    }

    public void DumpConnectionToTerminalStatus() {
        System.out.println("Terminal connection:"
            + " strategyRunner=" + strategyRunner
            + " isStrategyRunnerConnected=" + (strategyRunner != null && strategyRunner.isConnected())
            + " ordersProcessingChannel=" + (ordersProcessingChannel)
            + " isOrdersProcessingChannelConnected=" + (ordersProcessingChannel == null || ordersProcessingChannel.isConnected())
        );
    }

    public boolean isConnectedToTerminal() {
        return IsConnectedToTerminal();
    }

    public abstract int getMType();

    private class MT4DateFormat extends SimpleDateFormat {
        private MT4DateFormat() {
            super(YYYY_MM_DD_HH_MM_SS);
//            if (getMType() == 5) {
//                setTimeZone(TimeZone.getTimeZone("GMT"));
//            }
        }
    }

    protected StrategyRunner getChannel() {
        if (strategyRunner == null) {
            throw new RuntimeException("Not connected");
        }
        return strategyRunner;
    }

    private StrategyRunner getOrdersChannel() {
        return (ordersProcessingChannel == null ? getChannel() : ordersProcessingChannel);
    }

    protected MT4 getOrdersManipulationConnection() {
        return this;
    }

    protected void returnOrdersManipulationConnection(MT4 conn) {
    }

    private String delegateToTerminal(StringBuilder command) {
        MT4 mt4 = getOrdersManipulationConnection();
        String result;
        try {
            result = mt4.getOrdersChannel().sendCommandGetResult(command);
        } finally {
            returnOrdersManipulationConnection(mt4);
        }
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    private String delegateToTerminal(ByteBufferBuilder command) {
        MT4 mt4 = getOrdersManipulationConnection();
        String result;
        try {
            result = mt4.getOrdersChannel().sendCommandGetResult(command);
        } finally {
            returnOrdersManipulationConnection(mt4);
        }
        return result;
    }

    protected long getTZCorrectionOffset(long time) {
        return time == 0 /*|| getMType() == 5*/ ? 0 : -TimeZone.getDefault().getOffset(time);
    }

    public MT4() {
    }

    public void setStrategyRunner(StrategyRunner strategyRunner) {
        if (this.strategyRunner != null) {
            this.strategyRunner.close();
        }
        this.strategyRunner = strategyRunner;
    }

    public void setOrdersProcessingChannel(StrategyRunner strategyRunner) {
        if (this.ordersProcessingChannel != null) {
            this.ordersProcessingChannel.close();
        }
        this.ordersProcessingChannel = strategyRunner;
    }

    public StrategyRunner getStrategyRunner() {
        return strategyRunner;
    }

    protected StrategyRunner getOrdersProcessingChannel() {
        return ordersProcessingChannel;
    }

    /**
     * Returns the The last incoming tick time (last known server time).
     *
     * @param symbol symbol to get last know tick time of.
     * @return Returns the last The last incoming tick time (last known server time).
     * @throws com.jfx.ErrUnknownSymbol
     */
    public java.util.Date marketInfo_MODE_TIME(
            String symbol
    ) throws ErrUnknownSymbol {
        java.util.Date res;
        double dTime = marketInfo(symbol, MarketInfo.MODE_TIME);
        res = toDate(dTime);
        return res;
    }

    public Date toDate(double dTime) {
        long time = ((long) dTime) * 1000L;
        return new Date(time + getTZCorrectionOffset(time));
    }

    public int fromDate(Date dTime) {
        long time = dTime.getTime();
        return time == 0 ? 0 : (int) ((time - getTZCorrectionOffset(time)) / 1000L);
    }

    /**
     * The system calls RefreshRates() automatically before each MT4 method, - you can change this behaviour by setting autoRefresh to false.
     *
     * @param autoRefreshRates true|false
     */
    public void setAutoRefreshRates(boolean autoRefreshRates) {
        StringBuilder command = new StringBuilder("10000 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(autoRefreshRates ? 1 : 0).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("setAutoRefreshRates(");
            signature.append("autoRefreshRates=").append(autoRefreshRates);
            signature.append(')');
            switch (error) {
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
    }

    private String getUnexpectedErrorDesc(int error) {
        return error > 50000 ? "" + (error - 50000) + CHECK_HTTP_DOCS_MQL5_COM_RUNTIME_ERRORS_FOR_DETAILS
                : "" + error + CHECK_HTTP_DOCS_MQL4_COM_RUNTIME_ERRORS_FOR_DETAILS;
    }

    /**
     * Returns all market variables for the specified symbol.
     *
     * @param symbol symbol to get market info for
     * @return symbol's market information
     * @throws ErrUnknownSymbol if symbol is unknown to the broker
     */
    public MarketInformation marketInfo(
            String symbol
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("10001 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("marketInfo(");
            signature.append("symbol=").append(symbol);
            signature.append(", type=ALL");
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        MarketInformation res;
        //
        // decompose result
        //
        String[] mi = result.split("\\|");
        int p = 0;
        res = new MarketInformation(
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                toDate(Double.parseDouble(mi[p++])),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++])
        );
        //
        return res;
    }

    /**
     * Returns new tick information (time,bid,ask,orders P/L) for the specified symbol.
     *
     * @param symbol   of a new tick information
     * @param lastTick last tick information to check difference with
     * @return new tick information
     * @throws ErrUnknownSymbol if symbols is not known to the broker
     */
    public TickInfo newTick(
            String symbol,
            TickInfo lastTick
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("10002 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append((lastTick == null || lastTick.time == null) ? 0 : fromDate(lastTick.time)).append(ARG_END);
        command.append(ARG_BEGIN).append(lastTick == null ? 0 : lastTick.bid).append(ARG_END);
        command.append(ARG_BEGIN).append(lastTick == null ? 0 : lastTick.ask).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("newTick(");
            signature.append("symbol=").append(symbol);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        //
        // decompose result
        //
        String[] mi = result.split("\\|");
        int p = 0;
        HashMap<Long, Double> orderPlMap = new HashMap<Long, Double>();
        TickInfo res = new TickInfo(
                toDate(Double.parseDouble(mi[p++])),
                Double.parseDouble(mi[p++]),
                Double.parseDouble(mi[p++]),
                orderPlMap
        );
        int max = Integer.parseInt(mi[p++]);
        for (int i = 0; i < max; ++i) {
            orderPlMap.put(
                    Long.parseLong(mi[p++]),
                    Double.parseDouble(mi[p++])
            );
        }
        res.initPlMap();//todo remove as deprecated
        //
        return res;
    }

    /**
     * Returns available ticks for market watch selected symbols (see #symbolSelect)
     *
     * @return new ticks for the symbols from MarketWatch
     */
    public ArrayList<Tick> getTicks() {
        StringBuilder command = new StringBuilder("10012 ");
        //
        // compose command
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("getTicks(");
            signature.append(')');
            throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        //
        // decompose result
        //
        String[] mi = result.split("\\|");
        ArrayList<Tick> r = new ArrayList<Tick>();
        for (int i = 0; i < mi.length; ) {
            String s = mi[i];
            r.add(new Tick(mi[i++],
                    toDate(Double.parseDouble(mi[i++])),
                    Double.parseDouble(mi[i++]),
                    Double.parseDouble(mi[i++])
            ));
        }
        //
        return r;
    }

    /**
     * The function selects available information about the order.
     *
     * @param index  Order index or order ticket depending on the second parameter.
     * @param select Selecting flags. It can be any of the following values: SELECT_BY_POS - index in the order pool, SELECT_BY_TICKET - index is order ticket.
     * @param pool   Optional order pool index. Used when the selected parameter is SELECT_BY_POS. It can be any of the following values: MODE_TRADES (default)- order selected from trading pool(opened and pending orders), MODE_HISTORY - order selected from history pool (closed and canceled order).
     * @return It returns order information if the function succeeds or <code>null</code> if the function fails.
     */
    public OrderInfo orderGet(
            long index,
            SelectionType select,
            SelectionPool pool
    ) {
        StringBuilder command = new StringBuilder("10003 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(index).append(ARG_END);
        command.append(ARG_BEGIN).append(select.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(pool.getVal(getMType())).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderGet(");
            signature.append("index=").append(index);
            signature.append(", select=").append(select.getVal(getMType()));
            signature.append(", pool=").append(pool.getVal(getMType()));
            signature.append(')');
            switch (error) {
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        //
        // decompose result
        //
        if (result.length() <= 1) { // "0" unsuccessful orderSelect
            return null;
        } else {
            return new OrderImpl(new DDParser(result, ARG_BEGIN, ARG_END).pop(), this);
        }
    }

    /**
     * Returns information about new, modified, closed or deleted orders.
     *
     * @param prevPositionInfo position information to base modifications calculatation at
     * @return New information about created, modified, closed or deleted orders.
     */
    public PositionInfo newPosition(PositionInfo prevPositionInfo) {
        StringBuilder command = new StringBuilder("10004 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(prevPositionInfo == null ? -1 : ((PositionImpl) prevPositionInfo).getTCount()).append(ARG_END);
        command.append(ARG_BEGIN).append(prevPositionInfo == null ? -1 : ((PositionImpl) prevPositionInfo).getHCount()).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("newPosition()");
            if (error != 0) {
                throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        //
        // decompose result
        //
        PositionImpl res = new PositionImpl(result, this);
        //
        return res;
    }

    /**
     * The function provides information about all orders from the selected pool.
     *
     * @param pool order pool index, it can be any of the following values: MODE_TRADES (default)- order selected from trading pool(opened and pending orders), MODE_HISTORY - order selected from history pool (closed and canceled order).
     * @return It returns information about all orders from the selected pool.
     */
    public java.util.Map<Long, OrderInfo> orderGetAll(SelectionPool pool) {
        return orderGetAll(pool, null, null);
    }

    /**
     * The function provides information about all orders from the selected pool.
     *
     * @param pool order pool index, it can be any of the following values: MODE_TRADES (default)- order selected from trading pool(opened and pending orders), MODE_HISTORY - order selected from history pool (closed and canceled order).
     * @param dateFrom filter orders by open date (for live orders) or close date (for historical orders)
     * @param dateTo filter orders by open date (for live orders) or close date (for historical orders)
     * @return It returns information about all orders from the selected pool.
     */
    public java.util.Map<Long, OrderInfo> orderGetAll(SelectionPool pool, Date dateFrom, Date dateTo) {
        StringBuilder command = new StringBuilder("10005 ");
        //
        // compose command
        //
        pool = pool == null ? SelectionPool.MODE_TRADES : pool;
        command.append(ARG_BEGIN).append(pool.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(dateFrom)).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(dateTo)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderGetAll(");
            signature.append("pool=").append(pool.getVal(getMType()));
            signature.append(", dateFrom=").append(dateFrom);
            signature.append(", dateTo=").append(dateTo);
            signature.append(')');
            switch (error) {
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        //
        // decompose result
        //
        PositionImpl res = new PositionImpl(result, this);
        //
        return pool == SelectionPool.MODE_TRADES ? res.liveOrders() : res.historicalOrders();
    }

    /**
     * Gets history data of Rate structure of a specified symbol-period
     *     in specified quantity. The elements ordering of the copied data is from present to the past,
     *     i.e., starting position of 0 means the current bar.
     * @see Rate
     * @param symbol    Symbol name
     * @param timeframe Period.
     * @param startPos The start position for the first element to copy.
     * @param count Data count to copy.
     * @param startTime Bar time for the first element to copy.
     * @param stopTime Bar time, corresponding to the last element to copy.
     * @return History data of a specified symbol-period. Elements are ordered from the oldest (first element) to the newest (last element)
     */
    private List<Rate> copyRates(String symbol, Timeframe timeframe, int startPos, int count, Date startTime, Date stopTime, double[] bidAsk) {
        StringBuilder command = new StringBuilder("10006 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(startPos).append(ARG_END);
        command.append(ARG_BEGIN).append(count).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(startTime)).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(stopTime)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("copyRates(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", startPos=").append(startPos);
            signature.append(", count=").append(count);
            signature.append(", startTime=").append(startTime);
            signature.append(", stopTime=").append(stopTime);
            signature.append(')');
            switch (error) {
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        //
        // decompose result
        //
        return Rate.Decode(result, this, bidAsk);
    }

    /**
     * Gets history data of Rate structure of a specified symbol-period
     *     in specified quantity. The elements ordering of the copied data is from present to the past,
     *     i.e., starting position of 0 means the current bar.
     * @see Rate
     * @param symbol    Symbol name
     * @param timeframe Period.
     * @param startPos The start position for the first element to copy.
     * @param count Data count to copy.
     * @return History data of a specified symbol-period. Elements are ordered from the oldest (first element) to the newest (last element)
     */
    public List<Rate> copyRates(String symbol, Timeframe timeframe, int startPos, int count) {
        double[] ba = new double[2];
        return copyRates(symbol, timeframe, startPos, count, null, null, ba);
    }

    /**
     * Gets history data of Rate structure of a specified symbol-period
     *     in specified quantity. The elements ordering of the copied data is from present to the past,
     *     i.e., starting position of 0 means the current bar.
     * @see Rate
     * @param symbol    Symbol name
     * @param timeframe Period.
     * @param startTime Bar time for the first element to copy.
     * @param count Data count to copy.
     * @return History data of a specified symbol-period. Elements are ordered from the oldest (first element) to the newest (last element)
     */
    public List<Rate> copyRates(String symbol, Timeframe timeframe, Date startTime, int count) {
        double[] ba = new double[2];
        return copyRates(symbol, timeframe, 0, count, startTime, null, ba);
    }

    /**
     * Gets history data of Rate structure of a specified symbol-period
     *     in specified quantity. The elements ordering of the copied data is from present to the past,
     *     i.e., starting position of 0 means the current bar.
     * @see Rate
     * @param symbol    Symbol name
     * @param timeframe Period.
     * @param startTime Bar time for the first element to copy.
     * @param stopTime Bar time, corresponding to the last element to copy.
     * @return History data of a specified symbol-period. Elements are ordered from the oldest (first element) to the newest (last element)
     */
    public List<Rate> copyRates(String symbol, Timeframe timeframe, Date startTime, Date stopTime) {
        double[] ba = new double[2];
        return copyRates(symbol, timeframe, 0, 0, startTime, stopTime, ba);
    }

    /**
     * Gets current "Bar" of a specified symbol-period.
     * @see Bar
     * @param symbol    Symbol name
     * @param timeframe Period.
     * @return Current Bar info
     */
    public Bar getBar(String symbol, Timeframe timeframe)
    {
        double[] ba = new double[2];
        List<Rate> copyRates = copyRates(symbol, timeframe, 0, 1, null, null, ba);
        return copyRates.size() > 0 ? new Bar(copyRates.get(0), ba[0], ba[1]) : new Bar(new Rate(ba[0]), ba[0], ba[1]);
    }

    /**
     * Returns the number of bars on the specified chart. For the current chart, the information about the amount of bars is in the predefined variable named Bars.
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @return Returns the number of bars on the specified chart. For the current chart, the information about the amount of bars is in the predefined variable named Bars.
     * @throws com.jfx.ErrHistoryWillUpdated requested history is in updating state
     * @throws com.jfx.ErrUnknownSymbol      symbol is unknown to the server
     */
    public int iBars(
            String symbol,
            Timeframe timeframe
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("0 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iBars(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(')');
            switch (error) {
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Search for bar by open time. The function returns bar shift with the open time specified. If the bar having the specified open time is missing, the function will return -1 or the nearest bar shift depending on the exact.
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param time      value to find (bar's open time).
     * @param exact     Return mode when bar not found. false - iBarShift returns nearest. true - iBarShift returns -1.
     * @return Search for bar by open time. The function returns bar shift with the open time specified. If the bar having the specified open time is missing, the function will return -1 or the nearest bar shift depending on the exact.
     * @throws com.jfx.ErrIncorrectSeriesarrayUsing
     * @throws com.jfx.ErrUnknownSymbol
     * @throws com.jfx.ErrHistoryWillUpdated
     */
    public int iBarShift(
            String symbol,
            Timeframe timeframe,
            java.util.Date time,
            boolean exact
    ) throws ErrIncorrectSeriesarrayUsing, ErrUnknownSymbol, ErrHistoryWillUpdated {
        String result = null;
        //
        // compose command
        //
        switch (PROTO) {
            case 0:
                StringBuilder command = null;
                command = new StringBuilder("1 ");
                command.append(ARG_BEGIN).append(symbol).append(ARG_END);
                command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
                command.append(ARG_BEGIN).append(timeConvert(time)).append(ARG_END);
                command.append(ARG_BEGIN).append(exact ? 1 : 0).append(ARG_END);
                result = getChannel().sendCommandGetResult(command);
                break;
//            case 1:
//                UniArgsMapper m1 = null;
//                m1 = new UniArgsMapper("1 ");
//                m1.append(symbol);
//                m1.append(timeframe.getVal(getMType()));
//                m1.append(time == null || time.getTime() == 0 ? T1970_01_01 : new MT4DateFormat().format(time));
//                m1.append(exact ? 1 : 0);
//                result = getChannel().sendCommandGetResult(m1);
//                break;
            case 2:
                UbbArgsMapper m2 = null;
                m2 = new UbbArgsMapper(1);
                m2.append(symbol);
                m2.append(timeframe.getVal(getMType()));
                m2.append(timeConvert(time));
                m2.append(exact ? 1 : 0);
                result = getChannel().sendCommandGetResult(m2);
                break;
        }
        //
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iBarShift(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", time=").append(timeConvert(time));
            signature.append(", exact=").append(exact ? 1 : 0);
            signature.append(')');
            switch (error) {
                case 4054:
                    throw new ErrIncorrectSeriesarrayUsing(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }

    /**
     * Returns Close value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0.For the current chart, the information about close prices is in the predefined array named Close[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Close value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0.For the current chart, the information about close prices is in the predefined array named Close[].
     * @throws com.jfx.ErrHistoryWillUpdated
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iClose(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("2 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iClose(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns High value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about high prices is in the predefined array named High[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns High value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about high prices is in the predefined array named High[].
     * @throws com.jfx.ErrHistoryWillUpdated
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iHigh(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("3 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iHigh(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns Low value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about low prices is in the predefined array named Low[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Low value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about low prices is in the predefined array named Low[].
     * @throws com.jfx.ErrHistoryWillUpdated
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iLow(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("4 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iLow(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns Open value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about open prices is in the predefined array named Open[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Open value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about open prices is in the predefined array named Open[].
     * @throws com.jfx.ErrHistoryWillUpdated
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iOpen(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("5 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iOpen(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns Tick Volume value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about bars tick volumes is in the predefined array named Volume[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Tick Volume value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about bars tick volumes is in the predefined array named Volume[].
     * @throws com.jfx.ErrHistoryWillUpdated
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iVolume(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("6 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iVolume(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns Time value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about bars open times is in the predefined array named Time[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Time value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about bars open times is in the predefined array named Time[].
     * @throws com.jfx.ErrHistoryWillUpdated
     * @throws com.jfx.ErrUnknownSymbol
     */
    public java.util.Date iTime(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("7 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iTime(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        java.util.Date res;
        //
        // decompose result
        //
        long time = Long.parseLong(result) * 1000;
        res = new java.util.Date(time + getTZCorrectionOffset(time));
        return res;
    }


    /**
     * Returns the shift of the least value over a specific number of periods depending on type.
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param type      Series array identifier. It can be any of Series array identifier enumeration values.
     * @param count     Number of periods (in direction from the start bar to the back one) on which the calculation is carried out.
     * @param start     Shift showing the bar, relative to the current bar, that the data should be taken from.
     * @return Returns the shift of the least value over a specific number of periods depending on type.
     * @throws com.jfx.ErrHistoryWillUpdated
     * @throws com.jfx.ErrUnknownSymbol
     * @throws com.jfx.ErrIncorrectSeriesarrayUsing
     */
    public int iLowest(
            String symbol,
            Timeframe timeframe,
            Series type,
            int count,
            int start
    ) throws ErrIncorrectSeriesarrayUsing, ErrHistoryWillUpdated, ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("8 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(type.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(count).append(ARG_END);
        command.append(ARG_BEGIN).append(start).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iLowest(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", type=").append(type.getVal(getMType()));
            signature.append(", count=").append(count);
            signature.append(", start=").append(start);
            signature.append(')');
            switch (error) {
                case 4054:
                    throw new ErrIncorrectSeriesarrayUsing(signature.toString());
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the shift of the maximum value over a specific number of periods depending on type.
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param type      Series array identifier. It can be any of Series array identifier enumeration values.
     * @param count     Number of periods (in direction from the start bar to the back one) on which the calculation is carried out.
     * @param start     Shift showing the bar, relative to the current bar, that the data should be taken from.
     * @return Returns the shift of the maximum value over a specific number of periods depending on type.
     * @throws com.jfx.ErrIncorrectSeriesarrayUsing
     * @throws com.jfx.ErrHistoryWillUpdated
     * @throws com.jfx.ErrUnknownSymbol
     */
    public int iHighest(
            String symbol,
            Timeframe timeframe,
            Series type,
            int count,
            int start
    ) throws ErrIncorrectSeriesarrayUsing, ErrHistoryWillUpdated, ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("9 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(type.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(count).append(ARG_END);
        command.append(ARG_BEGIN).append(start).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iHighest(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", type=").append(type.getVal(getMType()));
            signature.append(", count=").append(count);
            signature.append(", start=").append(start);
            signature.append(')');
            switch (error) {
                case 4054:
                    throw new ErrIncorrectSeriesarrayUsing(signature.toString());
                case 4066:
                case 54401:
                    throw new ErrHistoryWillUpdated(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns balance value of the current account (the amount of money on the account).
     *
     * @return Returns balance value of the current account (the amount of money on the account).
     */
    public double accountBalance(
    ) {
        StringBuilder command = new StringBuilder("10 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountBalance(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns credit value of the current account.
     *
     * @return Returns credit value of the current account.
     */
    public double accountCredit(
    ) {
        StringBuilder command = new StringBuilder("11 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountCredit(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns the brokerage company name where the current account was registered.
     *
     * @return Returns the brokerage company name where the current account was registered.
     */
    public String accountCompany(
    ) {
        StringBuilder command = new StringBuilder("12 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountCompany(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Returns currency name of the current account.
     *
     * @return Returns currency name of the current account.
     */
    public String accountCurrency(
    ) {
        StringBuilder command = new StringBuilder("13 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountCurrency(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Returns equity value of the current account. Equity calculation depends on trading server settings.
     *
     * @return Returns equity value of the current account. Equity calculation depends on trading server settings.
     */
    public double accountEquity(
    ) {
        StringBuilder command = new StringBuilder("14 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountEquity(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns free margin value of the current account.
     *
     * @return Returns free margin value of the current account.
     */
    public double accountFreeMargin(
    ) {
        StringBuilder command = new StringBuilder("15 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountFreeMargin(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns margin value of the current account.
     *
     * @return Returns margin value of the current account.
     */
    public double accountMargin(
    ) {
        StringBuilder command = new StringBuilder("16 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountMargin(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns the current account name.
     *
     * @return Returns the current account name.
     */
    public String accountName(
    ) {
        StringBuilder command = new StringBuilder("17 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountName(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Returns the number of the current account
     *
     * @return Returns the number of the current account
     */
    public int accountNumber(
    ) {
        StringBuilder command = new StringBuilder("18 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountNumber(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns profit value of the current account.
     *
     * @return Returns profit value of the current account.
     */
    public double accountProfit(
    ) {
        StringBuilder command = new StringBuilder("19 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountProfit(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * The function returns the last occurred error, then the value of special last_error variable where the last error code is stored will be zeroized. So, the next call for GetLastError() will return 0.
     *
     * @return The function returns the last occurred error, then the value of special last_error variable where the last error code is stored will be zeroized. So, the next call for GetLastError() will return 0.
     */
    public int getLastError(
    ) {
        StringBuilder command = new StringBuilder("20 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("getLastError(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * The function returns the status of the main connection between client terminal and server that performs data pumping. It returns TRUE if connection to the server was successfully established, otherwise, it returns FALSE.
     *
     * @return The function returns the status of the main connection between client terminal and server that performs data pumping. It returns TRUE if connection to the server was successfully established, otherwise, it returns FALSE.
     */
    public boolean isConnected(
    ) {
        if (!isConnectedToTerminal()) {
            return false;
        }

        StringBuilder command = new StringBuilder("21 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isConnected(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns TRUE if the expert runs on a demo account, otherwise returns FALSE.
     *
     * @return Returns TRUE if the expert runs on a demo account, otherwise returns FALSE.
     */
    public boolean isDemo(
    ) {
        StringBuilder command = new StringBuilder("22 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isDemo(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns TRUE if expert runs in the testing mode, otherwise returns FALSE.
     *
     * @return Returns TRUE if expert runs in the testing mode, otherwise returns FALSE.
     */
    public boolean isTesting(
    ) {
        String result = null;
        //
        // compose command
        //
        switch (PROTO) {
            case 0:
                StringBuilder command = null;
                command = new StringBuilder("23 ");
                result = getChannel().sendCommandGetResult(command);
                break;
//            case 1:
//                UniArgsMapper m1 = null;
//                m1 = new UniArgsMapper("23 ");
//                result = getChannel().sendCommandGetResult(m1);
//                break;
            case 2:
                UbbArgsMapper m2 = null;
                m2 = new UbbArgsMapper(23);
                result = getChannel().sendCommandGetResult(m2);
                break;
        }
        //
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isTesting(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns TRUE if the expert is tested with checked 'Visual Mode' button, otherwise returns FALSE.
     *
     * @return Returns TRUE if the expert is tested with checked 'Visual Mode' button, otherwise returns FALSE.
     */
    public boolean isVisualMode(
    ) {
        StringBuilder command = new StringBuilder("24 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isVisualMode(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * The GetTickCount() function retrieves the number of milliseconds that have elapsed since the system was started. It is limited to the resolution of the system timer.
     *
     * @return The GetTickCount() function retrieves the number of milliseconds that have elapsed since the system was started. It is limited to the resolution of the system timer.
     */
    public long getTickCount(
    ) {
        StringBuilder command = new StringBuilder("25 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("getTickCount(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        long res;
        //
        // decompose result
        //
        res = Long.parseLong(result);
        return res;
    }


    /**
     * The function outputs the comment defined by the user in the left top corner of the chart.
     *
     * @param comments User defined comment.
     */
    public void comment(
            String comments
    ) {
        StringBuilder command = new StringBuilder("26 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(comments)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("comment(");
            signature.append("comments=").append(comments);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;

        //
        // decompose result
        //


    }


    /**
     * Returns specified market information for the symbol
     *
     * @param symbol symbol to get information for
     * @param type   market information type
     * @return specified market information for the symbol
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double marketInfo(
            String symbol,
            MarketInfo type
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("27 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(type.getVal(getMType())).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("marketInfo(");
            signature.append("symbol=").append(symbol);
            signature.append(", type=").append(type.getVal(getMType()));
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Prints a message to the experts log
     *
     * @param comments User defined message.
     */
    public void print(
            String comments
    ) {
        StringBuilder command = new StringBuilder("28 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(comments)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("print(");
            signature.append("comments=").append(comments);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;

        //
        // decompose result
        //


    }


    /**
     * Returns the current day of the month, i.e., the day of month of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current day of the month, i.e., the day of month of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int day(
    ) {
        StringBuilder command = new StringBuilder("29 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("day(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the current zero-based day of the week (0-Sunday,1,2,3,4,5,6) of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current zero-based day of the week (0-Sunday,1,2,3,4,5,6) of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int dayOfWeek(
    ) {
        StringBuilder command = new StringBuilder("30 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("dayOfWeek(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the current day of the year (1 means 1 January,..,365(6) does 31 December), i.e., the day of year of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current day of the year (1 means 1 January,..,365(6) does 31 December), i.e., the day of year of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int dayOfYear(
    ) {
        StringBuilder command = new StringBuilder("31 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("dayOfYear(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the hour (0,1,2,..23) of the last known server time by the moment of the program start (this value will not change within the time of the program execution). Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the hour (0,1,2,..23) of the last known server time by the moment of the program start (this value will not change within the time of the program execution). Note: At the testing, the last known server time is modelled.
     */
    public int hour(
    ) {
        StringBuilder command = new StringBuilder("32 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("hour(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the current minute (0,1,2,..59) of the last known server time by the moment of the program start (this value will not change within the time of the program execution).
     *
     * @return Returns the current minute (0,1,2,..59) of the last known server time by the moment of the program start (this value will not change within the time of the program execution).
     */
    public int minute(
    ) {
        StringBuilder command = new StringBuilder("33 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("minute(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the current month as number (1-January,2,3,4,5,6,7,8,9,10,11,12), i.e., the number of month of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current month as number (1-January,2,3,4,5,6,7,8,9,10,11,12), i.e., the number of month of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int month(
    ) {
        StringBuilder command = new StringBuilder("34 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("month(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the amount of seconds elapsed from the beginning of the current minute of the last known server time by the moment of the program start (this value will not change within the time of the program execution).
     *
     * @return Returns the amount of seconds elapsed from the beginning of the current minute of the last known server time by the moment of the program start (this value will not change within the time of the program execution).
     */
    public int seconds(
    ) {
        StringBuilder command = new StringBuilder("35 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("seconds(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the last known server time (time of incoming of the latest quote) as number of seconds elapsed from 00:00 January 1, 1970. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the last known server time (time of incoming of the latest quote) as number of seconds elapsed from 00:00 January 1, 1970. Note: At the testing, the last known server time is modelled.
     */
    public java.util.Date timeCurrent(
    ) {
        StringBuilder command = new StringBuilder("36 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("timeCurrent(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        java.util.Date res;
        //
        // decompose result
        //
        long time = Long.parseLong(result) * 1000;
        res = new java.util.Date(time + getTZCorrectionOffset(time));
        return res;
    }


    /**
     * Returns the current year, i.e., the year of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current year, i.e., the year of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int year(
    ) {
        StringBuilder command = new StringBuilder("37 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("year(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     *
     * @param name   Object unique name.
     * @param type   Object type. It can be any of the Object type enumeration values.
     * @param window Index of the window where the object will be added. Window index must exceed or equal to 0 and be less than WindowsTotal().
     * @param time1  Time part of the first point.
     * @param price1 Price part of the first point.
     * @param time2  Time part of the second point.
     * @param price2 Price part of the second point.
     * @param time3  Time part of the third point.
     * @param price3 Price part of the third point.
     * @return Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     */
    public boolean objectCreate(
            String name,
            ObjectType type,
            int window,
            java.util.Date time1,
            double price1,
            java.util.Date time2,
            double price2,
            java.util.Date time3,
            double price3
    ) {
        StringBuilder command = new StringBuilder("38 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(type.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(window).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(time1)).append(ARG_END);
        command.append(ARG_BEGIN).append(price1).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(time2)).append(ARG_END);
        command.append(ARG_BEGIN).append(price2).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(time3)).append(ARG_END);
        command.append(ARG_BEGIN).append(price3).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectCreate(");
            signature.append("name=").append(name);
            signature.append(", type=").append(type.getVal(getMType()));
            signature.append(", window=").append(window);
            signature.append(", time1=").append(timeConvert(time1));
            signature.append(", price1=").append(price1);
            signature.append(", time2=").append(timeConvert(time2));
            signature.append(", price2=").append(price2);
            signature.append(", time3=").append(timeConvert(time3));
            signature.append(", price3=").append(price3);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     *
     * @param name   Object unique name.
     * @param type   Object type. It can be any of the Object type enumeration values.
     * @param window Index of the window where the object will be added. Window index must exceed or equal to 0 and be less than WindowsTotal().
     * @param time1  Time part of the first point.
     * @param price1 Price part of the first point.
     * @return Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     */
    public boolean objectCreate(
            String name,
            ObjectType type,
            int window,
            java.util.Date time1,
            double price1
    ) {
        StringBuilder command = new StringBuilder("39 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(type.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(window).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(time1)).append(ARG_END);
        command.append(ARG_BEGIN).append(price1).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectCreate(");
            signature.append("name=").append(name);
            signature.append(", type=").append(type.getVal(getMType()));
            signature.append(", window=").append(window);
            signature.append(", time1=").append(timeConvert(time1));
            signature.append(", price1=").append(price1);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     *
     * @param name   Object unique name.
     * @param type   Object type. It can be any of the Object type enumeration values.
     * @param window Index of the window where the object will be added. Window index must exceed or equal to 0 and be less than WindowsTotal().
     * @param time1  Time part of the first point.
     * @param price1 Price part of the first point.
     * @param time2  Time part of the second point.
     * @param price2 Price part of the second point.
     * @return Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     */
    public boolean objectCreate(
            String name,
            ObjectType type,
            int window,
            java.util.Date time1,
            double price1,
            java.util.Date time2,
            double price2
    ) {
        StringBuilder command = new StringBuilder("40 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(type.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(window).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(time1)).append(ARG_END);
        command.append(ARG_BEGIN).append(price1).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(time2)).append(ARG_END);
        command.append(ARG_BEGIN).append(price2).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectCreate(");
            signature.append("name=").append(name);
            signature.append(", type=").append(type.getVal(getMType()));
            signature.append(", window=").append(window);
            signature.append(", time1=").append(timeConvert(time1));
            signature.append(", price1=").append(price1);
            signature.append(", time2=").append(timeConvert(time2));
            signature.append(", price2=").append(price2);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }

    private String timeConvert(Date time) {
        return time == null || time.getTime() == 0 ? T1970_01_01 : new MT4DateFormat().format(time);
    }

    /**
     * Deletes object having the specified name. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name Object unique name.
     * @return Deletes object having the specified name. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function.
     */
    public boolean objectDelete(
            String name
    ) {
        StringBuilder command = new StringBuilder("41 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectDelete(");
            signature.append("name=").append(name);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * The function returns the value of the specified object property. To check errors, one has to call the GetLastError() function. See also ObjectSet() function.
     *
     * @param name  Object unique name.
     * @param index Object property index. It can be any of the Object properties enumeration values.
     * @return The function returns the value of the specified object property. To check errors, one has to call the GetLastError() function. See also ObjectSet() function.
     */
    public double objectGet(
            String name,
            ObjectProperty index
    ) {
        StringBuilder command = new StringBuilder("42 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(index.getVal(getMType())).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectGet(");
            signature.append("name=").append(name);
            signature.append(", index=").append(index.getVal(getMType()));
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Changes the value of the specified object property. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. See also ObjectGet() function.
     *
     * @param name  Object unique name.
     * @param index Object property index. It can be any of the Object properties enumeration values.
     * @param value New value of the given property.
     * @return Changes the value of the specified object property. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. See also ObjectGet() function.
     */
    public boolean objectSet(
            String name,
            ObjectProperty index,
            double value
    ) {
        StringBuilder command = new StringBuilder("43 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(index.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(value).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectSet(");
            signature.append("name=").append(name);
            signature.append(", index=").append(index.getVal(getMType()));
            signature.append(", value=").append(value);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * The function returns the level description of a Fibonacci object. The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To get the detailed error information, one has to call the GetLastError() function. See also ObjectSetFiboDescription() function.
     *
     * @param name  Object unique name.
     * @param index Index of the Fibonacci level (0-31).
     * @return The function returns the level description of a Fibonacci object. The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To get the detailed error information, one has to call the GetLastError() function. See also ObjectSetFiboDescription() function.
     */
    public String objectGetFiboDescription(
            String name,
            int index
    ) {
        StringBuilder command = new StringBuilder("44 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(index).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectGetFiboDescription(");
            signature.append("name=").append(name);
            signature.append(", index=").append(index);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * The function assigns a new description to a level of a Fibonacci object. The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name  Object unique name.
     * @param index Index of the Fibonacci level (0-31).
     * @param text  New description of the level.
     * @return The function assigns a new description to a level of a Fibonacci object. The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To get the detailed error information, one has to call the GetLastError() function.
     */
    public boolean objectSetFiboDescription(
            String name,
            int index,
            String text
    ) {
        StringBuilder command = new StringBuilder("45 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(index).append(ARG_END);
        command.append(ARG_BEGIN).append(replaceSpecialChars(text)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectSetFiboDescription(");
            signature.append("name=").append(name);
            signature.append(", index=").append(index);
            signature.append(", text=").append(text);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Changes the object description. For objects of OBJ_TEXT and OBJ_LABEL, this description is shown as a text line in the chart. If the function succeeds, the returned value will be TRUE. Otherwise, it is FALSE. To get the detailed error information, one has to call the GetLastError() function. Parameters of font_size, font_name and text_color are used for objects of OBJ_TEXT and OBJ_LABEL only. For objects of other types, these parameters are ignored. See also ObjectDescription() function.
     *
     * @param name       Object unique name.
     * @param text       A text describing the object.
     * @param font_size  Font size in points.
     * @param font       Font name (NULL).
     * @param text_color Text color (CLR_NONE).
     * @return Changes the object description. For objects of OBJ_TEXT and OBJ_LABEL, this description is shown as a text line in the chart. If the function succeeds, the returned value will be TRUE. Otherwise, it is FALSE. To get the detailed error information, one has to call the GetLastError() function. Parameters of font_size, font_name and text_color are used for objects of OBJ_TEXT and OBJ_LABEL only. For objects of other types, these parameters are ignored. See also ObjectDescription() function.
     */
    public boolean objectSetText(
            String name,
            String text,
            int font_size,
            String font,
            long text_color
    ) {
        StringBuilder command = new StringBuilder("46 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(replaceSpecialChars(text)).append(ARG_END);
        command.append(ARG_BEGIN).append(font_size).append(ARG_END);
        command.append(ARG_BEGIN).append(font).append(ARG_END);
        command.append(ARG_BEGIN).append(text_color).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectSetText(");
            signature.append("name=").append(name);
            signature.append(", text=").append(text);
            signature.append(", font_size=").append(font_size);
            signature.append(", font=").append(font);
            signature.append(", text_color=").append(text_color);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns total amount of objects of the specified type in the chart.
     *
     * @param type Optional parameter. An object type to be counted. It can be any of the Object type enumeration values or EMPTY constant to count all objects with any types.
     * @return Returns total amount of objects of the specified type in the chart.
     */
    public int objectsTotal(
            ObjectType type
    ) {
        StringBuilder command = new StringBuilder("47 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(type.getVal(getMType())).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectsTotal(");
            signature.append("type=").append(type.getVal(getMType()));
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * The function returns the object type value. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name Object unique name.
     * @return The function returns the object type value. To get the detailed error information, one has to call the GetLastError() function.
     */
    public int objectType(
            String name
    ) {
        StringBuilder command = new StringBuilder("48 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectType(");
            signature.append("name=").append(name);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Calculates the Bill Williams' Accelerator/Decelerator oscillator.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bill Williams' Accelerator/Decelerator oscillator.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iAC(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("49 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iAC(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Accumulation/Distribution indicator and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Accumulation/Distribution indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iAD(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("50 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iAD(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Bill Williams' Alligator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param jawPeriod    Blue line averaging period (Alligator's Jaw).
     * @param jawShift     Blue line shift relative to the chart.
     * @param teethPeriod  Red line averaging period (Alligator's Teeth).
     * @param teethShift   Red line shift relative to the chart.
     * @param lipsPeriod   Green line averaging period (Alligator's Lips).
     * @param lipsShift    Green line shift relative to the chart.
     * @param maMethod     MA method. It can be any of Moving Average methods.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param mode         Data source, identifier of a line of the indicator. It can be any of the following values: MODE_GATORJAW - Gator Jaw (blue) balance line, MODE_GATORTEETH - Gator Teeth (red) balance line, MODE_GATORLIPS - Gator Lips (green) balance line.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bill Williams' Alligator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iAlligator(
            String symbol,
            Timeframe timeframe,
            int jawPeriod,
            int jawShift,
            int teethPeriod,
            int teethShift,
            int lipsPeriod,
            int lipsShift,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            GatorMode mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("51 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(jawPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(jawShift).append(ARG_END);
        command.append(ARG_BEGIN).append(teethPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(teethShift).append(ARG_END);
        command.append(ARG_BEGIN).append(lipsPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(lipsShift).append(ARG_END);
        command.append(ARG_BEGIN).append(maMethod.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iAlligator(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", jawPeriod=").append(jawPeriod);
            signature.append(", jawShift=").append(jawShift);
            signature.append(", teethPeriod=").append(teethPeriod);
            signature.append(", teethShift=").append(teethShift);
            signature.append(", lipsPeriod=").append(lipsPeriod);
            signature.append(", lipsShift=").append(lipsShift);
            signature.append(", maMethod=").append(maMethod.getVal(getMType()));
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Movement directional index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param mode         Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Movement directional index and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iADX(
            String symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            ADXIndicatorLines mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("52 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iADX(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Indicator of the average true range and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Averaging period for calculation.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Indicator of the average true range and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iATR(
            String symbol,
            Timeframe timeframe,
            int period,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("53 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iATR(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Bill Williams' Awesome oscillator and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bill Williams' Awesome oscillator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iAO(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("54 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iAO(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Bears Power indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bears Power indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iBearsPower(
            String symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("55 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iBearsPower(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Movement directional index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param deviation    Deviation from the main line.
     * @param bandsShift   The indicator shift relative to the chart.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param mode         Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Movement directional index and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iBands(
            String symbol,
            Timeframe timeframe,
            int period,
            double deviation,
            int bandsShift,
            AppliedPrice appliedPrice,
            BandsIndicatorLines mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("56 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(deviation).append(ARG_END);
        command.append(ARG_BEGIN).append(bandsShift).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iBands(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", deviation=").append(deviation);
            signature.append(", bandsShift=").append(bandsShift);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Bulls Power indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bulls Power indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iBullsPower(
            String symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("57 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iBullsPower(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Commodity channel index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Commodity channel index and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iCCI(
            String symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("58 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iCCI(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }

    /**
     * Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param name      Custom indicator compiled program name.
     * @param mode      Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer functions.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     */
    public double iCustom(
            String symbol,
            Timeframe timeframe,
            String name,
            int mode,
            int shift
    ) throws ErrUnknownSymbol {
        return iCustom(symbol, timeframe, name, mode, shift, (CustomParameters) null);
    }

    /**
     * Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param name      Custom indicator compiled program name.
     * @param mode      Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer functions.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @param params    Parameters set (if necessary). The passed parameters and their order must correspond with the desclaration order and the type of extern variables of the custom indicator.
     * @return Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iCustom(
            String symbol,
            Timeframe timeframe,
            String name,
            int mode,
            int shift,
            final String... params
    ) throws ErrUnknownSymbol {
        return iCustom(symbol, timeframe, name, mode, shift, new CustomParameters() {
            @Override
            public void checkParams() {
                if (params != null && params.length > 9) {
                    throw new RuntimeException("Maximum 9 iCustom arguments are allowed");
                }
            }

            @Override
            public void appendParams(ByteBufferBuilder command) {
                if (params != null) {
                    command.append("s" + params.length);
                    for (int i = 0; i < params.length; i++) {
                        command.append(params[i]);
                    }
                }
            }

            @Override
            public void appendParams(StringBuilder command) {
                if (params != null) {
                    command.append(ARG_BEGIN).append('s').append(params.length).append(ARG_END);
                    for (int i = 0; i < params.length; i++) {
                        command.append(ARG_BEGIN).append(params[i]).append(ARG_END);
                    }
                }
            }
        });
    }

    /**
     * Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param name      Custom indicator compiled program name.
     * @param mode      Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer functions.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @param params    Parameters set (if necessary). The passed parameters and their order must correspond with the desclaration order and the type of extern variables of the custom indicator.
     * @return Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iCustom(
            String symbol,
            Timeframe timeframe,
            String name,
            int mode,
            int shift,
            final int... params
    ) throws ErrUnknownSymbol {
        return iCustom(symbol, timeframe, name, mode, shift, new CustomParameters() {
            @Override
            public void checkParams() {
                if (params != null && params.length > 9) {
                    throw new RuntimeException("Maximum 9 iCustom arguments are allowed");
                }
            }

            @Override
            public void appendParams(ByteBufferBuilder command) {
                if (params != null) {
                    command.append("i" + params.length);
                    for (int i = 0; i < params.length; i++) {
                        command.append(params[i]);
                    }
                }
            }

            @Override
            public void appendParams(StringBuilder command) {
                if (params != null) {
                    command.append(ARG_BEGIN).append('i').append(params.length).append(ARG_END);
                    for (int i = 0; i < params.length; i++) {
                        command.append(ARG_BEGIN).append(params[i]).append(ARG_END);
                    }
                }
            }
        });
    }

    /**
     * Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param name      Custom indicator compiled program name.
     * @param mode      Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer functions.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @param params    Parameters set (if necessary). The passed parameters and their order must correspond with the desclaration order and the type of extern variables of the custom indicator.
     * @return Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iCustom(
            String symbol,
            Timeframe timeframe,
            String name,
            int mode,
            int shift,
            final double... params
    ) throws ErrUnknownSymbol {
        return iCustom(symbol, timeframe, name, mode, shift, new CustomParameters() {
            @Override
            public void checkParams() {
                if (params != null && params.length > 9) {
                    throw new RuntimeException("Maximum 9 iCustom arguments are allowed");
                }
            }

            @Override
            public void appendParams(ByteBufferBuilder command) {
                if (params != null) {
                    command.append("d" + params.length);
                    for (int i = 0; i < params.length; i++) {
                        command.append(params[i]);
                    }
                }
            }

            @Override
            public void appendParams(StringBuilder command) {
                if (params != null) {
                    command.append(ARG_BEGIN).append('d').append(params.length).append(ARG_END);
                    for (int i = 0; i < params.length; i++) {
                        command.append(ARG_BEGIN).append(params[i]).append(ARG_END);
                    }
                }
            }
        });
    }

    private interface CustomParameters {
        public void checkParams();

        public void appendParams(ByteBufferBuilder command);

        public void appendParams(StringBuilder command);
    }

    /**
     * Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param name      Custom indicator compiled program name.
     * @param mode      Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer functions.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     */
    private double iCustom(
            String symbol,
            Timeframe timeframe,
            String name,
            int mode,
            int shift,
            CustomParameters params
    ) throws ErrUnknownSymbol {
        if (params != null) {
            params.checkParams();
        }
        //
        StringBuilder command = new StringBuilder("59 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(name).append(ARG_END);
        command.append(ARG_BEGIN).append(mode).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        if (params != null) {
            params.appendParams(command);
        }
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iCustom(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", name=").append(name);
            signature.append(", mode=").append(mode);
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the DeMarker indicator and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Averaging period for calculation.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the DeMarker indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iDeMarker(
            String symbol,
            Timeframe timeframe,
            int period,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("60 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iDeMarker(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Envelopes indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param maPeriod     Averaging period for calculation of the main line.
     * @param maMethod     MA method. It can be any of Moving Average methods.
     * @param maShitf      MA shift. Indicator line offset relate to the chart by timeframe.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param deviation    Percent deviation from the main line.
     * @param mode         Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Envelopes indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iEnvelopes(
            String symbol,
            Timeframe timeframe,
            int maPeriod,
            MovingAverageMethod maMethod,
            int maShitf,
            AppliedPrice appliedPrice,
            double deviation,
            BandsIndicatorLines mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("61 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(maPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(maMethod.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(maShitf).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(deviation).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iEnvelopes(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", maPeriod=").append(maPeriod);
            signature.append(", maMethod=").append(maMethod.getVal(getMType()));
            signature.append(", maShitf=").append(maShitf);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", deviation=").append(deviation);
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Force index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param maMethod     MA method. It can be any of Moving Average methods.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Force index and returns its value.
     */
    public double iForce(
            String symbol,
            Timeframe timeframe,
            int period,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("62 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(maMethod.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iForce(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", maMethod=").append(maMethod.getVal(getMType()));
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Fractals and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param mode      Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Fractals and returns its value.
     */
    public double iFractals(
            String symbol,
            Timeframe timeframe,
            BandsIndicatorLines mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("63 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iFractals(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Gator oscillator calculation. The oscillator displays the difference between the Alligator red and blue lines (the upper histogram) and that between red and green lines (the lower histogram).
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param jawPeriod    Blue line averaging period (Alligator's Jaw).
     * @param jawShift     Blue line shift relative to the chart.
     * @param teethPeriod  Red line averaging period (Alligator's Teeth).
     * @param teethShift   Red line shift relative to the chart.
     * @param lipsPeriod   Green line averaging period (Alligator's Lips).
     * @param lipsShift    Green line shift relative to the chart.
     * @param maMethod     MA method. It can be any of Moving Average methods.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param mode         Indicator line index. It can be any of Indicators line identifiers enumeration value.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Gator oscillator calculation. The oscillator displays the difference between the Alligator red and blue lines (the upper histogram) and that between red and green lines (the lower histogram).
     */
    public double iGator(
            String symbol,
            Timeframe timeframe,
            int jawPeriod,
            int jawShift,
            int teethPeriod,
            int teethShift,
            int lipsPeriod,
            int lipsShift,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            BandsIndicatorLines mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("64 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(jawPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(jawShift).append(ARG_END);
        command.append(ARG_BEGIN).append(teethPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(teethShift).append(ARG_END);
        command.append(ARG_BEGIN).append(lipsPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(lipsShift).append(ARG_END);
        command.append(ARG_BEGIN).append(maMethod.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iGator(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", jawPeriod=").append(jawPeriod);
            signature.append(", jawShift=").append(jawShift);
            signature.append(", teethPeriod=").append(teethPeriod);
            signature.append(", teethShift=").append(teethShift);
            signature.append(", lipsPeriod=").append(lipsPeriod);
            signature.append(", lipsShift=").append(lipsShift);
            signature.append(", maMethod=").append(maMethod.getVal(getMType()));
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Bill Williams Market Facilitation index and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bill Williams Market Facilitation index and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iBWMFI(
            String symbol,
            Timeframe timeframe,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("65 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iBWMFI(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Momentum indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Momentum indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iMomentum(
            String symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("66 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iMomentum(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Money flow index and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Averaging period for calculation.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Money flow index and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iMFI(
            String symbol,
            Timeframe timeframe,
            int period,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("67 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iMFI(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Moving average indicator and returns its value.
     *
     * @param symbol       Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param maShift      MA shift. Indicators line offset relate to the chart by timeframe.
     * @param maMethod     MA method. It can be any of the Moving Average method enumeration value.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Moving average indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iMA(
            String symbol,
            Timeframe timeframe,
            int period,
            int maShift,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("68 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(maShift).append(ARG_END);
        command.append(ARG_BEGIN).append(maMethod.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iMA(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", maShift=").append(maShift);
            signature.append(", maMethod=").append(maMethod.getVal(getMType()));
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Moving Average of Oscillator and returns its value. Sometimes called MACD Histogram in some systems.
     *
     * @param symbol        Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe     Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param fastEMAPeriod Number of periods for fast moving average calculation.
     * @param slowEMAPeriod Number of periods for slow moving average calculation.
     * @param signalPeriod  Number of periods for signal moving average calculation.
     * @param appliedPrice  Applied price. It can be any of Applied price enumeration values.
     * @param shift         Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Moving Average of Oscillator and returns its value. Sometimes called MACD Histogram in some systems.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iOsMA(
            String symbol,
            Timeframe timeframe,
            int fastEMAPeriod,
            int slowEMAPeriod,
            int signalPeriod,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("69 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(fastEMAPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(slowEMAPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(signalPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iOsMA(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", fastEMAPeriod=").append(fastEMAPeriod);
            signature.append(", slowEMAPeriod=").append(slowEMAPeriod);
            signature.append(", signalPeriod=").append(signalPeriod);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Moving averages convergence/divergence and returns its value. In the systems where OsMA is called MACD Histogram, this indicator is displayed as two lines. In the Client Terminal, the Moving Average Convergence/Divergence is drawn as a histogram.
     *
     * @param symbol        Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe     Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param fastEMAPeriod Number of periods for fast moving average calculation.
     * @param slowEMAPeriod Number of periods for slow moving average calculation.
     * @param signalPeriod  Number of periods for signal moving average calculation.
     * @param appliedPrice  Applied price. It can be any of Applied price enumeration values.
     * @param mode          Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift         Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Moving averages convergence/divergence and returns its value. In the systems where OsMA is called MACD Histogram, this indicator is displayed as two lines. In the Client Terminal, the Moving Average Convergence/Divergence is drawn as a histogram.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iMACD(
            String symbol,
            Timeframe timeframe,
            int fastEMAPeriod,
            int slowEMAPeriod,
            int signalPeriod,
            AppliedPrice appliedPrice,
            MACDIndicatorLines mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("70 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(fastEMAPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(slowEMAPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(signalPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iMACD(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", fastEMAPeriod=").append(fastEMAPeriod);
            signature.append(", slowEMAPeriod=").append(slowEMAPeriod);
            signature.append(", signalPeriod=").append(signalPeriod);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the On Balance Volume indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the On Balance Volume indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iOBV(
            String symbol,
            Timeframe timeframe,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("71 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iOBV(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Parabolic Stop and Reverse system and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param step      Increment, usually 0.02.
     * @param maximum   Maximum value, usually 0.2.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Parabolic Stop and Reverse system and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iSAR(
            String symbol,
            Timeframe timeframe,
            double step,
            double maximum,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("72 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(step).append(ARG_END);
        command.append(ARG_BEGIN).append(maximum).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iSAR(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", step=").append(step);
            signature.append(", maximum=").append(maximum);
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Relative strength index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Number of periods for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Relative strength index and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iRSI(
            String symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("73 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iRSI(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Relative Vigor index and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Number of periods for calculation.
     * @param mode      Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Relative Vigor index and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iRVI(
            String symbol,
            Timeframe timeframe,
            int period,
            MACDIndicatorLines mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("74 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iRVI(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Standard Deviation indicator and returns its value.
     *
     * @param symbol       Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param maPeriod     MA period
     * @param maShift      MA shift. Indicators line offset relate to the chart by timeframe.
     * @param maMethod     MA method. It can be any of the Moving Average method enumeration value.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Standard Deviation indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iStdDev(
            String symbol,
            Timeframe timeframe,
            int maPeriod,
            int maShift,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("75 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(maPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(maShift).append(ARG_END);
        command.append(ARG_BEGIN).append(maMethod.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(appliedPrice.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iStdDev(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", maPeriod=").append(maPeriod);
            signature.append(", maShift=").append(maShift);
            signature.append(", maMethod=").append(maMethod.getVal(getMType()));
            signature.append(", appliedPrice=").append(appliedPrice.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Stochastic oscillator and returns its value.
     *
     * @param symbol     Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe  Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param kPeriod    %K line period.
     * @param dPeriod    %D line period.
     * @param slowing    Slowing value.
     * @param maMethod   MA method. It can be any of the Moving Average method enumeration value.
     * @param priceField Price field parameter. Can be one of this values: 0 - Low/High or 1 - Close/Close.
     * @param mode       Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift      Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Stochastic oscillator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iStochastic(
            String symbol,
            Timeframe timeframe,
            int kPeriod,
            int dPeriod,
            int slowing,
            MovingAverageMethod maMethod,
            int priceField,
            MACDIndicatorLines mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("76 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(kPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(dPeriod).append(ARG_END);
        command.append(ARG_BEGIN).append(slowing).append(ARG_END);
        command.append(ARG_BEGIN).append(maMethod.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(priceField).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iStochastic(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", kPeriod=").append(kPeriod);
            signature.append(", dPeriod=").append(dPeriod);
            signature.append(", slowing=").append(slowing);
            signature.append(", maMethod=").append(maMethod.getVal(getMType()));
            signature.append(", priceField=").append(priceField);
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculates the Larry William's percent range indicator and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Averaging period for calculation.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Larry William's percent range indicator and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iWPR(
            String symbol,
            Timeframe timeframe,
            int period,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("77 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(period).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iWPR(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", period=").append(period);
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Closes opened order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     *
     * @param ticket     Unique number of the order ticket.
     * @param lots       Number of lots.
     * @param price      Preferred closing price.
     * @param slippage   Value of the maximum price slippage in points.
     * @param arrowColor Color of the closing arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return Closes opened order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     * @throws com.jfx.ErrCustomIndicatorError      Custom indicator error.
     * @throws com.jfx.ErrIntegerParameterExpected  Integer parameter expected.
     * @throws com.jfx.ErrInvalidFunctionParamvalue Invalid function parameter value.
     * @throws com.jfx.ErrInvalidPriceParam         Invalid price.
     * @throws com.jfx.ErrInvalidTicket             Invalid ticket.
     * @throws com.jfx.ErrUnknownSymbol             Unknown symbol.
     * @throws com.jfx.ErrTradeNotAllowed           Trade is not allowed.
     * @throws com.jfx.ErrCommonError               Common error.
     * @throws com.jfx.ErrInvalidTradeParameters    Invalid trade parameters.
     * @throws com.jfx.ErrServerBusy                Trade server is busy.
     * @throws com.jfx.ErrOldVersion                Old version of the client terminal.
     * @throws com.jfx.ErrNoConnection              No connection with trade server.
     * @throws com.jfx.ErrTooFrequentRequests       Too frequent requests.
     * @throws com.jfx.ErrAccountDisabled           Account disabled.
     * @throws com.jfx.ErrInvalidAccount            Invalid account.
     * @throws com.jfx.ErrTradeTimeout              Trade timeout.
     * @throws com.jfx.ErrInvalidPrice              Invalid price.
     * @throws com.jfx.ErrInvalidStops              Invalid stops.
     * @throws com.jfx.ErrInvalidTradeVolume        Invalid trade volume.
     * @throws com.jfx.ErrMarketClosed              Market is closed.
     * @throws com.jfx.ErrTradeDisabled             Trade is disabled.
     * @throws com.jfx.ErrNotEnoughMoney            Not enough money.
     * @throws com.jfx.ErrPriceChanged              Price changed.
     * @throws com.jfx.ErrOffQuotes                 Off quotes.
     * @throws com.jfx.ErrRequote                   Requote is required.
     * @throws com.jfx.ErrOrderLocked               Order is locked.
     * @throws com.jfx.ErrLongPositionsOnlyAllowed  Long positions only allowed.
     * @throws com.jfx.ErrTooManyRequests           Too many requests.
     * @throws com.jfx.ErrTradeTimeout2             trade timeout 2.
     * @throws com.jfx.ErrTradeTimeout3             trade timeout 3.
     * @throws com.jfx.ErrTradeTimeout4             trade timeout 3.
     * @throws com.jfx.ErrTradeModifyDenied         Modification denied because an order is too close to market.
     * @throws com.jfx.ErrTradeContextBusy          Trade context is busy.
     * @throws com.jfx.ErrTradeExpirationDenied     Expirations are denied by broker.
     * @throws com.jfx.ErrTradeTooManyOrders        The amount of opened and pending orders has reached the limit set by a broker.
     */
    public boolean orderClose(
            long ticket,
            double lots,
            double price,
            int slippage,
            long arrowColor
    ) throws ErrCustomIndicatorError, ErrIntegerParameterExpected, ErrInvalidFunctionParamvalue, ErrInvalidPriceParam, ErrInvalidTicket, ErrUnknownSymbol, ErrTradeNotAllowed, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders {
        StringBuilder command = new StringBuilder("78 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(ticket).append(ARG_END);
        command.append(ARG_BEGIN).append(lots).append(ARG_END);
        command.append(ARG_BEGIN).append(price).append(ARG_END);
        command.append(ARG_BEGIN).append(slippage).append(ARG_END);
        command.append(ARG_BEGIN).append(arrowColor).append(ARG_END);
        //
        String result = delegateToTerminal(command);
        //
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderClose(");
            signature.append("ticket=").append(ticket);
            signature.append(", lots=").append(lots);
            signature.append(", price=").append(price);
            signature.append(", slippage=").append(slippage);
            signature.append(", arrowColor=").append(arrowColor);
            signature.append(')');
            switch (error) {
                case 4055:
                    throw new ErrCustomIndicatorError(signature.toString());
                case 4063:
                    throw new ErrIntegerParameterExpected(signature.toString());
                case 4051:
                    throw new ErrInvalidFunctionParamvalue(signature.toString());
                case 4107:
                    throw new ErrInvalidPriceParam(signature.toString());
                case 4108:
                    throw new ErrInvalidTicket(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                case 4109:
                    throw new ErrTradeNotAllowed(signature.toString());
                case 0:
                    break;
                case 2:
                    throw new ErrCommonError(signature.toString());
                case 3:
                    throw new ErrInvalidTradeParameters(signature.toString());
                case 4:
                    throw new ErrServerBusy(signature.toString());
                case 5:
                    throw new ErrOldVersion(signature.toString());
                case 6:
                    throw new ErrNoConnection(signature.toString());
                case 8:
                    throw new ErrTooFrequentRequests(signature.toString());
                case 64:
                    throw new ErrAccountDisabled(signature.toString());
                case 65:
                    throw new ErrInvalidAccount(signature.toString());
                case 128:
                    throw new ErrTradeTimeout(signature.toString());
                case 129:
                    throw new ErrInvalidPrice(signature.toString());
                case 130:
                    throw new ErrInvalidStops(signature.toString());
                case 131:
                    throw new ErrInvalidTradeVolume(signature.toString());
                case 132:
                    throw new ErrMarketClosed(signature.toString());
                case 133:
                    throw new ErrTradeDisabled(signature.toString());
                case 134:
                    throw new ErrNotEnoughMoney(signature.toString());
                case 135:
                    throw new ErrPriceChanged(signature.toString());
                case 136:
                    throw new ErrOffQuotes(signature.toString());
                case 138:
                    throw new ErrRequote(signature.toString());
                case 139:
                    throw new ErrOrderLocked(signature.toString());
                case 140:
                    throw new ErrLongPositionsOnlyAllowed(signature.toString());
                case 141:
                    throw new ErrTooManyRequests(signature.toString());
                case 142:
                    throw new ErrTradeTimeout2(signature.toString());
                case 143:
                    throw new ErrTradeTimeout3(signature.toString());
                case 144:
                    throw new ErrTradeTimeout4(signature.toString());
                case 145:
                    throw new ErrTradeModifyDenied(signature.toString());
                case 146:
                    throw new ErrTradeContextBusy(signature.toString());
                case 147:
                    throw new ErrTradeExpirationDenied(signature.toString());
                case 148:
                    throw new ErrTradeTooManyOrders(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }

    /**
     * Closes an opened order by another opposite opened order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     *
     * @param ticket     Unique number of the order ticket.
     * @param opposite   Unique number of the opposite order ticket.
     * @param arrowColor Color of the closing arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return Closes an opened order by another opposite opened order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     * @throws com.jfx.ErrCustomIndicatorError      Custom indicator error.
     * @throws com.jfx.ErrIntegerParameterExpected  Integer parameter expected.
     * @throws com.jfx.ErrInvalidFunctionParamvalue Invalid function parameter value.
     * @throws com.jfx.ErrInvalidTicket             Invalid ticket.
     * @throws com.jfx.ErrUnknownSymbol             Unknown symbol.
     * @throws com.jfx.ErrTradeNotAllowed           Trade is not allowed.
     * @throws com.jfx.ErrCommonError               Common error.
     * @throws com.jfx.ErrInvalidTradeParameters    Invalid trade parameters.
     * @throws com.jfx.ErrServerBusy                Trade server is busy.
     * @throws com.jfx.ErrOldVersion                Old version of the client terminal.
     * @throws com.jfx.ErrNoConnection              No connection with trade server.
     * @throws com.jfx.ErrTooFrequentRequests       Too frequent requests.
     * @throws com.jfx.ErrAccountDisabled           Account disabled.
     * @throws com.jfx.ErrInvalidAccount            Invalid account.
     * @throws com.jfx.ErrTradeTimeout              Trade timeout.
     * @throws com.jfx.ErrInvalidPrice              Invalid price.
     * @throws com.jfx.ErrInvalidStops              Invalid stops.
     * @throws com.jfx.ErrInvalidTradeVolume        Invalid trade volume.
     * @throws com.jfx.ErrMarketClosed              Market is closed.
     * @throws com.jfx.ErrTradeDisabled             Trade is disabled.
     * @throws com.jfx.ErrNotEnoughMoney            Not enough money.
     * @throws com.jfx.ErrPriceChanged              Price changed.
     * @throws com.jfx.ErrOffQuotes                 Off quotes.
     * @throws com.jfx.ErrRequote                   Requote is required.
     * @throws com.jfx.ErrOrderLocked               Order is locked.
     * @throws com.jfx.ErrLongPositionsOnlyAllowed  Long positions only allowed.
     * @throws com.jfx.ErrTooManyRequests           Too many requests.
     * @throws com.jfx.ErrTradeTimeout2             trade timeout 2.
     * @throws com.jfx.ErrTradeTimeout3             trade timeout 3.
     * @throws com.jfx.ErrTradeTimeout4             trade timeout 3.
     * @throws com.jfx.ErrTradeModifyDenied         Modification denied because an order is too close to market.
     * @throws com.jfx.ErrTradeContextBusy          Trade context is busy.
     * @throws com.jfx.ErrTradeExpirationDenied     Expirations are denied by broker.
     * @throws com.jfx.ErrTradeTooManyOrders        The amount of opened and pending orders has reached the limit set by a broker.
     */
    public boolean orderCloseBy(
            long ticket,
            int opposite,
            long arrowColor
    ) throws ErrCustomIndicatorError, ErrIntegerParameterExpected, ErrInvalidFunctionParamvalue, ErrInvalidTicket, ErrUnknownSymbol, ErrTradeNotAllowed, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders {
        StringBuilder command = new StringBuilder("79 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(ticket).append(ARG_END);
        command.append(ARG_BEGIN).append(opposite).append(ARG_END);
        command.append(ARG_BEGIN).append(arrowColor).append(ARG_END);
        //
        String result = delegateToTerminal(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderCloseBy(");
            signature.append("ticket=").append(ticket);
            signature.append(", opposite=").append(opposite);
            signature.append(", arrowColor=").append(arrowColor);
            signature.append(')');
            switch (error) {
                case 4055:
                    throw new ErrCustomIndicatorError(signature.toString());
                case 4063:
                    throw new ErrIntegerParameterExpected(signature.toString());
                case 4051:
                    throw new ErrInvalidFunctionParamvalue(signature.toString());
                case 4108:
                    throw new ErrInvalidTicket(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                case 4109:
                    throw new ErrTradeNotAllowed(signature.toString());
                case 0:
                    break;
                case 2:
                    throw new ErrCommonError(signature.toString());
                case 3:
                    throw new ErrInvalidTradeParameters(signature.toString());
                case 4:
                    throw new ErrServerBusy(signature.toString());
                case 5:
                    throw new ErrOldVersion(signature.toString());
                case 6:
                    throw new ErrNoConnection(signature.toString());
                case 8:
                    throw new ErrTooFrequentRequests(signature.toString());
                case 64:
                    throw new ErrAccountDisabled(signature.toString());
                case 65:
                    throw new ErrInvalidAccount(signature.toString());
                case 128:
                    throw new ErrTradeTimeout(signature.toString());
                case 129:
                    throw new ErrInvalidPrice(signature.toString());
                case 130:
                    throw new ErrInvalidStops(signature.toString());
                case 131:
                    throw new ErrInvalidTradeVolume(signature.toString());
                case 132:
                    throw new ErrMarketClosed(signature.toString());
                case 133:
                    throw new ErrTradeDisabled(signature.toString());
                case 134:
                    throw new ErrNotEnoughMoney(signature.toString());
                case 135:
                    throw new ErrPriceChanged(signature.toString());
                case 136:
                    throw new ErrOffQuotes(signature.toString());
                case 138:
                    throw new ErrRequote(signature.toString());
                case 139:
                    throw new ErrOrderLocked(signature.toString());
                case 140:
                    throw new ErrLongPositionsOnlyAllowed(signature.toString());
                case 141:
                    throw new ErrTooManyRequests(signature.toString());
                case 142:
                    throw new ErrTradeTimeout2(signature.toString());
                case 143:
                    throw new ErrTradeTimeout3(signature.toString());
                case 144:
                    throw new ErrTradeTimeout4(signature.toString());
                case 145:
                    throw new ErrTradeModifyDenied(signature.toString());
                case 146:
                    throw new ErrTradeContextBusy(signature.toString());
                case 147:
                    throw new ErrTradeExpirationDenied(signature.toString());
                case 148:
                    throw new ErrTradeTooManyOrders(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns close price for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns close price for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected No order selected.
     */
    public double orderClosePrice(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("80 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderClosePrice(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns close time for the currently selected order. If order close time is not 0 then the order selected and has been closed and retrieved from the account history. Open and pending orders close time is equal to 0. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns close time for the currently selected order. If order close time is not 0 then the order selected and has been closed and retrieved from the account history. Open and pending orders close time is equal to 0. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected No order selected.
     */
    public java.util.Date orderCloseTime(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("81 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderCloseTime(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        java.util.Date res;
        //
        // decompose result
        //
        long time = Long.parseLong(result) * 1000;
        res = new java.util.Date(time + getTZCorrectionOffset(time));
        return res;
    }


    /**
     * Returns comment for the selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns comment for the selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected No order selected.
     */
    public String orderComment(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("82 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderComment(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Returns calculated commission for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns calculated commission for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected No order selected.
     */
    public double orderCommission(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("83 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderCommission(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Deletes previously opened pending order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     *
     * @param ticket     Unique number of the order ticket.
     * @param arrowColor Color of the arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return Deletes previously opened pending order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     * @throws com.jfx.ErrCustomIndicatorError      Custom indicator error.
     * @throws com.jfx.ErrInvalidFunctionParamvalue Invalid function parameter value.
     * @throws com.jfx.ErrInvalidTicket             Invalid ticket.
     * @throws com.jfx.ErrUnknownSymbol             Unknown symbol.
     * @throws com.jfx.ErrTradeNotAllowed           Trade is not allowed.
     * @throws com.jfx.ErrCommonError               Common error.
     * @throws com.jfx.ErrInvalidTradeParameters    Invalid trade parameters.
     * @throws com.jfx.ErrServerBusy                Trade server is busy.
     * @throws com.jfx.ErrOldVersion                Old version of the client terminal.
     * @throws com.jfx.ErrNoConnection              No connection with trade server.
     * @throws com.jfx.ErrTooFrequentRequests       Too frequent requests.
     * @throws com.jfx.ErrAccountDisabled           Account disabled.
     * @throws com.jfx.ErrInvalidAccount            Invalid account.
     * @throws com.jfx.ErrTradeTimeout              Trade timeout.
     * @throws com.jfx.ErrInvalidPrice              Invalid price.
     * @throws com.jfx.ErrInvalidStops              Invalid stops.
     * @throws com.jfx.ErrInvalidTradeVolume        Invalid trade volume.
     * @throws com.jfx.ErrMarketClosed              Market is closed.
     * @throws com.jfx.ErrTradeDisabled             Trade is disabled.
     * @throws com.jfx.ErrNotEnoughMoney            Not enough money.
     * @throws com.jfx.ErrPriceChanged              Price changed.
     * @throws com.jfx.ErrOffQuotes                 Off quotes.
     * @throws com.jfx.ErrRequote                   Requote is required.
     * @throws com.jfx.ErrOrderLocked               Order is locked.
     * @throws com.jfx.ErrLongPositionsOnlyAllowed  Long positions only allowed.
     * @throws com.jfx.ErrTooManyRequests           Too many requests.
     * @throws com.jfx.ErrTradeTimeout2             trade timeout 2.
     * @throws com.jfx.ErrTradeTimeout3             trade timeout 3.
     * @throws com.jfx.ErrTradeTimeout4             trade timeout 3.
     * @throws com.jfx.ErrTradeModifyDenied         Modification denied because an order is too close to market.
     * @throws com.jfx.ErrTradeContextBusy          Trade context is busy.
     * @throws com.jfx.ErrTradeExpirationDenied     Expirations are denied by broker.
     * @throws com.jfx.ErrTradeTooManyOrders        The amount of opened and pending orders has reached the limit set by a broker.
     */
    public boolean orderDelete(
            long ticket,
            long arrowColor
    ) throws ErrCustomIndicatorError, ErrInvalidFunctionParamvalue, ErrInvalidTicket, ErrUnknownSymbol, ErrTradeNotAllowed, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders {
        StringBuilder command = new StringBuilder("84 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(ticket).append(ARG_END);
        command.append(ARG_BEGIN).append(arrowColor).append(ARG_END);
        //
        String result = delegateToTerminal(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderDelete(");
            signature.append("ticket=").append(ticket);
            signature.append(", arrowColor=").append(arrowColor);
            signature.append(')');
            switch (error) {
                case 4055:
                    throw new ErrCustomIndicatorError(signature.toString());
                case 4051:
                    throw new ErrInvalidFunctionParamvalue(signature.toString());
                case 4108:
                    throw new ErrInvalidTicket(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                case 4109:
                    throw new ErrTradeNotAllowed(signature.toString());
                case 0:
                    break;
                case 2:
                    throw new ErrCommonError(signature.toString());
                case 3:
                    throw new ErrInvalidTradeParameters(signature.toString());
                case 4:
                    throw new ErrServerBusy(signature.toString());
                case 5:
                    throw new ErrOldVersion(signature.toString());
                case 6:
                    throw new ErrNoConnection(signature.toString());
                case 8:
                    throw new ErrTooFrequentRequests(signature.toString());
                case 64:
                    throw new ErrAccountDisabled(signature.toString());
                case 65:
                    throw new ErrInvalidAccount(signature.toString());
                case 128:
                    throw new ErrTradeTimeout(signature.toString());
                case 129:
                    throw new ErrInvalidPrice(signature.toString());
                case 130:
                    throw new ErrInvalidStops(signature.toString());
                case 131:
                    throw new ErrInvalidTradeVolume(signature.toString());
                case 132:
                    throw new ErrMarketClosed(signature.toString());
                case 133:
                    throw new ErrTradeDisabled(signature.toString());
                case 134:
                    throw new ErrNotEnoughMoney(signature.toString());
                case 135:
                    throw new ErrPriceChanged(signature.toString());
                case 136:
                    throw new ErrOffQuotes(signature.toString());
                case 138:
                    throw new ErrRequote(signature.toString());
                case 139:
                    throw new ErrOrderLocked(signature.toString());
                case 140:
                    throw new ErrLongPositionsOnlyAllowed(signature.toString());
                case 141:
                    throw new ErrTooManyRequests(signature.toString());
                case 142:
                    throw new ErrTradeTimeout2(signature.toString());
                case 143:
                    throw new ErrTradeTimeout3(signature.toString());
                case 144:
                    throw new ErrTradeTimeout4(signature.toString());
                case 145:
                    throw new ErrTradeModifyDenied(signature.toString());
                case 146:
                    throw new ErrTradeContextBusy(signature.toString());
                case 147:
                    throw new ErrTradeExpirationDenied(signature.toString());
                case 148:
                    throw new ErrTradeTooManyOrders(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns expiration date for the selected pending order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns expiration date for the selected pending order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected No order selected.
     */
    public Date orderExpiration(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("85 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderExpiration(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return toDate(res);
    }


    /**
     * Returns amount of lots for the selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns amount of lots for the selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected No order selected.
     */
    public double orderLots(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("86 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderLots(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns an identifying (magic) number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns an identifying (magic) number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected No order selected.
     */
    public int orderMagicNumber(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("87 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderMagicNumber(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Modification of characteristics for the previously opened position or pending orders. If the function succeeds, the returned value will be TRUE. If the function fails, the returned value will be FALSE. To get the detailed error information, call GetLastError() function. Notes: Open price and expiration time can be changed only for pending orders. If unchanged values are passed as the function parameters, the error 1 (ERR_NO_RESULT) will be generated. Pending order expiration time can be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated.
     *
     * @param ticket     Unique number of the order ticket.
     * @param price      New open price of the pending order.
     * @param stoploss   New StopLoss level.
     * @param takeprofit New TakeProfit level.
     * @param expiration Pending order expiration time.
     * @param arrowColor Color of the arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return Modification of characteristics for the previously opened position or pending orders. If the function succeeds, the returned value will be TRUE. If the function fails, the returned value will be FALSE. To get the detailed error information, call GetLastError() function. Notes: Open price and expiration time can be changed only for pending orders. If unchanged values are passed as the function parameters, the error 1 (ERR_NO_RESULT) will be generated. Pending order expiration time can be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated.
     * @throws com.jfx.ErrCustomIndicatorError      Custom indicator error.
     * @throws com.jfx.ErrIntegerParameterExpected  Integer parameter expected.
     * @throws com.jfx.ErrInvalidFunctionParamvalue Invalid function parameter value.
     * @throws com.jfx.ErrInvalidPriceParam         Invalid price.
     * @throws com.jfx.ErrInvalidTicket             Invalid ticket.
     * @throws com.jfx.ErrUnknownSymbol             Unknown symbol.
     * @throws com.jfx.ErrTradeNotAllowed           Trade is not allowed.
     * @throws com.jfx.ErrCommonError               Common error.
     * @throws com.jfx.ErrInvalidTradeParameters    Invalid trade parameters.
     * @throws com.jfx.ErrServerBusy                Trade server is busy.
     * @throws com.jfx.ErrOldVersion                Old version of the client terminal.
     * @throws com.jfx.ErrNoConnection              No connection with trade server.
     * @throws com.jfx.ErrTooFrequentRequests       Too frequent requests.
     * @throws com.jfx.ErrAccountDisabled           Account disabled.
     * @throws com.jfx.ErrInvalidAccount            Invalid account.
     * @throws com.jfx.ErrTradeTimeout              Trade timeout.
     * @throws com.jfx.ErrInvalidPrice              Invalid price.
     * @throws com.jfx.ErrInvalidStops              Invalid stops.
     * @throws com.jfx.ErrInvalidTradeVolume        Invalid trade volume.
     * @throws com.jfx.ErrMarketClosed              Market is closed.
     * @throws com.jfx.ErrTradeDisabled             Trade is disabled.
     * @throws com.jfx.ErrNotEnoughMoney            Not enough money.
     * @throws com.jfx.ErrPriceChanged              Price changed.
     * @throws com.jfx.ErrOffQuotes                 Off quotes.
     * @throws com.jfx.ErrRequote                   Requote is required.
     * @throws com.jfx.ErrOrderLocked               Order is locked.
     * @throws com.jfx.ErrLongPositionsOnlyAllowed  Long positions only allowed.
     * @throws com.jfx.ErrTooManyRequests           Too many requests.
     * @throws com.jfx.ErrTradeTimeout2             trade timeout 2.
     * @throws com.jfx.ErrTradeTimeout3             trade timeout 3.
     * @throws com.jfx.ErrTradeTimeout4             trade timeout 3.
     * @throws com.jfx.ErrTradeModifyDenied         Modification denied because an order is too close to market.
     * @throws com.jfx.ErrTradeContextBusy          Trade context is busy.
     * @throws com.jfx.ErrTradeExpirationDenied     Expirations are denied by broker.
     * @throws com.jfx.ErrTradeTooManyOrders        The amount of opened and pending orders has reached the limit set by a broker.
     * @throws com.jfx.ErrNoResult                  No error returned, but the result is unknown.
     */
    public boolean orderModify(
            long ticket,
            double price,
            double stoploss,
            double takeprofit,
            java.util.Date expiration,
            long arrowColor
    ) throws ErrCustomIndicatorError, ErrIntegerParameterExpected, ErrInvalidFunctionParamvalue, ErrInvalidPriceParam, ErrInvalidTicket, ErrUnknownSymbol, ErrTradeNotAllowed, ErrNoResult, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders {
        StringBuilder command = new StringBuilder("88 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(ticket).append(ARG_END);
        command.append(ARG_BEGIN).append(price).append(ARG_END);
        command.append(ARG_BEGIN).append(stoploss).append(ARG_END);
        command.append(ARG_BEGIN).append(takeprofit).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(expiration)).append(ARG_END);
        command.append(ARG_BEGIN).append(arrowColor).append(ARG_END);
        //
        String result = delegateToTerminal(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderModify(");
            signature.append("ticket=").append(ticket);
            signature.append(", price=").append(price);
            signature.append(", stoploss=").append(stoploss);
            signature.append(", takeprofit=").append(takeprofit);
            signature.append(", expiration=").append(timeConvert(expiration));
            signature.append(", arrowColor=").append(arrowColor);
            signature.append(')');
            switch (error) {
                case 4055:
                    throw new ErrCustomIndicatorError(signature.toString());
                case 4063:
                    throw new ErrIntegerParameterExpected(signature.toString());
                case 4051:
                    throw new ErrInvalidFunctionParamvalue(signature.toString());
                case 4107:
                    throw new ErrInvalidPriceParam(signature.toString());
                case 4108:
                    throw new ErrInvalidTicket(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                case 4109:
                    throw new ErrTradeNotAllowed(signature.toString());
                case 1:
                    throw new ErrNoResult(signature.toString());
                case 2:
                    throw new ErrCommonError(signature.toString());
                case 3:
                    throw new ErrInvalidTradeParameters(signature.toString());
                case 4:
                    throw new ErrServerBusy(signature.toString());
                case 5:
                    throw new ErrOldVersion(signature.toString());
                case 6:
                    throw new ErrNoConnection(signature.toString());
                case 8:
                    throw new ErrTooFrequentRequests(signature.toString());
                case 64:
                    throw new ErrAccountDisabled(signature.toString());
                case 65:
                    throw new ErrInvalidAccount(signature.toString());
                case 128:
                    throw new ErrTradeTimeout(signature.toString());
                case 129:
                    throw new ErrInvalidPrice(signature.toString());
                case 130:
                    throw new ErrInvalidStops(signature.toString());
                case 131:
                    throw new ErrInvalidTradeVolume(signature.toString());
                case 132:
                    throw new ErrMarketClosed(signature.toString());
                case 133:
                    throw new ErrTradeDisabled(signature.toString());
                case 134:
                    throw new ErrNotEnoughMoney(signature.toString());
                case 135:
                    throw new ErrPriceChanged(signature.toString());
                case 136:
                    throw new ErrOffQuotes(signature.toString());
                case 138:
                    throw new ErrRequote(signature.toString());
                case 139:
                    throw new ErrOrderLocked(signature.toString());
                case 140:
                    throw new ErrLongPositionsOnlyAllowed(signature.toString());
                case 141:
                    throw new ErrTooManyRequests(signature.toString());
                case 142:
                    throw new ErrTradeTimeout2(signature.toString());
                case 143:
                    throw new ErrTradeTimeout3(signature.toString());
                case 144:
                    throw new ErrTradeTimeout4(signature.toString());
                case 145:
                    throw new ErrTradeModifyDenied(signature.toString());
                case 146:
                    throw new ErrTradeContextBusy(signature.toString());
                case 147:
                    throw new ErrTradeExpirationDenied(signature.toString());
                case 148:
                    throw new ErrTradeTooManyOrders(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns open price for the currently selected order. Order must be first selected by the OrderSelect() function.
     *
     * @return Returns open price for the currently selected order. Order must be first selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     */
    public double orderOpenPrice(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("89 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderOpenPrice(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns open time for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns open time for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     */
    public java.util.Date orderOpenTime(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("90 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderOpenTime(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        java.util.Date res;
        //
        // decompose result
        //
        long time = Long.parseLong(result) * 1000;
        res = new java.util.Date(time + getTZCorrectionOffset(time));
        return res;
    }


    /**
     * Prints information about the selected order in the log in the following format: ticket number; open time; trade operation; amount of lots; open price; Stop Loss; Take Profit; close time; close price; commission; swap; profit; comment; magic number; pending order expiration date. Order must be selected by the OrderSelect() function.
     *
     * @throws com.jfx.ErrNoOrderSelected
     */
    public void orderPrint(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("91 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderPrint(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;

        //
        // decompose result
        //


    }


    /**
     * Returns the net profit value (without swaps or commissions) for the selected order. For open positions, it is the current unrealized profit. For closed orders, it is the fixed profit. Returns profit for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns the net profit value (without swaps or commissions) for the selected order. For open positions, it is the current unrealized profit. For closed orders, it is the fixed profit. Returns profit for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     */
    public double orderProfit(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("92 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderProfit(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * The function selects an order for further processing. It returns TRUE if the function succeeds. It returns FALSE if the function fails. To get the error information, one has to call the GetLastError() function. The pool parameter is ignored if the order is selected by the ticket number. The ticket number is a unique order identifier. To find out from what list the order has been selected, its close time must be analyzed. If the order close time equals to 0, the order is open or pending and taken from the terminal open positions list. One can distinguish an open position from a pending order by the order type. If the order close time does not equal to 0, the order is a closed order or a deleted pending order and was selected from the terminal history. They also differ from each other by their order types.
     *
     * @param index  Order index or order ticket depending on the second parameter.
     * @param select Selecting flags. It can be any of the following values: SELECT_BY_POS - index in the order pool, SELECT_BY_TICKET - index is order ticket.
     * @param pool   Optional order pool index. Used when the selected parameter is SELECT_BY_POS. It can be any of the following values: MODE_TRADES (default)- order selected from trading pool(opened and pending orders), MODE_HISTORY - order selected from history pool (closed and canceled order).
     * @return The function selects an order for further processing. It returns TRUE if the function succeeds. It returns FALSE if the function fails. To get the error information, one has to call the GetLastError() function. The pool parameter is ignored if the order is selected by the ticket number. The ticket number is a unique order identifier. To find out from what list the order has been selected, its close time must be analyzed. If the order close time equals to 0, the order is open or pending and taken from the terminal open positions list. One can distinguish an open position from a pending order by the order type. If the order close time does not equal to 0, the order is a closed order or a deleted pending order and was selected from the terminal history. They also differ from each other by their order types.
     */
    public boolean orderSelect(
            long index,
            SelectionType select,
            SelectionPool pool
    ) {
        StringBuilder command = new StringBuilder("93 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(index).append(ARG_END);
        command.append(ARG_BEGIN).append(select.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(pool.getVal(getMType())).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderSelect(");
            signature.append("index=").append(index);
            signature.append(", select=").append(select.getVal(getMType()));
            signature.append(", pool=").append(pool.getVal(getMType()));
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }

    /**
     * The main function used to open a position or place a pending order. Returns number of the ticket assigned to the order by the trade server or -1 if it fails. To get additional error information, one has to call the GetLastError() function. Notes: At opening of a market order (OP_SELL or OP_BUY), only the latest prices of Bid (for selling) or Ask (for buying) can be used as open price. If operation is performed with a security differing from the current one, the MarketInfo() function must be used with MODE_BID or MODE_ASK parameter for the latest quotes for this security to be obtained. Calculated or unnormalized price cannot be applied. If there has not been the requested open price in the price thread or it has not been normalized according to the amount of digits after decimal point, the error 129 (ERR_INVALID_PRICE) will be generated. If the requested open price is fully out of date, the error 138 (ERR_REQUOTE) will be generated independently on the slippage parameter. If the requested price is out of date, but present in the thread, the position will be opened at the current price and only if the current price lies within the range of price+-slippage. StopLoss and TakeProfit levels cannot be too close to the market. The minimal distance of stop levels in points can be obtained using the MarketInfo() function with MODE_STOPLEVEL parameter. In the case of erroneous or unnormalized stop levels, the error 130 (ERR_INVALID_STOPS) will be generated. At placing of a pending order, the open price cannot be too close to the market. The minimal distance of the pending price from the current market one in points can be obtained using the MarketInfo() function with the MODE_STOPLEVEL parameter. In case of false open price of a pending order, the error 130 (ERR_INVALID_STOPS) will be generated. Applying of pending order expiration time can be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated. On some trade servers, the total amount of open and pending orders can be limited. If this limit has been exceeded, no new position will be opened (or no pending order will be placed) and trade server will return error 148 (ERR_TRADE_TOO_MANY_ORDERS).
     *
     * @param symbol     Symbol for trading.
     * @param cmd        Operation type. It can be any of the Trade operation enumeration.
     * @param volume     Number of lots.
     * @param price      Preferred price of the trade.
     * @param slippage   Maximum price slippage for buy or sell orders in points.
     * @param stoploss   StopLoss level.
     * @param takeprofit TakeProfit level.
     * @param comment    Order comment text. Last part of the comment may be changed by server.
     * @param magic      Order magic number. May be used as user defined identifier.
     * @param expiration Order expiration time (for pending orders only).
     * @param arrowColor Color of the arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return Returns ticket number assigned to the order by the trade server and throws an exception in case of failure.
     * @throws com.jfx.ErrCustomIndicatorError      Custom indicator error.
     * @throws com.jfx.ErrIntegerParameterExpected  Integer parameter expected.
     * @throws com.jfx.ErrInvalidFunctionParamvalue Invalid function parameter value.
     * @throws com.jfx.ErrInvalidPriceParam         Invalid price.
     * @throws com.jfx.ErrUnknownSymbol             Unknown symbol.
     * @throws com.jfx.ErrTradeNotAllowed           Trade is not allowed.
     * @throws com.jfx.ErrCommonError               Common error.
     * @throws com.jfx.ErrInvalidTradeParameters    Invalid trade parameters.
     * @throws com.jfx.ErrServerBusy                Trade server is busy.
     * @throws com.jfx.ErrOldVersion                Old version of the client terminal.
     * @throws com.jfx.ErrNoConnection              No connection with trade server.
     * @throws com.jfx.ErrTooFrequentRequests       Too frequent requests.
     * @throws com.jfx.ErrAccountDisabled           Account disabled.
     * @throws com.jfx.ErrInvalidAccount            Invalid account.
     * @throws com.jfx.ErrTradeTimeout              Trade timeout.
     * @throws com.jfx.ErrInvalidPrice              Invalid price.
     * @throws com.jfx.ErrInvalidStops              Invalid stops.
     * @throws com.jfx.ErrInvalidTradeVolume        Invalid trade volume.
     * @throws com.jfx.ErrMarketClosed              Market is closed.
     * @throws com.jfx.ErrTradeDisabled             Trade is disabled.
     * @throws com.jfx.ErrNotEnoughMoney            Not enough money.
     * @throws com.jfx.ErrPriceChanged              Price changed.
     * @throws com.jfx.ErrOffQuotes                 Off quotes.
     * @throws com.jfx.ErrRequote                   Requote is required.
     * @throws com.jfx.ErrOrderLocked               Order is locked.
     * @throws com.jfx.ErrLongPositionsOnlyAllowed  Long positions only allowed.
     * @throws com.jfx.ErrTooManyRequests           Too many requests.
     * @throws com.jfx.ErrTradeTimeout2             trade timeout 2.
     * @throws com.jfx.ErrTradeTimeout3             trade timeout 3.
     * @throws com.jfx.ErrTradeTimeout4             trade timeout 3.
     * @throws com.jfx.ErrTradeModifyDenied         Modification denied because an order is too close to market.
     * @throws com.jfx.ErrTradeContextBusy          Trade context is busy.
     * @throws com.jfx.ErrTradeExpirationDenied     Expirations are denied by broker.
     * @throws com.jfx.ErrTradeTooManyOrders        The amount of opened and pending orders has reached the limit set by a broker.
     * @throws com.jfx.ErrStringParameterExpected   String parameter expected.
     * @throws com.jfx.ErrLongsNotAllowed           Longs are not allowed.
     * @throws com.jfx.ErrShortsNotAllowed          Shorts are not allowed.
     * @deprecated - ticket's data type should be 'long', please use {@link #orderSend(String, TradeOperation, double, double, int, double, double, String, int, java.util.Date) orderSend()} method instead.
     */
    public int orderSend(
            String symbol,
            TradeOperation cmd,
            double volume,
            double price,
            int slippage,
            double stoploss,
            double takeprofit,
            String comment,
            int magic,
            java.util.Date expiration,
            long arrowColor
    ) throws ErrInvalidFunctionParamvalue, ErrCustomIndicatorError, ErrStringParameterExpected, ErrIntegerParameterExpected, ErrUnknownSymbol, ErrInvalidPriceParam, ErrTradeNotAllowed, ErrLongsNotAllowed, ErrShortsNotAllowed, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders {
        return (int) orderSend(symbol, cmd, volume, price, slippage, stoploss, takeprofit, comment, magic, expiration);
    }

    /**
     * The main function used to open a position or place a pending order. Returns number of the ticket assigned to the order by the trade server or -1 if it fails. To get additional error information, one has to call the GetLastError() function. Notes: At opening of a market order (OP_SELL or OP_BUY), only the latest prices of Bid (for selling) or Ask (for buying) can be used as open price. If operation is performed with a security differing from the current one, the MarketInfo() function must be used with MODE_BID or MODE_ASK parameter for the latest quotes for this security to be obtained. Calculated or unnormalized price cannot be applied. If there has not been the requested open price in the price thread or it has not been normalized according to the amount of digits after decimal point, the error 129 (ERR_INVALID_PRICE) will be generated. If the requested open price is fully out of date, the error 138 (ERR_REQUOTE) will be generated independently on the slippage parameter. If the requested price is out of date, but present in the thread, the position will be opened at the current price and only if the current price lies within the range of price+-slippage. StopLoss and TakeProfit levels cannot be too close to the market. The minimal distance of stop levels in points can be obtained using the MarketInfo() function with MODE_STOPLEVEL parameter. In the case of erroneous or unnormalized stop levels, the error 130 (ERR_INVALID_STOPS) will be generated. At placing of a pending order, the open price cannot be too close to the market. The minimal distance of the pending price from the current market one in points can be obtained using the MarketInfo() function with the MODE_STOPLEVEL parameter. In case of false open price of a pending order, the error 130 (ERR_INVALID_STOPS) will be generated. Applying of pending order expiration time can be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated. On some trade servers, the total amount of open and pending orders can be limited. If this limit has been exceeded, no new position will be opened (or no pending order will be placed) and trade server will return error 148 (ERR_TRADE_TOO_MANY_ORDERS).
     *
     * @param symbol     Symbol for trading.
     * @param cmd        Operation type. It can be any of the Trade operation enumeration.
     * @param volume     Number of lots.
     * @param price      Preferred price of the trade.
     * @param slippage   Maximum price slippage for buy or sell orders in points.
     * @param stoploss   StopLoss level.
     * @param takeprofit TakeProfit level.
     * @param comment    Order comment text. Last part of the comment may be changed by server.
     * @param magic      Order magic number. May be used as user defined identifier.
     * @param expiration Order expiration time (for pending orders only).
     * @return The main function used to open a position or place a pending order. Returns number of the ticket assigned to the order by the trade server or -1 if it fails. To get additional error information, one has to call the GetLastError() function. Notes: At opening of a market order (OP_SELL or OP_BUY), only the latest prices of Bid (for selling) or Ask (for buying) can be used as open price. If operation is performed with a security differing from the current one, the MarketInfo() function must be used with MODE_BID or MODE_ASK parameter for the latest quotes for this security to be obtained. Calculated or unnormalized price cannot be applied. If there has not been the requested open price in the price thread or it has not been normalized according to the amount of digits after decimal point, the error 129 (ERR_INVALID_PRICE) will be generated. If the requested open price is fully out of date, the error 138 (ERR_REQUOTE) will be generated independently on the slippage parameter. If the requested price is out of date, but present in the thread, the position will be opened at the current price and only if the current price lies within the range of price+-slippage. StopLoss and TakeProfit levels cannot be too close to the market. The minimal distance of stop levels in points can be obtained using the MarketInfo() function with MODE_STOPLEVEL parameter. In the case of erroneous or unnormalized stop levels, the error 130 (ERR_INVALID_STOPS) will be generated. At placing of a pending order, the open price cannot be too close to the market. The minimal distance of the pending price from the current market one in points can be obtained using the MarketInfo() function with the MODE_STOPLEVEL parameter. In case of false open price of a pending order, the error 130 (ERR_INVALID_STOPS) will be generated. Applying of pending order expiration time can be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated. On some trade servers, the total amount of open and pending orders can be limited. If this limit has been exceeded, no new position will be opened (or no pending order will be placed) and trade server will return error 148 (ERR_TRADE_TOO_MANY_ORDERS).
     * @throws com.jfx.ErrCustomIndicatorError      Custom indicator error.
     * @throws com.jfx.ErrIntegerParameterExpected  Integer parameter expected.
     * @throws com.jfx.ErrInvalidFunctionParamvalue Invalid function parameter value.
     * @throws com.jfx.ErrInvalidPriceParam         Invalid price.
     * @throws com.jfx.ErrUnknownSymbol             Unknown symbol.
     * @throws com.jfx.ErrTradeNotAllowed           Trade is not allowed.
     * @throws com.jfx.ErrCommonError               Common error.
     * @throws com.jfx.ErrInvalidTradeParameters    Invalid trade parameters.
     * @throws com.jfx.ErrServerBusy                Trade server is busy.
     * @throws com.jfx.ErrOldVersion                Old version of the client terminal.
     * @throws com.jfx.ErrNoConnection              No connection with trade server.
     * @throws com.jfx.ErrTooFrequentRequests       Too frequent requests.
     * @throws com.jfx.ErrAccountDisabled           Account disabled.
     * @throws com.jfx.ErrInvalidAccount            Invalid account.
     * @throws com.jfx.ErrTradeTimeout              Trade timeout.
     * @throws com.jfx.ErrInvalidPrice              Invalid price.
     * @throws com.jfx.ErrInvalidStops              Invalid stops.
     * @throws com.jfx.ErrInvalidTradeVolume        Invalid trade volume.
     * @throws com.jfx.ErrMarketClosed              Market is closed.
     * @throws com.jfx.ErrTradeDisabled             Trade is disabled.
     * @throws com.jfx.ErrNotEnoughMoney            Not enough money.
     * @throws com.jfx.ErrPriceChanged              Price changed.
     * @throws com.jfx.ErrOffQuotes                 Off quotes.
     * @throws com.jfx.ErrRequote                   Requote is required.
     * @throws com.jfx.ErrOrderLocked               Order is locked.
     * @throws com.jfx.ErrLongPositionsOnlyAllowed  Long positions only allowed.
     * @throws com.jfx.ErrTooManyRequests           Too many requests.
     * @throws com.jfx.ErrTradeTimeout2             trade timeout 2.
     * @throws com.jfx.ErrTradeTimeout3             trade timeout 3.
     * @throws com.jfx.ErrTradeTimeout4             trade timeout 3.
     * @throws com.jfx.ErrTradeModifyDenied         Modification denied because an order is too close to market.
     * @throws com.jfx.ErrTradeContextBusy          Trade context is busy.
     * @throws com.jfx.ErrTradeExpirationDenied     Expirations are denied by broker.
     * @throws com.jfx.ErrTradeTooManyOrders        The amount of opened and pending orders has reached the limit set by a broker.
     * @throws com.jfx.ErrStringParameterExpected   String parameter expected.
     * @throws com.jfx.ErrLongsNotAllowed           Longs are not allowed.
     * @throws com.jfx.ErrShortsNotAllowed          Shorts are not allowed.
     */
    public long orderSend(
            String symbol,
            TradeOperation cmd,
            double volume,
            double price,
            int slippage,
            double stoploss,
            double takeprofit,
            String comment,
            int magic,
            java.util.Date expiration
    ) throws ErrInvalidFunctionParamvalue, ErrCustomIndicatorError, ErrStringParameterExpected, ErrIntegerParameterExpected, ErrUnknownSymbol, ErrInvalidPriceParam, ErrTradeNotAllowed, ErrLongsNotAllowed, ErrShortsNotAllowed, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders {
        StringBuilder command = new StringBuilder("94 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(cmd.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(volume).append(ARG_END);
        command.append(ARG_BEGIN).append(price).append(ARG_END);
        command.append(ARG_BEGIN).append(slippage).append(ARG_END);
        command.append(ARG_BEGIN).append(stoploss).append(ARG_END);
        command.append(ARG_BEGIN).append(takeprofit).append(ARG_END);
        command.append(ARG_BEGIN).append(replaceSpecialChars(comment)).append(ARG_END);
        command.append(ARG_BEGIN).append(magic).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(expiration)).append(ARG_END);
        command.append(ARG_BEGIN).append(0).append(ARG_END);
        //
        String result = delegateToTerminal(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderSend(");
            signature.append("symbol=").append(symbol);
            signature.append(", cmd=").append(cmd.getVal(getMType()));
            signature.append(", volume=").append(volume);
            signature.append(", price=").append(price);
            signature.append(", slippage=").append(slippage);
            signature.append(", stoploss=").append(stoploss);
            signature.append(", takeprofit=").append(takeprofit);
            signature.append(", comment=").append(comment);
            signature.append(", magic=").append(magic);
            signature.append(", expiration=").append(timeConvert(expiration));
//            signature.append(", arrowColor=").append(arrowColor);
            signature.append(')');
            switch (error) {
                case 4051:
                    throw new ErrInvalidFunctionParamvalue(signature.toString());
                case 4055:
                    throw new ErrCustomIndicatorError(signature.toString());
                case 4062:
                    throw new ErrStringParameterExpected(signature.toString());
                case 4063:
                    throw new ErrIntegerParameterExpected(signature.toString());
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                case 4107:
                    throw new ErrInvalidPriceParam(signature.toString());
                case 4109:
                    throw new ErrTradeNotAllowed(signature.toString());
                case 4110:
                    throw new ErrLongsNotAllowed(signature.toString());
                case 4111:
                    throw new ErrShortsNotAllowed(signature.toString());
                case 2:
                    throw new ErrCommonError(signature.toString());
                case 3:
                    throw new ErrInvalidTradeParameters(signature.toString());
                case 4:
                    throw new ErrServerBusy(signature.toString());
                case 5:
                    throw new ErrOldVersion(signature.toString());
                case 6:
                    throw new ErrNoConnection(signature.toString());
                case 8:
                    throw new ErrTooFrequentRequests(signature.toString());
                case 64:
                    throw new ErrAccountDisabled(signature.toString());
                case 65:
                    throw new ErrInvalidAccount(signature.toString());
                case 128:
                    throw new ErrTradeTimeout(signature.toString());
                case 129:
                    throw new ErrInvalidPrice(signature.toString());
                case 130:
                    throw new ErrInvalidStops(signature.toString());
                case 131:
                    throw new ErrInvalidTradeVolume(signature.toString());
                case 132:
                    throw new ErrMarketClosed(signature.toString());
                case 133:
                    throw new ErrTradeDisabled(signature.toString());
                case 134:
                    throw new ErrNotEnoughMoney(signature.toString());
                case 135:
                    throw new ErrPriceChanged(signature.toString());
                case 136:
                    throw new ErrOffQuotes(signature.toString());
                case 138:
                    throw new ErrRequote(signature.toString());
                case 139:
                    throw new ErrOrderLocked(signature.toString());
                case 140:
                    throw new ErrLongPositionsOnlyAllowed(signature.toString());
                case 141:
                    throw new ErrTooManyRequests(signature.toString());
                case 142:
                    throw new ErrTradeTimeout2(signature.toString());
                case 143:
                    throw new ErrTradeTimeout3(signature.toString());
                case 144:
                    throw new ErrTradeTimeout4(signature.toString());
                case 145:
                    throw new ErrTradeModifyDenied(signature.toString());
                case 146:
                    throw new ErrTradeContextBusy(signature.toString());
                case 147:
                    throw new ErrTradeExpirationDenied(signature.toString());
                case 148:
                    throw new ErrTradeTooManyOrders(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        long res;
        //
        // decompose result
        //
        res = Long.parseLong(result);
        return res;
    }

    private String replaceSpecialChars(String s) {
        return s == null ? null : s.replace(ARG_BEGIN, ' ').replace(ARG_END, ' ');
    }

    /**
     * Returns the number of closed orders in the account history loaded into the terminal. The history list size depends on the current settings of the Account history tab of the terminal.
     *
     * @return Returns the number of closed orders in the account history loaded into the terminal. The history list size depends on the current settings of the Account history tab of the terminal.
     */
    public int ordersHistoryTotal(
    ) {
        StringBuilder command = new StringBuilder("95 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("ordersHistoryTotal(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns stop loss value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns stop loss value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     */
    public double orderStopLoss(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("96 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderStopLoss(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns market and pending orders count.
     *
     * @return Returns market and pending orders count.
     */
    public int ordersTotal(
    ) {
        StringBuilder command = new StringBuilder("97 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("ordersTotal(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns swap value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns swap value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     */
    public double orderSwap(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("98 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderSwap(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns the order symbol value for selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns the order symbol value for selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     */
    public String orderSymbol(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("99 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderSymbol(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Returns take profit value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns take profit value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     */
    public double orderTakeProfit(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("100 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderTakeProfit(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns ticket number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns ticket number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     * @deprecated - ticket's data type should be 'long', please use {@link #orderTicketNumber orderTicketNumber()} method instead.
     */
    public int orderTicket() throws ErrNoOrderSelected {
        return (int) orderTicketNumber();
    }

    /**
     * Returns ticket number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns ticket number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     */
    public long orderTicketNumber() throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("101 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderTicket(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        long res;
        //
        // decompose result
        //
        res = Long.parseLong(result);
        return res;
    }


    /**
     * Returns order operation type for the currently selected order. It can be any of the following values: OP_BUY - buying position, OP_SELL - selling position, OP_BUYLIMIT - buy limit pending position, OP_BUYSTOP - buy stop pending position, OP_SELLLIMIT - sell limit pending position, OP_SELLSTOP - sell stop pending position. Note: order must be selected by OrderSelect() function.
     *
     * @return Returns order operation type for the currently selected order. It can be any of the following values: OP_BUY - buying position, OP_SELL - selling position, OP_BUYLIMIT - buy limit pending position, OP_BUYSTOP - buy stop pending position, OP_SELLLIMIT - sell limit pending position, OP_SELLSTOP - sell stop pending position. Note: order must be selected by OrderSelect() function.
     * @throws com.jfx.ErrNoOrderSelected
     */
    public int orderType(
    ) throws ErrNoOrderSelected {
        StringBuilder command = new StringBuilder("102 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("orderType(");

            signature.append(')');
            switch (error) {
                case 4105:
                    throw new ErrNoOrderSelected(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns TRUE if a thread for trading is occupied by another expert advisor, otherwise returns FALSE.
     *
     * @return Returns TRUE if a thread for trading is occupied by another expert advisor, otherwise returns FALSE.
     */
    public boolean isTradeContextBusy(
    ) {
        StringBuilder command = new StringBuilder("103 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isTradeContextBusy(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Refreshing of data in pre-defined variables and series arrays. This function is used when expert advisor has been calculating for a long time and needs data refreshing. Returns TRUE if data are refreshed, otherwise returns FALSE. The only reason for data cannot be refreshed is that they are the current data of the client terminal. Experts and scripts operate with their own copy of history data. Data of the current symbol are copied at the first launch of the expert or script. At each subsequent launch of the expert (remember that script is executed only once and does not depend on incoming ticks), the initial copy will be updated. One or more new ticks can income while the expert or script is operating, and data can become out of date.
     *
     * @return Refreshing of data in pre-defined variables and series arrays. This function is used when expert advisor has been calculating for a long time and needs data refreshing. Returns TRUE if data are refreshed, otherwise returns FALSE. The only reason for data cannot be refreshed is that they are the current data of the client terminal. Experts and scripts operate with their own copy of history data. Data of the current symbol are copied at the first launch of the expert or script. At each subsequent launch of the expert (remember that script is executed only once and does not depend on incoming ticks), the initial copy will be updated. One or more new ticks can income while the expert or script is operating, and data can become out of date.
     */
    public boolean refreshRates(
    ) {
        StringBuilder command = new StringBuilder("104 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("refreshRates(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns the value of the Stop Out level.
     *
     * @return Returns the value of the Stop Out level.
     */
    public int accountStopoutLevel(
    ) {
        StringBuilder command = new StringBuilder("105 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountStopoutLevel(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the calculation mode for the Stop Out level. Calculation mode can take the following values: 0 - calculation of percentage ratio between margin and equity; 1 - comparison of the free margin level to the absolute value.
     *
     * @return Returns the calculation mode for the Stop Out level. Calculation mode can take the following values: 0 - calculation of percentage ratio between margin and equity; 1 - comparison of the free margin level to the absolute value.
     */
    public int accountStopoutMode(
    ) {
        StringBuilder command = new StringBuilder("106 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountStopoutMode(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * The MessageBox function creates, displays, and operates message box. The message box contains an application-defined message and header, as well as a random combination of predefined icons and push buttons. If the function succeeds, the returned value is one of the MessageBox return code values.The function cannot be called from custom indicators since they are executed within interface thread and may not decelerate it.
     *
     * @param text    Text that contains the message to be displayed.
     * @param caption Text to be displayed in the header of the dialog box. If this parameter is NULL, the expert name will be displayed in the header.
     * @param flags   Flags that determine the type and behavior of the dialog box (see enumeration class MessageBoxFlag). They can represent a conbination of flags from the following groups.
     * @return The MessageBox function creates, displays, and operates message box. The message box contains an application-defined message and header, as well as a random combination of predefined icons and push buttons. If the function succeeds, the returned value is one of the MessageBox return code values.The function cannot be called from custom indicators since they are executed within interface thread and may not decelerate it.
     */
    public int messageBox(
            String text,
            String caption,
            int flags
    ) {
        StringBuilder command = new StringBuilder("107 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(text)).append(ARG_END);
        command.append(ARG_BEGIN).append(replaceSpecialChars(caption)).append(ARG_END);
        command.append(ARG_BEGIN).append(flags).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("messageBox(");
            signature.append("text=").append(text);
            signature.append(", caption=").append(caption);
            signature.append(", flags=").append(flags);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the code of the uninitialization reason for the experts, custom indicators, and scripts. The returned values can be ones of Uninitialize reason codes. This function can also be called in function init() to analyze the reasons for deinitialization of the previous launch.
     *
     * @return Returns the code of the uninitialization reason for the experts, custom indicators, and scripts. The returned values can be ones of Uninitialize reason codes. This function can also be called in function init() to analyze the reasons for deinitialization of the previous launch.
     */
    public int uninitializeReason(
    ) {
        StringBuilder command = new StringBuilder("108 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("uninitializeReason(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns TRUE if the expert is allowed to trade and a thread for trading is not occupied, otherwise returns FALSE.
     *
     * @return Returns TRUE if the expert is allowed to trade and a thread for trading is not occupied, otherwise returns FALSE.
     */
    public boolean isTradeAllowed(
    ) {
        StringBuilder command = new StringBuilder("109 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isTradeAllowed(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }

    /**
     * Checks trade status for the specified symbol at the specified time
     *
     * @return Returns TRUE if the expert was allowed to trade the symbol at the specified time, otherwise returns FALSE.
     */
    public boolean isTradeAllowed(
            String symbol,
            Date testedTime
    ) {
        StringBuilder command = new StringBuilder("166 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(testedTime)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isTradeAllowed(");
            signature.append(symbol).append(',');
            signature.append(testedTime);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }

    /**
     * Applies a specific template from a specified file to the chart. The command is added to chart message queue and executed only after all previous commands have been processed.
     *
     * @param chartId Chart ID. 0 means the current chart.
     * @param fileName The name of the file containing the template.
     * @return Returns true if the command has been added to chart queue, otherwise false.
     * <b>Note:</b> The Expert Advisor will be unloaded and will not be able to continue operating in case of successful loading of a new template to the chart it is attached to.
     * <b>MQL4: http://docs.mql4.com/chart_operations/chartapplytemplate</b>
     */
    public boolean chartApplyTemplate(
            long chartId,
            String fileName
    ) {
        StringBuilder command = new StringBuilder("170 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(chartId).append(ARG_END);
        command.append(ARG_BEGIN).append(fileName).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("chartApplyTemplate(");
            signature.append(chartId).append(',');
            signature.append(fileName);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }

    /**
     * Saves current chart settings in a template with a specified name. The command is added to chart message queue and executed only after all previous commands have been processed.
     *
     * @param chartId Chart ID. 0 means the current chart.
     * @param fileName The filename to save the template. The ".tpl" extension will be added to the filename automatically, there is no need to specify it. The template is saved in terminal_directory\Profiles\Templates\ and can be used for manual application in the terminal. If a template with the same filename already exists, the contents of this file will be overwritten.
     * @return Returns true if the command has been added to chart queue, otherwise false.
     * <b>Note:</b> Using templates, you can save chart settings with all applied indicators and graphical objects, to then apply it to another chart.
     * <b>MQL4: http://docs.mql4.com/chart_operations/chartsavetemplate</b>
     */
    public boolean chartSaveTemplate(
            long chartId,
            String fileName
    ) {
        StringBuilder command = new StringBuilder("171 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(chartId).append(ARG_END);
        command.append(ARG_BEGIN).append(fileName).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("chartSaveTemplate(");
            signature.append(chartId).append(',');
            signature.append(fileName);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }

    /**
     * Opens a new chart with the specified symbol and period. The command is added to chart message queue and executed only after all previous commands have been processed.
     *
     * @param symbol Chart symbol.
     * @param period Chart period (timeframe).
     * @return If successful, it returns the opened chart ID. Otherwise returns 0.
     * <b>MQL4: http://docs.mql4.com/chart_operations/chartopen</b>
     */
    public long chartOpen(
            String symbol,
            Timeframe period
    ) {
        StringBuilder command = new StringBuilder("172 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(period.getVal(getMType())).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("chartOpen(");
            signature.append(symbol).append(',');
            signature.append(period.getVal(getMType()));
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        long res;
        //
        // decompose result
        //
        res = Long.parseLong(result);
        return res;
    }

    /**
     * Closes the specified chart.
     *
     * @param chartId Chart ID. 0 means the current chart.
     * @return If successful, returns true, otherwise false.
     * <b>MQL4: http://docs.mql4.com/chart_operations/chartclose</b>
     */
    public boolean chartClose(
            long chartId
    ) {
        StringBuilder command = new StringBuilder("173 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(chartId).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("chartClose(");
            signature.append(chartId);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }

    /**
     * Returns TRUE if the program (an expert or a script) has been commanded to stop its operation, otherwise returns FALSE. The program can continue operation for 2.5 seconds more before the client terminal stops its performing forcedly.
     *
     * @return Returns TRUE if the program (an expert or a script) has been commanded to stop its operation, otherwise returns FALSE. The program can continue operation for 2.5 seconds more before the client terminal stops its performing forcedly.
     */
    public boolean isStopped(
    ) {
        StringBuilder command = new StringBuilder("110 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isStopped(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns TRUE if expert runs in the strategy tester optimization mode, otherwise returns FALSE.
     *
     * @return Returns TRUE if expert runs in the strategy tester optimization mode, otherwise returns FALSE.
     */
    public boolean isOptimization(
    ) {
        StringBuilder command = new StringBuilder("111 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isOptimization(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns TRUE if the expert can call library function, otherwise returns FALSE. See also IsDllsAllowed(), IsTradeAllowed().
     *
     * @return Returns TRUE if the expert can call library function, otherwise returns FALSE. See also IsDllsAllowed(), IsTradeAllowed().
     */
    public boolean isLibrariesAllowed(
    ) {
        StringBuilder command = new StringBuilder("112 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isLibrariesAllowed(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns TRUE if the function DLL call is allowed for the expert, otherwise returns FALSE.
     *
     * @return Returns TRUE if the function DLL call is allowed for the expert, otherwise returns FALSE.
     */
    public boolean isDllsAllowed(
    ) {
        StringBuilder command = new StringBuilder("113 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isDllsAllowed(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns TRUE if expert advisors are enabled for running, otherwise returns FALSE.
     *
     * @return Returns TRUE if expert advisors are enabled for running, otherwise returns FALSE.
     */
    public boolean isExpertEnabled(
    ) {
        StringBuilder command = new StringBuilder("114 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("isExpertEnabled(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns free margin that remains after the specified position has been opened at the current price on the current account. If the free margin is insufficient, an error 134 (ERR_NOT_ENOUGH_MONEY) will be generated.
     *
     * @param symbol Symbol for trading operation.
     * @param cmd    Operation type. It can be either OP_BUY or OP_SELL.
     * @param volume Number of lots.
     * @return Returns free margin that remains after the specified position has been opened at the current price on the current account. If the free margin is insufficient, an error 134 (ERR_NOT_ENOUGH_MONEY) will be generated.
     */
    public double accountFreeMarginCheck(
            String symbol,
            TradeOperation cmd,
            double volume
    ) {
        StringBuilder command = new StringBuilder("115 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(cmd.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(volume).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountFreeMarginCheck(");
            signature.append("symbol=").append(symbol);
            signature.append(", cmd=").append(cmd.getVal(getMType()));
            signature.append(", volume=").append(volume);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Calculation mode of free margin allowed to open positions on the current account. The calculation mode can take the following values: 0 - floating profit/loss is not used for calculation; 1 - both floating profit and loss on open positions on the current account are used for free margin calculation; 2 - only profit value is used for calculation, the current loss on open positions is not considered; 3 - only loss value is used for calculation, the current loss on open positions is not considered.
     *
     * @return Calculation mode of free margin allowed to open positions on the current account. The calculation mode can take the following values: 0 - floating profit/loss is not used for calculation; 1 - both floating profit and loss on open positions on the current account are used for free margin calculation; 2 - only profit value is used for calculation, the current loss on open positions is not considered; 3 - only loss value is used for calculation, the current loss on open positions is not considered.
     */
    public double accountFreeMarginMode(
    ) {
        StringBuilder command = new StringBuilder("116 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountFreeMarginMode(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns leverage of the current account.
     *
     * @return Returns leverage of the current account.
     */
    public int accountLeverage(
    ) {
        StringBuilder command = new StringBuilder("117 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountLeverage(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the connected server name.
     *
     * @return Returns the connected server name.
     */
    public String accountServer(
    ) {
        StringBuilder command = new StringBuilder("118 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountServer(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Returns the name of company owning the client terminal.
     *
     * @return Returns the name of company owning the client terminal.
     */
    public String terminalCompany(
    ) {
        StringBuilder command = new StringBuilder("119 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("terminalCompany(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Returns client terminal name.
     *
     * @return Returns client terminal name.
     */
    public String terminalName(
    ) {
        StringBuilder command = new StringBuilder("120 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("terminalName(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Returns the directory, from which the client terminal was launched.
     *
     * @return Returns the directory, from which the client terminal was launched.
     */
    public String terminalPath(
    ) {
        StringBuilder command = new StringBuilder("121 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("terminalPath(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Displays a dialog box containing the user-defined data.
     *
     * @param arg Any value.
     */
    public void alert(
            String arg
    ) {
        StringBuilder command = new StringBuilder("122 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(arg)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("alert(");
            signature.append("arg=").append(arg);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;

        //
        // decompose result
        //


    }


    /**
     * Function plays a sound file. The file must be located in the terminal_dir\sounds directory (see TerminalPath()) or in its subdirectory.
     *
     * @param filename Path to the sound file.
     */
    public void playSound(
            String filename
    ) {
        StringBuilder command = new StringBuilder("123 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(filename).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("playSound(");
            signature.append("filename=").append(filename);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;

        //
        // decompose result
        //


    }


    /**
     * Return object description. For objects of OBJ_TEXT and OBJ_LABEL types, the text drawn by these objects will be returned.
     *
     * @param name Object name.
     * @return Return object description. For objects of OBJ_TEXT and OBJ_LABEL types, the text drawn by these objects will be returned.
     */
    public String objectDescription(
            String name
    ) {
        StringBuilder command = new StringBuilder("124 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectDescription(");
            signature.append("name=").append(name);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Search for an object having the specified name. The function returns index of the windows that contains the object to be found. If it fails, the returned value will be -1. To get the detailed error information, one has to call the GetLastError() function. The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index.
     *
     * @param name Object name to search for.
     * @return Search for an object having the specified name. The function returns index of the windows that contains the object to be found. If it fails, the returned value will be -1. To get the detailed error information, one has to call the GetLastError() function. The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index.
     */
    public int objectFind(
            String name
    ) {
        StringBuilder command = new StringBuilder("125 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectFind(");
            signature.append("name=").append(name);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * The function calculates and returns bar index (shift related to the current bar) for the given price. The bar index is calculated by the first and second coordinates using a linear equation. Applied to trendlines and similar objects. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name  Object name.
     * @param value Price value.
     * @return The function calculates and returns bar index (shift related to the current bar) for the given price. The bar index is calculated by the first and second coordinates using a linear equation. Applied to trendlines and similar objects. To get the detailed error information, one has to call the GetLastError() function.
     */
    public int objectGetShiftByValue(
            String name,
            double value
    ) {
        StringBuilder command = new StringBuilder("126 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(value).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectGetShiftByValue(");
            signature.append("name=").append(name);
            signature.append(", value=").append(value);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * The function calculates and returns the price value for the specified bar (shift related to the current bar). The price value is calculated by the first and second coordinates using a linear equation. Applied to trendlines and similar objects. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name  Object name.
     * @param shift Bar index.
     * @return The function calculates and returns the price value for the specified bar (shift related to the current bar). The price value is calculated by the first and second coordinates using a linear equation. Applied to trendlines and similar objects. To get the detailed error information, one has to call the GetLastError() function.
     */
    public double objectGetValueByShift(
            String name,
            int shift
    ) {
        StringBuilder command = new StringBuilder("127 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectGetValueByShift(");
            signature.append("name=").append(name);
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * The function moves an object coordinate in the chart. Objects can have from one to three coordinates depending on their types. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. The object coordinates are numbered starting from 0.
     *
     * @param name   Object name.
     * @param point  Coordinate index (0-2).
     * @param time1  New time value.
     * @param price1 New price value.
     * @return The function moves an object coordinate in the chart. Objects can have from one to three coordinates depending on their types. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. The object coordinates are numbered starting from 0.
     */
    public boolean objectMove(
            String name,
            int point,
            java.util.Date time1,
            double price1
    ) {
        StringBuilder command = new StringBuilder("128 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(point).append(ARG_END);
        command.append(ARG_BEGIN).append(timeConvert(time1)).append(ARG_END);
        command.append(ARG_BEGIN).append(price1).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectMove(");
            signature.append("name=").append(name);
            signature.append(", point=").append(point);
            signature.append(", time1=").append(timeConvert(time1));
            signature.append(", price1=").append(price1);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * The function returns the object name by its index in the objects list. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param index Object index in the objects list. Object index must exceed or equal to 0 and be less than ObjectsTotal().
     * @return The function returns the object name by its index in the objects list. To get the detailed error information, one has to call the GetLastError() function.
     */
    public String objectName(
            int index
    ) {
        StringBuilder command = new StringBuilder("129 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(index).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectName(");
            signature.append("index=").append(index);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Removes all objects of the specified type and in the specified sub-window of the chart. The function returns the count of removed objects. To get the detailed error information, one has to call the GetLastError() function. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. If the window index is missing or it has the value of -1, the objects will be removed from the entire chart. If the type value equals to -1 or this parameter is missing, all objects will be removed from the specified sub-window.
     *
     * @param window Index of the window in which the objects will be deleted. Must exceed or equal to -1 (EMPTY, the default value) and be less than WindowsTotal().
     * @param type   An object type to be deleted. It can be any of the Object type enumeration values or EMPTY constant to delete all objects with any types.
     * @return Removes all objects of the specified type and in the specified sub-window of the chart. The function returns the count of removed objects. To get the detailed error information, one has to call the GetLastError() function. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. If the window index is missing or it has the value of -1, the objects will be removed from the entire chart. If the type value equals to -1 or this parameter is missing, all objects will be removed from the specified sub-window.
     */
    public int objectsDeleteAll(
            int window,
            int type
    ) {
        StringBuilder command = new StringBuilder("130 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(window).append(ARG_END);
        command.append(ARG_BEGIN).append(type).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("objectsDeleteAll(");
            signature.append("window=").append(window);
            signature.append(", type=").append(type);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Calculates the Ichimoku Kinko Hyo and returns its value.
     *
     * @param symbol        Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe     Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param tenkan_sen    Tenkan Sen averaging period.
     * @param kijun_sen     Kijun Sen averaging period.
     * @param senkou_span_b Senkou SpanB averaging period.
     * @param mode          Source of data. It can be one of the Ichimoku Kinko Hyo mode enumeration.
     * @param shift         Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Ichimoku Kinko Hyo and returns its value.
     * @throws com.jfx.ErrUnknownSymbol
     */
    public double iIchimoku(
            String symbol,
            Timeframe timeframe,
            int tenkan_sen,
            int kijun_sen,
            int senkou_span_b,
            IchimokuSource mode,
            int shift
    ) throws ErrUnknownSymbol {
        StringBuilder command = new StringBuilder("131 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(tenkan_sen).append(ARG_END);
        command.append(ARG_BEGIN).append(kijun_sen).append(ARG_END);
        command.append(ARG_BEGIN).append(senkou_span_b).append(ARG_END);
        command.append(ARG_BEGIN).append(mode.getVal(getMType())).append(ARG_END);
        command.append(ARG_BEGIN).append(shift).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("iIchimoku(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(", tenkan_sen=").append(tenkan_sen);
            signature.append(", kijun_sen=").append(kijun_sen);
            signature.append(", senkou_span_b=").append(senkou_span_b);
            signature.append(", mode=").append(mode.getVal(getMType()));
            signature.append(", shift=").append(shift);
            signature.append(')');
            switch (error) {
                case 4106:
                case 54301:
                    throw new ErrUnknownSymbol(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * The function sets a flag hiding indicators called by the Expert Advisor. After the expert has been tested and the appropriate chart opened, the flagged indicators will not be drawn in the testing chart. Every indicator called will first be flagged with the current hiding flag. It must be noted that only those indicators can be drawn in the testing chart that are directly called from the expert under test.
     *
     * @param shift TRUE, if there is a need to hide indicators, or else FALSE.
     */
    public void hideTestIndicators(
            boolean shift
    ) {
        StringBuilder command = new StringBuilder("132 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(shift ? 1 : 0).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("hideTestIndicators(");
            signature.append("shift=").append(shift ? 1 : 0);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;

        //
        // decompose result
        //


    }


    /**
     * Returns the amount of minutes determining the used period (chart timeframe).
     *
     * @return Returns the amount of minutes determining the used period (chart timeframe).
     */
    public int period(
    ) {
        StringBuilder command = new StringBuilder("133 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("period(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns a text string with the name of the current financial instrument.
     *
     * @return Returns a text string with the name of the current financial instrument.
     */
    public String symbol(
    ) {
        StringBuilder command = new StringBuilder("134 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("symbol(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Function returns the amount of bars visible on the chart.
     *
     * @return Function returns the amount of bars visible on the chart.
     */
    public int windowBarsPerChart(
    ) {
        StringBuilder command = new StringBuilder("135 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowBarsPerChart(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * The function returns the first visible bar number in the current chart window. It must be taken into consideration that price bars are numbered in the reverse order, from the last to the first one. The current bar, the latest in the price array, is indexed as 0. The oldest bar is indexed as Bars-1. If the first visible bar number is 2 or more bars less than the amount of visible bars in the chart, it means that the chart window has not been fully filled out and there is a space to the left.
     *
     * @return The function returns the first visible bar number in the current chart window. It must be taken into consideration that price bars are numbered in the reverse order, from the last to the first one. The current bar, the latest in the price array, is indexed as 0. The oldest bar is indexed as Bars-1. If the first visible bar number is 2 or more bars less than the amount of visible bars in the chart, it means that the chart window has not been fully filled out and there is a space to the left.
     */
    public int windowFirstVisibleBar(
    ) {
        StringBuilder command = new StringBuilder("136 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowFirstVisibleBar(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns name of the executed expert, script, custom indicator, or library, depending on the MQL4 program, from which this function has been called.
     *
     * @return Returns name of the executed expert, script, custom indicator, or library, depending on the MQL4 program, from which this function has been called.
     */
    public String windowExpertName(
    ) {
        StringBuilder command = new StringBuilder("137 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowExpertName(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * If indicator with name was found, the function returns the window index containing this specified indicator, otherwise it returns -1. Note: WindowFind() returns -1 if custom indicator searches itself when init() function works.
     *
     * @param name Indicator short name.
     * @return If indicator with name was found, the function returns the window index containing this specified indicator, otherwise it returns -1. Note: WindowFind() returns -1 if custom indicator searches itself when init() function works.
     */
    public int windowFind(
            String name
    ) {
        StringBuilder command = new StringBuilder("138 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowFind(");
            signature.append("name=").append(name);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns TRUE if the chart subwindow is visible, otherwise returns FALSE. The chart subwindow can be hidden due to the visibility properties of the indicator placed in it.
     *
     * @param index Chart subwindow index.
     * @return Returns TRUE if the chart subwindow is visible, otherwise returns FALSE. The chart subwindow can be hidden due to the visibility properties of the indicator placed in it.
     */
    public boolean windowIsVisible(
            int index
    ) {
        StringBuilder command = new StringBuilder("139 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(index).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowIsVisible(");
            signature.append("index=").append(index);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns maximal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the indicators' subwindows are numbered starting from 1). If the subwindow index has not been specified, the maximal value of the price scale of the main chart window is returned.
     *
     * @param index Chart subwindow index (0 - main chart window).
     * @return Returns maximal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the indicators' subwindows are numbered starting from 1). If the subwindow index has not been specified, the maximal value of the price scale of the main chart window is returned.
     */
    public double windowPriceMax(
            int index
    ) {
        StringBuilder command = new StringBuilder("140 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(index).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowPriceMax(");
            signature.append("index=").append(index);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns minimal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the indicators' subwindows are numbered starting from 1). If the subwindow index has not been specified, the minimal value of the price scale of the main chart window is returned.
     *
     * @param index Chart subwindow index (0 - main chart window).
     * @return Returns minimal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the indicators' subwindows are numbered starting from 1). If the subwindow index has not been specified, the minimal value of the price scale of the main chart window is returned.
     */
    public double windowPriceMin(
            int index
    ) {
        StringBuilder command = new StringBuilder("141 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(index).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowPriceMin(");
            signature.append("index=").append(index);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns window index where expert, custom indicator or script was dropped. This value is valid if the expert, custom indicator or script was dropped by mouse. Note: For custom indicators being initialized (call from the init() function), this index is not defined. The returned index is the number of window (0-chart main menu, subwindows of indicators are numbered starting from 1) where the custom indicator is working. A custom indicator can create its own new subwindow during its work, and the number of this subwindow will differ from that of the window where the indicator was really dropped in.
     *
     * @return Returns window index where expert, custom indicator or script was dropped. This value is valid if the expert, custom indicator or script was dropped by mouse. Note: For custom indicators being initialized (call from the init() function), this index is not defined. The returned index is the number of window (0-chart main menu, subwindows of indicators are numbered starting from 1) where the custom indicator is working. A custom indicator can create its own new subwindow during its work, and the number of this subwindow will differ from that of the window where the indicator was really dropped in.
     */
    public int windowOnDropped(
    ) {
        StringBuilder command = new StringBuilder("142 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowOnDropped(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the value at X axis in pixels for the chart window client area point at which the expert or script was dropped. The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.
     *
     * @return Returns the value at X axis in pixels for the chart window client area point at which the expert or script was dropped. The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.
     */
    public int windowXOnDropped(
    ) {
        StringBuilder command = new StringBuilder("143 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowXOnDropped(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the value at Y axis in pixels for the chart window client area point at which the expert or script was dropped. The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.
     *
     * @return Returns the value at Y axis in pixels for the chart window client area point at which the expert or script was dropped. The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.
     */
    public int windowYOnDropped(
    ) {
        StringBuilder command = new StringBuilder("144 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowYOnDropped(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns the price part of the chart point where expert or script was dropped. This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value is undefined.
     *
     * @return Returns the price part of the chart point where expert or script was dropped. This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value is undefined.
     */
    public double windowPriceOnDropped(
    ) {
        StringBuilder command = new StringBuilder("145 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowPriceOnDropped(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * Returns the time part of the chart point where expert or script was dropped. This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value is undefined.
     *
     * @return Returns the time part of the chart point where expert or script was dropped. This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value is undefined.
     */
    public java.util.Date windowTimeOnDropped(
    ) {
        StringBuilder command = new StringBuilder("146 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowTimeOnDropped(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        java.util.Date res;
        //
        // decompose result
        //
        long time = Long.parseLong(result) * 1000;
        res = new java.util.Date(time + getTZCorrectionOffset(time));
        return res;
    }


    /**
     * Returns count of indicator windows on the chart (including main chart).
     *
     * @return Returns count of indicator windows on the chart (including main chart).
     */
    public int windowsTotal(
    ) {
        StringBuilder command = new StringBuilder("147 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowsTotal(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Redraws the current chart forcedly. It is normally used after the objects properties have been changed.
     */
    public void windowRedraw(
    ) {
        StringBuilder command = new StringBuilder("148 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowRedraw(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;

        //
        // decompose result
        //


    }


    /**
     * Saves current chart screen shot as a GIF file. Returns FALSE if it fails. To get the error code, one has to use the GetLastError() function. The screen shot is saved in the terminal_dir\experts\files (terminal_dir\tester\files in case of testing) directory or its subdirectories.
     *
     * @param filename   Screen shot file name.
     * @param sizeX      Screen shot width in pixels.
     * @param sizeY      Screen shot height in pixels.
     * @param startBar   Index of the first visible bar in the screen shot. If 0 value is set, the current first visible bar will be shot. If no value or negative value has been set, the end-of-chart screen shot will be produced, indent being taken into consideration.
     * @param chartScale Horizontal chart scale for screen shot. Can be in the range from 0 to 5. If no value or negative value has been set, the current chart scale will be used.
     * @param chartMode  Chart displaying mode. It can take the following values: CHART_BAR (0 is a sequence of bars), CHART_CANDLE (1 is a sequence of candlesticks), CHART_LINE (2 is a close prices line). If no value or negative value has been set, the chart will be shown in its current mode.
     * @return Saves current chart screen shot as a GIF file. Returns FALSE if it fails. To get the error code, one has to use the GetLastError() function. The screen shot is saved in the terminal_dir\experts\files (terminal_dir\tester\files in case of testing) directory or its subdirectories.
     */
    public boolean windowScreenShot(
            String filename,
            int sizeX,
            int sizeY,
            int startBar,
            int chartScale,
            int chartMode
    ) {
        StringBuilder command = new StringBuilder("149 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(filename).append(ARG_END);
        command.append(ARG_BEGIN).append(sizeX).append(ARG_END);
        command.append(ARG_BEGIN).append(sizeY).append(ARG_END);
        command.append(ARG_BEGIN).append(startBar).append(ARG_END);
        command.append(ARG_BEGIN).append(chartScale).append(ARG_END);
        command.append(ARG_BEGIN).append(chartMode).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowScreenShot(");
            signature.append("filename=").append(filename);
            signature.append(", sizeX=").append(sizeX);
            signature.append(", sizeY=").append(sizeY);
            signature.append(", startBar=").append(startBar);
            signature.append(", chartScale=").append(chartScale);
            signature.append(", chartMode=").append(chartMode);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns the system window handler containing the given chart. If the chart of symbol and timeframe has not been opened by the moment of function calling, 0 will be returned.
     *
     * @param symbol    symbol name.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @return Returns the system window handler containing the given chart. If the chart of symbol and timeframe has not been opened by the moment of function calling, 0 will be returned.
     */
    public int windowHandle(
            String symbol,
            Timeframe timeframe
    ) {
        StringBuilder command = new StringBuilder("150 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(timeframe.getVal(getMType())).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("windowHandle(");
            signature.append("symbol=").append(symbol);
            signature.append(", timeframe=").append(timeframe.getVal(getMType()));
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * Returns TRUE if the global variable exists, otherwise, returns FALSE.
     *
     * @param name Global variable name.
     * @return Returns TRUE if the global variable exists, otherwise, returns FALSE.
     */
    public boolean globalVariableCheck(
            String name
    ) {
        StringBuilder command = new StringBuilder("151 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("globalVariableCheck(");
            signature.append("name=").append(name);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Deletes the global variable. If the function succeeds, the returned value will be TRUE, otherwise, it will be FALSE.
     *
     * @param name Global variable name.
     * @return Deletes the global variable. If the function succeeds, the returned value will be TRUE, otherwise, it will be FALSE.
     */
    public boolean globalVariableDel(
            String name
    ) {
        StringBuilder command = new StringBuilder("152 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("globalVariableDel(");
            signature.append("name=").append(name);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result) : (Integer.parseInt(result) != 0);
        return res;
    }


    /**
     * Returns the value of an existing global variable or 0 if an error occurs.
     *
     * @param name Global variable name.
     * @return Returns the value of an existing global variable or 0 if an error occurs.
     */
    public double globalVariableGet(
            String name
    ) {
        StringBuilder command = new StringBuilder("153 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("globalVariableGet(");
            signature.append("name=").append(name);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        double res;
        //
        // decompose result
        //
        res = Double.parseDouble(result);
        return res;
    }


    /**
     * The function returns the name of a global variable by its index in the list of global variables.
     *
     * @param index Index in the list of global variables. It must exceed or be equal to 0 and be less than GlobalVariablesTotal().
     * @return The function returns the name of a global variable by its index in the list of global variables.
     */
    public String globalVariableName(
            int index
    ) {
        StringBuilder command = new StringBuilder("154 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(index).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("globalVariableName(");
            signature.append("index=").append(index);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        String res;
        //
        // decompose result
        //
        res = result;
        return res;
    }


    /**
     * Sets a new value of the global variable. If it does not exist, the system creates a new gloabl variable. If the function succeeds, the returned value will be the last access time. Otherwise, the returned value will be 0.
     *
     * @param name  Global variable name.
     * @param value The new numeric value.
     * @return Sets a new value of the global variable. If it does not exist, the system creates a new gloabl variable. If the function succeeds, the returned value will be the last access time. Otherwise, the returned value will be 0.
     */
    public java.util.Date globalVariableSet(
            String name,
            double value
    ) {
        StringBuilder command = new StringBuilder("155 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(value).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("globalVariableSet(");
            signature.append("name=").append(name);
            signature.append(", value=").append(value);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        java.util.Date res;
        //
        // decompose result
        //
        long time = Long.parseLong(result) * 1000;
        res = new java.util.Date(time + getTZCorrectionOffset(time));
        return res;
    }


    /**
     * Sets the new value of the existing global variable if the current value equals to the third parameter check_value. When successfully executed, the function returns TRUE, otherwise, it returns FALSE.
     *
     * @param name        Global variable name.
     * @param value       The new numeric value.
     * @param check_value Value to be compared to the current global variable value.
     * @return Sets the new value of the existing global variable if the current value equals to the third parameter check_value. When successfully executed, the function returns TRUE, otherwise, it returns FALSE.
     * @throws com.jfx.ErrGlobalVariableNotFound global variable not found.
     */
    public boolean globalVariableSetOnCondition(
            String name,
            double value,
            double check_value
    ) throws ErrGlobalVariableNotFound {
        StringBuilder command = new StringBuilder("156 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(name)).append(ARG_END);
        command.append(ARG_BEGIN).append(value).append(ARG_END);
        command.append(ARG_BEGIN).append(check_value).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("globalVariableSetOnCondition(");
            signature.append("name=").append(name);
            signature.append(", value=").append(value);
            signature.append(", check_value=").append(check_value);
            signature.append(')');
            switch (error) {
                case 4058:
                    throw new ErrGlobalVariableNotFound(signature.toString());
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        boolean res;
        //
        // decompose result
        //
        res = result.length() > 1 ? Boolean.valueOf(result).booleanValue() : (Integer.parseInt(result) == 0 ? false : true);
        return res;
    }


    /**
     * Deletes global variables. If the name prefix is not specified, all global variables will be deleted. Otherwise, only those variables will be deleted, the names of which begin with the specified prefix. The function returns the count of deleted variables.
     *
     * @param prefix Name prefix of the global variables to be deleted.
     * @return Deletes global variables. If the name prefix is not specified, all global variables will be deleted. Otherwise, only those variables will be deleted, the names of which begin with the specified prefix. The function returns the count of deleted variables.
     */
    public int globalVariablesDeleteAll(
            String prefix
    ) {
        StringBuilder command = new StringBuilder("157 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(replaceSpecialChars(prefix)).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("globalVariablesDeleteAll(");
            signature.append("prefix=").append(prefix);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }


    /**
     * The function returns the total count of global variables.
     *
     * @return The function returns the total count of global variables.
     */
    public int globalVariablesTotal(
    ) {
        StringBuilder command = new StringBuilder("158 ");
        //
        // compose command
        //

        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("globalVariablesTotal(");

            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }

    /**
     * Returns the number of available (selected in Market Watch or all) symbols.
     *
     * @param selected Request mode. Can be true or false: true - return number of symbols in MarketWatch
     * @return If the 'selected' parameter is true, the function returns the number of symbols selected in MarketWatch. If the value is false, it returns the total number of all symbols.
     */
    public int symbolsTotal(boolean selected) {
        StringBuilder command = new StringBuilder("159 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(selected ? 1 : 0).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("symbolsTotal(");
            signature.append("selected=").append(selected);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }

    /**
     * Returns the name of a symbol.
     *
     * @param pos      Order number of a symbol.
     * @param selected Request mode. If the value is true, the symbol is taken from the list of symbols selected in MarketWatch. If the value is false, the symbol is taken from the general list.
     * @return Value of string type with the symbol name.
     */
    public String symbolName(int pos, boolean selected) {
        StringBuilder command = new StringBuilder("160 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(pos).append(ARG_END);
        command.append(ARG_BEGIN).append(selected ? 1 : 0).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("symbolName(");
            signature.append("pos=").append(pos);
            signature.append("selected=").append(selected);
            signature.append(')');
            switch (error) {

                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        return result;
    }

    /**
     * Selects a symbol in the Market Watch window or removes a symbol from the window.
     *
     * @param symbol   Symbol name.
     * @param selected Switch. If the value is false, a symbol should be removed from MarketWatch, otherwise a symbol should be selected in this window. A symbol can't be removed if the symbol chart is open, or there are open orders for this symbol.
     * @return In case of failure returns false.
     */
    public boolean symbolSelect(String symbol, boolean selected) {
        StringBuilder command = new StringBuilder("161 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        command.append(ARG_BEGIN).append(selected ? 1 : 0).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("symbolSelect(");
            signature.append("symbol=").append(symbol);
            signature.append(", select=").append(selected);
            signature.append(')');
            switch (error) {
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        return result.length() > 1 ? Boolean.valueOf(result).booleanValue() : (Integer.parseInt(result) == 0 ? false : true);
    }

    /**
     * Passes stop request to the terminal application.
     *
     * @param exitCode application exit code.
     * @return In case of failure returns false.
     */
    public boolean terminalClose(int exitCode) {
        StringBuilder command = new StringBuilder("162 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(exitCode).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("terminalClose(");
            signature.append("exitCode=").append(exitCode);
            signature.append(')');
            switch (error) {
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        return result.length() > 1 ? Boolean.valueOf(result).booleanValue() : (Integer.parseInt(result) == 0 ? false : true);
    }

    /**
     * Obtains current market information for the symbol
     *
     * @param symbol symbol to obtain market information for
     * @return current market information for the specified symbol.
     */
    public SymbolInfo symbolInfo(String symbol) {
        StringBuilder command = new StringBuilder("163 ");
        //
        // compose command
        //
        command.append(ARG_BEGIN).append(symbol).append(ARG_END);
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("symbolInfo(");
            signature.append("symbol=").append(symbol);
            signature.append(')');
            switch (error) {
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        return new SymbolInfo(this, result);
    }

    /**
     * Obtains information about connected account, e.g. name, server currency, company, balance, credit, profit, equity, margin, freemargin, trade_mode, leverage, margin_so_mode
     *
     * @return connected account information
     */
    public AccountInfo accountInfo() {
        StringBuilder command = new StringBuilder("164 ");
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("accountInfo(");
            signature.append(')');
            switch (error) {
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        return new AccountInfo(this, result);
    }

    /**
     * Offset in seconds between current server time and GMT time, so {server time} + {offset} = {gmt time}
     *
     * @return seconds between current server time and GMT time.
     */
    public int serverTimeGMTOffset() {
        StringBuilder command = new StringBuilder("165 ");
        //
        String result = getChannel().sendCommandGetResult(command);
        int ix = result.lastIndexOf('@');
        int error = ix >= 0 ? Integer.parseInt(result.substring(ix + 1)) : 0;
        StringBuilder signature = null;
        if (error != 0) {
            signature = new StringBuilder("serverTimeGMTOffset(");
            signature.append(')');
            switch (error) {
                default:
                    if (error != 0) {
                        throw new MT4RuntimeException(error, signature + " -> " + UNEXPECTED_ERROR_OCCURRED + getUnexpectedErrorDesc(error));
                    }
            }
        }
        //
        result = ix >= 0 ? result.substring(0, ix) : result;
        int res;
        //
        // decompose result
        //
        res = Integer.parseInt(result);
        return res;
    }

    /**
     * Converts server time to default time zone.
     *
     * @param serverTime time at mt4 server, e.g. timeCurrent()
     * @return Date representing local time for a specified server time.
     */
    public Date toLocalTime(Date serverTime) {
        return new Date((fromDate(serverTime) + cachedGMTOffset()) * 1000L);
    }

    long lastCacheTime = 0;
    int gmtOffset = 0;

    private int cachedGMTOffset() {
        long now = System.currentTimeMillis();
        if (now - lastCacheTime > 3600000L) {
            gmtOffset = serverTimeGMTOffset();
            lastCacheTime = now;
        }
        return gmtOffset;
    }
}
