package com.jfx.ts.net;

import com.jfx.net.Config;
import com.jfx.ts.io.BoxUtils;
import com.jfx.ts.io.Log4JUtil;
import com.jfx.ts.net.ws.dto.Nj4xClientInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Future;

/**
 * todo: comments
 * User: roman
 * Date: 05/08/2014
 * Time: 21:54
 */
@SuppressWarnings("unchecked")
class ClientWorkerThread extends ClientWorker implements Runnable {
    //private TS ts;
    private final Socket socket;
    static final String RUNTERM = "RUNTERM:";
    static final String CHKTERM = "CHKTERM:";
    static final String STOPTERM = "STOPTERM:";
    static final String KILLTERM = "KILLTERM:";
    static final String GETSYMBOLS = "GETSYMBOLS:";
    static final String GETSRV = "GETSRV:";
    static final String COUNTTERMS = "COUNTTERMS:";
    static final String GETBOXID = "GETBOXID:";
    static final String KILLTERMS = "KILLTERMS:";
    static final String STOPSERVICE= "STOPSERVICE";
    static final String GETMODE= "GETMODE";
    private String originalThreadName;

    public ClientWorkerThread(TS ts, Socket socket) {
        super(ts);
        this.socket = socket;
        cInfo = null;
        //noinspection unchecked
    }

