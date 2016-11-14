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

package com.jfx.strategy;

import com.jfx.*;
import com.jfx.io.Log4JUtil;
import com.jfx.md5.MD5;
import com.jfx.net.JFXServer;
import com.jfx.net.TSClient;
import com.jfx.net.tsapi.Nj4XChartParams;
import com.jfx.net.tsapi.Nj4XMT4Account;
import com.jfx.net.tsapi.Nj4XParams;
import com.jfx.net.tsapi.Nj4XTSInfo;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.LockSupport;

/**
 * User: roman
 * Date: 22/5/2009
 * Time: 15:39:32
 */
public class Strategy extends MT4 {
    public static final int MT4_CONNECTION_WAIT_SECONDS = 9;
    public static final String SPCH = "SPCH";
    public static final String MINIMUM_TS_VERSION = "2.4.0";
    private static final Logger LOGGER = Logger.getLogger(Strategy.class);
    private final static HashMap<String, Strategy> registeredStrategies = new HashMap<String, Strategy>();
    private final ArrayList<Terminal> terminals = new ArrayList<Terminal>();
    private final Stack<Terminal> orderTerminals = new Stack<Terminal>();
    private final Object disconnectSynch;
    public HashMap<String, ArrayList<String>> groupsMap;
    public ArrayList<String> groups;
    public ArrayList<String> symbols;
    public ArrayList<Instrument> instruments;
    //
    protected String symbol;
    protected int period;
    protected TSClient tsClient;
    //
    boolean isReconnect;
    boolean asynchOrderOperations;
    long timeZoneCorrectionOffset = 0;
    boolean isCorrectionOffsetCalculated;
    //
    private boolean isAlive;
    private String termServerHost;
    private String termServerHostName;
    private String termServerVersion;
    private int termServerPort;
    private com.jfx.Broker mt4Server;
    private String mt4User;
    private String mt4Password;
    private String id;
    private boolean isLimitedFunctionality;
    private Strategy.Chart positionListener;
    private Strategy.Chart timerListener;
    private Strategy.Chart ordersWorker;
    private boolean isOrderTerminalsPresent = false;
    private HistoryPeriod historyPeriod;
    private String jfxServerHost;
    private int jfxServerPort;
    private TickListenerChart bulkTickListenerChart;
    private Terminal terminal;
    private ArrayList<Chart> charts;
    private boolean isTickModeStrategy;

    public Strategy() {
        disconnectSynch = new Object();
        isReconnect = true;
    }

    private static int signum(long n) {
        return n > 0 ? 1 : (n < 0 ? -1 : 0);
    }

    private static String registerStrategyGetRegID(Strategy s) {
        String id;
        if (s instanceof Chart) {
            Chart c = ((Chart) s);
            synchronized (registeredStrategies) {
                registeredStrategies.put((id = c.getStrategyId()) + '|' + c.getChartId(), c);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("REGISTER wID CHART: " + (c.getStrategyId() + '|' + c.getChartId()));
            }
        } else {
            synchronized (registeredStrategies) {
                //System.out.println("MD5 of [" + s.termServerHostName + s.getTermServerPort() + s.getSymbol() + s.mt4User + s.mt4Server.val + s.mt4Password + s.getTenant() + ']');
                id = calcId(s);
                //System.out.println("... " + id);
                if (registeredStrategies.get(id + '1') != null || registeredStrategies.get(id + '2') != null) {
                    throw new RuntimeException(s.mt4User + " is already connecting to " + s.mt4Server.getVal());
                }
                registeredStrategies.put(id + '1', s);
                if (LOGGER.isDebugEnabled()) LOGGER.debug("REGISTER wID: " + (id + '1'));
                if (s.asynchOrderOperations) {
                    registeredStrategies.put(id + '2', s);
                    if (LOGGER.isDebugEnabled()) LOGGER.debug("REGISTER wID: " + (id + '2'));
                } else {
                    registeredStrategies.remove(id + '2');
                }
                ArrayList<Chart> charts = s.getCharts();
                if (charts != null && charts.size() > 0) {
                    for (Chart c : charts) {
                        //                    registeredStrategies.put(id + '|' + c.getSymbol(), c);
                        registeredStrategies.put(id + '|' + c.getChartId(), c);
                        if (LOGGER.isDebugEnabled()) LOGGER.debug("REGISTER wID: " + (id + '|' + c.getChartId()));
                    }
                }
            }
        }
        return id;
    }

    private static String calcId(Strategy s) {
        return MD5.MD5(s.termServerHostName + s.getTermServerPort() + s.getSymbol() + s.mt4User + s.mt4Server.getVal() + s.mt4Password + s.getTenants());
    }

    private static void registerStrategy(Strategy s) {
        if (s instanceof Chart) {
            Chart c = ((Chart) s);
            synchronized (registeredStrategies) {
                registeredStrategies.put(c.getStrategyId() + '|' + c.getChartId(), c);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("REGISTER CHART: " + (c.getStrategyId() + '|' + c.getChartId()));
            }
        } else {
            synchronized (registeredStrategies) {
                Strategy s1 = registeredStrategies.get(s.id + '1');
                Strategy s2 = registeredStrategies.get(s.id + '2');
                if (s1 != null && s1 != s || s2 != null && s2 != s) {
                    throw new RuntimeException(s.id + ":" + s.mt4User + " is already connecting to " + s.mt4Server.getVal() + ": \n" + s + "\n but " + s1 + " | " + s2);
                }
                registeredStrategies.put(s.id + '1', s);
                if (LOGGER.isDebugEnabled()) LOGGER.debug("REGISTER: " + (s.id + '1'));
                if (s.asynchOrderOperations) {
                    registeredStrategies.put(s.id + '2', s);
                    if (LOGGER.isDebugEnabled()) LOGGER.debug("REGISTER: " + (s.id + '2'));
                } else {
                    registeredStrategies.remove(s.id + '2');
                }
                //
                ArrayList<Chart> charts = s.getCharts();
                if (charts != null && charts.size() > 0) {
                    for (Chart c : charts) {
                        registeredStrategies.put(s.id + '|' + c.getChartId(), c);
                        if (LOGGER.isDebugEnabled()) LOGGER.debug("REGISTER: " + (s.id + '|' + c.getChartId()));
                    }
                }
            }
        }
    }

    public static Strategy getStrategy(String id) {
        synchronized (registeredStrategies) {
            return registeredStrategies.get(id);
        }
    }

    public static Strategy removeStrategy(String id) {
        synchronized (registeredStrategies) {
            return registeredStrategies.remove(id);
        }
    }

