package com.jfx.strategy;

import com.jfx.*;
import com.jfx.net.JFXServer;
import com.jfx.net.TSClient;
import com.jfx.net.TerminalClient;
import com.jfx.net.tsapi.TS;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StrategyITCase {
    private static final String TEST_SYMBOL = null;//"EURUSD";
    //
    String tsHost = "127.0.0.1";
//    String tsHost = "101.201.82.70";
    int tsPort = 7788;
//    int tsPort = 7799;

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp: User.dir=" + System.getProperty("user.dir"));
        TSClient.setWebEndpointInterceptor(
                new TSClient.WebEndpointInterceptor() {
                    @Override
                    public void customizeEndpoint(TS endpoint) {
                        //Setting infinite connection timeout
//                Client client = ClientProxy.getClient(ts);
//                HTTPConduit http = (HTTPConduit) client.getConduit();
//                HTTPClientPolicy policy = http.getClient();
//                policy.setReceiveTimeout(0);
//                policy.setConnectionTimeout(0);
                        //
                    }
                }
        );
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tearDown: User.dir=" + System.getProperty("user.dir"));
        PUtils.sleep(5000);
    }

    @Test
    public void testNoActivation() throws Exception {
        String jfx_activation_key = DemoAccount.JFX_ACTIVATION_KEY;
        String nj4x_mt5_activation_key = DemoAccount.NJ4X_MT5_ACTIVATION_KEY;
        Strategy s = new Strategy();
        try {
            //
            System.setProperty("jfx_activation_key", "123456"); // set wrong ak
            System.setProperty("nj4x_mt5_activation_key", "123456");
            s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
            System.setProperty("jfx_activation_key", DemoAccount.JFX_ACTIVATION_KEY);
            System.setProperty("nj4x_mt5_activation_key", DemoAccount.NJ4X_MT5_ACTIVATION_KEY);
            Date date = s.timeCurrent();
            System.out.println("Current time (server): " + date);
            System.out.println("Current time (local): " + s.toLocalTime(date));
            System.out.println("ordersTotal: " + s.ordersTotal());
            s.accountName();
            Assert.assertTrue(s.isLimitedFunctionality());
            System.out.println("Wrong JFX_ACTIVATION_KEY is Ok.");
            PUtils.sleep(5000);
            s.disconnect();
            //
            s = new Strategy();
            s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
            Assert.assertFalse(s.isLimitedFunctionality());
            System.out.println("Right JFX_ACTIVATION_KEY is Ok.");
            System.setProperty("jfx_activation_key", "123456");// set wrong ak
            System.setProperty("nj4x_mt5_activation_key", "123456");
            s.accountName();
            boolean tradeAllowed = s.isTradeAllowed("GBPUSD", new Date());
            PUtils.sleep(5000);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.setProperty("jfx_activation_key", jfx_activation_key);
            System.setProperty("nj4x_mt5_activation_key", nj4x_mt5_activation_key);
            s.close(true);
        }
    }

//    @Test
    public void testServer() throws Exception {
        JFXServer.getInstance();
        Thread.sleep(100000);
    }

//        @Test
    public void testWCS2MBS() throws Exception {
        Strategy s = new Strategy();
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD);
        String symbol = "EURUSD";
        s.symbolSelect(symbol, true);
        SymbolInfo symbolInfo = s.symbolInfo(symbol);
        System.out.println("askHigh=" + symbolInfo.askHigh);
        System.out.println("bidHigh=" + symbolInfo.bidHigh);
        System.out.println("askLow=" + symbolInfo.askLow);
        System.out.println("bidLow=" + symbolInfo.bidLow);
        System.out.println("path=" + symbolInfo.path);
        System.out.println("bidHigh=" + s.iHigh(symbol, Timeframe.PERIOD_D1, 0));
        System.out.println("bidLow=" + s.iLow(symbol, Timeframe.PERIOD_D1, 0));
        s.close(true);
    }