    public void run() {
        originalThreadName = Thread.currentThread().getName();
        long start = System.currentTimeMillis();
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            while (true) {
                try {
                    int b = socket.getInputStream().read();
                    if (b < 0) {
                        if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                            TS.LOGGER.info("TS: Client " + (getClientInfoName()) + " disconnected");
                        }
                        return;
                    }
                    if (b == '\n' || b == '\r') {
                        line = sb.toString();
                        sb.setLength(0);
                        processClientRequest(line);
                    } else {
                        sb.append((char) b);
                    }
                } catch (Throwable e) {
                    if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                        TS.LOGGER.info("TS: Client " + (getClientInfoName()) + " disconnected (" + e + ")", e);
                    }
                    break;
                }
            }
        } finally {
            Thread.currentThread().setName(originalThreadName);
            long time = (System.currentTimeMillis() - start) / 1000;
            if (time > 3 && Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                TS.LOGGER.info("TS: Client " + (getClientInfoName()) + " exiting, " + originalThreadName + " has been used for " + time + " sec");
            }
        }
    }

    private void processClientRequest(final String line) throws IOException {
        if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
            TS.LOGGER.info("GOT [" + TerminalParams.maskPassword(line) + "] from " + getClientInfoName());        }
        //
        if (line.startsWith("HELLO")) {
            //HELLO4469602@_MW_120_50_42_102_443 4469602@120.50.42.102:443:_T_LSNR[\u0001]2.3.5-P5[\u0001]31a45963-bf38-4957-9f44-39c752ac6829
            String clientUUID = "-";
            String clientName = line.substring(5);
            int ix = clientName.indexOf('\u0001');
            String clientApiVersion = "1.7.1";
            if (ix >= 0) {
                clientApiVersion = clientName.substring(ix + 1);
                clientName = clientName.substring(0, ix);
                //
                int ix2 = clientApiVersion.indexOf('\u0001');
                if (ix2 >= 0) {
                    clientUUID = clientApiVersion.substring(ix2 + 1);
                    clientApiVersion = clientApiVersion.substring(0, ix2);
                }
            }
            //
            if (clientApiVersion.compareTo(TerminalServer.MINIMUM_CLIENT_VERSION) < 0) {
                sendToClient("ERROR" + TS.hostname + "\u0001" + TS.NJ4X);
            } else {
                cInfo = null;
                cInfo = new Nj4xClientInfo(clientName + " " + getClientInfoName(), clientApiVersion);
                //
                if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                    TS.LOGGER.debug(getClientInfoName() + ", client_uuid=" + clientUUID);
                }
                Thread.currentThread().setName(getClientInfoName());
                //
                String msg = "Client " + getClientInfoName() + " connected, NJ4X version " + clientApiVersion;
                //ts.log(msg);
                if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
                    TS.LOGGER.info(msg);
                }
                sendToClient("HELLO" + TS.hostname + "\u0001" + TS.NJ4X);
                //
            }
        } else if (line.startsWith(RUNTERM) || line.startsWith("L" + RUNTERM)) {
            if (cInfo.apiVersion.compareTo(TerminalServer.MINIMUM_CLIENT_VERSION) < 0) {
                String m = cInfo.clientName + "> Unsupported NJ4X API version: " + cInfo.apiVersion;
                TS.LOGGER.error(m);
                sendToClient(m);
            } else {
                try {
                    Future submit = TerminalServer.FIXED_THREAD_POOL.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runTerminal(line, line.startsWith("L" + RUNTERM));
                            } catch (NoSrvConnection e) {
                                String m = "No connection to server: " + e;
                                TS.LOGGER.error(m, e);
                                sendToClientIgnoreException(m);
                            } catch (SrvFileNotFound e) {
                                String m = "SRV file not found: " + e;
                                TS.LOGGER.error(m, e);
                                sendToClientIgnoreException(m);
                            } catch (MaxNumberOfTerminalsReached e) {
                                String m = "Reached max number of terminals: " + e;
                                TS.LOGGER.error(m, e);
                                sendToClientIgnoreException(m);
                            } catch (InvalidUserNamePassword e) {
                                String m = "Invalid user name or password: " + e;
                                TS.LOGGER.error(m, e);
                                sendToClientIgnoreException(m);
                            } catch (Throwable e) {
                                e.printStackTrace();
                                String m = "Unexpected error: " + e;
                                TS.LOGGER.error(m, e);
                                sendToClientIgnoreException(m);
                            }
                        }
                    });
                    //
                    submit.get();
                } catch (Throwable e) {
                    e.printStackTrace();
                    String m = "Unexpected error: " + e;
                    TS.LOGGER.error(m, e);
                    sendToClient(m);
                }
            }
        } else if (line.startsWith(CHKTERM)) {
            try {
                checkTerminal(line);
            } catch (NoSrvConnection e) {
                String m = "No connection to server: " + e;
                TS.LOGGER.error(m, e);
                sendToClientIgnoreException(m);
            } catch (SrvFileNotFound e) {
                String m = "SRV file not found: " + e;
                TS.LOGGER.error(m, e);
                sendToClientIgnoreException(m);
            } catch (MaxNumberOfTerminalsReached e) {
                String m = "Reached max number of terminals: " + e;
                TS.LOGGER.error(m, e);
                sendToClientIgnoreException(m);
            } catch (InvalidUserNamePassword e) {
                String m = "Invalid user name or password: " + e;
                TS.LOGGER.error(m, e);
                sendToClientIgnoreException(m);
            } catch (Throwable e) {
                e.printStackTrace();
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClientIgnoreException(m);
            }
        } else if (line.startsWith(STOPTERM) || line.startsWith(KILLTERM)) {
            try {
                sendToClient(stopTerminal(line, line.startsWith(KILLTERM)));
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.startsWith(GETSYMBOLS)) {
            try {
                sendToClient(getSymbols(line));
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.startsWith(GETSRV)) {
            try {
                sendToClient(getSRV());
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.startsWith(COUNTTERMS)) {
            try {
                sendToClient("" + countTerminals());
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.startsWith(GETBOXID)) {
            try {
                sendToClient("" + BoxUtils.BOXID);
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.startsWith(KILLTERMS)) {
            try {
                ts.killTerminals();
                sendToClient("OK, killed");
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.equals(STOPSERVICE)) {
            try {
                TS.LOGGER.info("Terminal Server Stop Requested");
                sendToClient("OK");
                System.exit(0);
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.equals(GETMODE)) {
            try {
                sendToClient("MSTSC="+TS.P_USE_MSTSC);
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else {
            String m = "Unrecognized client (" + (cInfo == null ? "" : cInfo.clientName) + ") request: [" + line + "]";
            TS.LOGGER.error(m);
            sendToClient(m);
        }
    }

    private String getClientInfoName() {
        if (cInfo == null) {
            if (socket != null) {
                SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
                if (remoteSocketAddress != null) {
                    return remoteSocketAddress.toString();
                } else {
                    return "" + socket;
                }
            } else {
                return "<socket==null>";
            }
        } else {
            return cInfo.clientName;
        }
    }

    private void runTerminal(String line, boolean longRun) throws IOException {
        sendToClient("ARE_YOU_STILL_THERE");
        sendToClient(new TerminalParams(line.substring(RUNTERM.length() + (longRun ? 1 : 0))).runTerminal(longRun, ClientWorkerThread.this));
    }

    private void checkTerminal(String line) throws IOException {
        sendToClient(new TerminalParams(line.substring(CHKTERM.length())).checkTerminal(ClientWorkerThread.this));
    }

    private void sendToClientIgnoreException(String line) {
        try {
            sendToClient(line);
        } catch (IOException e) {
            TS.LOGGER.error("Error sending [" + line + "]", e);
        }
    }

    private void sendToClient(String line) throws IOException {
        if (line != null) {
            OutputStream os = socket.getOutputStream();
            os.write((line + '\n').getBytes(Config.CHARSET));
            os.flush();
            //
            if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                TS.LOGGER.debug("SENT TO " + (cInfo == null ? "<?>" : cInfo.clientName) + ": " + line);
            }
        }
    }

}
