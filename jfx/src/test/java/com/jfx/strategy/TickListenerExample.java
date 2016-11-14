package com.jfx.strategy;
import com.jfx.*;

import java.io.IOException;

public class TickListenerExample extends Strategy {
    public TickListenerExample() {
    }

    @Override
    public synchronized void init(String symbol, int period, StrategyRunner strategyRunner) throws ErrUnknownSymbol, IOException {
        super.init(symbol, period, strategyRunner);
        System.out.println("Connected, " + getVersion());
        System.out.println("accountServer=" + accountServer());
        setAutoRefreshRates(false);
    }

    public void coordinate() {

/*
        try {
            System.out.println("BID: " + marketInfo("EURUSDpro", MarketInfo.MODE_BID));
        } catch (ErrUnknownSymbol errUnknownSymbol) {
            errUnknownSymbol.printStackTrace();
        }
*/
    }

    @Override
    public long coordinationIntervalMillis() {
        return 10;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        connectToMT4Server();
        //
        System.out.println("30-seconds demo...");
        Thread.sleep(300000);
        //
        System.out.println("Exit.");
        System.exit(0);
    }

    private static void connectToMT4Server() throws IOException {
        final TickListenerExample jfxExample = new TickListenerExample();
        //
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    System.out.println("MT4 Terminal client disconnect...");
                    jfxExample.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        jfxExample.setReconnect(true);
        String terminalServerIpAddress = "127.0.0.1";
        jfxExample.addTickListener("EURUSDpro", new TickListener() {
            @Override
            public void onTick(TickInfo tick, MT4 connection) {
                System.out.println("" + System.currentTimeMillis() + "> " + tick + " " + connection.accountBalance());
            }
        });
        jfxExample.connect(terminalServerIpAddress, 7788, DemoAccount.MT_4_SERVER, DemoAccount.MT_4_USER, DemoAccount.MT_4_PASSWORD);
    }
}