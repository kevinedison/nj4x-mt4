
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for runMT4Expert complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="runMT4Expert">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="mt4Account" type="{http://tester.nj4x.com/}nj4XMT4Account" minOccurs="0"/>
 *         &lt;element name="expertParams" type="{http://tester.nj4x.com/}mt4ExpertParams" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "runMT4Expert", propOrder = {
    "token",
    "mt4Account",
    "expertParams"
})
public class RunMT4Expert {

    protected long token;
    protected Nj4XMT4Account mt4Account;
    protected Mt4ExpertParams expertParams;

    /**
     * Gets the value of the token property.
     * 
     */
    public long getToken() {
        return token;
    }

    /**
     * Sets the value of the token property.
     * 
     */
    public void setToken(long value) {
        this.token = value;
    }

    /**
     * Gets the value of the mt4Account property.
     * 
     * @return
     *     possible object is
     *     {@link Nj4XMT4Account }
     *     
     */
    public Nj4XMT4Account getMt4Account() {
        return mt4Account;
    }

    /**
     * Sets the value of the mt4Account property.
     * 
     * @param value
     *     allowed object is
     *     {@link Nj4XMT4Account }
     *     
     */
    public void setMt4Account(Nj4XMT4Account value) {
        this.mt4Account = value;
    }

    /**
     * Gets the value of the expertParams property.
     * 
     * @return
     *     possible object is
     *     {@link Mt4ExpertParams }
     *     
     */
    public Mt4ExpertParams getExpertParams() {
        return expertParams;
    }

    /**
     * Sets the value of the expertParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mt4ExpertParams }
     *     
     */
    public void setExpertParams(Mt4ExpertParams value) {
        this.expertParams = value;
    }

}
