
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for eaTestOptimizedParameter.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="eaTestOptimizedParameter">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BALANCE"/>
 *     &lt;enumeration value="PROFIT_FACTOR"/>
 *     &lt;enumeration value="EXPECTED_PAYOFF"/>
 *     &lt;enumeration value="MAX_DRAWDOWN"/>
 *     &lt;enumeration value="DRAWDOWN_PCT"/>
 *     &lt;enumeration value="CUSTOM"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "eaTestOptimizedParameter")
@XmlEnum
public enum EaTestOptimizedParameter {

    BALANCE,
    PROFIT_FACTOR,
    EXPECTED_PAYOFF,
    MAX_DRAWDOWN,
    DRAWDOWN_PCT,
    CUSTOM;

    public String value() {
        return name();
    }

    public static EaTestOptimizedParameter fromValue(String v) {
        return valueOf(v);
    }

}
