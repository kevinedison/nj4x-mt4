package com.jfx.ts.net.ws;

import com.jfx.ts.io.BoxUtils;
import com.jfx.ts.net.ClientWorker;
import com.jfx.ts.net.TS;
import com.jfx.ts.net.TerminalServer;
import com.jfx.ts.net.ws.dto.Nj4xInvalidTokenException;
import com.jfx.ts.net.ws.dto.Nj4xMT4Account;
import com.jfx.ts.net.ws.dto.Nj4xParams;
import com.jfx.ts.net.ws.dto.Nj4xSessionExpiredException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * User: roman
 * Date: 05/08/2014
 * Time: 22:11
 */
@WebService(targetNamespace = "http://ts.nj4x.com/", serviceName = "TSService", portName = "TSPort", name = "TS")
public class TsWS extends BaseWS {

    public TsWS() {
        super(null);
    }

    public TsWS(TS ts) {
        super(ts);
    }

    @SuppressWarnings("UnusedDeclaration")
    @WebMethod
    public String runMT4Terminal(@WebParam(name = "token") final long token, @WebParam(name = "mt4Account") final Nj4xMT4Account account, @WebParam(name = "nj4xEAParams") final Nj4xParams nj4xEAParams,
                                 @WebParam(name = "restartTerminalIfRunning") final boolean restartTerminalIfRunning) throws Nj4xInvalidTokenException, Nj4xSessionExpiredException {
        Future<String> future = TerminalServer.FIXED_THREAD_POOL.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                ClientWorker cw = ts.getClientWorker(token, "runMT4Terminal");
                //
                try {
                    return cw.runTerminal(account, nj4xEAParams, restartTerminalIfRunning);
                } finally {
                    cw.restoreCurrentThreadName();
                }
            }
        });
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Nj4xInvalidTokenException) {
                throw (Nj4xInvalidTokenException) cause;
            } else if (cause instanceof Nj4xSessionExpiredException) {
                throw (Nj4xSessionExpiredException) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @WebMethod
    public String checkMT4Terminal(@WebParam(name = "token") final long token, @WebParam(name = "mt4Account") final Nj4xMT4Account account, @WebParam(name = "nj4xEAParams") final Nj4xParams nj4xEAParams) throws Nj4xInvalidTokenException, Nj4xSessionExpiredException {
        ClientWorker cw = ts.getClientWorker(token, "checkMT4Terminal");
        //
        try {
            return cw.checkTerminal(account, nj4xEAParams);
        } finally {
            cw.restoreCurrentThreadName();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @WebMethod
    public String disconnectMT4Terminal(@WebParam(name = "token") long token, @WebParam(name = "mt4Account") Nj4xMT4Account account, @WebParam(name = "nj4xEAParams") Nj4xParams nj4xEAParams) throws Nj4xInvalidTokenException, Nj4xSessionExpiredException {
        ClientWorker cw = ts.getClientWorker(token, "disconnectMT4Terminal");
        //
        try {
            return cw.stopTerminal(account, nj4xEAParams, false);
        } finally {
            cw.restoreCurrentThreadName();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @WebMethod
    public String killMT4Terminal(@WebParam(name = "token") long token, @WebParam(name = "mt4Account") Nj4xMT4Account account, @WebParam(name = "nj4xEAParams") Nj4xParams nj4xEAParams) throws Nj4xInvalidTokenException, Nj4xSessionExpiredException {
        ClientWorker cw = ts.getClientWorker(token, "killMT4Terminal");
        //
        try {
            return cw.stopTerminal(account, nj4xEAParams, true);
        } finally {
            cw.restoreCurrentThreadName();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @WebMethod
    public ArrayList<String> getAvailableSRVFiles(@WebParam(name = "token") long token) throws Nj4xInvalidTokenException, Nj4xSessionExpiredException {
        ClientWorker cw = ts.getClientWorker(token, "getAvailableSRVFiles");
        try {
            return cw.getSRVFilesList();
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            cw.restoreCurrentThreadName();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @WebMethod
    public long getBoxID() {
        try {
            return BoxUtils.BOXID;
        } catch (Exception io) {
            throw new RuntimeException(io);
        }
    }
}
