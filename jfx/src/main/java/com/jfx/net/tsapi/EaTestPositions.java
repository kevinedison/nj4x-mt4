
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for eaTestPositions.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="eaTestPositions">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BUY"/>
 *     &lt;enumeration value="SELL"/>
 *     &lt;enumeration value="BUY_AND_SELL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "eaTestPositions")
@XmlEnum
public enum EaTestPositions {

    BUY,
    SELL,
    BUY_AND_SELL;

    public String value() {
        return name();
    }

    public static EaTestPositions fromValue(String v) {
        return valueOf(v);
    }

}
