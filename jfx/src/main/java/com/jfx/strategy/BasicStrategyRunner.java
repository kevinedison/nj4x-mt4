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

import com.jfx.MT4;
import com.jfx.io.Log4JUtil;
import com.jfx.net.Greeter;
import com.jfx.net.InprocessServer;
import com.jfx.net.UbbArgsMapper;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 *
 * User: roman
 * Date: 21/5/2009
 * Time: 14:55:10
 */
class BasicStrategyRunner implements StrategyRunner, Runnable {
    private static final Logger LOGGER = Logger.getLogger(BasicStrategyRunner.class);
    protected String clientName;
    //    protected static final StringBuilder IDLE = new StringBuilder("-1 IDLE");
    protected static final StringBuilder IDLE = new StringBuilder("-1 ");
    protected static final long _IDLE = -1;
    protected String clientId;
    public String dllVersion, mqlVersion;
    private long delay;
    private long executionFrequencyPeriod;

    public BasicStrategyRunner() {
    }

    public String getClientName() {
        return clientName;
    }

    Thread strategyThread;
    protected boolean isAlive;
    protected boolean isPendingClose = false;
    private String symbol;
    private int period;
    protected Strategy strategy;

    public synchronized boolean start(String clientName, boolean isLimited, Greeter greeter) {
        setFullClientName(clientName);
        //
        String id = clientId;
        //
        this.strategy = null;
        this.strategy = Strategy.getStrategy(id);
        if (this.strategy == null) {
            //noinspection EmptyCatchBlock
            if (id.matches("\\d+_\\d+")) {
                throw new RuntimeException("No such strategy waiting for connection: id=" + id);
            } else if (id.length() == 33 && id.matches("[\\dABCDEF]+") || id.length() > 33 && id.charAt(32) == '|' && id.substring(0, 32).matches("[\\dABCDEF]+")) {
                return false;
            } else if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                LOGGER.debug("ID [" + id + "] is accepted as manual strategy name");
            }
//            boolean isLong = id.indexOf('.') > 0;
            int paramsDelimiter = id.indexOf(':');
            String className;
            ArrayList<Double> args = new ArrayList<Double>();
            if (paramsDelimiter < 0) {
                className = id;
            } else {
                className = id.substring(0, paramsDelimiter);
                String[] _params = id.substring(paramsDelimiter + 1).split(",");
                for (int i = 0; i < _params.length; i++) {
                    String _p = _params[i];
                    args.add(new Double(_p));
                }
            }
            //
            try {
                Class cStrategyOrFactory = Class.forName(className);
                //
                if (Strategy.class.isAssignableFrom(cStrategyOrFactory)) {
                    ArrayList<Constructor> okConstructors = new ArrayList<Constructor>();
                    Constructor[] constructors = cStrategyOrFactory.getConstructors();
                    for (int i = 0; i < constructors.length; i++) {
                        Constructor c = constructors[i];
                        Class[] parameterTypes = c.getParameterTypes();
                        boolean ok = true;
                        for (int j = 0; j < parameterTypes.length; j++) {
                            Class parameterType = parameterTypes[j];
                            if (!parameterType.equals(Double.TYPE)) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            okConstructors.add(c);
                        }
                    }
                    //
                    if (okConstructors.size() == 0) {
                        throw new RuntimeException("Can not initialize strategy (no matching constructors found): " + id);
                    }
                    //
                    Collections.sort(okConstructors, new Comparator<Constructor>() {
                        public int compare(Constructor c1, Constructor c2) {
                            return c1.getParameterTypes().length - c2.getParameterTypes().length;
                        }
                    });
                    Constructor c = okConstructors.get(okConstructors.size() - 1);
                    Object[] params = new Object[c.getParameterTypes().length];
                    System.arraycopy(
                            args.toArray(),
                            0,
                            params,
                            0,
                            params.length
                    );
                    this.strategy = (Strategy) c.newInstance(params);
                    //
//                    this.strategy = (Strategy) cStrategyOrFactory.newInstance();
                } else {
                    Method mCreate = cStrategyOrFactory.getMethod("create");
                    Object s = mCreate.invoke(null);
                    if (s instanceof Strategy) {
                        this.strategy = ((Strategy) s);
                    } else {
                        LOGGER.error("Factory.create() returns not Stratey (but '" + s + "') instance (" + clientName + "): [" + className + "]");
                    }
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("Unrecognized strategy (" + clientName + "): [" + className + "]", e);
            } catch (IllegalAccessException e) {
                LOGGER.error("Error accessing strategy (" + clientName + "): [" + className + "]", e);
            } catch (InstantiationException e) {
                LOGGER.error("Error instantiating strategy (" + clientName + "): [" + className + "]", e);
            } catch (NoSuchMethodException e) {
                LOGGER.error("Erroneous Factory class /no 'create' method/ (" + clientName + "): [" + className + "]", e);
            } catch (InvocationTargetException e) {
                LOGGER.error("Error in Factory.create() method call (" + clientName + "): [" + className + "]", e);
            }
            //
            if (this.strategy != null) {
                this.strategy.setLimitedFunctionality(isLimited);
                this.strategy.setStrategyRunner(this);
                try {
                    greeter.sendToClient("START");
                } catch (IOException e) {
                    LOGGER.error("Error START-ing the connection", e);
                }
                preInit();
                startCoordination();
            } else {
                throw new RuntimeException("Can not initialize strategy (ClassNotFound ?): " + id);
            }
        } else {
            if (id.endsWith("1") || id.length() > 33 && id.charAt(32) == '|' && id.substring(0, 32).matches("[\\dABCDEF]+")) {
                this.strategy.setLimitedFunctionality(isLimited);
                this.strategy.setStrategyRunner(this);
                //noinspection SynchronizeOnNonFinalField
                synchronized (this.strategy) {
                    Strategy.removeStrategy(id);
                    try {
                        greeter.sendToClient("START");
                    } catch (IOException e) {
                        LOGGER.error("Error START-ing the connection", e);
                    }
                    this.strategy.notify();
                }
            } else {
                this.strategy.setLimitedFunctionality(isLimited);
                this.strategy.setOrdersProcessingChannel(this);
                //noinspection SynchronizeOnNonFinalField
                synchronized (this.strategy) {
                    Strategy.removeStrategy(id);
                    try {
                        greeter.sendToClient("START");
                    } catch (IOException e) {
                        LOGGER.error("Error START-ing the connection", e);
                    }
                    this.strategy.notify();
                }
            }
        }
        //
        return true;
    }