    private void dumpState() {
        LOGGER.debug("groupsMap=" + groupsMap
                + ", groups=" + groups
                + ", symbols=" + symbols
                + ", instruments=" + instruments
                + ", isAlive=" + isAlive
                + ", termServerHost=" + termServerHost
                + ", termServerHostName=" + termServerHostName
                + ", termServerVersion=" + termServerVersion
                + ", termServerPort=" + termServerPort
                + ", mt4Server=" + mt4Server
                + ", mt4User=" + mt4User
                + ", mt4Password=" + mt4Password
                + ", id=" + id
                + ", isLimitedFunctionality=" + isLimitedFunctionality
                + ", positionListener=" + positionListener
                + ", timerListener=" + timerListener
                + ", ordersWorker=" + ordersWorker
                + ", terminals=" + terminals
                + ", orderTerminals=" + orderTerminals
                + ", isOrderTerminalsPresent=" + isOrderTerminalsPresent
                + ", historyPeriod=" + historyPeriod
                + ", jfxServerHost=" + jfxServerHost
                + ", bulkTickListenerChart=" + bulkTickListenerChart
                + ", terminal=" + terminal
                + ", charts=" + charts
                + ", disconnectSynch=" + disconnectSynch
                + ", symbol=" + symbol
                + ", period=" + period
                + ", tsClient=" + tsClient
                + ", isReconnect=" + isReconnect
                + ", asynchOrderOperations=" + asynchOrderOperations
                + ", timeZoneCorrectionOffset=" + timeZoneCorrectionOffset
                + ", isCorrectionOffsetCalculated=" + isCorrectionOffsetCalculated
        );
    }

    /**
     * Returns NJ4X API version registry, i.e. local API version, Terminal Server version, communication DLL version
     * and MQL Expert Advisor version.
     *
     * @return NJ4X API version.
     */
    public Version getVersion() {
        BasicStrategyRunner c = (BasicStrategyRunner) getChannel();
        return new Version(termServerVersion, c.dllVersion, c.mqlVersion);
    }

    public boolean isLimitedFunctionality() {
        return isLimitedFunctionality;
    }

