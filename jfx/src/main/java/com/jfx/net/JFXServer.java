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

import com.jfx.io.Log4JUtil;
import com.jfx.io.ResourceReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JFXServer accepts MT4 Terminals connections, one should define system properties
 * 'jfx_server_host' and 'jfx_server_port' for JFXServer binding (127.0.0.1:7777 is used by default).
 */
@SuppressWarnings({"unused", "rawtypes"})
public class JFXServer {

    private static JFXServer instance;
    private final ListenerThread listenerThread;

    private String bindHost;
    private int bindPort;
    private DatagramSocket datagramSocket;

    public static synchronized JFXServer getInstance() {
        return instance == null
                ? (instance = getInstance(System.getProperty("jfx_server_host", "127.0.0.1"), Integer.parseInt(System.getProperty("nj4x_server_port", System.getProperty("jfx_server_port", "7777")))))
                : instance;
    }

    public static synchronized JFXServer getInstance(String bindHost, int bindPort) {
        String key = bindHost + ':' + bindPort;
        JFXServer jfxServer = instances.get(key);
        if (jfxServer == null) {
            instances.put(key, jfxServer = new JFXServer(bindHost, bindPort));
        }
        return jfxServer;
    }

    private ServerSocket serverSocket;

    private static final String JFX_HOME;
    private static final boolean NO_LOG4J;

    static {
        JFX_HOME = System.getProperty("home", "./jfx");
        System.setProperty("home", JFX_HOME);
        NO_LOG4J = System.getProperty("jfx.no_log4j", "false").equals("true");
    }

    private static final String JFX_HOME_CONFIG = JFX_HOME + "/config";
    private static final String JFX_HOME_LOG = JFX_HOME + "/log";
    public static final String LOGGING_CONFIG_XML = JFX_HOME_CONFIG + "/logging.xml";

    static {
        try {
            if (!NO_LOG4J && !Log4JUtil.isConfigured()) {
                new File(JFX_HOME_CONFIG).mkdirs();
                new File(JFX_HOME_LOG).mkdirs();
                //
                if (!new File(LOGGING_CONFIG_XML).exists()) {
                    try {
                        FileOutputStream fos = new FileOutputStream(LOGGING_CONFIG_XML);
                        fos.write(ResourceReader.getClassResourceReader(JFXServer.class, true).getProperty("logging.xml")
                                .getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //
                Log4JUtil.callStaticMethod("org.apache.log4j.xml.DOMConfigurator", "configureAndWatch", LOGGING_CONFIG_XML);
            }
        } catch (Exception ignore) {
        }
    }

    public static void main(String[] args) throws IOException {
        new JFXServer("localhost", 7777);
    }

    public String getBindHost() {
        return bindHost.equals("*") ? "127.0.0.1" : bindHost;
    }

    public int getBindPort() {
        return bindPort;
    }

    private static ConcurrentHashMap<String, JFXServer> instances = new ConcurrentHashMap<>();
    public JFXServer(String host, int port) {
        this.bindHost = host;
        this.bindPort = port;
        //
        try {
            if (bindHost.equals("*") || !System.getProperty("jfx_server_host_only", "false").equals("true")) {
                datagramSocket = new DatagramSocket(bindPort);
                serverSocket = new ServerSocket(bindPort);
            } else {
                InetAddress address = getInetAddress();
                datagramSocket = new DatagramSocket(bindPort, address);
                serverSocket = new ServerSocket(bindPort, -1, address);
            }
        } catch (BindException e) {
            if (e.getMessage() != null && e.getMessage().contains("Address already in use") && bindPort < 65534) {
                throw new RuntimeException("Address already in use " + bindHost + ":" + bindPort, e);
            } else {
                throw new RuntimeException("Error binding at " + bindHost + ":" + bindPort, e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error binding at " + bindHost + ":" + bindPort, e);
        }
        listenerThread = new ListenerThread();
        listenerThread.start();
    }

    private InetAddress getInetAddress() {
        InetAddress address;
        try {
            address = InetAddress.getByName(bindHost);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        return address;
    }

    boolean warnIsPrinted;

    private int incomingConnectionID = 777777;

    public synchronized int getNextIncomingConnectionID() {
        return ++incomingConnectionID;
    }

    public synchronized static void stop() {
        try {
            InprocessServer.scheduleService.shutdown();
            InprocessServer.executorService.shutdown();
            for (JFXServer jfxServer : instances.values()) {
                if (jfxServer != null) {
                    jfxServer.listenerThread.isStop = true;
                    DatagramSocket socket = jfxServer.datagramSocket;
                    if (socket != null) {
                        socket.close();
                    }
                    ServerSocket socket1 = jfxServer.serverSocket;
                    if (socket1 != null) {
                        socket1.close();
                    }
                }
            }
            instances.clear();
            instance = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static long udpNo = 0;

    private static final boolean UDP_TRIAL = false;//System.getProperty("jfx_use_udp", "false").equals("true");


    private class ListenerThread extends Thread {

        public ListenerThread() {
            super("JFXServer ListenerThread");
            setDaemon(true);
        }

        public boolean isStop;

        public void run() {
            //noinspection InfiniteLoopStatement
            while (!isStop) {
                try {
                    final Socket socket = serverSocket.accept();
                    socket.setKeepAlive(true);
                    socket.setTcpNoDelay(true);
                    socket.setReuseAddress(true);
                    socket.setTrafficClass(0x10);
                    //
                    InprocessServer.executorService.submit(new Greeter(socket, UDP_TRIAL ? getNextIncomingConnectionID() : 0));
                } catch (IOException e) {
                    if (!isStop && !InprocessServer.scheduleService.isShutdown()) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
