package com.jfx.ts.net.ws.dto;

/**
 * Terminal Server Client info
 * User: roman
 * Date: 26/06/2014
 * Time: 19:12
 */
public class Nj4xClientInfo {
    /**
     * Custom client name
     */
    public String clientName;
    /**
     * Used NJ4X API version (e.g. "2.4.2")
     */
    public String apiVersion;

    public Nj4xClientInfo() {
    }

    public Nj4xClientInfo(String clientName, String apiVersion) {
        this.clientName = clientName.trim();
        this.apiVersion = apiVersion;
    }
}