    void setLimitedFunctionality(boolean limitedFunctionality) {
        isLimitedFunctionality = limitedFunctionality;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getPeriod() {
        return period;
    }

    public Strategy setPeriod(Timeframe period) {
        this.period = period.val;
        return this;
    }

    public Timeframe getTimeframe() {
        return Timeframe.getTimeframe(getPeriod());
    }

    public Timeframe getPrevTimeframe(Timeframe tf) {
        switch (tf.val) {
            case 1:
                return tf;
            case 5:
                return Timeframe.getTimeframe(1);
            case 15:
                return Timeframe.getTimeframe(5);
            case 30:
                return Timeframe.getTimeframe(15);
            case 60:
                return Timeframe.getTimeframe(30);
            case 240:
                return Timeframe.getTimeframe(60);
            case 1440:
                return Timeframe.getTimeframe(240);
            case 10080:
                return Timeframe.getTimeframe(1440);
            case 43200:
                return Timeframe.getTimeframe(10080);
            default:
                return null;
        }
    }

    public Timeframe getNextTimeframe(Timeframe tf) {
        switch (tf.val) {
            case 1:
                return Timeframe.getTimeframe(5);
            case 5:
                return Timeframe.getTimeframe(15);
            case 15:
                return Timeframe.getTimeframe(30);
            case 30:
                return Timeframe.getTimeframe(60);
            case 60:
                return Timeframe.getTimeframe(240);
            case 240:
                return Timeframe.getTimeframe(1440);
            case 1440:
                return Timeframe.getTimeframe(10080);
            case 10080:
                return Timeframe.getTimeframe(43200);
            case 43200:
                return tf;
            default:
                return null;
        }
    }

    /*
        public boolean isTradeAllowed() throws ErrUnknownSymbol {
            return 1 == marketInfo(getSymbol(), MarketInfo.MODE_TRADEALLOWED);
        }

    */
    public int getSlippage() {
        return 2;
    }

    /**
     * Provides list of all available on MT4 Terminal instruments.
     * Valid only when connect() and init() methods are completed.
     *
     * @return List of all available on MT4 Terminal symbols.
     */
    public synchronized ArrayList<String> getSymbols() {
        initSymbols();
        return symbols;
    }
/*
    protected long getTZCorrectionOffset(long time) {
        if (!isCorrectionOffsetCalculated) {
            try {
                long serverTime = ((long) marketInfo(symbol, MarketInfo.MODE_TIME)) * 1000;
                long serverTZAndCurrentTZDiffMinutes = (serverTime - System.currentTimeMillis()) / 1000 / 60;
                if (-11 * 60 <= serverTZAndCurrentTZDiffMinutes && serverTZAndCurrentTZDiffMinutes <= 14 * 60) {
                    long l1 = Math.abs(serverTZAndCurrentTZDiffMinutes) % 15;
                    timeZoneCorrectionOffset = serverTZAndCurrentTZDiffMinutes / 15 * 15 + (l1 > 10 ? signum(serverTZAndCurrentTZDiffMinutes) * 15 : 0);
                    timeZoneCorrectionOffset = serverTZAndCurrentTZDiffMinutes * 60 * 1000L;
                }
            } catch (ErrUnknownSymbol errUnknownSymbol) {
                timeZoneCorrectionOffset = 0;
            } finally {
                isCorrectionOffsetCalculated = true;
            }
        }
        //
        return -TimeZone.getDefault().getOffset(time) + timeZoneCorrectionOffset;
    }
*/

    /**
     * Provides list of all available on MT4 Terminal instruments.
     * Valid only when connect() and init() methods are completed.
     *
     * @return List of all available on MT4 Terminal instruments.
     */
    public synchronized ArrayList<Instrument> getInstruments() {
        initSymbols();
        //
        return instruments;
    }

/*
    private void addSymbol(String sg) {
        String[] sym_grp = sg.split(",");
        String sym = sym_grp[0];
        String grp = sym_grp[1];
        ArrayList<String> gSymbols = groupsMap.get(grp);
        if (gSymbols == null) {
            groupsMap.put(grp, gSymbols = new ArrayList<String>());
        }
        gSymbols.add(sym);
        symbols.add(sym);
    }
*/

    public void init(String symbol, int period, StrategyRunner strategyRunner) throws ErrUnknownSymbol, IOException {
    }

    private void initSymbols() {
        int symbolsTotal = symbolsTotal(false);
        if (instruments != null && instruments.size() == symbolsTotal) {
            return;
        }

        groupsMap = new HashMap<String, ArrayList<String>>();
        groups = new ArrayList<String>();
        symbols = new ArrayList<String>();
        instruments = new ArrayList<Instrument>();
        //
        for (int i = 0; i < symbolsTotal; i++) {
            String sym = symbolName(i, false);
            String grp = "all";

            ArrayList<String> gSymbols = groupsMap.get(grp);
            if (gSymbols == null) {
                groupsMap.put(grp, gSymbols = new ArrayList<String>());
            }
            gSymbols.add(sym);
            symbols.add(sym);
            boolean six = sym.length() >= 6 && !sym.contains("#");
            instruments.add(new Instrument(sym, six ? sym.substring(0, 3) : "", six ? sym.substring(3, 6) : ""));
        }
    }

    synchronized void init(String symbol, int period) throws ErrUnknownSymbol, IOException {
        isAlive = true;
        //
        groupsMap = new HashMap<String, ArrayList<String>>();
        groups = new ArrayList<String>();
        symbols = new ArrayList<String>();
        instruments = new ArrayList<Instrument>();
        //
//        if (cli != null && !(this instanceof Chart) && !(mt4Server instanceof com.jfx.mt5.Broker)) {
//            cli.close();
//            cli = null;
//        }
        //
        if (tsClient != null && !(this instanceof Chart) && !(mt4Server instanceof com.jfx.mt5.Broker)) {
            tsClient.close();
            tsClient = null;
        }
        //
        this.symbol = symbol;
        this.period = period;
    }

    private String getBindHost() {
        return jfxServerHost == null || jfxServerHost.length() == 0 ? JFXServer.getInstance().getBindHost() : jfxServerHost;
    }

    private int getBindPort() {
        return jfxServerPort == 0 ? JFXServer.getInstance().getBindPort() : jfxServerPort;
    }

    public void setTimeZoneCorrectionOffset(long timeZoneCorrectionOffset) {
        this.timeZoneCorrectionOffset = timeZoneCorrectionOffset;
        isCorrectionOffsetCalculated = true;
    }

    protected long getTZCorrectionOffset(long time) {
        return super.getTZCorrectionOffset(time) + timeZoneCorrectionOffset;
    }

    public boolean isReconnect() {
        return isReconnect;
    }

    public void setReconnect(boolean reconnect) {
        isReconnect = reconnect;
    }

    public void deinit() {
        isAlive = false;
        if (isReconnect && terminalServerOk() && !(this instanceof Chart)) {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    setStrategyRunner(null);
                    setOrdersProcessingChannel(null);
                    connect(getTermServerHost(), getTermServerPort(), getMt4Server(), getMt4User(), getMt4Password(), isTickModeStrategy ? this.symbol : null, this.asynchOrderOperations);
                    break;
                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        System.err.println("Reconnection process broken.");
                        break;
                    }
                }
            }
        }
    }

    private boolean terminalServerOk() {
        return getTermServerHost() != null &&
                getTermServerPort() != 0 &&
                getMt4User() != null &&
                getMt4Password() != null;
    }

    public long coordinationIntervalMillis() {
        return 10000;
    }

    public void coordinate() {

    }

    protected String getTenants() {
        return getTenant() + '\u0002' + getParentTenant();
    }

    protected String getParentTenant() {
        return getTenant();
    }

    protected String getTenant() {
        if (charts == null || charts.size() == 0/* || terminal != null*/) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (Chart c : charts) {
                if (sb.length() > 0)
                    sb.append('_');
                if (c.getChartId().startsWith(SPCH)) {
                    sb.append(c.getChartId().substring(SPCH.length()));
                } else {
                    sb.append(c.getChartId());
                }
            }
            if (sb.length() < 32) {
                return sb.toString();
            } else {
                return /*"" + charts.size() + " charts " + */MD5.MD5(sb.toString());
            }
        }
    }

    /**
     * Disconnects from MT4 Client Terminal.
     *
     * @throws IOException in case of Terminal Server is unreachable
     */
    public void disconnect() throws IOException {
        close();
    }

    /**
     * Kills MT4 Client Terminal with timeout (JFX_TERM_IDLE_TMOUT_SECONDS env. variable)
     *
     * @throws IOException in case of Terminal Server is unreachable
     */
    public void close() throws IOException {
        close(false);
    }

    private void tryCloseTerminalGracefully(boolean immediately) {
        if (getStrategyRunner() != null) {
            ((BasicStrategyRunner) getStrategyRunner()).isPendingClose = true;
        }
        if (getOrdersProcessingChannel() != null) {
            ((BasicStrategyRunner) getOrdersProcessingChannel()).isPendingClose = true;
        }
        if (charts != null && charts.size() > 0) {
            for (Chart c : charts) {
                c.isReconnect = false;
                StrategyRunner strategyRunner = c.getStrategyRunner();
                if (strategyRunner != null) {
                    ((BasicStrategyRunner) strategyRunner).isPendingClose = true;
                }
            }
        }
//        if (!immediately) {
//            try {
//                terminalClose(0); // causing hangs on blocked connections
//            } catch (Exception ignore) {
//            }
//        }
    }

    /**
     * Kills MT4 Client Terminal
     *
     * @param immediately pass true if immediate mt4 terminal process termination is required, otherwise JFX_TERM_IDLE_TMOUT_SECONDS env. variable will be used by MT4 terminal to exit at idle timeout.
     * @throws IOException in case of Terminal Server is unreachable
     */
    public void close(boolean immediately) throws IOException {
        synchronized (disconnectSynch) {
            if (getStrategyRunner() == null) {
                if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                    LOGGER.info("Close method called: " + id + ", skipped - getStrategyRunner() == null");
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error("DUMP", new RuntimeException("DUMP"));
                    }
                }
                return;
            }
            if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                LOGGER.info("Close method called: " + id);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("DUMP", new RuntimeException("DUMP"));
                }
            }
