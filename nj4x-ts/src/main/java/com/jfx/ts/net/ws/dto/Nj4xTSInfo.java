package com.jfx.ts.net.ws.dto;

/**
 * Terminal Server info
 * User: roman
 * Date: 26/06/2014
 * Time: 19:12
 */
public class Nj4xTSInfo {
    /**
     * Terminal host name.
     */
    public String hostName;

    /**
     * Terminal NJ4X API version.
     */
    public String apiVersion;

    public Nj4xTSInfo() {
    }

    public Nj4xTSInfo(String hostName, String apiVersion) {
        this.hostName = hostName;
        this.apiVersion = apiVersion;
    }

}
