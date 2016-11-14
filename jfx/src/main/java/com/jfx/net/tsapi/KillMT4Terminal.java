
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for killMT4Terminal complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="killMT4Terminal">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="mt4Account" type="{http://ts.nj4x.com/}nj4XMT4Account" minOccurs="0"/>
 *         &lt;element name="nj4xEAParams" type="{http://ts.nj4x.com/}nj4XParams" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "killMT4Terminal", propOrder = {
    "token",
    "mt4Account",
    "nj4XEAParams"
})
public class KillMT4Terminal {

    protected long token;
    protected Nj4XMT4Account mt4Account;
    @XmlElement(name = "nj4xEAParams")
    protected Nj4XParams nj4XEAParams;

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
     * Gets the value of the nj4XEAParams property.
     * 
     * @return
     *     possible object is
     *     {@link Nj4XParams }
     *     
     */
    public Nj4XParams getNj4XEAParams() {
        return nj4XEAParams;
    }

    /**
     * Sets the value of the nj4XEAParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link Nj4XParams }
     *     
     */
    public void setNj4XEAParams(Nj4XParams value) {
        this.nj4XEAParams = value;
    }

}