    protected boolean isOrdersProcessingChannel() {
        return Strategy.getStrategy(clientId) != null && !clientId.endsWith("1");
    }

    protected void setFullClientName(String clientName) {
        int u1ix = clientName.lastIndexOf('\u0001');
        if (u1ix > 0) {
            dllVersion = clientName.substring(u1ix + 1);
            clientName = clientName.substring(0, u1ix);
        } else {
            dllVersion = "<1.7.2";
        }
        this.clientName = clientName;
        isAlive = true;
        //
        // GBPUSD..60.strategy
        //
        int dotIx = clientName.indexOf('.');
        while (clientName.charAt(dotIx + 1) == '.') dotIx++;
        while (true) {
            try {
                symbol = clientName.substring(0, dotIx);
                period = Integer.parseInt(clientName.substring(symbol.length() + 1, clientName.indexOf('.', symbol.length() + 1)));
                break;
            } catch (NumberFormatException e) {
                dotIx = clientName.indexOf('.', dotIx + 1);
            }
        }
        //
        clientId = clientName.substring(clientName.indexOf('.', symbol.length() + 1) + 1);
        int atIx = clientId.indexOf('@');
        if (atIx >= 0) {
            int mqlvIx = clientId.indexOf(" MQLv");
            if (mqlvIx > 0) {
                mqlVersion = clientId.substring(mqlvIx + 5, atIx);
            } else {
                mqlVersion = "<1.7.2";
            }
            clientId = clientId.substring(atIx + 1);
        } else {
            mqlVersion = "unknown";
        }
    }

    public void preInit() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.strategy) {
            this.strategy.notify();
        }
        //
        try {
            this.strategy.init(symbol, period);
        } catch (Throwable e) {
            LOGGER.error("Initialization error (" + Thread.currentThread().getName() + ")", e);
            isAlive = false;
        }
    }

    public void close() {
        isAlive = false;
        try {
            if (this.strategy != null && this.strategy._ordersProcessingChannel() != this) {
                this.strategy.deinit();
            }
        } catch (Throwable e) {
            LOGGER.error("De-initialization error (" + Thread.currentThread().getName() + ")", e);
        }
     }

    public synchronized String getCommand() {
        while (this.command == null && isAlive) {
            //noinspection EmptyCatchBlock
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
        if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("getCommand: " + this.command);
        }
        if (!isAlive) {
            throw new RuntimeException("Is not alive");
        }
        return this.command;
    }

    private String command;
    private String result;

    public synchronized void resCommand(String s) {
        result = s;
        if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("resCommand: " + s);
        }
        this.command = null;
        this.notify();
        if (!isAlive) {
            throw new RuntimeException("Is not alive");
        }
    }

