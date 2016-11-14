package com.jfx.net;

import com.jfx.Broker;
import com.jfx.Version;
import com.jfx.net.tsapi.*;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * User: roman
 * Date: 28/06/2014
 * Time: 12:53
 */
@SuppressWarnings("UnusedDeclaration")
public class TSClient {

    private String tsWSHost;
    private int tsWSPort;
    private TS ts;
    private long sessionToken;
    private Nj4XTSInfo tsInfo;
    private ScheduledFuture<?> keepAliveJob;

    public TSClient(String tsWSHost, int tsWSPort) {
        this.tsWSHost = tsWSHost;
        this.tsWSPort = tsWSPort;
    }

    public static Nj4XMT4Account getNj4xMT4Account(Broker broker, String user, String password) {
        Nj4XMT4Account mt4Account = new Nj4XMT4Account();
        mt4Account.setSrv(broker.getSrv());
        mt4Account.setUser(user);
        mt4Account.setPassword(password);
        if (broker.getProxyServer() != null) {
            mt4Account.setProxyServer(broker.getProxyServer());
            mt4Account.setProxyType("" + broker.getProxyType());
            mt4Account.setProxyUser(broker.getProxyLogin());
            mt4Account.setProxyPassword(broker.getProxyPassword());
        }
        return mt4Account;
    }

    public static Nj4XMT4Account getNj4xMT4Account(String srv, String user, String password) {
        return getNj4xMT4Account(new Broker(srv), user, password);
    }

    private static WebEndpointInterceptor webEndpointInterceptor;

    public static void setWebEndpointInterceptor(WebEndpointInterceptor webEndpointInterceptor) {
        TSClient.webEndpointInterceptor = webEndpointInterceptor;
    }

    public interface WebEndpointInterceptor {
        public void customizeEndpoint(TS endpoint);
    }

    public static final int SESSION_TIMEOUT_MILLIS = 60000; // see com.jfx.net.TS
    public TSClient connect(String clientName) {
        ts = getTS(tsWSHost, tsWSPort);
        //
        WebEndpointInterceptor interceptor = webEndpointInterceptor;
        if (interceptor != null) {
            interceptor.customizeEndpoint(ts);
        }
        //
        Nj4XClientInfo clientInfo = new Nj4XClientInfo();
        clientInfo.setClientName((clientName == null ? "?" : clientName.replace('\u0001', ' ').replace('\u0002', ' ').replace('\u0005', ' ')).trim());
        clientInfo.setApiVersion(Version.NJ4X);
        sessionToken = ts.startSession(clientInfo);
        //
        keepAliveJob = InprocessServer.scheduleService.schedule(new Runnable() {
            @Override
            public void run() {
                ts.getTSInfo(sessionToken);
                InprocessServer.scheduleService.schedule(this, SESSION_TIMEOUT_MILLIS / 2, TimeUnit.MILLISECONDS);
            }
        }, SESSION_TIMEOUT_MILLIS / 2, TimeUnit.MILLISECONDS);
        //
        return this;
    }

    public Nj4XTSInfo getTsInfo() {
        return tsInfo == null ? (tsInfo = ts.getTSInfo(sessionToken)) : tsInfo;
    }

    public void close() {
        if (ts != null && sessionToken != 0) {
            keepAliveJob.cancel(true);
            ts.close(sessionToken);
        }
    }

    public String disconnectMT4Terminal(Nj4XMT4Account mt4Account, Nj4XParams nj4XEAParams) {
        try {
            return ts.disconnectMT4Terminal(sessionToken, mt4Account, nj4XEAParams);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getAvailableSRVFiles() {
        try {
            return ts.getAvailableSRVFiles(sessionToken);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public long getBoxID() {
        return ts.getBoxID();
    }

    public String runMT4Terminal(Nj4XMT4Account mt4Account, Nj4XParams nj4XEAParams, boolean restartTerminalIfRunning) {
        try {
            return ts.runMT4Terminal(sessionToken, mt4Account, nj4XEAParams, restartTerminalIfRunning);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String checkMT4Terminal(Nj4XMT4Account mt4Account, Nj4XParams nj4XEAParams) {
        try {
            return ts.checkMT4Terminal(sessionToken, mt4Account, nj4XEAParams);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String killMT4Terminal(Nj4XMT4Account mt4Account, Nj4XParams nj4XEAParams) {
        try {
            return ts.killMT4Terminal(sessionToken, mt4Account, nj4XEAParams);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static TS getTS() {
        String tsWSHost = System.getProperty("ts_host", "localhost");
        int tsWSPort = Integer.parseInt(System.getProperty("ts_ws_port", "7789"));
        return getTS(tsWSHost, tsWSPort);
    }

    private final static QName TSSERVICE_QNAME = new QName("http://ts.nj4x.com/", "TSService");

    private static TS getTS(String tsWSHost, int tsWSPort) {
        TSService s;
        try {
            s = new TSService(
                    new URL(TSService.class.getResource("."), "http://" + tsWSHost + ":" + tsWSPort + "/nj4x/ts?wsdl"),
                    TSSERVICE_QNAME
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return s.getTSPort();
    }
}