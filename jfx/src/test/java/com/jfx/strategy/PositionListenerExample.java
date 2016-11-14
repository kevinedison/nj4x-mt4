package com.jfx.strategy;

import com.jfx.*;
import com.jfx.net.JFXServer;
import com.jfx.strategy.*;

import java.io.IOException;

public class PositionListenerExample {

    String eurusd = "EURUSD";
    Strategy s;
    boolean ordersComplete;

    long ticket;

    public static void main(String[] args) throws InterruptedException {

//        System.setProperty("jfx_activation_key", DemoAccount.JFX_ACTIVATION_KEY);
//        System.setProperty("jfx_server_host", "192.168.0.244");
        System.setProperty("jfx_server_port", "7779");

        PositionListenerExample example = new PositionListenerExample();
        example.run();
        try {
            example.s.disconnect();
            System.out.println("Account disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        }
        JFXServer.stop();
    }

    private void run() throws InterruptedException {
        s = new Strategy() {
            {
                setPeriod(Timeframe.PERIOD_M1);
            }

            @Override
            public void coordinate() {
                try {
                    for (int p = 0; p < 10; p++) {
                        System.out.println("shift = " + p + " iFractals(lower)=" + iFractals("EURUSD", Timeframe.PERIOD_M1, BandsIndicatorLines.MODE_LOWER, p));
                        System.out.println("shift = " + p + " iFractals(upper)=" + iFractals("EURUSD", Timeframe.PERIOD_M1, BandsIndicatorLines.MODE_UPPER, p));
                    }
                } catch (ErrUnknownSymbol errUnknownSymbol) {
                    errUnknownSymbol.printStackTrace();
                }
            }

            @Override
            public long coordinationIntervalMillis() {
                return 5000;
            }
        };
        s.setPositionListener(new PositionListener() {
            @Override
            public void onInit(PositionInfo initialPositionInfo) {
                System.out.println("Initial Orders: " + initialPositionInfo.liveOrders());
            }

            @Override
            public void onChange(PositionInfo currentPositionInfo,
                                 PositionChangeInfo changes) {
                for (OrderInfo o : changes.getNewOrders()) {
                    System.out.println("NEW:      " + o);
                }
                for (OrderInfo o : changes.getClosedOrders()) {
                    System.out.println("CLOSED:   " + o);
                }

                if (s.orderSelect(0, SelectionType.SELECT_BY_POS,
                        SelectionPool.MODE_TRADES)) {
                    try {
                        long ticket = s.orderTicketNumber();
                        System.out.println("OrderTicketNumber() after OrderSelect() " + ticket);
                        OrderInfo orderInfo = s.orderGet(ticket, SelectionType.SELECT_BY_TICKET, SelectionPool.MODE_TRADES);
                        if (orderInfo != null) {
                            double openPrice = orderInfo.getOpenPrice();
                            System.out.println("Opened at price " + openPrice);
                        } else {
                            System.out.println("*** BUG ??? ***" +
                                    " s.orderGet(ticket, SelectionType.SELECT_BY_TICKET, SelectionPool.MODE_TRADES)" +
                                    " returns null");
                        }
                    } catch (ErrNoOrderSelected e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 10, 1000);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    ticket = s.orderSend(eurusd, TradeOperation.OP_BUY, 0.1,
                            s.marketInfo(eurusd, MarketInfo.MODE_ASK), 100, 0,
                            0, "asynch orders example", 0, null);
                    System.out.println("Order sent: ticket=" + ticket);
                    Thread.sleep(5000);
                } catch (InterruptedException ignore) {
                } catch (Exception e) {
                    System.out.println("Order sending error: " + e);
                }

                if (ticket > 0) {
                    try {
                        boolean b = s.orderClose(ticket, 0.1,
                                s.marketInfo(eurusd, MarketInfo.MODE_BID), 100,
                                0);
                        System.out.println("Order "
                                + (b ? "closed" : "not closed") + ": ticket="
                                + ticket);
                        Thread.sleep(2000);
                    } catch (InterruptedException ignore) {
                    } catch (Exception e) {
                        System.out.println("Order closing error: " + e);
                    } finally {
                        ordersComplete = true;
                    }
                }
            }
        };

        try {
            s.setPeriod(Timeframe.PERIOD_M1).connect("127.0.0.1", 7788, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD);
//            s.setPeriod(Timeframe.PERIOD_M5).connect("192.168.0.240", 7788, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD);
            System.out.println("Account connected");
        } catch (IOException e) {
        }

        task.run();

        Thread.sleep(100000);
    }
}