//        isAlive = false;
            isReconnect = false;
            //
            tryCloseTerminalGracefully(immediately);
            //
            if (getStrategyRunner() != null) {
                getStrategyRunner().close();
            }
            if (getOrdersProcessingChannel() != null) {
                getOrdersProcessingChannel().close();
            }
            stopCharts();
            //
            try {
                if (termServerHost != null) {
                    tsClient = new TSClient(termServerHost, termServerPort + 1);
                    tsClient.connect(id == null ? calcId(this) : id);
                    Nj4XMT4Account mt4Account = TSClient.getNj4xMT4Account(mt4Server, mt4User, mt4Password);
                    if (immediately) {
                        tsClient.killMT4Terminal(mt4Account, getNj4xParams());
                    } else {
                        tsClient.disconnectMT4Terminal(mt4Account, getNj4xParams());
                    }
                    //
                    for (Terminal t : terminals) {
                        Strategy mt4Connection = (Strategy) t.getMt4Connection();
                        //
                        String id1;
                        if (mt4Connection == null) {
                            id1 = "";
                        } else {
                            //                    mt4Connection.isAlive = false;
                            mt4Connection.isReconnect = false;
                            mt4Connection.tryCloseTerminalGracefully(immediately);
                            mt4Connection.getStrategyRunner().close();
                            mt4Connection.stopCharts();
                            id1 = mt4Connection.id;
                            //
                            if (immediately) {
                                tsClient.killMT4Terminal(mt4Account, mt4Connection.getNj4xParams());
                            } else {
                                tsClient.disconnectMT4Terminal(mt4Account, mt4Connection.getNj4xParams());
                            }
                        }
                    }
                    synchronized (terminals) {
                        terminals.clear();
                    }
                }
            } finally {
                if (tsClient != null) {
                    tsClient.close();
                }
            }
        }
    }

    private void stopCharts() {
        if (charts != null && charts.size() > 0) {
            for (Chart c : charts) {
                c.isReconnect = false;
                StrategyRunner strategyRunner = c.getStrategyRunner();
                if (strategyRunner != null) {
                    strategyRunner.close();
                }
            }
        }
    }

    public void connect(String termServerHost, int termServerPort, com.jfx.Broker mt4Server, String mt4User, String mt4Password) throws IOException {
        connect(termServerHost, termServerPort, mt4Server, mt4User, mt4Password, null, false);
    }

    public void connect(String termServerHost, int termServerPort, com.jfx.Broker mt4Server, String mt4User, String mt4Password, String symbol) throws IOException {
        connect(termServerHost, termServerPort, mt4Server, mt4User, mt4Password, symbol, false);
    }

    public void connect(String termServerHost, int termServerPort, com.jfx.Broker mt4Server, String mt4User, String mt4Password, boolean asynchOrderOperations) throws IOException {
        connect(termServerHost, termServerPort, mt4Server, mt4User, mt4Password, null, asynchOrderOperations);
    }

    protected void addChart(Chart c) {
        charts = charts == null ? new ArrayList<Chart>() : charts;
        charts.add(c);
    }

    private ArrayList<Chart> getCharts() {
        return charts;
    }

    /**
     * Adds TickListener to the strategy. Must be called before connect(...) method.
     *
     * @param symbol       symbol to listen for the ticks of
     * @param tickListener ticks listener
     * @return this strategy object
     */
    public synchronized Strategy addTickListener(String symbol, TickListener tickListener) {
        if (terminal == null) {
            terminal = new Terminal(TerminalType.TICK_WORKER);
        }
        terminal.addTickListener(symbol, tickListener);
        return this;
    }

    /**
     * Sets BulkTickListener for the strategy. Must be called before connect(...) method.
     *
     * @param bulkTickListener multiple symbols ticks' listener
     * @return this strategy object
     */
    public synchronized Strategy setBulkTickListener(BulkTickListener bulkTickListener) {
        if (bulkTickListenerChart != null) {
            charts.remove(bulkTickListenerChart);
        }
        bulkTickListenerChart = new TickListenerChart(SPCH + "_T_LSNR", bulkTickListener);
        addChart(bulkTickListenerChart);
        return this;
    }

    /**
     * Installs user defined PositionListener for the strategy. Must be called before connect() method.
     *
     * @param lsnr user defined position listener
     * @return Strategy object itself
     */
    public Strategy setPositionListener(final PositionListener lsnr) {
        return setPositionListener(lsnr, 50, 500);
    }

    /**
     * Installs user defined PositionListener for the strategy. Must be called before connect() method.
     *
     * @param lsnr           user defined position listener
     * @param minDelayMillis minimum new orders check interval millis
     * @param maxDelayMillis maximum new orders check interval millis
     * @return Strategy object itself
     */
    public Strategy setPositionListener(final PositionListener lsnr, final int minDelayMillis, final int maxDelayMillis) {
        if (positionListener != null) {
            if (isAlive()) {
                throw new RuntimeException("PositionListener is already set");
            } else {
                charts.remove(positionListener);
            }
        } else {
            if (isAlive()) {
                throw new RuntimeException("PositionListener must be set before connection.");
            }
        }
        positionListener = new Chart(SPCH + "_P_LSNR") {
            private PositionInfo positionInfo;

            @Override
            public long coordinationIntervalMillis() {
                return 1;
            }

            @Override
            public synchronized void init(String symbol, int period, StrategyRunner strategyRunner) throws ErrUnknownSymbol, IOException {
                super.init(symbol, period, strategyRunner);
                positionInfo = newPosition(null);
                lsnr.onInit(positionInfo);
            }

            @Override
            public void coordinate() {
                while (isAlive) {
                    PositionInfo newPositionInfo = newPosition(positionInfo);
                    lsnr.onChange(positionInfo, positionInfo.mergePosition(newPositionInfo));
                }
            }

            @Override
            public String getChartParams() {
                return getChartId() + ':' + minDelayMillis + ':' + maxDelayMillis;
            }

            @Override
            public List<String> getParams() {
                ArrayList<String> params = new ArrayList<String>();
                params.add(String.valueOf(minDelayMillis));
                params.add(String.valueOf(maxDelayMillis));
                return params;
            }
        };
        addChart(positionListener);
        return this;
    }

    /**
     * Installs user defined TimerListener for the strategy. Must be called before connect() method.
     *
     * @param lsnr           user defined timer listener
     * @param intervalMillis minimum new orders check interval millis
     * @return Strategy object itself
     */
    public Strategy setTimerListener(final TimerListener lsnr, final int intervalMillis) {
        if (timerListener != null) {
            if (isAlive()) {
                throw new RuntimeException("TimerListener is already set");
            } else {
                charts.remove(timerListener);
            }
        } else {
            if (isAlive()) {
                throw new RuntimeException("TimerListener must be set before connection.");
            }
        }
        timerListener = new Chart(SPCH + "_T_LSNR") {
            @Override
            public long coordinationIntervalMillis() {
                return intervalMillis;
            }

            @Override
            public void coordinate() {
                lsnr.onTimer(this);
            }
        };
        addChart(timerListener);
        return this;
    }

    /**
     * Installs Orders-worker chart for the strategy. Must be called before connect() method.
     *
     * @param _symbol symbol of the chart to put order worker on.
     * @return Strategy object itself
     */
    public Strategy withDedicatedInstrumentOrdersWorker(final String _symbol) {
        if (ordersWorker != null) {
            if (isAlive()) {
                throw new RuntimeException("TimerListener is already set");
            } else {
                charts.remove(ordersWorker);
            }
        } else {
            if (isAlive()) {
                throw new RuntimeException("OrderWorker must be set before connection.");
            }
        }
        ordersWorker = new Chart(SPCH + _symbol + ".OW") {
            @Override
            public String getChartParams() {
                return getChartId() + ':' + _symbol;
            }

            @Override
            public List<String> getParams() {
                ArrayList<String> params = new ArrayList<String>();
                params.add(_symbol);
                return params;
            }
        };
        addChart(ordersWorker);
        return this;
    }

    public Strategy withJfxServer(String jfxServerHost, int jfxServerPort) {
        this.jfxServerHost = jfxServerHost;
        this.jfxServerPort = jfxServerPort;
        return this;
    }

    public Strategy withJfxServerHost(String jfxServerHost) {
        this.jfxServerHost = jfxServerHost;
        return this;
    }

    public Strategy withJfxServerPort(int jfxServerPort) {
        this.jfxServerPort = jfxServerPort;
        return this;
    }

    /**
     * Used to setup history period client terminal will load historical orders of at startup.
     *
     * @param p History period
     * @return
     */
    public Strategy withHistoryPeriod(HistoryPeriod p) {
        this.historyPeriod = p;
        return this;
    }

    public synchronized void connect(String termServerHost, int termServerPort, com.jfx.Broker mt4Server, String mt4User, String mt4Password, String symbol, boolean asynchOrderOperations) throws NJ4XInvalidUserNameOrPasswordException, NJ4XMaxNumberOfTerminalsExceededException, NJ4XNoConnectionToServerException, IOException {
//        dumpState();
        //
        this.termServerHost = termServerHost;
        this.termServerPort = termServerPort;
        this.mt4Server = mt4Server;
        this.mt4User = mt4User;
        this.mt4Password = mt4Password;
        this.asynchOrderOperations = asynchOrderOperations;
        this.symbol = symbol;
        this.isTickModeStrategy = symbol != null;
        //
        if (terminal != null) {
            terminal.injectCharts();
        }
        //
        if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
            LOGGER.info("Connecting to     " + mt4User + '/' + mt4Server.getVal());
        }