//    @Test
    public void testSymbolInfo() throws Exception {
        Strategy s = new Strategy();
        s.connect(tsHost, tsPort, new Broker("mForex-Demo"), "77081378", "6zktowk");
        for (String symbol : s.getSymbols()) {
            try {
                SymbolInfo symbolInfo = s.symbolInfo(symbol);
                System.out.println("askHigh=" + symbolInfo.askHigh);
                System.out.println("bidHigh=" + symbolInfo.bidHigh);
                System.out.println("askLow=" + symbolInfo.askLow);
                System.out.println("bidLow=" + symbolInfo.bidLow);
                System.out.println("path=" + symbolInfo.path);
                System.out.println("bidHigh=" + s.iHigh(symbol, Timeframe.PERIOD_D1, 0));
                System.out.println("bidLow=" + s.iLow(symbol, Timeframe.PERIOD_D1, 0));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        s.close(true);
    }

    @Test
    public void testIdleTerminalReconnection() throws Exception {
        PUtils.Job repeat = new PUtils.Job() {
            @Override
            public void run() throws Exception {
                final Strategy s = new Strategy();
                s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
                PUtils.calcTPS(new PUtils.Job() {
                    @Override
                    public void run() throws Exception {
                        double iClose = s.iClose("GBPUSD", Timeframe.PERIOD_M1, 0);
                    }
                }, 1000, "iClose tps report");
                s.disconnect();
            }
        };
        //
        repeat.run();
        //
        long time = PUtils.time(repeat);
        Assert.assertTrue(time < 20000);
        //
        Strategy s = new Strategy();
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        //
        MarketInformation marketInformation = s.marketInfo("GBPUSD");
        double GBPUSDPoint = marketInformation.POINT; // 0.00001
        double GBPUSDPointTheSame = s.marketInfo("GBPUSD", MarketInfo.MODE_POINT);
        //
        s.close(true);
    }

    @Test
    public void testTickListener() throws Exception {
        final Strategy s = new Strategy() {
            @Override
            public long coordinationIntervalMillis() {
                return 1000;
            }

            @Override
            public void coordinate() {
                System.out.println("terminalCompany: " + terminalCompany());
                System.out.println("accountBalance: " + accountBalance());
            }
        };
        final String symbol = "EURJPY";
        s.addTickListener(symbol, new Strategy.TickListener() {
            @Override
            public void onTick(TickInfo tick, MT4 connection) {
                System.out.println("(" + symbol + ") Tick: " + tick + ", date=" + new Date());
/*
                try {
                    Date date = null;
                    for (int i = 0; i < 100; i++) {
                        date = connection.iTime(symbol, Timeframe.PERIOD_M1, 0);
                        boolean tradeAllowed = connection.isTradeAllowed();
                    }
                    System.out.println("(" + symbol + ") Tick: " + tick + ", date=" + date);
                } catch (MT4Exception e) {
                    e.printStackTrace();
                }
*/
            }
        });
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        //
        PUtils.sleep(15000);
        s.close(true);
    }

    @Test
    public void testTimer() throws Exception {
        final Strategy s = new Strategy() {
            @Override
            public long coordinationIntervalMillis() {
                return 1000;
            }

            @Override
            public void coordinate() {
                System.out.println("terminalCompany: " + terminalCompany());
                System.out.println("accountBalance: " + accountBalance());
            }
        };
        final String symbol = "EURJPY";
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        s.addTerminal(new Strategy.TimerListener() {
            @Override
            public void onTimer(MT4 c) {
                System.out.println("ab=" + c.accountBalance());
            }
        }, 1000).connect();
        //
        PUtils.sleep(6000);
//        System.in.read();
        //
        s.close(true);
    }

    @Test
    public void testTerminalTickListener() throws Exception {
        final Exception[] exceptions = new Exception[1];
        final Strategy s = new Strategy() {
            @Override
            public long coordinationIntervalMillis() {
                return 5000;
            }

            @Override
            public void coordinate() {
                try {
                    System.out.println("terminalCompany: " + terminalCompany());
                    System.out.println("accountBalance: " + accountBalance());
                } catch (Exception e) {
                    e.printStackTrace();
                    exceptions[0] = e;
                }
            }
        };
        final String symbol = "EURJPY";
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        s.addTerminal(symbol, new Strategy.TickListener() {
            @Override
            public void onTick(TickInfo tick, MT4 connection) {
                try {
                    System.out.println("(" + symbol + ") Tick: " + tick);
                    Date date = null;
                    for (int i = 0; i < 10; i++) {
                        date = connection.iTime(symbol, Timeframe.PERIOD_M1, 0);
                        boolean tradeAllowed = connection.isTradeAllowed();
                    }
                    System.out.println("(" + symbol + ") Tick: " + tick + ", date=" + date);
                } catch (MT4Exception e) {
                    e.printStackTrace();
                }
            }
        }).connect();
        //
        PUtils.sleep(5000);
        Exception exception = exceptions[0];
        s.close(true);
        //
        if (exception != null) {
            throw exception;
        }
    }

    @Test
    public void testPositionListener() throws Exception {
        final Strategy s = new Strategy();
        s.setReconnect(true);
        s.setPositionListener(new PositionListener() {
            @Override
            public void onInit(PositionInfo initialPositionInfo) {
                System.out.println("\nInit with Live orders: " + initialPositionInfo.liveOrders().size());
                System.out.println("Init with Hist orders: " + initialPositionInfo.historicalOrders().size());
            }

            @Override
            public void onChange(PositionInfo currentPositionInfo, PositionChangeInfo changes) {
                try {
                    System.out.println("");
                    System.out.println("NEW: " + changes.getNewOrders());
                    System.out.println("MODIFIED: " + changes.getModifiedOrders());
                    System.out.println("CLOSED: " + changes.getClosedOrders());
                    System.out.println("DELETED: " + changes.getDeletedOrders());
                    for (OrderInfo closedOrder : changes.getClosedOrders()) {
                        OrderInfo loadedOrder = s.orderGet(closedOrder.getTicket(), SelectionType.SELECT_BY_TICKET, SelectionPool.MODE_HISTORY);
                        int synchWait = 0;
                        while (closedOrder.getClosePrice() != loadedOrder.getClosePrice()) {
                            try {
                                synchWait++;
                                System.out.println("Wait synch #" + synchWait);
                                Thread.sleep(1000);
                            } catch (Exception ignore) {
                            }
                            loadedOrder = s.orderGet(closedOrder.getTicket(), SelectionType.SELECT_BY_TICKET, SelectionPool.MODE_HISTORY);
                        }
                        System.out.println("A #" + closedOrder.getTicket() + " " + closedOrder.getComment() + " - " + closedOrder.getProfit() + " closePrice=" + closedOrder.getClosePrice());
                        System.out.println("B #" + loadedOrder.getTicket() + " " + loadedOrder.getComment() + " - " + loadedOrder.getProfit() + " closePrice=" + loadedOrder.getClosePrice());
                    }
                } catch (Exception e) {
                    System.err.println("Exception in onChange PositionListener" + e);
                }
            }
        });
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        //
        PUtils.sleep(10000 /*commentme*//* *100*/);
        s.close(true);
    }

    @Test
    public void testAllSymbolsTickListener() throws Exception {
        final Exception[] exceptions = new Exception[1];
        final Strategy mt4 = new Strategy() {
            @Override
            public long coordinationIntervalMillis() {
                return 5000;
            }

            @Override
            public void coordinate() {
                try {
                    StringBuilder sb = new StringBuilder("@@ COORDINATE TICKS @@");
                    ArrayList<Tick> ticks = getTicks();
                    for (int i = 0; i < ticks.size(); i++) {
                        Tick tick = ticks.get(i);
                        sb.append('\t').append(tick.toString()).append("\r\n");
                    }
                    //System.out.println(sb.toString());
                    AccountInfo accountInfo = accountInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                    exceptions[0] = e;
                }
            }
        };
        //
        mt4.setBulkTickListener(new Strategy.BulkTickListener() {
            private int run = 0;

            {
                exceptions[0] = new RuntimeException("BulkTickListener failure");
            }

            @Override
            public void onTicks(List<Tick> ticks, MT4 connection) {
                run++;
                if (run == 2) {
                    exceptions[0] = null;
                }
                for (int i = 0; i < ticks.size(); i++) {
                    Tick tick = ticks.get(i);
                    System.out.println("" + run + "> " + tick + " conn=" + connection);
                }
//                mt4.copyRates("", Timeframe.PERIOD_H1, 0, 2).get(0).time;
            }
        });
        mt4.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        int ssCount = mt4.symbolsTotal(false);
        ArrayList<String> selSymbols = new ArrayList<String>();
        for (int i = 0; i < ssCount; ++i) {
            selSymbols.add(mt4.symbolName(i, false));
        }
        int size = selSymbols.size();
        for (int i = 9; i < size; ++i) {
            mt4.symbolSelect(selSymbols.get(i), false);
        }
        //
        for (String symbol : mt4.getSymbols()) {
            mt4.symbolSelect(symbol, true); // false
        }
        //
        PUtils.sleep(5000);
        //
        Map<Long, OrderInfo> liveOrders = mt4.orderGetAll(SelectionPool.MODE_TRADES);
        Map<Long, OrderInfo> hOrders = mt4.orderGetAll(SelectionPool.MODE_HISTORY);
        //
/*
        ArrayList<OrderInfo> closedOrders = mt4.orderGetAll(SelectionPool.MODE_HISTORY).values()
                .stream().filter(o -> (o.getType() == TradeOperation.OP_BUY || o.getType() == TradeOperation.OP_SELL))
                .collect(Collectors.toCollection(ArrayList::new));
*/
        //
        ArrayList<OrderInfo> _closedOrders = new ArrayList<OrderInfo>();
        for (OrderInfo o : mt4.orderGetAll(SelectionPool.MODE_HISTORY).values()) {
            if (o.getType() == TradeOperation.OP_BUY || o.getType() == TradeOperation.OP_SELL) {
                _closedOrders.add(o);
            }
        }
        //
        Exception exception = exceptions[0];
        mt4.close(true);
        //
        if (exception != null) {
            throw exception;
        }
    }

    @Test
    public void testTerminalTickListenerLong() throws Exception {
        final Exception[] exceptions = new Exception[1];
        Strategy s = new Strategy();
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        ArrayList<String> symbols = s.getSymbols();
        s.close();
        s = new Strategy();
        for (int i = 0; i < Math.min(symbols.size(), 10); i++) {
            final String sym = symbols.get(i);
            s.addTickListener(sym, new Strategy.TickListener() {
                @Override
                public void onTick(TickInfo tick, MT4 connection) {
                    System.out.println(sym + " " + tick);
                }
            });
        }
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        s.addTerminal(Strategy.TerminalType.ORDERS_WORKER).connect();
        s.addTerminal(Strategy.TerminalType.ORDERS_WORKER).connect();
        s.addTerminal(Strategy.TerminalType.ORDERS_WORKER).connect();
        //
        PUtils.sleep(10000);
        s.close(true);
    }

    @Test
    public void testTsClient() throws Exception {
        TSClient tsClient = new TSClient(tsHost, tsPort + 1);
        try {
            System.out.println("" + tsClient.connect("testSRVList"));
        } finally {
            tsClient.close();
        }
    }

    @Test
    public void testSRVList() throws Exception {
        System.out.println("" + TerminalClient.getAvailableSRVFiles(tsHost, tsPort));
        System.out.println("BoxID: " + TerminalClient.getBoxID(tsHost, tsPort));
        //
        TSClient tsClient = new TSClient(tsHost, tsPort + 1);
        try {
            System.out.println("" + tsClient.connect("testSRVList").getAvailableSRVFiles());
            System.out.println("BoxID: " + tsClient.connect("testBoxID").getBoxID());
        } finally {
            tsClient.close();
        }
    }

    @Test
    public void testAccountInfo() throws Exception {
        System.out.println(new Date(1441097776000L).toString());
        final Strategy s = new Strategy() {
            @Override
            public long coordinationIntervalMillis() {
                return 5000;
            }

            @Override
            public void coordinate() {
                System.out.println("terminalCompany: " + terminalCompany());
                System.out.println("accountBalance: " + accountBalance());
                System.out.println("accountServer: " + accountServer());
                System.out.println("accountNumber: " + accountNumber());
                System.out.println("copyRates: " + copyRates("EURUSD", Timeframe.PERIOD_M15, 0, 20));
                System.out.println("M5 bar: " + getBar("EURUSD", Timeframe.PERIOD_M5));
                try {
                    double open = iOpen("GBPUSD", Timeframe.PERIOD_M1, 1);
                    double high = iHigh("GBPUSD", Timeframe.PERIOD_M1, 1);
                    double low = iLow("GBPUSD", Timeframe.PERIOD_M1, 1);
                    double close = iClose("GBPUSD", Timeframe.PERIOD_M1, 1);
                    String sURL = "GBPUSD;" + open + ";" + high + ";" + low + ";" + close + "@";
                    System.out.println(sURL);
                } catch (ErrHistoryWillUpdated errHistoryWillUpdated) {
                    errHistoryWillUpdated.printStackTrace();
                } catch (ErrUnknownSymbol errUnknownSymbol) {
                    errUnknownSymbol.printStackTrace();
                }
            }
        };

        try {
            s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
            AccountInfo accountInfo = s.accountInfo();
            accountInfo.getLeverage();
//        s.connect(tsHost, tsPort, new Broker("AlpariUK-Demo - Micro+Classic"), "625839", "Kct4ubLktH");
//        int ticket = s.orderSend("EURJPY", TradeOperation.OP_SELL, 0.3, s.marketInfo("EURJPY", MarketInfo.MODE_BID), 100, 0, 0, "RG-" + System.currentTimeMillis(), 0, null, 0);
//        System.out.println("Ticket: " + ticket);
            //
            for (String sym : s.getSymbols()) {
                s.symbolInfo(sym);
            }
            PUtils.sleep(5000);
//        s.print("SW_SHOW");
//        s.print("SW_HIDE");
//        s.print("SW_SHOW");
        } finally {
            s.close(true);
        }
    }

    //    @Test
    public void testAlbertoMethod() throws Exception {
        final Strategy s = new Strategy() {
            public long lastMinute;

            @Override
            public long coordinationIntervalMillis() {
                return 1000;
            }

            @Override
            public void coordinate() {
                int iMinute = Calendar.getInstance().get(Calendar.MINUTE);
                if (this.lastMinute != iMinute) {
                    System.out.println("last minute = " + this.lastMinute + " current minute = " + iMinute);
                    this.lastMinute = iMinute;

                    String tickers[] = {"GBPUSD", "GBPUSD"};//theFrame.jHistoricalTickersToUpload.getText().split(",");
                    String sURL = "";

                    for (int i = 0; i < tickers.length; i++) {
                        try {
                            // upload historical [x] min candles
                            double open = iOpen(tickers[i], Timeframe.PERIOD_M1, 1);
                            System.out.println("what's the iOPEN ? " + iOpen("GBPUSD", Timeframe.PERIOD_M1, 1));
                            System.out.println("what's the iOPEN ? " + iOpen("GBPUSD", Timeframe.PERIOD_M1, 0));
                            double high = iHigh(tickers[i], Timeframe.PERIOD_M1, 1);
                            double low = iLow(tickers[i], Timeframe.PERIOD_M1, 1);
                            double close = iClose(tickers[i], Timeframe.PERIOD_M1, 1);
                            sURL += tickers[i] + ";" + open + ";" + high + ";" + low + ";" + close + "@";

                        } catch (Exception ex) {
                            System.out.println("Couldn't retrieve open high low close : " + ex.getMessage());
                        }
                    }

                    // sURL = sURL.substring(0, -1);

                    System.out.println(iMinute + " + " + sURL);
                    //Frame.log(API.GET("api.quote.php?min_quotes=" + sURL));
                }
            }
        };
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
//        s.connect(tsHost, tsPort, new Broker("AlpariUK-Demo - Micro+Classic"), "625839", "Kct4ubLktH");
//        int ticket = s.orderSend("EURJPY", TradeOperation.OP_SELL, 0.3, s.marketInfo("EURJPY", MarketInfo.MODE_BID), 100, 0, 0, "RG-" + System.currentTimeMillis(), 0, null, 0);
//        System.out.println("Ticket: " + ticket);
        //
//        PUtils.sleep(10000);
        PUtils.sleep(10000);
        s.close(true);
    }

    @Test
    public void testOrderInfo() throws Exception {
        final Strategy s = new Strategy();
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        //
        for(String sym : s.getSymbols()) {
            s.symbolInfo(sym);
        }
        //
        int max = Math.min(s.ordersHistoryTotal(), 10);
        for (int i = 0; i < max; ++i) {
            OrderInfo orderInfo = s.orderGet(i, SelectionType.SELECT_BY_POS, SelectionPool.MODE_HISTORY);
            Assert.assertTrue(orderInfo.getCloseTime().getTime() > 0);
            OrderInfo orderInfo2 = s.orderGet(orderInfo.getTicket(), SelectionType.SELECT_BY_TICKET, SelectionPool.MODE_TRADES /*this must be ignored when selecting by ticket*/);
            Assert.assertTrue(orderInfo2.getCloseTime().getTime() > 0);
        }
        //
        s.close(true);
    }

    @Test
    public void testJfxServer() throws Exception {
        String appServerHost = "127.0.0.1";
        //
        // App #1
        final Strategy s1 = new Strategy();
        int app_1_Port = 7777;
        s1.withJfxServer(appServerHost, app_1_Port).connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        //
        // App #2
        final Strategy s2 = new Strategy();
        int app_2_Port = 7755;
        s2.withJfxServer(appServerHost, app_2_Port).connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        //
        for(String sym : s1.getSymbols()) {
            s2.symbolInfo(sym);
        }
        //
        int max = Math.min(s1.ordersHistoryTotal(), 10);
        for (int i = 0; i < max; ++i) {
            OrderInfo orderInfo = s2.orderGet(i, SelectionType.SELECT_BY_POS, SelectionPool.MODE_HISTORY);
            Assert.assertTrue(orderInfo.getCloseTime().getTime() > 0);
            OrderInfo orderInfo2 = s2.orderGet(orderInfo.getTicket(), SelectionType.SELECT_BY_TICKET, SelectionPool.MODE_TRADES /*this must be ignored when selecting by ticket*/);
            Assert.assertTrue(orderInfo2.getCloseTime().getTime() > 0);
        }
        //
        s1.close(true);
        s2.close(true);
    }

    @Test
    public void testOrderGet() throws Exception {
        final Strategy s = new Strategy();
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        //
        s.print(null);
        s.print("[\u0001test\u0002]");
        int max = s.ordersTotal();
        for (int i = 0; i < max; ++i) {
            String comment = null;
            if (s.orderSelect(i, SelectionType.SELECT_BY_POS, SelectionPool.MODE_TRADES)) {
                comment = s.orderComment();
            }
            OrderInfo orderInfo = s.orderGet(i, SelectionType.SELECT_BY_POS, SelectionPool.MODE_TRADES);
            Assert.assertTrue(orderInfo.getCloseTime().getTime() == 0);
        }
        //
        s.close(true);
    }

    @Test
    public void test4054() throws Exception {
        final Strategy s = new Strategy();
        try {
            s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
            //
            ArrayList<Strategy.Instrument> instruments = s.getInstruments();
            for (int i = 0; i < instruments.size(); i++) {
                Strategy.Instrument instrument = instruments.get(i);
                if (instrument.getName().contains("#") || instrument.getName().endsWith("fm"))
                    continue;
                try {
                    s.symbolSelect(instrument.getName(), true);
                    s.iHigh(instrument.getName(), Timeframe.PERIOD_M1, 0);
                    s.iHigh(instrument.getName(), Timeframe.PERIOD_M5, 0);
                } catch (MT4RuntimeException x) {
                    if (x.getErrorCode() == 4054) {
                        PUtils.sleep(1000);
                        s.iHigh(instrument.getName(), Timeframe.PERIOD_M1, 0);
                        s.iHigh(instrument.getName(), Timeframe.PERIOD_M5, 0);
                        break;
                    }
                }
            }
        } finally {
            s.close(true);
        }
    }

    //@Test
    public void testDisconnection() throws Exception {
        final Strategy s = new Strategy() {
            ArrayList<Strategy.Instrument> instruments;

            {
                setReconnect(false);
            }

            @Override
            public long coordinationIntervalMillis() {
                return 5000;
            }

            @Override
            public void init(String symbol, int period, StrategyRunner strategyRunner) throws ErrUnknownSymbol, IOException {
                instruments = getInstruments();
                System.out.println(String.format("Connected ports: read=%d write=%d",
                        ((MT4TerminalConnection) getChannel()).getSocket().getPort(),
                        ((MT4TerminalConnection) getOrdersProcessingChannel()).getSocket().getPort()
                ));
            }

            @Override
            public void coordinate() {
                if (instruments == null) return;
                Strategy.Instrument instrument = instruments.get(new Random().nextInt(instruments.size()));
                try {
                    iHigh(instrument.getName(), Timeframe.PERIOD_M1, 0);
                    iHigh(instrument.getName(), Timeframe.PERIOD_M5, 0);
                } catch (MT4RuntimeException x) {
                    x.printStackTrace();
                    if (x.getErrorCode() == 4054) {
                        PUtils.sleep(5000);
                        try {
                            iHigh(instrument.getName(), Timeframe.PERIOD_M1, 0);
                            iHigh(instrument.getName(), Timeframe.PERIOD_M5, 0);
                        } catch (Exception x2) {
                            x2.printStackTrace();
                        }
                    }
                } catch (Exception x3) {
                    x3.printStackTrace();
                }
            }
        };
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, true);
        //
        PUtils.sleep(30000);
        //
        s.close(true);
    }

    //@Test
    public void testSomeMethods() throws Exception {
        final Strategy s = new Strategy();
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        //
        callSomeMethods(s);
        //
        s.close(true);
    }

    //@Test
    public void testMT5() throws Exception {
        Broker mt4Server = DemoAccount.MT_4_SERVER;
        //
        final Strategy s = new Strategy();
        s.connect(tsHost, tsPort, new com.jfx.mt5.Broker("178.255.202.19:443"), "20098441", "ih1odgn", "GBPUSD");
        //
        callSomeMethods(s);
        //
        Thread.sleep(5000);
        s.close(false);
    }

    private void callSomeMethods(Strategy s) throws ErrUnknownSymbol, ErrHistoryWillUpdated, ErrIncorrectSeriesarrayUsing, InterruptedException {
/*
        extern double  select_other_tf_to_show = 0;
        extern double  H_Pos_MTF               = 1;
        extern double  V_Pos_MTF               = 50;
        extern double  Corner_MTF              = 3;

        extern double  eintPeriod              = 20;
        extern double  edblHigh1               = 0.4;
        extern double  edblLow1                = -0.4;
        extern double  atrPeriod               = 100;

        extern double  H_Pos                   = 1;
        extern double  V_Pos                   = 25;
*/
//        double rainbow = s.iCustom("GBPUSD", Timeframe.PERIOD_M30, "RainbowSlope", 6, 0, 0.0, 1.0, 50, 3, 20, 0.4, -0.4, 100, 1);
        //s.orderGet(63734253, SelectionType.SELECT_BY_TICKET, SelectionPool.MODE_TRADES);
        //
        int symbolsTotal = s.symbolsTotal(false);
        for (int i = 0; i < 5; i++) {
            String symbolName = s.symbolName(i, false);
            SymbolInfo symbolInfo = s.symbolInfo(symbolName);
            System.out.println(symbolInfo.path);
        }
        //
        for (int i = 0; i < 10; i++) {
            double ask = s.marketInfo("GBPUSD", MarketInfo.MODE_ASK);
        }
        double bid = s.marketInfo("GBPUSD", MarketInfo.MODE_BID);
        //
        s.iBars("GBPUSD", Timeframe.PERIOD_M30);
        s.iBars("GBPUSD", com.jfx.Timeframe.PERIOD_MN1);
        try {
            s.iBars("~GBPUSD", Timeframe.PERIOD_M30);
            Assert.fail("ErrUnknownSymbol expected");
        } catch (MT4RuntimeException | ErrUnknownSymbol ignore) {
            int lastError = s.getLastError();
            Assert.assertTrue(lastError == 54301 || lastError == 4106 || lastError == 4054
                    || ignore instanceof MT4RuntimeException && ((MT4RuntimeException) ignore).getErrorCode() == 4054
            );
        }
        boolean isMT4 = !(s.getMt4Server() instanceof com.jfx.mt5.Broker);
        Timestamp t0 = Timestamp.valueOf(isMT4 ? "2014-08-01 07:21:00.0" : "2014-06-06 07:21:00.0");
        Timestamp t1 = Timestamp.valueOf(isMT4 ? "2014-08-01 07:20:00.0" : "2014-06-06 07:20:00.0");
        //
        int shift1 = s.iBarShift("GBPUSD", Timeframe.PERIOD_M5, t0, false);
        shift1 = s.iBarShift("GBPUSD", Timeframe.PERIOD_M5, t0, false);
        int shift2 = s.iBarShift("GBPUSD", Timeframe.PERIOD_M5, t1, false);
        Assert.assertEquals(shift1, shift2);
        //
        double CHLOV[] = {1.36621, 1.36631, 1.36612, 1.36617, 232};
        //
        double closePrice = s.iClose("GBPUSD", Timeframe.PERIOD_M5, shift1);
        Assert.assertTrue(closePrice == CHLOV[0] || isMT4);
        //
        double highPrice = s.iHigh("GBPUSD", Timeframe.PERIOD_M5, shift1);
        Assert.assertTrue(highPrice == CHLOV[1] || isMT4);
        //
        double lowPrice = s.iLow("GBPUSD", Timeframe.PERIOD_M5, shift1);
        Assert.assertTrue(lowPrice == CHLOV[2] || isMT4);
        //
        double openPrice = s.iOpen("GBPUSD", Timeframe.PERIOD_M5, shift1);
        Assert.assertTrue(openPrice == CHLOV[3] || isMT4);
        //
        double volume = s.iVolume("GBPUSD", Timeframe.PERIOD_M5, shift1);
        Assert.assertTrue(volume == CHLOV[4] || isMT4);
        //
        Date time = s.iTime("GBPUSD", Timeframe.PERIOD_M5, shift1);
        Assert.assertTrue(time.getTime() == t1.getTime());
        //
        int lowestClose = s.iLowest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_CLOSE, 10, shift1);
        int lowestHigh = s.iLowest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_HIGH, 10, shift1);
        int lowestLow = s.iLowest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_LOW, 10, shift1);
        int lowestOpen = s.iLowest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_OPEN, 10, shift1);
        //int lowestTime = s.iLowest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_TIME, 10, shift1);
        //int lowestVolume = s.iLowest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_VOLUME, 10, shift1);
        //
        int highestClose = s.iHighest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_CLOSE, 10, shift1);
        int highestHigh = s.iHighest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_HIGH, 10, shift1);
        int highestLow = s.iHighest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_LOW, 10, shift1);
        int highestOpen = s.iHighest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_OPEN, 10, shift1);
        //int highestTime = s.iHighest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_TIME, 10, shift1);
        //int highestVolume = s.iHighest("GBPUSD", Timeframe.PERIOD_M5, Series.MODE_VOLUME, 10, shift1);
        //
        double accountBalance = s.accountBalance();
        double accountCredit = s.accountCredit();
        String accountCompany = s.accountCompany();
        String accountCurrency = s.accountCurrency();
        double accountEquity = s.accountEquity();
        double accountFreeMargin = s.accountFreeMargin();
        double accountMargin = s.accountMargin();
        String accountName = s.accountName();
        int accountNumber = s.accountNumber();
        double accountProfit = s.accountProfit();
        Assert.assertTrue(accountBalance > 0);
        //
        boolean connected = s.isConnected();
        int lastError = s.getLastError();
        boolean demo = s.isDemo();
        boolean testing = s.isTesting();
        boolean visualMode = s.isVisualMode();
        long tickCount = s.getTickCount(); //uint
        s.comment("comment");
        MarketInformation mi = s.marketInfo("GBPUSD");
        s.print("print");
        int day = s.day();
        int dayOfWeek = s.dayOfWeek();
        int dayOfYear = s.dayOfYear();
        int hour = s.hour();
        int minute = s.minute();
        int month = s.month();
        int seconds = s.seconds();
        int year = s.year();
        Date timeCurrent = s.timeCurrent();
        //
        try {
            s.objectDelete("HLINE-1");
            s.objectDelete("FIBO-2");
        } catch (Exception ignore) {
        }
        boolean testObj = s.objectCreate("HLINE-1", ObjectType.OBJ_HLINE, 0, timeCurrent, mi.BID);
        testObj = s.objectCreate("FIBO-2", ObjectType.OBJ_FIBO, 0, timeCurrent, mi.BID, new Date(timeCurrent.getTime() - 1000 * 3600), mi.BID - 0.01);
        double hLineColor = s.objectGet("HLINE-1", ObjectProperty.OBJPROP_COLOR);
        try {
            String fiboDescription = s.objectGetFiboDescription("FIBO-2", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //
        int ordersTotal0 = s.ordersTotal();
        int ordersHistoryTotal0 = s.ordersHistoryTotal();
/*
        long start = System.currentTimeMillis();
        while (true) {
            if (ordersHistoryTotal0 != s.ordersHistoryTotal() || System.currentTimeMillis() - start > 10000) {
                System.out.println("Orders hist total: was=" + ordersHistoryTotal0 + ", is=" + s.ordersHistoryTotal());
                ordersHistoryTotal0 = s.ordersHistoryTotal();
                break;
            }
            System.out.print(".");
            System.out.flush();
            Thread.sleep(1000);
        }
*/
        for (int i = 0; i < ordersHistoryTotal0; i++) {
            OrderInfo o = s.orderGet(i, SelectionType.SELECT_BY_POS, SelectionPool.MODE_HISTORY);
            System.out.println("--- Order #" + i + ": " + o);
        }
        try {
            for (int i = 0; i < ordersTotal0; i++) {
                OrderInfo o = s.orderGet(i, SelectionType.SELECT_BY_POS, SelectionPool.MODE_TRADES);
                if (o.getType() != TradeOperation.OP_BUY && o.getType() != TradeOperation.OP_SELL) {
                    // pending order
                }
                System.out.println("Active order #" + i + ": " + o);
                if (o.getType() != TradeOperation.OP_BUY && o.getType() != TradeOperation.OP_SELL) {
                    s.orderDelete(o.ticket(), 0);
                }
            }
            long ticket1 = s.orderSend("GBPUSD", TradeOperation.OP_BUY, 0.1, s.marketInfo("GBPUSD", MarketInfo.MODE_ASK), 2, 0, 0, "j-test", 0, null);
            long ticket2 = s.orderSend("GBPUSD", TradeOperation.OP_BUY, 0.1, s.marketInfo("GBPUSD", MarketInfo.MODE_ASK), 2, 0, 0, "j-test", 0, null);
            Assert.assertTrue(ticket1 == ticket2);
            OrderInfo orderInfo = s.orderGet(ticket2, SelectionType.SELECT_BY_TICKET, SelectionPool.MODE_TRADES);
            //
            Assert.assertTrue(s.orderClose(orderInfo.getTicket(), orderInfo.getLots(), s.marketInfo("GBPUSD", MarketInfo.MODE_BID), 2, 0));
            //
            int ordersHistoryTotal1 = s.ordersHistoryTotal();
            Assert.assertTrue("Orders total now==" + ordersHistoryTotal1 + ", before=" + ordersHistoryTotal0 + ",  closed order=" + orderInfo.getTicket(), ordersHistoryTotal0 + 1 == ordersHistoryTotal1);
            for (int i = 0; i < ordersHistoryTotal1; i++) {
                OrderInfo o = s.orderGet(i, SelectionType.SELECT_BY_POS, SelectionPool.MODE_HISTORY);
                System.out.println("Order #" + i + ": " + o);
            }
        } catch (MT4Exception err) {
            Assert.fail("" + err);
        }
        int d = 0;
    }

    //@Test
    public void testUDP() throws Exception {
        System.setProperty("jfx_use_udp", "true");
        try {
            testTickListener();

        } finally {
            System.setProperty("jfx_use_udp", "false");
        }
    }

    //    @Test
    public void AntonyDisconnectionTest() throws IOException, InterruptedException {
        Strategy stg = new Strategy() {
            @Override
            public long coordinationIntervalMillis() {
                return 1000;
            }

            @Override
            public void coordinate() {
                System.out.println("In coordinate: bal=" + accountBalance());
            }

            @Override
            public void deinit() {
                System.out.println("-- Deinit --");
                super.deinit();
            }

            @Override
            synchronized void init(String symbol, int period) throws ErrUnknownSymbol, IOException {
                super.init(symbol, period);
                System.out.println("~~-- Init --~~");
            }
        };

        stg.setReconnect(true);
        stg.setPositionListener(new PositionListener() {
            @Override
            public void onInit(PositionInfo initialPositionInfo) {
                System.out.println("PosLsnr: init");
            }

            @Override
            public void onChange(PositionInfo currentPositionInfo, PositionChangeInfo changes) {
                System.out.println("PosLsnr: " + changes);
            }
        }, 100, 500);
        stg.setBulkTickListener(new Strategy.BulkTickListener() {
            @Override
            public void onTicks(List<Tick> ticks, MT4 connection) {
                System.out.println("BulkTick: " + ticks);
            }
        });
        stg.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, true);
        stg.setAutoRefreshRates(false);
        stg.addTerminal(Strategy.TerminalType.ORDERS_WORKER).connect();
        //
        Thread.sleep(10000);
        //
        stg.close(true);
    }

    //    @Test
    public void AntonTest() throws IOException {
        Strategy strategy = new Strategy();
        strategy.addTickListener("EURUSD.", new Strategy.TickListener() {
            @Override
            public void onTick(TickInfo tickInfo, MT4 mt4) {
                System.out.println("New EURUSD. tick");
            }
        });
        strategy.connect("127.0.0.1", tsPort, new Broker("XTrade-MT4 Demo"), "1122871170", "QAZwsx123");
        strategy.close(true);

        Strategy strategy2 = new Strategy();
        strategy2.addTickListener("EURUSD.", new Strategy.TickListener() {
            @Override
            public void onTick(TickInfo tickInfo, MT4 mt4) {
                System.out.println("New EURUSD. tick");
            }
        });
        strategy2.addTickListener("GBPUSD.", new Strategy.TickListener() {
            @Override
            public void onTick(TickInfo tickInfo, MT4 mt4) {
                System.out.println("New GBPUSD. tick");
            }
        });
        strategy2.connect("127.0.0.1", tsPort, new Broker("XTrade-MT4 Demo"), "1122871170", "QAZwsx123");
        strategy2.close(true);
    }

    //@Test
    public void HyperTest() throws IOException, InterruptedException {
        final Strategy s = new Strategy();
        s.setBulkTickListener(new TestBugBulkTickListener());
        s.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD, TEST_SYMBOL);
        Thread.sleep(100000);
        s.close(true);
    }

    //    @Test
    public void ChrisMT5ConnectionTest() throws IOException, InterruptedException {
        Strategy s = new Strategy() {
            @Override
            public long coordinationIntervalMillis() {
                return 1000;
            }

            @Override
            public void coordinate() {
                System.out.println("In coordinate: bal=" + accountBalance());
            }

            @Override
            public void deinit() {
                System.out.println("-- Deinit --");
                super.deinit();
            }

            @Override
            synchronized void init(String symbol, int period) throws ErrUnknownSymbol, IOException {
                super.init(symbol, period);
                System.out.println("-- Init --");
            }
        };
        //
        String jfxActivationKey = DemoAccount.JFX_ACTIVATION_KEY;
        s.connect(tsHost, tsPort, new com.jfx.mt5.Broker("195.143.228.193"), "752953", "OCYKerou", "EURUSD");
        //
        Thread.sleep(10000);
        //
        s.close(true);
    }

