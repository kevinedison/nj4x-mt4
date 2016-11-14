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

package com.jfx.net;

import com.jfx.MT4;
import com.jfx.io.Log4JUtil;
import com.jfx.strategy.MT4InprocessConnection;
import com.jfx.strategy.MT4TerminalConnection;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: roman
 * Date: 12.05.12
 * Time: 7:09
 * To change this template use File | Settings | File Templates.
 */
public class Greeter implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Greeter.class);
    //
    private final Socket socket;
    private final int id;
    private String clientName;
    private String boxId;
    private MT4TerminalConnection strategyRunner;
    private static boolean warnIsPrinted;
    private ScheduledFuture connFuture;

    public Greeter(Socket socket, int id) {
        this.socket = socket;
        this.id = id;
        //noinspection unchecked
    }

    private static String warnMessage(String boxId) {
        return "\n*************************************************************************"
                + "\nTrial period for your BOXID (" + boxId + ") has ended."
                + "\nIn order to continue using NJ4X API you need to buy NJ4X Personal license"
                + "\nat http://www.nj4x.com/pricing, register BOXID and get activation key"
                + "\n-------------------------------------------------------------------------"
                + "\n   API functionality is limited: all methods will respond in random delay"
                + "\n-------------------------------------------------------------------------"
                + "\n*************************************************************************\n";
        //
        /*return "\n*************************************************************************"
                + "\nAsk for 'jfx_activation_key' for your box: id=" + boxId
                + "\nJFX functionality is limited: API methods will respond in random delay."
                + "\n-------------------------------------------------------------------------"
                + "\nRegister your BOXID (" + boxId + ") for free 30-days trial period at"
                + "\n    http://www.nj4x.com/downloads."
                + "\n*************************************************************************\n";*/
    }

    private static ConcurrentHashMap<String, Greeter> _pendingConnections = new ConcurrentHashMap<>();
    public void run() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        String line;
        boolean resStarted = false;
        char firstChar = 0;
        while (true) {
            try {
                int b = socket.getInputStream().read();
                if (b < 0) {
                    if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                        LOGGER.info("Client " + clientName + " disconnected");
                    }
                    return;
                }
                if (firstChar == 0) {
                    firstChar = (char) b;
                }
//                if (b == '\n' || b == '\r') {
                if (b == MT4.ARG_END || (firstChar != MT4.ARG_BEGIN && (b == '\n' || b == '\r'))) {
                    resStarted = false;
                    line = sb.toString();
                    sb.setLength(0);
                    if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("GOT FROM " + clientName + ": " + line);
                    }
                    //
                    if (line.startsWith("HELLO")) {
                        clientName = line.substring(5);
                        if (socket instanceof InprocessSocket) {
                            strategyRunner = new MT4InprocessConnection(clientName);
                        } else {
                            strategyRunner = new MT4TerminalConnection(clientName);
                            strategyRunner.setNoMultilineConnection(firstChar != MT4.ARG_BEGIN);
                        }
                        strategyRunner.setSocket(socket);
                        if (id != 0 && !strategyRunner.isNoUDPAllowed()) {
                            sendToClient("HELLO " + id + " " + MT4.PROTO);
                            try {
                                strategyRunner.setId(id);
                            } catch (Exception e) {
                                e.printStackTrace();
                                socket.close();
                                return;
                            }
                        } else {
                            sendToClient("HELLO 0 " + MT4.PROTO);
                        }
                        //
                    } else if (line.startsWith("BOX")) {
                        boxId = line.substring(3);
//                            if (LOGGER.isInfoEnabled()) {
//                                LOGGER.info("Client " + clientName + ": box=" + boxId);
//                            }
                        //
                        String activationKey = boxId.equals("5")
                                ? System.getProperty("nj4x_mt5_activation_key", "5551234")
                                : System.getProperty("jfx_activation_key", System.getProperty("nj4x_mt4_activation_key", System.getProperty("nj4x_activation_key", "943289279")));
                        sendToClient(activationKey + " ");
                        //
                    } else if (line.startsWith("XOB")) {
                        final String res = line.substring(3);
                        //
                        try {
                            int udpPort = 0;
                            if (res.startsWith("OK") && res.length() > 2) {
                                udpPort = Integer.parseInt(res.substring(2));
                            } else if (res.startsWith("NOK") && res.length() > 3) {
                                boxId = res.substring(3);
                            }
                            if (udpPort > 0) {
                                strategyRunner.setUDPPort(udpPort);
                            }
                            final boolean isLimited = !res.startsWith("OK");
                            if (strategyRunner.start(clientName, isLimited, this)) {
                                if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                                    LOGGER.info("Client " + clientName + " connected.");
                                }
                            } else {
                                _pendingConnections.put(clientName, this); // overrides existing
                                connFuture = InprocessServer.scheduleService.scheduleAtFixedRate(
                                        new Runnable() {
                                            public void run() {
                                                Greeter greeter = _pendingConnections.get(clientName);
                                                if (Greeter.this == greeter) {
                                                    try {
                                                        sendToClient("WAIT");
                                                        if (strategyRunner.start(clientName, isLimited, Greeter.this)) {
//                                                            sendToClient("START");
                                                            if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                                                                LOGGER.info("Client " + clientName + " connected.");
                                                            }
                                                        } else {
                                                            return; // wait more
                                                        }
                                                    } catch (IOException e) {
                                                        if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                                                            LOGGER.info("Client " + clientName + " disconnected (" + e + ")");
                                                        }
                                                    }
                                                }
                                                connFuture.cancel(false);
                                                _pendingConnections.remove(clientName, Greeter.this);
                                            }
                                        },
                                        1, 2, TimeUnit.SECONDS
                                );
                                synchronized (InprocessServer.class) {
                                    if (!warnIsPrinted && isLimited) {
                                        InprocessServer.scheduleService.scheduleAtFixedRate(
                                                new Runnable() {
                                                    public void run() {
                                                        if (Log4JUtil.isConfigured()) {
                                                            LOGGER.warn(warnMessage(boxId));
                                                        } else {
                                                            System.out.println(warnMessage(boxId));
                                                        }
                                                    }
                                                },
                                                5, 60, TimeUnit.SECONDS
                                        );
                                        //
                                        warnIsPrinted = true;
                                    }
                                }
                            }
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            socket.close();
                        }
                    } else {
                        LOGGER.error("Unrecognized client (" + clientName + ") request: [" + line + "]");
                    }
                } else {
                    if (resStarted || firstChar != MT4.ARG_BEGIN) {
                        sb.append((char) b);
                    } else {
                        sb2.append((char) b);
                        resStarted = (b == MT4.ARG_BEGIN);
                    }
                }
            } catch (IOException e) {
                if (Log4JUtil.isConfigured() && LOGGER.isInfoEnabled()) {
                    LOGGER.info("Client " + clientName + " disconnected (" + e + ")");
                }
                break;
            }
        }
    }

    public void sendToClient(String line) throws IOException {
//        socket.getOutputStream().write((line + '\n').getBytes());
//        System.out.println(line);
        //
        OutputStream os = socket.getOutputStream();
        os.write(("" + MT4.ARG_BEGIN + line + MT4.ARG_END).getBytes(Config.CHARSET));
        os.flush();
        //
        if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
            LOGGER.debug("SENT TO " + clientName + ": " + line);
        }
    }
}
