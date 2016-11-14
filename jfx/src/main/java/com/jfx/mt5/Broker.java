package com.jfx.mt5;


/**
 *
 * User: roman
 * Date: 03/02/14
 * Time: 12:37
 */
public class Broker extends com.jfx.Broker {
    public Broker(String val) {
        super(val.indexOf(':') < 0 ? val + ":443" : val);
    }

    public Broker(com.jfx.Broker b, String proxyServer, ProxyType proxyType, String proxyLogin, String proxyPassword) {
        super(b, proxyServer, proxyType, proxyLogin, proxyPassword);
    }

    public Broker(String val, String proxyServer, ProxyType proxyType) {
        super(val, proxyServer, proxyType);
    }

    public Broker(String val, String proxyServer, ProxyType proxyType, String proxyLogin, String proxyPassword) {
        super(val, proxyServer, proxyType, proxyLogin, proxyPassword);
    }

    @Override
    public String getVal() {
        return "5*" + super.getVal();
    }

    @Override
    public String getSrv() {
        return "5*" + super.getSrv();
    }

    public int getType() {
        return 5;
    }
}
