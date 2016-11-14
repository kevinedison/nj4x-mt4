package com.jfx.ts.net.ws.dto;

/**
 * Broker connection configuration.
 * User: roman
 * Date: 27/06/2014
 * Time: 10:25
 */
public class Nj4xMT4Account {
    public String proxyServer, proxyType, proxyUser, proxyPassword;
    public String srv, user, password;

    @Override
    public String toString() {
        return user + "@" + srv;
    }
}
