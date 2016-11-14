
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for period.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="period">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="M1"/>
 *     &lt;enumeration value="M5"/>
 *     &lt;enumeration value="M15"/>
 *     &lt;enumeration value="M30"/>
 *     &lt;enumeration value="H1"/>
 *     &lt;enumeration value="H4"/>
 *     &lt;enumeration value="D1"/>
 *     &lt;enumeration value="W1"/>
 *     &lt;enumeration value="MN1"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "period")
@XmlEnum
public enum Period {

    @XmlEnumValue("M1")
    M_1("M1"),
    @XmlEnumValue("M5")
    M_5("M5"),
    @XmlEnumValue("M15")
    M_15("M15"),
    @XmlEnumValue("M30")
    M_30("M30"),
    @XmlEnumValue("H1")
    H_1("H1"),
    @XmlEnumValue("H4")
    H_4("H4"),
    @XmlEnumValue("D1")
    D_1("D1"),
    @XmlEnumValue("W1")
    W_1("W1"),
    @XmlEnumValue("MN1")
    MN_1("MN1");
    private final String value;

    Period(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Period fromValue(String v) {
        for (Period c: Period.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