//    public synchronized String sendCommandGetResult(UniArgsMapper command) {
//        return null;
//    }
//
    public synchronized String sendCommandGetResult(UbbArgsMapper command) {
        return null;
    }

    public synchronized String sendCommandGetResult(StringBuilder command) {
        if (this.command != null) {
            throw new RuntimeException("Active command processing error.");
        }
        //
        this.command = command.toString();
        this.result = null;
        try {
            this.notify();
            this.wait();
        } catch (InterruptedException e) {
            return null;
        }
        //
        if (this.result == null) {
            throw new RuntimeException("No result recieved for command: " + this.command);
        }
        return result;
    }

    protected static long callsCount = 0;

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isConnected() {
        return isAlive();
    }

    public void startCoordination() {
        delay = Math.max(1, Math.min(strategy.coordinationIntervalMillis(), 1000));
        executionFrequencyPeriod = strategy.coordinationIntervalMillis();
/*
        InprocessServer.scheduleService.scheduleAtFixedRate(
                new Runnable() {
                    public void run() {
                        if (!isAlive) {
                            throw new RuntimeException("The end");
                        }
                        if (!isRunning()) {
                            InprocessServer.executorService.submit(BasicStrategyRunner.this);
                        }
                    }
                },
                delay,
                executionFrequencyPeriod,
                TimeUnit.MILLISECONDS
        );
*/
        scheduleNextCoordination(true);
    }

    protected boolean isRunning;
    protected final Object coordinationSynch = new Object();

    protected boolean isRunning() {
        synchronized (coordinationSynch) {
            if (isRunning) {
                return true;
            } else {
                try {
                    return false;
                } finally {
                    isRunning = true;
                }
            }
        }
    }

    boolean isTestingEverTested;
    boolean isTesting = false;

    public void run() {
        if (isRunning()) {
            return;
        }
        //
        try {
            do {
                callsCount++;
                if (callsCount % 5000 == 0 && LOGGER.isDebugEnabled()) {
                    LOGGER.debug("" + callsCount + " calls have been processed");
                }
                //
                try {
                    if (!isTestingEverTested) {
                        isTesting = strategy.isTesting();
                        isTestingEverTested = true;
                    }
                    if (strategy.isConnectedToTerminal()) {
                        strategy.coordinate();
                    } else {
                        strategy.DumpConnectionToTerminalStatus();
                    }
                } catch (Throwable e) {
                    //e.printStackTrace();
                    if (strategy.isAlive()) {
                        LOGGER.error("Coordination error (" + Thread.currentThread().getName() + ")", e);
                    }
                }
                //
                try {
                    switch (MT4.PROTO) {
                        case 0:
                            sendCommandGetResult(new StringBuilder("134 "));//verifies connection
                            sendCommandGetResult(IDLE);
                            StrategyRunner ordersProcessingChannel = strategy._ordersProcessingChannel();
                            if (ordersProcessingChannel != null) {
//                                ordersProcessingChannel.sendCommandGetResult(IDLE);//verifies connection
                                ordersProcessingChannel.sendCommandGetResult(new StringBuilder("134 "));//verifies connection
                            }
                            break;
//                        case 1:
//                            UniArgsMapper m1 = null;
//                            m1 = new UniArgsMapper("-1 ");
//                            sendCommandGetResult(m1);
//                            break;
                        case 2:
                            UbbArgsMapper m2 = null;
                            m2 = new UbbArgsMapper(-1);
                            sendCommandGetResult(m2);
                            StrategyRunner ordersProcessingChannel2 = strategy._ordersProcessingChannel();
                            if (ordersProcessingChannel2 != null) {
//                                ordersProcessingChannel2.sendCommandGetResult(m2);//verifies connection
                                ordersProcessingChannel2.sendCommandGetResult(new UbbArgsMapper(134));//verifies connection
                            }
                            break;
                    }
//                    sendCommandGetResult(IDLE);

//                    UniArgsMapper idle = new UniArgsMapper("-1");
//                    sendCommandGetResult(idle);

/*
                    UbbArgsMapper idle = new UbbArgsMapper("-1 ");
                    //
                    idle.addDouble(1.0);
                    idle.addDouble(15.0);
                    idle.addDouble(255.0);
                    idle.addDouble(4294967295.0);
                    idle.addDouble(0.00390625);
                    idle.addDouble(0.00000000023283064365386962890625);
                    idle.addDouble(1.234567890123E-300);
                    idle.addDouble(1.23456789012345E-150);
                    idle.addDouble(1.2345678901234565);
                    idle.addDouble(1.2345678901234567);
                    idle.addDouble(1.2345678901234569);
                    idle.addDouble(1.23456789012345678E+150);
                    idle.addDouble(1.234567890123456789E+300);

                    sendCommandGetResult(idle);
*/
                } catch (Exception e) {
                    //LOGGER.error("Sending IDLE cmd error (" + Thread.currentThread().getName() + ") this=" + this, e);
                    //e.printStackTrace();
                    close();
                    break;
                }
            } while (isTesting);
        } finally {
            synchronized (coordinationSynch) {
                isRunning = false;
            }
            scheduleNextCoordination(false);
        }
    }

    private void scheduleNextCoordination(final boolean first) {
        InprocessServer.scheduleService.schedule(new Runnable() {
            @Override
            public void run() {
                if (isAlive) {
                    //
                    if (first) {
                        try {
                            strategy.init(symbol, period, BasicStrategyRunner.this);
                        } catch (Throwable e) {
                            LOGGER.error("Initialization error (" + Thread.currentThread().getName() + ")", e);
                        }
                    }
                    InprocessServer.executorService.submit(BasicStrategyRunner.this);
                }
            }
        }, first ? delay : executionFrequencyPeriod, TimeUnit.MILLISECONDS);
    }
}
