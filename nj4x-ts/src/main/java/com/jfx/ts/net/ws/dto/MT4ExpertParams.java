package com.jfx.ts.net.ws.dto;

import javax.xml.bind.annotation.XmlElement;

/**
 * User: roman
 * Date: 02/07/2014
 * Time: 10:09
 */
public class MT4ExpertParams {
    @XmlElement(nillable = false)
    public String expertName;
    @XmlElement(nillable = false)
    public String symbol;
    @XmlElement(nillable = false)
    public MT4EAInputs inputs;
    @XmlElement(nillable = false)
    public Period period;

    public static enum Period {
        M1, M5, M15, M30, H1, H4, D1, W1, MN1;

        public int toInt() {
            switch (this) {
                case M1:
                    return 1;
                case M5:
                    return 5;
                case M15:
                    return 15;
                case M30:
                    return 30;
                case H1:
                    return 60;
                case H4:
                    return 60 * 4;
                case D1:
                    return 60 * 24;
                case W1:
                    return 10080;
                case MN1:
                    return 43200;
            }
            return 240;
        }
    }

    @Override
    public String toString() {
        return expertName;
    }
}
