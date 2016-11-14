package com.jfx.ts.net.ws.dto;

import java.util.ArrayList;

/**
 * NJ4X Strategy parameters.
 * User: roman
 * Date: 27/06/2014
 * Time: 10:47
 */
public class Nj4xParams {
    public String jfxHost, tenant, strategy, symbol, parentTenant/*, termHomeDir*/;
    public int period, historyPeriod, jfxPort;
    public ArrayList<Nj4xChartParams> charts;
    public boolean asynchOrdersOperations;

    public Nj4xParams() {
    }

    public Nj4xParams(String jfxHost, int jfxPort, String tenant, String strategy) {
        this.jfxHost = jfxHost;
        this.jfxPort = jfxPort;
        this.tenant = tenant;
        this.strategy = strategy;
    }

    public Nj4xParams(String jfxHost, int jfxPort, String tenant, String strategy, String symbol, int period, int historyPeriod, String parentTenant, ArrayList<Nj4xChartParams> charts, boolean asynchOrdersOperations) {
        this.jfxHost = jfxHost;
        this.jfxPort = jfxPort;
        this.tenant = tenant;
        this.strategy = strategy;
        this.symbol = symbol;
        this.period = period;
        this.historyPeriod = historyPeriod;
        this.parentTenant = parentTenant;
        this.charts = charts;
        this.asynchOrdersOperations = asynchOrdersOperations;
    }
}
