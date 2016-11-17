package com.jfx.ts.net;


import com.jfx.io.CachedThreadFactory;
import com.jfx.ts.io.LibrariesUtil;
import com.jfx.ts.io.PSUtils;
import com.jfx.ts.net.ws.TsWS;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Terminal Server runner.
 * User: roman
 * Date: 06/02/14
 * Time: 12:46
 * 这个是主函数，是入口函数
 */
public class TerminalServer {
    /**
     * The constant MINIMUM_CLIENT_VERSION.
     */
    public static final String MINIMUM_CLIENT_VERSION = "2.4.0";
    /**
     * The constant CACHED_THREAD_POOL.
     */
    public static final ExecutorService CACHED_THREAD_POOL = Executors.newCachedThreadPool(new CachedThreadFactory("NJ4X Services #"));
    /**
     * The constant AVAILABLE_PROCESSORS.
     */
//    public static final ExecutorService CACHED_THREAD_POOL = EfficientThreadPoolExecutor.get(16, 128, 10, TimeUnit.SECONDS, 256, "NJ4X Services #");
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    /**
     * The constant MAX_TERMINAL_STARTUP_THREADS.
     */
    public static int MAX_TERMINAL_STARTUP_THREADS;// = Integer.parseInt(System.getProperty("max_terminal_connection_threads", "" + (AVAILABLE_PROCESSORS > 3 ? Math.max(AVAILABLE_PROCESSORS - 2, AVAILABLE_PROCESSORS / 2) : AVAILABLE_PROCESSORS)));
    /**
     * The constant FIXED_THREAD_POOL.
     */
    public static ExecutorService FIXED_THREAD_POOL;/* = Executors.newFixedThreadPool(MAX_TERMINAL_STARTUP_THREADS,
            new ThreadFactory() {
                private volatile int cnt = 0;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "NJ4X Fixed (" + MAX_TERMINAL_STARTUP_THREADS + ") Pool (#" + (++cnt) + ")");
                }
            });*/
    /**
     * The constant IS_DEPLOY_EA_WS.
     */
    public static boolean IS_DEPLOY_EA_WS;
    private static TS ts;
    private static HttpServer httpServer;
    private static boolean eaWsDeployed = false;

    static {
        System.setProperty("org.jboss.netty.epollBugWorkaround", "true");
        System.setProperty("io.netty.epollBugWorkaround", "true");
    }

    /**
     * Stop service.
     *
     * @param args the args
     *
     * @exception IOException the io exception
     */
    public static void stopService(String[] args) throws IOException {
/*
        if (args.length % 2 == 0) {
            for (int i = 0; i < args.length; i++) {
                String pn = args[i++];
                String pv = args[i];
                System.setProperty(pn, pv);
            }
        }
        final String port = System.getProperty("port", "7788");
        TerminalClient terminalClient = new TerminalClient("127.0.0.1", Integer.parseInt(port));
        System.out.println("Connected to " + terminalClient.getTSName());
        System.out.println("Stop service res: " + terminalClient.ask(ClientWorkerThread.STOPSERVICE));
        terminalClient.close();
*/
        TS.LOGGER.info("Terminal Server Stop Requested");
        System.out.println("Service terminated");
        System.exit(0);
    }

