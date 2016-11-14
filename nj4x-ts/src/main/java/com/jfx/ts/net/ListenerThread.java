package com.jfx.ts.net;

import com.jfx.ts.io.BoxUtils;
import com.jfx.ts.io.Log4JUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * todo: comments
 * User: roman
 * Date: 05/08/2014
 * Time: 21:59
 */
class ListenerThread extends Thread {
    private TS ts;
    ServerSocket serverSocket;

    ListenerThread(TS ts) throws IOException {
        super("Listener");
        setDaemon(true);
        this.ts = ts;
        this.serverSocket = new ServerSocket(ts.getPort());
    }

    public void run() {
        String hello = "NJ4X " + TS.NJ4X + (BoxUtils.BOXID == 0 ? "" : " Personal") + " Terminal Server, port=" + serverSocket.getLocalPort();
        if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
            TS.LOGGER.info("Terms dir: " + TS.getTermDir());
            TS.LOGGER.info("  TS Home: " + TS.JFX_HOME);
            TS.LOGGER.info(hello);
        } else {
            System.out.println(hello);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            TS.LOGGER.error("ListenerThread/init", e);
            return;
        }
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                final Socket socket = serverSocket.accept();
                //
                socket.setTcpNoDelay(true);
                ClientWorkerThread clientWorkerThread = new ClientWorkerThread(ts, socket);
                TerminalServer.CACHED_THREAD_POOL.submit(clientWorkerThread);
            } catch (Throwable e) {
                TS.LOGGER.error("ListenerThread/work", e);
                try {
                    serverSocket.close();
                } catch (Throwable ignore) {
                }
                try {
                    this.serverSocket = new ServerSocket(Integer.parseInt(ts.getPortAsString()));
                } catch (Throwable e1) {
                    TS.LOGGER.error("ListenerThread/reconnect", e1);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }
    }
}
