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

import com.jfx.Version;
import com.jfx.io.Log4JUtil;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * User: roman
 * Date: 6/9/2009
 * Time: 14:45:19
 */
public class TerminalClient {
    private static final Logger LOGGER = Logger.getLogger(TerminalClient.class);

    private String myName;
    String terminalHost;
    int terminalPort;
    TerminalClient.ConnectionWorkerThread connectionWorkerThread;
    public String tsVersion;

    public TerminalClient(String terminalHost, int terminalPort) throws IOException {
        this("", terminalHost, terminalPort);
    }

    public TerminalClient(String myName, String terminalHost, int terminalPort) throws IOException {
        this.myName = myName;
        this.terminalHost = terminalHost;
        this.terminalPort = terminalPort;
        connect();
    }

    private void connect() throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(terminalHost, terminalPort);
        } catch (IOException e) {
            System.err.println("Could not establish connection to terminal server: " + terminalHost + ':' + terminalPort);
            throw e;
        }
        connectionWorkerThread = new ConnectionWorkerThread(socket);
        connectionWorkerThread.start();
        String s = ask("HELLO" + myName + "\u0001" + Version.NJ4X);
        int ix = s.lastIndexOf('\u0001');
        if (ix > 0) {
            tsVersion = s.substring(ix + 1);
        } else {
            tsVersion = "<1.7.2";
        }
        //
//System.out.println("---> peerName=" + s);
        connectionWorkerThread.peerName = s.substring(5, ix > 0 ? ix : s.length());
    }

    public String getTSName() {
//System.out.println("---> getTSName=" + (connectionWorkerThread == null ? null : connectionWorkerThread.peerName));
        return connectionWorkerThread == null ? null : connectionWorkerThread.peerName;
    }

    public String ask(String msg) throws IOException {
        return connectionWorkerThread.ask(msg);
    }

    public void close() throws IOException {
        connectionWorkerThread.socket.close();
    }

    /**
     * Returns a list of available SRV configuration files.
     * @return list of SRV file names
     * @throws IOException thrown in case of communication error
     */
    public ArrayList<String> getAvailableSRVFiles() throws IOException {
        ArrayList<String> res = new ArrayList<String>();
        String ask = ask("GETSRV:");
        String[] split = ask.split("\\|");
        Collections.addAll(res, split);
        return res;
    }

    /**
     * Returns a list of available SRV configuration files.
     * @return list of SRV file names
     * @throws IOException thrown in case of communication error
     */
    public long getBoxID() throws IOException {
        String res = ask("GETBOXID:");
        try {
            return Long.parseLong(res);
        } catch (NumberFormatException ex) {
            throw new IOException(res);
        }
    }

    /**
     * Kills all terminals visible to the Terminal Server.
     * @return operation result: true - success, false - failure
     * @throws IOException thrown in case of communication error
     */
    public boolean killTerminals() throws IOException {
        String res = ask("KILLTERMS:");
        return res.startsWith("OK");
    }

    public static void main(String[] args) throws IOException {
        String terminalHost = "localhost";
        int terminalPort = 7788;
        long boxID = TerminalClient.getBoxID(terminalHost, terminalPort);
        System.out.println("TS("+terminalHost+":"+terminalPort+").boxid="+ boxID);
    }

    /**
     * Connects to the Terminal Server and returns a list of available SRV configuration files.
     * @param terminalHost TS host IP address.
     * @param terminalPort TS port number
     * @return list of SRV file names
     * @throws IOException thrown in case of communication error
     */
    public static ArrayList<String> getAvailableSRVFiles(String terminalHost, int terminalPort) throws IOException {
        TerminalClient cli = null;
        ArrayList<String> availableSRVFiles = null;
        try {
            cli = new TerminalClient("SRVGetter", terminalHost, terminalPort);
            availableSRVFiles = cli.getAvailableSRVFiles();
        } finally {
            if (cli != null) {
                cli.close();
            }
        }
        return availableSRVFiles;
    }

    /**
     * Connects to the Terminal Server and returns its BOXID.
     * @param terminalHost TS host IP address.
     * @param terminalPort TS port number
     * @return Terminal Server's BOXID
     * @throws IOException thrown in case of communication error
     */
    public static long getBoxID(String terminalHost, int terminalPort) throws IOException {
        TerminalClient cli = null;
        try {
            cli = new TerminalClient("BoxIDGetter", terminalHost, terminalPort);
            return cli.getBoxID();
        } finally {
            if (cli != null) {
                cli.close();
            }
        }
    }

    private class ConnectionWorkerThread extends Thread {
        private final Socket socket;
        private String peerName;
        private String ret;
        private static final String NONE = "NONE";
        private static final int TIMEOUT = 300000;

        public ConnectionWorkerThread(Socket socket) {
            super("TerminalClient:" + myName);
            this.socket = socket;
            setDaemon(true);
        }

        public void run() {
            StringBuffer sb = new StringBuffer();
            String line;
            while (true) {
                try {
                    int b = socket.getInputStream().read();
                    if (b < 0) {
                        if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Peer " + peerName + " disconnected");
                        }
                        return;
                    }
                    if (b == '\n' || b == '\r') {
                        line = sb.toString();
                        sb.setLength(0);
                        if (!line.equals("ARE_YOU_STILL_THERE"))
                            processClientRequest(line);
                    } else {
                        sb.append((char) b);
                    }
                } catch (IOException e) {
                    if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Peer " + peerName + " disconnected (" + e + ")");
                    }
                    break;
                }
            }
        }

        private synchronized void processClientRequest(String line) throws IOException {
            if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                LOGGER.debug("GOT FROM " + peerName + ": " + line);
            }
            //
            ret = line;
            this.notify();
        }

        private synchronized String ask(String line) throws IOException {
            socket.getOutputStream().write((line + '\n').getBytes());
            //
            if (Log4JUtil.isConfigured() && LOGGER.isDebugEnabled()) {
                LOGGER.debug("SENT TO " + peerName + ": " + line);
            }
            //
            long start = System.currentTimeMillis();
            ret = NONE;
            //noinspection StringEquality
            while (ret == NONE && isAlive()) {
                try {
                    this.wait(1000);
                    if (System.currentTimeMillis() - start > TIMEOUT) {
                        throw new IOException("Timeout");
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            return ret;
        }

    }

}
