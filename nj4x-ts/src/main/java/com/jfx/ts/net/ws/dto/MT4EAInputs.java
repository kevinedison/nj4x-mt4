package com.jfx.ts.net.ws.dto;

import java.util.ArrayList;

/**
 * User: roman
 * Date: 01/07/2014
 * Time: 10:24
 */
public class MT4EAInputs {
    public ArrayList<MT4EAInput> inputs;

    //
    public static class MT4EAInput {
        public String name;
        public String value;
        public double startValue, step, endValue;

        public String getInputIni() {
//        TakeProfit=50.00000000
//        TakeProfit,F=1
//        TakeProfit,1=80.00000000
//        TakeProfit,2=10.00000000
//        TakeProfit,3=120.00000000
            return name + "=" + value + "\n"
                    + (startValue == 0 && step == 0 && endValue == 0
                    ? ""
                    : name + ",F=1\n"
                    + name + ",1=" + startValue + "\n"
                    + name + ",2=" + step + "\n"
                    + name + ",3=" + endValue + "\n")
                    ;
        }

        public String getInputsChartContent() {
            return name + "=" + value + "\n";
        }
    }

    public String getInputsIni() {
        StringBuilder sb = new StringBuilder();
        for (MT4EAInput mt4EAInput : inputs) {
            sb.append(mt4EAInput.getInputIni());
        }
        return sb.toString();
    }

    public String getInputsChartContent() {
        StringBuilder sb = new StringBuilder();
        for (MT4EAInput mt4EAInput : inputs) {
            sb.append(mt4EAInput.getInputsChartContent());
        }
        return sb.toString();
    }

}
