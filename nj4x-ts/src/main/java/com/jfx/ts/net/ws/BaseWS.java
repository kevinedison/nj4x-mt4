package com.jfx.ts.net.ws;

import com.jfx.ts.net.ClientWorker;
import com.jfx.ts.net.TS;
import com.jfx.ts.net.TerminalServer;
import com.jfx.ts.net.ws.dto.Nj4xClientInfo;
import com.jfx.ts.net.ws.dto.Nj4xInvalidTokenException;
import com.jfx.ts.net.ws.dto.Nj4xSessionExpiredException;
import com.jfx.ts.net.ws.dto.Nj4xTSInfo;
import org.apache.log4j.Logger;

import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 * Collection of common NJ4X Web service methods - startSession, close (session), getTSInfo..  NJ4X的webservice的集合
 */
public class BaseWS {
    static final Logger LOGGER = TS.LOGGER;
    protected TS ts;

    public BaseWS(TS ts) {
        this.ts = ts;
    }

    /**
     * Opens new session to the Web service.
     * @param cInfo client information
     * @return session ID, required by all subsequent Web service methods.
     */
    @SuppressWarnings("UnusedDeclaration")
    @WebMethod
    public long startSession(@WebParam(name = "clientInfo") Nj4xClientInfo cInfo) {
        if (cInfo != null) {
            if (cInfo.apiVersion.compareTo(TerminalServer.MINIMUM_CLIENT_VERSION) < 0) {
                String m = cInfo.clientName + "> Unsupported NJ4X API version: " + cInfo.apiVersion + " <= " + TerminalServer.MINIMUM_CLIENT_VERSION;
                LOGGER.error(m);
                throw new RuntimeException(m);
            } else {
                ClientWorker cw = ts.newClientWorker(cInfo);
                ts.log(cInfo.clientName + " session started. NJ4X version: " + cInfo.apiVersion);
                return cw.token;
            }
        } else {
            //log("<null> client connected");
            return -1;
        }
    }

    /**
     * Gets TS information.
     * @param token session ID
     * @return TS information
     * @throws Nj4xInvalidTokenException
     * @throws Nj4xSessionExpiredException
     */
    @SuppressWarnings("UnusedDeclaration")
    @WebMethod
    public Nj4xTSInfo getTSInfo(@WebParam(name = "token") long token) /*throws Nj4xInvalidTokenException, Nj4xSessionExpiredException */{
        ClientWorker cw = ts.getClientWorker(token, "getTSInfo");
        //
        try {
            ts.log("Sending TS info to " + cw);
            //
            return new Nj4xTSInfo(TS.hostname, TS.NJ4X);
        } finally {
            cw.restoreCurrentThreadName();
        }
    }

    /**
     * Closes Web Service session
     * @param token session id
     * @throws Nj4xInvalidTokenException
     * @throws Nj4xSessionExpiredException
     */
    @SuppressWarnings("UnusedDeclaration")
    @WebMethod
    public void close(@WebParam(name = "token") long token) /*throws Nj4xInvalidTokenException, Nj4xSessionExpiredException */{
        try {
            ClientWorker cw = ts.removeClientWorker(token);
            //
            ts.log("" + cw + " session closed.");
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
}