    /**
     * 客户端的入口函数
     *
     * @param args the input arguments
     *
     * @exception Exception the exception
     */
    public static void main(String[] args) throws Exception {
        try {
            if (args.length % 2 == 0) {
                for (int i = 0; i < args.length; i++) {
                    String pn = args[i++];
                    String pv = args[i];
                    System.setProperty(pn, pv);   //指定系统的全局变量，但是不知道是干嘛的
                }
            }
            //
            // sun.java.command=C:\nj4x\bin\nj4x-ts-2.4.9.exe use_mstsc true port 1054
            //
            boolean asAdministrator = false;
            try {
                LibrariesUtil.initEmbeddedLibraries();   //加载PSUtils_x64.dll这个库，具体这个库是干嘛的还不知道
                asAdministrator = PSUtils.asAdministrator();   //判断是不是以管理员身份运行的，其目的还不知道
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // *********************************************************************
            //
            //
            if (TS.P_USE_MSTSC && TS.canNotUseMstsc(args)) {       //貌似是远程的问题
                throw new RuntimeException("Can not run in MSTSC mode.");
            }
            //
            //
            // *********************************************************************
            //
            //最大连接数线程数
            MAX_TERMINAL_STARTUP_THREADS = Integer.parseInt(System.getProperty("max_terminal_connection_threads", "" +
                    (AVAILABLE_PROCESSORS >= 24 ? AVAILABLE_PROCESSORS / 2
                            : (AVAILABLE_PROCESSORS >= 12 ? AVAILABLE_PROCESSORS / 3
                            : (AVAILABLE_PROCESSORS > 3 ? AVAILABLE_PROCESSORS - 2
                            : AVAILABLE_PROCESSORS)))));
            //构建一个线程池
            //newFixedThreadPool内部有个任务队列，假设线程池里有3个线程，提交了5个任务，那么后两个任务就放在任务队列了，即使前3个任务sleep或者堵塞了，也不会执行后两个任务，除非前三个任务有执行完的
            FIXED_THREAD_POOL = Executors.newFixedThreadPool(MAX_TERMINAL_STARTUP_THREADS,
                    new ThreadFactory() {
                        private volatile int cnt = 0;

                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, "NJ4X Fixed (" + MAX_TERMINAL_STARTUP_THREADS + ") Pool (#" + (++cnt) + ")");
                        }
                    });
            //
            IS_DEPLOY_EA_WS = System.getProperty("deploy_EA_WS", "false").equals("true");
            final String port = System.getProperty("port", "7788");
            if (port == null) {
                //noinspection SpellCheckingInspection
                System.err.println("Wrong Parameters: -Dport");
                return;
            }
            //
            deploy(port);
            //
//            startCommandLineListener();
        } catch (UnsatisfiedLinkError e) {
            TS.LOGGER.error("Startup Error", e);
            displayVCRedistributableDownloadInfo();
            System.exit(-1);
        } catch (Throwable e) {
            TS.LOGGER.error("Startup Error", e);
            displayUnexpectedError(e);

            System.exit(-1);
        }
        //
        Thread.sleep(10000);
    }

    /**
     * Display unexpected error.
     *
     * @param e the e
     */
    public static void displayUnexpectedError(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        JEditorPane pane = new JEditorPane("text/html",
                "An error occurred: <b>" + e + "</b><br>" + sw.toString().replace("\n", "<br>"));
        pane.setEditable(false);
        pane.setBackground(Color.YELLOW);
        JOptionPane.showMessageDialog(null,
                pane,
                "Unexpected Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static void displayVCRedistributableDownloadInfo() {
        JEditorPane pane = new JEditorPane("text/html",
                "NJ4X TS requires latest vcredist_x86.exe (v120) and vcredist_x64.exe (v120) " +
                        "to be installed in the system." +
                        "<br>Please download those packages at <b>http://nj4x.com/downloads</b> " +
                        "<br>or at microsoft.com - <b>http://www.microsoft.com/en-us/download/details.aspx?id=40784</b>");
        pane.setEditable(false);
        pane.setBackground(Color.YELLOW);
        JOptionPane.showMessageDialog(null,
                pane,
                "Initialization Error",
                JOptionPane.WARNING_MESSAGE
        );
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("http://www.microsoft.com/en-us/download/details.aspx?id=40784"));
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 部署函数
     *
     * @param _port the port
     *
     * @exception Exception the exception
     */
    public static void deploy(String _port) throws Exception {
        Logger logger = TS.LOGGER;
        //
        logger.info("--");
        logger.info("--");
        logger.info("--");
        logger.info("-- TS " + TS.NJ4X + " STARTUP --");
        logger.info("-- " + TS.NJ4X_UUID + " --");
        logger.info("--");
        logger.info("--");
        logger.info("--");
        logger.info("-- System properties --");
        for (Map.Entry e : new TreeMap<>(System.getProperties()).entrySet()) {
            logger.info("" + e.getKey() + "=" + e.getValue());
        }
        logger.info("-- Environment --");
        for (Map.Entry e : new TreeMap<>(System.getenv()).entrySet()) {
            logger.info("" + e.getKey() + "=" + e.getValue());
        }
        logger.info("-- Deployment --");
        //
/*
        boolean debug = logger.isDebugEnabled();
        System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", !debug ? "true" : "false");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", debug ? "true" : "false");
        if (debug) {
            System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
            System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
            System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
            System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        }
*/
        //
        ts = new TS(_port);
        //
        String host = System.getProperty("ws_host", "0.0.0.0");  //目前还不知道ws是干嘛的
        int port = (System.getProperty("ws_port") == null ? 1 : 0) + Integer.parseInt(System.getProperty("ws_port", ts.getPortAsString()));
        System.setProperty("nj4x_server_host", "127.0.0.1");
        System.setProperty("nj4x_server_port", String.valueOf(Integer.parseInt(_port) + 4));
        //
        boolean deployWS = !TS.P_GUI_ONLY && System.getProperty("deployWS", "true").equals("true");
        if (deployWS) {
//            httpServer = HttpServer.create(new InetSocketAddress(port), -1); // consumes 12% CPU (1 logical processor)
            //
//            if (logger.isDebugEnabled()) {
//                String maxReqTime = System.getProperty("sun.net.httpserver.maxReqTime");
//                String maxRespTime = System.getProperty("sun.net.httpserver.maxRspTime");
//                logger.debug("sun.net.httpserver: maxReqTime=" + maxReqTime + " maxRespTime=" + maxRespTime);
//            }
            //
            {
                TsWS ws = new TsWS(ts);
                String address = "http://" + host + ":" + port + "/nj4x/ts";
                //
                Endpoint endpoint = Endpoint.publish(address, ws);
//                Endpoint endpoint = Endpoint.create(ws/*, new LoggingFeature()*/);
//                Endpoint endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, ws);
                List<Handler> handlerChain = endpoint.getBinding().getHandlerChain();
                handlerChain.add(new SOAPLoggingHandler());
                endpoint.getBinding().setHandlerChain(handlerChain);
//                endpoint.publish(httpServer.createContext("/nj4x/ts"));
//                endpoint.publish(address);
                //
                Map<String, Object> properties = endpoint.getProperties();
            }
            //
//            ExecutorService executor = CACHED_THREAD_POOL;
//            httpServer.setExecutor(executor);
//            httpServer.start();
            //
            String httpPrefix = /*deployWS ? "https://" : */ "http://";
//            String host = "127.0.0.1";
            try {
                Class<?> cTesterWS = Class.forName("com.jfx.ts.net.ws.tests.TesterWS");
                Constructor<?> cTesterWSConstructor = cTesterWS.getConstructor(TS.class);
                //
                String address = "http://127.0.0.1:" + port + "/nj4x/tester";
                Endpoint endpoint = Endpoint.publish(address, cTesterWSConstructor.newInstance(ts));
//                Endpoint endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, cTesterWSConstructor.newInstance(ts));
                List<Handler> handlerChain = endpoint.getBinding().getHandlerChain();
                handlerChain.add(new SOAPLoggingHandler());
                endpoint.getBinding().setHandlerChain(handlerChain);
//                endpoint.publish(httpServer.createContext("/nj4x/tester"));
                logger.info("    " + httpPrefix + host + ":" + port + "/nj4x/tester?wsdl");
            } catch (Exception ignore) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skip deployment: " + ignore.getMessage());
                }
            }
            //
            deployEaWs(true);
        }
    }

    /**
     * Deploy ea ws boolean.
     *
     * @param deploy the deploy
     *
     * @return the boolean
     */
    public static boolean deployEaWs(boolean deploy) {
        if (deploy) {
            if (IS_DEPLOY_EA_WS && !eaWsDeployed) {
                Logger logger = TS.LOGGER;
                try {
                    boolean isSSL = System.getProperty("use_ssl", "false").equals("true");
                    String httpPrefix = isSSL ? "https://" : "http://";
                    String host = "127.0.0.1";
                    int port = (System.getProperty("ws_port") == null ? 1 : 0) + Integer.parseInt(System.getProperty("ws_port", System.getProperty("port", "7788")));
                    //
                    Class<?> cTesterWS = Class.forName("com.jfx.ts.net.ws.experts.EaWS");
                    Constructor<?> cTesterWSConstructor = cTesterWS.getConstructor(TS.class);
                    String address = "http://127.0.0.1:" + port + "/nj4x/experts";
                    Endpoint endpoint = Endpoint.publish(address, cTesterWSConstructor.newInstance(ts));
//                    Endpoint endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, cTesterWSConstructor.newInstance(ts));
                    List<Handler> handlerChain = endpoint.getBinding().getHandlerChain();
                    handlerChain.add(new SOAPLoggingHandler());
                    endpoint.getBinding().setHandlerChain(handlerChain);
//                    endpoint.publish(httpServer.createContext("/nj4x/experts"));
                    eaWsDeployed = true;
                    logger.info("    " + httpPrefix + host + ":" + port + "/nj4x/experts?wsdl");
                    return true;
                } catch (Exception ignore) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skip deployment: " + ignore.getMessage());
                    }
                }
            }
        } else {
            return !eaWsDeployed;
        }
        return false;
    }

    private static class SOAPLoggingHandler implements SOAPHandler<SOAPMessageContext> {
        /**
         * The Logger.
         */
        Logger logger = TS.LOGGER;

        public Set<QName> getHeaders() {
            return null;
        }

        public boolean handleMessage(SOAPMessageContext smc) {
            log(smc);
            return true;
        }

        public boolean handleFault(SOAPMessageContext smc) {
            log(smc);
            return true;
        }

        public void close(MessageContext messageContext) {
        }

        private void log(SOAPMessageContext smc) {
            if (logger.isDebugEnabled()) {
                StringBuilder msg = new StringBuilder();
/*
                Boolean outboundProperty = (Boolean)
                        smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
                if (outboundProperty) {
//                    out.println("\nOutbound message:");
                    msg.append("OUT> ");
                } else {
//                    out.println("\nInbound message:");
                    msg.append(" IN> ");
                }
*/
                //
                try {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    smc.getMessage().writeTo(new PrintStream(os));
                    msg.append(getMsgBody(os));
//                    msg.append(smc.getMessage().getSOAPBody().getValue());
                } catch (Exception e) {
                    logger.error("Dump SOAP msg error", e);
                }
                //
                logger.debug(msg.toString());
            }
        }

        private String trimXMLNS(String msg) {
            int ix1 = msg.indexOf(" xmlns");
            while (ix1 > 0) {
                int ix2 = msg.indexOf('"', ix1);
                if (ix2 > 0) {
                    int ix3 = msg.indexOf('"', ix2 + 1);
                    msg = msg.substring(0, ix1) + msg.substring(ix3 + 1);
                    ix1 = msg.indexOf(" xmlns");
                } else {
                    ix1 = -1;
                }
            }
            return msg;
        }

        private String trimNS(String msg) {
            int ix1 = msg.indexOf('<');
            while (ix1 >= 0) {
                int ix2 = msg.indexOf('>', ix1);
                if (ix2 > 0) {
                    int ix3 = msg.indexOf(':', ix1 + 1);
                    if (ix3 > 0 && ix3 < ix2) {
                        msg = msg.substring(0, ix1 + msg.charAt(ix1 + 1) == '/' ? 2 : 1) + msg.substring(ix3 + 1);
                        ix1 = msg.indexOf('<', ix1 + 1);
                        continue;
                    }
                }
                ix1 = -1;
            }
            return msg;
        }

        private String getMsgBody(ByteArrayOutputStream os) {
            String s = os.toString();
            String sLower = s.toLowerCase();
            int ix = sLower.indexOf("<s:body");
            int ix2 = sLower.indexOf("</s:body>");
            String msgTrimmed = s;
            try {
                String s1 = (ix >= 0 && ix2 >= 0 ? s.substring(s.indexOf('>', ix) + 1, ix2) : s);
                msgTrimmed = trimNS(trimXMLNS(s1));
            } catch (Exception e) {
                TS.LOGGER.error("[" + s + "]", e);
            }
            //
            ix = msgTrimmed.indexOf("<password>");
            if (ix >= 0) {
                ix2 = msgTrimmed.indexOf("</password>", ix);
                if (ix2 > 0) {
                    msgTrimmed = msgTrimmed.substring(0, ix) + msgTrimmed.substring(ix2);
                }
            }
            return msgTrimmed;
        }
    }

}
