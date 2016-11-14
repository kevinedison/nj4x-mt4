
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mt4ExpertTesterParams complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mt4ExpertTesterParams">
 *   &lt;complexContent>
 *     &lt;extension base="{http://tester.nj4x.com/}mt4ExpertParams">
 *       &lt;sequence>
 *         &lt;element name="testerConfig" type="{http://tester.nj4x.com/}mt4EATesterConfiguration" minOccurs="0"/>
 *         &lt;element name="limits" type="{http://tester.nj4x.com/}mt4EATesterLimits" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mt4ExpertTesterParams", propOrder = {
    "testerConfig",
    "limits"
})
public class Mt4ExpertTesterParams
    extends Mt4ExpertParams
{

    protected Mt4EATesterConfiguration testerConfig;
    protected Mt4EATesterLimits limits;

    /**
     * Gets the value of the testerConfig property.
     * 
     * @return
     *     possible object is
     *     {@link Mt4EATesterConfiguration }
     *     
     */
    public Mt4EATesterConfiguration getTesterConfig() {
        return testerConfig;
    }

    /**
     * Sets the value of the testerConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mt4EATesterConfiguration }
     *     
     */
    public void setTesterConfig(Mt4EATesterConfiguration value) {
        this.testerConfig = value;
    }

    /**
     * Gets the value of the limits property.
     * 
     * @return
     *     possible object is
     *     {@link Mt4EATesterLimits }
     *     
     */
    public Mt4EATesterLimits getLimits() {
        return limits;
    }

    /**
     * Sets the value of the limits property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mt4EATesterLimits }
     *     
     */
    public void setLimits(Mt4EATesterLimits value) {
        this.limits = value;
    }

}
