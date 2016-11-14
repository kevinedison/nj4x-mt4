package com.jfx.ts.net;

import com.jfx.ts.io.BoxUtils;
import com.jfx.ts.io.Log4JUtil;
import com.jfx.ts.net.ws.dto.Nj4xClientInfo;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class ClientWorkerHdlr extends SimpleChannelInboundHandler<String> {
    static final String RUNTERM = "RUNTERM:";
    static final String CHKTERM = "CHKTERM:";
    static final String STOPTERM = "STOPTERM:";
    static final String KILLTERM = "KILLTERM:";
    static final String GETSYMBOLS = "GETSYMBOLS:";
    static final String GETSRV = "GETSRV:";
    static final String COUNTTERMS = "COUNTTERMS:";
    static final String GETBOXID = "GETBOXID:";
    static final String KILLTERMS = "KILLTERMS:";
    static final String CMD = "CMD";
    boolean _isAsynch = false;
    private String originalThreadName;
    private ChannelHandlerContext ctx;
    private ClientWorker cw;
    private boolean _isActive;

    public ClientWorkerHdlr(TS ts) {
        ctx = null;
        cw = new ClientWorker(ts);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
        TS.LOGGER.info("Channel active: " + this);
        _isActive = true;
    }

    public boolean isActive() {
        return _isActive;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        _isActive = false;
        if (ctx != null) {
            this.ctx = null;
            super.channelUnregistered(ctx);
            TS.LOGGER.info("ClientWorkerHdlr channel inactive: " + this);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        TS.LOGGER.error("Channel error: " + this
                , cause
        );
    }

    private void send(String s) {
        if (this.ctx != null && _isActive) { // channel is active
            try {
                ChannelFuture future = this.ctx.writeAndFlush(s);
                future.await(5, TimeUnit.SECONDS);
                if (TS.LOGGER.isDebugEnabled()) {
                    TS.LOGGER.debug("Channel: " + this
                            + " OUT> " + s
                    );
                }
            } catch (Throwable t) {
                TS.LOGGER.error("SEND Error, Channel: " + this
                                + " OUT> " + s
                        , t
                );
            }
        }
    }

    @Override
    public String toString() {
        return (cw.cInfo == null ? "<?>" : cw.cInfo.clientName);
    }

    void sendToClient(String line) throws IOException {
        if (line != null && _isActive) {
            send(line);
        }
    }

    void sendToClientIgnoreException(String line) {
        try {
            sendToClient(line);
        } catch (Throwable t) {
            TS.LOGGER.error("SEND Error, Channel: " + this
                            + " OUT> " + line
                    , t
            );
        }
    }

    public void processClientRequest(ChannelHandlerContext ctx, String msg) throws Exception {
        _isAsynch = true;
        channelRead0(ctx, msg);
    }

    @Override
    protected synchronized void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (TS.LOGGER.isDebugEnabled()) {
            TS.LOGGER.debug("Channel: " + this
                    + " IN> " + TerminalParams.maskPassword(msg)
            );
        }
        //
        this.ctx = ctx;
        processClientRequest(msg);
    }

    private String getClientInfoName() {
        if (cw.cInfo == null) {
            if (ctx != null) {
                SocketAddress remoteSocketAddress = ctx.channel().remoteAddress();
                if (remoteSocketAddress != null) {
                    return remoteSocketAddress.toString();
                } else {
                    return "" + ctx.channel();
                }
            } else {
                return "<socket==null>";
            }
        } else {
            return cw.cInfo.clientName;
        }
    }

    private void runTerminal(String line, boolean longRun) throws IOException {
        if (_isActive) {
            try {
                new TerminalParams(line.substring(RUNTERM.length() + (longRun ? 1 : 0))).runTerminal(longRun, this);
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
    }

    public ClientWorker getClientWorker() {
        return cw;
    }

    private void checkTerminal(String line) throws IOException {
        sendToClient(new TerminalParams(line.substring(CHKTERM.length())).checkTerminal(cw));
    }

    private void processClientRequest(final String line) throws IOException {
        if (Log4JUtil.isConfigured() && TS.LOGGER.isInfoEnabled()) {
            TS.LOGGER.info("GOT [" + TerminalParams.maskPassword(line) + "] from " + getClientInfoName());
        }
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
                cw.cInfo = null;
                cw.cInfo = new Nj4xClientInfo(clientName + " " + getClientInfoName(), clientApiVersion);
                //
                if (Log4JUtil.isConfigured() && TS.LOGGER.isDebugEnabled()) {
                    TS.LOGGER.debug(getClientInfoName() + ", client_uuid=" + clientUUID);
                }
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
            if (cw.cInfo.apiVersion.compareTo(TerminalServer.MINIMUM_CLIENT_VERSION) < 0) {
                String m = cw.cInfo.clientName + "> Unsupported NJ4X API version: " + cw.cInfo.apiVersion;
                TS.LOGGER.error(m);
                sendToClient(m);
            } else {
                try {
                    runTerminal(line, line.startsWith("L" + RUNTERM));
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
                sendToClient(cw.stopTerminal(line, line.startsWith(KILLTERM)));
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.startsWith(CMD)) {
            long cmdId = 0;
            try {
                int dIx = line.indexOf(' ');
                cmdId = Long.parseLong(line.substring(CMD.length(), dIx));
                String cmd = line.substring(dIx + 1);
                String[] cmdArgs = cmd.split("\\|");
                switch (cmdArgs[0]) {
                    case "NUMTERMS":
                        switch (cmdArgs.length) {
                            case 1:
                                sendRes(cmdId, "" + cw.countTerminals());
                                break;
                            case 3: // broker, account
                                int[] totalBrokerAccountTerms = cw.countTerminals(cmdArgs[1], cmdArgs[2]);
                                sendRes(cmdId, String.format("%d:%d:%d:%d", totalBrokerAccountTerms[0], totalBrokerAccountTerms[1], totalBrokerAccountTerms[2], totalBrokerAccountTerms[3]));
                                break;
                        }
                        return;
                }
                sendRes(cmdId, "Unrecognized command: " + cmd);
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                if (cmdId > 0) {
                    sendRes(cmdId, m);
                } else {
                    sendToClient(m);
                }
            }
        } else if (line.startsWith(GETSYMBOLS)) {
            try {
                sendToClient(cw.getSymbols(line));
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.startsWith(GETSRV)) {
            try {
                sendToClient(cw.getSRV());
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else if (line.startsWith(COUNTTERMS)) {
            try {
                sendToClient("" + cw.countTerminals());
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
                cw.ts.killTerminals();
                sendToClient("OK, killed");
            } catch (Throwable e) {
                String m = "Unexpected error: " + e;
                TS.LOGGER.error(m, e);
                sendToClient(m);
            }
        } else {
            String m = "Unrecognized client (" + this + ") request: [" + line + "]";
            TS.LOGGER.error(m);
            sendToClient(m);
        }
    }

    private void sendRes(long cmdId, String res) throws IOException {
        sendToClient("RES" + cmdId + " " + res);
    }

}