//    @Test
    public void javaTimerTest() throws IOException {
        final Strategy mt4 = new Strategy();
        mt4.connect(tsHost, tsPort, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD);
        Timer t = new Timer("demo timer");
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder("@@ TICKS @@\r\n");
                ArrayList<Tick> ticks = mt4.getTicks();
                for (int i = 0; i < ticks.size(); i++) {
                    Tick tick = ticks.get(i);
                    sb.append('\t').append(tick.toString()).append("\r\n");
                }
                System.out.println(sb.toString());
            }
        }, 10000, 10000);
        //System.in.read();
        mt4.close(true);
    }


    @Test
    public void utf8Test() {
        byte[] b = new byte[]{
                (byte) 0x01,
                (byte) 0x61,
                (byte) 0x73,
                (byte) 0x63,
                (byte) 0x69,
                (byte) 0x69,
                (byte) 0x28,
                (byte) 0xd0,
                (byte) 0x90,
                (byte) 0xd0,
                (byte) 0xb1,
                (byte) 0xd1,
                (byte) 0x80,
                (byte) 0xd0,
                (byte) 0xb0,
                (byte) 0xd0,
                (byte) 0xba,
                (byte) 0xd0,
                (byte) 0xb0,
                (byte) 0xd0,
                (byte) 0xb4,
                (byte) 0xd0,
                (byte) 0xb0,
                (byte) 0xd0,
                (byte) 0xb1,
                (byte) 0xd1,
                (byte) 0x80,
                (byte) 0xd0,
                (byte) 0xb0,
                (byte) 0x29,
                (byte) 0x02
        };
        String s = new String(b, Charset.forName("UTF8"));
        System.out.println(s);
    }

    public class TestBugBulkTickListener implements Strategy.BulkTickListener {

        final String symbolToWatch = "EURUSD";

        long lastSymbolToWatchTimestamp = 0;

        @Override
        public void onTicks(List<Tick> ticks, MT4 connection) {
            int listSize = ticks.size();
            for (int i = 0; i < listSize; i++) {
                Tick tick = ticks.get(i);
                if (!tick.symbol.equals(symbolToWatch)) {
                    continue; // wait for our symbol
                }
                // this is our symbol
                long timestamp = tick.time.getTime();
                if (timestamp == 0) {
                    lastSymbolToWatchTimestamp = timestamp; // first time only
                    return;
                }
                // now measure delta time since last tick from this symbol
                int deltaMsecs = (int) (timestamp - lastSymbolToWatchTimestamp);
                System.out.println("Delta msecs is: " + deltaMsecs);
                // BUG: deltaMsecs is seen as an integral multiple of 1000
                lastSymbolToWatchTimestamp = timestamp; // for next time
                return;
            }
        }
    }
}