//        if (cli != null) {
//            //noinspection EmptyCatchBlock
//            try {
//                cli.close();
//            } catch (Throwable t) {
//            }
//        }
        if (tsClient != null) {
            //noinspection EmptyCatchBlock
            try {
                tsClient.close();
            } catch (Throwable t) {
            }
        }
        //
        tsClient = new TSClient(termServerHost, termServerPort + 1);
        //
        try {
//            String clientName = mt4User + "@" + mt4Server.getVal() + "+" + getTenants();
            String clientName = Version.NJ4X_UUID + " " + getTenants() + " " + mt4Server.getVal() + " " + mt4User;
            //
//            cli = new TerminalClient(clientName, termServerHost, termServerPort);
            //
            tsClient.connect(clientName);
            //
//            this.termServerVersion = cli.tsVersion;
//            this.termServerHostName = cli.getTSName();
            //
            Nj4XTSInfo tsInfo = tsClient.getTsInfo();
            this.termServerVersion = tsInfo.getApiVersion();
            if (this.termServerVersion == null || this.termServerVersion.compareTo(MINIMUM_TS_VERSION) < 0) {
                throw new RuntimeException("Unsupported Terminal Server version: " + this.termServerVersion);
            }

            this.termServerHostName = tsInfo.getHostName();
            //
            if (this.termServerHostName == null || this.termServerHostName.equals("TerminalServer")) {
                LOGGER.warn("Invalid TS hostname response: " + this.termServerHostName);
                this.termServerHostName = this.termServerHost;
            }
            //
            if (id == null) {
                id = registerStrategyGetRegID(this);
            } else {
                registerStrategy(this);
            }
            //
//            String bindHost = getBindHost();
//            String cmd = "RUNTERM" + (asynchOrderOperations ? "" : ":") + ":" + mt4Server.getVal() + '|' + mt4User + '|' + mt4Password + '|' + id + '|' + bindHost + '|' + JFXServer.getInstance().getBindPort() + ':' + getTenants()
//                    + '|' + (symbol != null && symbol.trim().length() > 0 ? symbol.trim() : "");
//            //
//            //
//            if (charts != null && charts.size() > 0) {
//                StringBuilder csb = new StringBuilder();
//                for (Chart c : charts) {
//                    csb.append('|').append(c.getChartParams());
//                }
//                //
//                cmd = cmd + '|' + charts.size() + csb;
//                //
//            }
            //
            Nj4XMT4Account nj4xMT4Account = TSClient.getNj4xMT4Account(mt4Server, mt4User, mt4Password);
            JFXServer.getInstance(getBindHost(), getBindPort());
            Nj4XParams nj4xParams = getNj4xParams();
            String s = /*mt4Password.equals("EMULATE") ? "OK, started" : */tsClient.runMT4Terminal(nj4xMT4Account, nj4xParams, false);
//            String s = mt4Password.equals("EMULATE") ? "OK, started" : cli.ask(cmd);
            //
            if (s.startsWith("OK") && s.endsWith(" running")) {
                Strategy strategy1 = this instanceof Chart ? checkStrategyRegistration() : getStrategy(id + '1');
                Strategy strategy2 = getStrategy(id + '2');
                for (int t = 0; t < ((strategy1 != this || strategy2 != this) ? 36 : 18) /*terminal is already running, waiting for it to connect back 9 seconds (15 sec if one chart is connected)*/ && (strategy1 == this || strategy2 == this); ++t) {
                    try {
                        wait(500);
                    } catch (InterruptedException ignore) {
                    }
                    strategy1 = checkStrategyRegistration();
//                    if (strategy1 == this) {
//                        continue;
//                    }
                    strategy2 = getStrategy(id + '2');
                }
                if (strategy1 == this || strategy2 == this) {
//                    String cmd2 = "KILLTERM:" + mt4Server.getVal() + '|' + mt4User + '|' + mt4Password + '|' + id + "|||";
                    tsClient.killMT4Terminal(nj4xMT4Account, nj4xParams);
//                    cli.ask(cmd2);
                    registerStrategy(this);
                    tsClient.runMT4Terminal(nj4xMT4Account, nj4xParams, true);
//                    s = cli.ask("L" + cmd);
                }
            }
            if (s.startsWith("OK")) {
                try {
                    Strategy strategy1 = checkStrategyRegistration();
                    Strategy strategy2 = getStrategy(id + '2');
                    for (int t = 0; (strategy1 == this || strategy2 == this); ++t) {
                        if (t % 2 == 1) {
                            s = tsClient.checkMT4Terminal(nj4xMT4Account, nj4xParams);
                            if (!s.startsWith("OK")) {
                                processFailedConnResp(s);
                            }
                        } else {
                            wait(2000);//todo check notification
                        }
                        strategy1 = checkStrategyRegistration();
/*
                        if (strategy1 == this) {
//                            if (LOGGER.isInfoEnabled()) {
//                                LOGGER.info("Connecting to " + mt4Server.val + " ...");
//                            }
                            continue;
                        }
*/
                        strategy2 = getStrategy(id + '2');
//                        if (strategy2 == this) {
//                            if (LOGGER.isInfoEnabled()) {
//                                LOGGER.info("Connecting to " + mt4Server.val + " ...");
//                            }
//                        }
                    }
                    //
                    if (strategy1 == this || strategy2 == this) {
//                        try {
//                            cli.close();
//                        } finally {
//                            cli = null;
//                        }
                        String msg = "Connection to " + mt4Server.getVal() + " has been timed out, check server/user/password, date=" + new Date();
                        LOGGER.error(msg, new RuntimeException());
                        LOGGER.info("this=" + this);
                        LOGGER.info("strategy1=" + strategy1);
                        LOGGER.info("strategy2=" + strategy2);
                        LOGGER.info("registeredStrategies=" + registeredStrategies);
                        if (LOGGER.isDebugEnabled()) {
                            System.out.println(msg);
                            System.out.println(msg);
                            System.out.println(msg);
                            wait(120000);
                        }
                        throw new IOException(msg);
                    } else {
                        startCoordination();
                    }
                } catch (InterruptedException e) {
                    //noinspection UnnecessaryReturnStatement
                    return;
                }
            } else {
                processFailedConnResp(s);
            }
        } finally {
            removeStrategy(id + '1');
            removeStrategy(id + '2');
            //
            if (tsClient != null) {
                try {
                    tsClient.close();
                } finally {
                    tsClient = null;
                }
            }
        }
        Version version = getVersion();
        if (!version.isConsistent()) {
            LOGGER.warn("Inconsistent API used - " + version);
        }
    }

    public void processFailedConnResp(String s) throws IOException {
        if (s.startsWith("No connection to server: ")) {
            throw new NJ4XNoConnectionToServerException(s);
        } else if (s.startsWith("Reached max number of terminals: ")) {
            throw new NJ4XMaxNumberOfTerminalsExceededException(s);
        } else if (s.startsWith("Invalid user name or password: ")) {
            throw new NJ4XInvalidUserNameOrPasswordException(s);
        }
        throw new IOException(s);
    }

    private Nj4XParams getNj4xParams() {
        Nj4XParams nj4xEAParams = new Nj4XParams();
        nj4xEAParams.setAsynchOrdersOperations(asynchOrderOperations);
        nj4xEAParams.setJfxHost(getBindHost());
        nj4xEAParams.setJfxPort(getBindPort());
        nj4xEAParams.setStrategy(id);
        nj4xEAParams.setSymbol((symbol != null && symbol.trim().length() > 0 ? symbol.trim() : ""));
        nj4xEAParams.setPeriod(period == 0 ? 240 : period);
        nj4xEAParams.setHistoryPeriod(historyPeriod == null ? 0 : historyPeriod.toInt());
        nj4xEAParams.setTenant(getTenant());
        nj4xEAParams.setParentTenant(getParentTenant());
        //
        if (charts != null && charts.size() > 0) {
            for (Chart c : charts) {
                Nj4XChartParams p = new Nj4XChartParams();
                p.setChartID(c.getChartId());
                List<String> params = c.getParams();
                if (params != null && params.size() > 0) {
                    p.getParams().addAll(params);
                }
                nj4xEAParams.getCharts().add(p);
            }
        }
        return nj4xEAParams;
    }

    private void startCoordination() {
        BasicStrategyRunner sr = ((BasicStrategyRunner) getStrategyRunner());
        if (sr == null) {
            throw new RuntimeException("Multiple instances of Strategy[" + id + " usr/srv=" + mt4User + '/' + mt4Server + " TS=" + termServerHost + " async=" + asynchOrderOperations + " sym=" + symbol + "] have been detected. Use close/disconnect methods as soon as Strategy object is not longer needed.");
        } else {
            sr.preInit();
            sr.startCoordination();
            if (charts != null && charts.size() > 0) {
                for (Chart c : charts) {
                    sr = ((BasicStrategyRunner) c.getStrategyRunner());
                    sr.preInit();
                    sr.startCoordination();
                }
            }
        }
    }

    private Strategy checkStrategyRegistration() {
        if (this instanceof Chart) {
            Chart c = ((Chart) this);
            return getStrategy(c.getStrategyId() + '|' + c.getChartId());
        } else {
            Strategy strategy1 = getStrategy(id + '1');
            if (strategy1 == null && charts != null && charts.size() > 0) {
                for (Chart c : charts) {
                    if (getStrategy(id + '|' + c.getChartId()) != null) {
                        strategy1 = this;
                        break;
                    }
                }
            }
            return strategy1;
        }
    }

    public String getTermServerHost() {
        return termServerHost;
    }

    public void setTermServerHost(String termServerHost) {
        this.termServerHost = termServerHost;
    }

    public int getTermServerPort() {
        return termServerPort;
    }

    public void setTermServerPort(int termServerPort) {
        this.termServerPort = termServerPort;
    }

    public com.jfx.Broker getMt4Server() {
        return mt4Server;
    }

    public void setMt4Server(com.jfx.Broker mt4Server) {
        this.mt4Server = mt4Server;
    }

    public int getMType() {
        return mt4Server == null ? 0 : mt4Server.getType();
    }

    public String getMt4User() {
        return mt4User;
    }

    public void setMt4User(String mt4User) {
        this.mt4User = mt4User;
    }

    protected String getMt4Password() {
        return mt4Password;
    }

    public void setMt4Password(String mt4Password) {
        this.mt4Password = mt4Password;
    }

    StrategyRunner _ordersProcessingChannel() {
        return super.getOrdersProcessingChannel();
    }

    protected MT4 getOrdersManipulationConnection() {
        if (isOrderTerminalsPresent) {
            Terminal pop;
            while (true) {
                synchronized (orderTerminals) {
                    if (!orderTerminals.empty()) {
                        pop = orderTerminals.pop();
                        break;
                    }
                }
                LockSupport.parkNanos(1L);
            }
            return pop.getMt4Connection();
        } else {
            if (ordersWorker == null) {
                return this;
            } else {
                return ordersWorker;
            }
        }
    }

    protected void returnOrdersManipulationConnection(MT4 conn) {
        if (conn != this && conn != ordersWorker) {
            orderTerminals.push(((TerminalStrategy) conn).terminal);
        }
    }

    /**
     * Creates another terminal instance to get specified symbol's ticks in real-time.
     *
     * @param symbol symbol to subscribe ticks listener for
     * @param lsnr   listener to process symbol's tick events
     * @return same account/broker terminal instance.
     */
    public Terminal addTerminal(String symbol, TickListener lsnr) {
        Terminal t = new Terminal(symbol, lsnr);
//        t.connect();
        return t;
    }

    /**
     * Creates another terminal instance and registers timer event handler for it.
     *
     * @param lsnr                timer listener to perform regular mt4 account jobs under.
     * @param timerIntervalMillis timer interval.
     * @return same account/broker terminal instance.
     */
    public Terminal addTerminal(TimerListener lsnr, int timerIntervalMillis) {
        Terminal t = new Terminal(lsnr, timerIntervalMillis);
//        t.connect();
        return t;
    }

    /**
     * Creates another terminal instance integrated with this strategy object.
     *
     * @param type TerminalType.ORDERS_WORKER or TerminalType.FREE_WORKER
     * @return terminal instance of the specified type.
     */
    public Terminal addTerminal(TerminalType type) {
        Terminal t = new Terminal(type);
//        t.connect();
        return t;
    }

    public boolean isTickListenerStrategy() {
        return false;
    }

    public static enum HistoryPeriod {
        DEFAULT, TODAY, LAST_3_DAYS, LAST_WEEK, LAST_MONTH, LAST_3_MONTHS, LAST_6_MONTHS, ALL_HISTORY;

        public int toInt() {
            switch (this) {
                case TODAY:
                    return 1;
                case LAST_3_DAYS:
                    return 2;
                case LAST_WEEK:
                    return 3;
                case LAST_MONTH:
                    return 4;
                case LAST_3_MONTHS:
                    return 5;
                case LAST_6_MONTHS:
                    return 6;
                case ALL_HISTORY:
                    return 7;
            }
            return 0;
        }
    }

    /**
     * Enumeration of additional strategy terminal workers.
     */
    public static enum TerminalType {
        /**
         * Unspecified purpose worker - terminals of this type can be used for custom parallel processing under the MT4 account.
         */
        FREE_WORKER,
        /**
         * Terminals of this type provide simultaneous OrderSend/Modify/Cancel/Delete operations - one can close two
         * different orders from two different Threads in parallel - Strategy.OrderClose method will not block if there
         * is enough ORDERS_WORKER terminals are opened for the strategy.
         */
        ORDERS_WORKER,
        /**
         * Terminals of this type are used to get different symbol's real-time ticks in parallel.
         */
        TICK_WORKER
    }

    /**
     * Tick events handler interface.
     */
    public interface TickListener {
        /**
         * Handle new tick info.
         *
         * @param tick       received tick information
         * @param connection MT4 connection to be used for tick processing
         */
        public void onTick(TickInfo tick, MT4 connection);
    }

    /**
     * Multiple symbols' tick events handler interface.
     */
    public interface BulkTickListener {
        /**
         * Handle new ticks.
         *
         * @param ticks      received symbols' ticks information
         * @param connection MT4 connection to be used for tick processing
         */
        public void onTicks(List<Tick> ticks, MT4 connection);
    }

    public interface TimerListener {
        public void onTimer(MT4 connection);
    }

    public class Instrument {
        String name, symbol1, symbol2;

        public Instrument(String name, String symbol1, String symbol2) {
            this.name = name;
            this.symbol1 = symbol1;
            this.symbol2 = symbol2;
        }

        public String getName() {
            return name;
        }

        public String getSymbol1() {
            return symbol1;
        }

        public String getSymbol2() {
            return symbol2;
        }

        public String toString() {
            return "Instrument [name=" + name + "] [sym_1=" + symbol1 + "] [sym_2=" + symbol2 + ']';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Instrument that = (Instrument) o;

            if (!name.equals(that.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    protected abstract class Chart extends Strategy {
        public String chartId;

        public Chart(String symbol) {
            setReconnect(true);
            chartId = this.symbol = symbol;
        }

        public String getChartId() {
            return chartId;
        }

        public String getChartParams() {
            return symbol;
        }

        @Override
        protected String getTenant() {
            return Strategy.this.getTenant();
        }

        @Override
        protected String getParentTenant() {
            return Strategy.this.getParentTenant();
        }

        @Override
        public String getTermServerHost() {
            return Strategy.this.getTermServerHost();
        }

        @Override
        public int getTermServerPort() {
            return Strategy.this.getTermServerPort();
        }

        @Override
        public com.jfx.Broker getMt4Server() {
            return Strategy.this.getMt4Server();
        }

        @Override
        public String getMt4User() {
            return Strategy.this.getMt4User();
        }

        @Override
        protected String getMt4Password() {
            return Strategy.this.getMt4Password();
        }

        public String getStrategyId() {
            return Strategy.this.id;
        }

        public List<String> getParams() {
            return null;
        }
    }

    /**
     * An additional Terminal using Strategy's credentials to connect to mt4 broker in parallel.
     */
    public class Terminal {
        private MT4 mt4Connection;
        private TerminalType type;
        private int timerIntervalMillis;
        private TimerListener timerListener;
        private HashMap<String, TickListener> tickListeners;
        private String id;
        private boolean isInjected;

        private Terminal(TerminalType type) {
            this.type = type;
            if (type != TerminalType.TICK_WORKER) {
                calcId();
            }
        }

        private Terminal(String symbol, TickListener tickListener) {
            this.type = TerminalType.TICK_WORKER;
            tickListeners = new HashMap<String, TickListener>();
            tickListeners.put(symbol, tickListener);
            if (symbol == null || symbol.length() == 0) {
                throw new RuntimeException("Tick worker's symbol is empty.");
            }
            if (tickListener == null) {
                throw new RuntimeException("Tick worker's listener must be provided.");
            }
        }

        private Terminal(TimerListener timerListener, int timerIntervalMillis) {
            this.type = TerminalType.FREE_WORKER;
            this.timerListener = timerListener;
            this.timerIntervalMillis = timerIntervalMillis;
            if (timerIntervalMillis <= 0) {
                throw new RuntimeException("timerIntervalMillis must be greater than zero.");
            }
            if (timerListener == null) {
                throw new RuntimeException("Timer listener must be provided.");
            }
            calcId();
        }

        private void calcId() {
            synchronized (terminals) {
                switch (type) {
                    case TICK_WORKER:
                        if (tickListeners == null || tickListeners.size() == 0) {
                            throw new RuntimeException("TickListener must be added for this terminal type.");
                        }
                        ArrayList<String> symbols = new ArrayList<String>(tickListeners.keySet());
                        Collections.sort(symbols);
                        if (id != null) {
                            terminals.remove(this);
                        }
                        StringBuilder sb = new StringBuilder();
                        for (String s : symbols) {
                            sb.append(' ').append(s);
                        }
                        if (sb.length() < 64) {
                            id = "" + type + sb;
                        } else {
                            id = "" + type + " " + symbols.size() + " symbols " + MD5.MD5(sb.toString());
                        }
//                        id = "" + type + " " + symbol;
                        break;
                    default:
                        int cnt = 0;
                        for (Terminal t : terminals) {
                            cnt += (t.type == this.type ? 1 : 0);
                        }
                        id = (timerListener == null ? "" + type : "TIMER") + " " + String.format("%03d", cnt + 1);
                        break;
                }
                terminals.add(this);
            }
        }

        public Terminal addTickListener(String symbol, TickListener tickListener) {
            if (type != TerminalType.TICK_WORKER) {
                throw new RuntimeException("Terminal must be of " + TerminalType.TICK_WORKER + " type.");
            }
            tickListeners = tickListeners == null ? new HashMap<String, TickListener>() : tickListeners;
            if (symbol == null || symbol.length() == 0) {
                throw new RuntimeException("Tick worker's symbol is empty.");
            }
            if (tickListener == null) {
                throw new RuntimeException("Tick worker's listener must be provided.");
            }
            tickListeners.put(symbol, tickListener);
            //
            return this;
        }

        public Terminal connect() throws IOException {
            if (mt4Connection == null) {
                mt4Connection = new TerminalStrategy(this);
                synchronized (terminals) {
                    if (type == TerminalType.ORDERS_WORKER) {
                        orderTerminals.push(this);
                        isOrderTerminalsPresent = true;
                    }
                }
            }
            return this;
        }

        /**
         * Used to kill mt4 terminal process (makes this Terminal unusable)
         *
         * @throws IOException in case of Terminal Server errors.
         */
        public void close() throws IOException {
            if (type == TerminalType.ORDERS_WORKER) {
                if (orderTerminals.search(this) < 0) {
                    throw new IOException("Terminal is currently used.");
                } else {
                    orderTerminals.remove(this);
                }
            }
            synchronized (terminals) {
                terminals.remove(this);
            }
            ((TerminalStrategy) mt4Connection).close(true);
        }

        /**
         * Terminal's type: FREE, ORDERS or TICK WORKER.
         *
         * @return returns type of the terminal.
         */
        public TerminalType getType() {
            return type;
        }

        /**
         * MT4 connection associated with this terminal.
         *
         * @return MT4 connection to be used elsewhere.
         */
        public MT4 getMt4Connection() {
            return mt4Connection;
        }

        /**
         * Returns terminal identifier composed of type and (optionally) ordinal number.
         *
         * @return terminal identifier
         */
        public String getId() {
            if (id == null) {
                calcId();
            }
            return id;
        }

        private void injectCharts() {
            if (tickListeners != null && !isInjected) {
                isInjected = true;
                for (final Map.Entry<String, TickListener> tle : tickListeners.entrySet()) {
                    final String instrument = tle.getKey();
                    final TickListener _tickListener = tle.getValue();
                    addChart(new TickListenerChart(instrument, _tickListener));
                }
            }
        }

    }

    protected class TerminalStrategy extends Strategy {
        private Terminal terminal;

        private TerminalStrategy(Terminal t) throws IOException {
            setReconnect(true);
            terminal = t;
            //
            HashMap<String, TickListener> tickListeners = t.tickListeners;
            if (tickListeners != null) {
                for (final Map.Entry<String, TickListener> tle : tickListeners.entrySet()) {
                    addChart(new TickListenerChart(tle.getKey(), tle.getValue()) {
                        @Override
                        protected String getTenant() {
                            return TerminalStrategy.this.getTenant();
                        }

                        @Override
                        protected String getParentTenant() {
                            return TerminalStrategy.this.getParentTenant();
                        }
                    });
                }
            }
            //
            String mt4User = getMt4User();
            connect(
                    getTermServerHost(),
                    getTermServerPort(),
                    getMt4Server(),
                    mt4User.indexOf('@') > 0 ? mt4User.substring(0, mt4User.indexOf('@')) : mt4User,
                    getMt4Password()
            );
        }

        public Terminal getTerminal() {
            return terminal;
        }

        @Override
        public synchronized void init(String symbol, int period, StrategyRunner strategyRunner) throws ErrUnknownSymbol, IOException {
            isAlive = true;
            this.symbol = symbol;
            this.period = period;
//            if (this.cli != null) {
//                try {
//                    this.cli.close();
//                } finally {
//                    this.cli = null;
//                }
//            }
        }

        @Override
        public String toString() {
            return Strategy.this.mt4User
                    + "/" + Strategy.this.mt4Server
                    + "/" + terminal.getId()
                    ;
        }

        @Override
        public String getTermServerHost() {
            return Strategy.this.getTermServerHost();
        }

        @Override
        public int getTermServerPort() {
            return Strategy.this.getTermServerPort();
        }

        @Override
        public com.jfx.Broker getMt4Server() {
            return Strategy.this.getMt4Server();
        }

        @Override
        public String getMt4User() {
            return Strategy.this.getMt4User();
        }

        @Override
        protected String getMt4Password() {
            return Strategy.this.getMt4Password();
        }

        @Override
        public long coordinationIntervalMillis() {
            return (terminal.timerIntervalMillis == 0 ? 2000 : terminal.timerIntervalMillis);
        }

        @Override
        public void coordinate() {
            if (terminal.timerListener != null) {
                terminal.timerListener.onTimer(this);
            }
        }

        @Override
        protected String getTenant() {
            return terminal.getId()/* + '\u0002' + Strategy.this.getTenant()*/;
        }

        @Override
        protected String getParentTenant() {
            return Strategy.this.getTenant();
        }
    }

    class TickListenerChart extends Chart {
        private final TickListener _tickListener;
        private final BulkTickListener _bulkTickListener;
        private TickInfo lastTick;

        public TickListenerChart(String instrument, TickListener _tickListener) {
            super(instrument);
            this._tickListener = _tickListener;
            this._bulkTickListener = null;
            lastTick = new TickInfo();
        }

        public TickListenerChart(String instrument, BulkTickListener _bulkTickListener) {
            super(instrument);
            this._tickListener = null;
            this._bulkTickListener = _bulkTickListener;
            lastTick = new TickInfo();
        }

        @Override
        public long coordinationIntervalMillis() {
            return 1;
        }

        @Override
        public void coordinate() {
            try {
                if (_bulkTickListener == null) {
                    TickInfo tick = newTick(getSymbol(), lastTick);
                    lastTick = tick;
                    Chart chart = this;
                    _tickListener.onTick(tick, chart);
                } else {
                    _bulkTickListener.onTicks(getTicks(), this);
                }
            } catch (ErrUnknownSymbol errUnknownSymbol) {
                errUnknownSymbol.printStackTrace();
            }
        }

        @Override
        public boolean isTickListenerStrategy() {
            return true;
        }
    }

//    public
}
