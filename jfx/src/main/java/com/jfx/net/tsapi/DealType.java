
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dealType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="dealType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BUY"/>
 *     &lt;enumeration value="SELL"/>
 *     &lt;enumeration value="TP"/>
 *     &lt;enumeration value="SL"/>
 *     &lt;enumeration value="MODIFY"/>
 *     &lt;enumeration value="CLOSE"/>
 *     &lt;enumeration value="CLOSE_AT_STOP"/>
 *     &lt;enumeration value="UNKNOWN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "dealType")
@XmlEnum
public enum DealType {

    BUY,
    SELL,
    TP,
    SL,
    MODIFY,
    CLOSE,
    CLOSE_AT_STOP,
    UNKNOWN;

    public String value() {
        return name();
    }

    public static DealType fromValue(String v) {
        return valueOf(v);
    }

}
