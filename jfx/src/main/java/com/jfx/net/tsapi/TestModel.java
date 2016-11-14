
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for testModel.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="testModel">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="EveryTick"/>
 *     &lt;enumeration value="ControlPoints"/>
 *     &lt;enumeration value="OpenPrices"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "testModel")
@XmlEnum
public enum TestModel {

    @XmlEnumValue("EveryTick")
    EVERY_TICK("EveryTick"),
    @XmlEnumValue("ControlPoints")
    CONTROL_POINTS("ControlPoints"),
    @XmlEnumValue("OpenPrices")
    OPEN_PRICES("OpenPrices");
    private final String value;

    TestModel(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TestModel fromValue(String v) {
        for (TestModel c: TestModel.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
