package com.jfx.ts.net.ws.dto;

/**
 * NJ4X additional chart parameters.
 * User: roman
 * Date: 27/06/2014
 * Time: 10:47
 */
public class Nj4xChartParams {
    public String chartID;
    public String params[];

    @Override
    public String toString() {
        if (params == null || params.length == 0) {
            return chartID;
        } else {
            StringBuilder sb = new StringBuilder(chartID);
            for (String param : params) {
                sb.append(':');
                sb.append(param);
            }
            return sb.toString();
        }
    }
}
