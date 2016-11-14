
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for deal complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="deal">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dealNo" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="time" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="type" type="{http://tester.nj4x.com/}dealType" minOccurs="0"/>
 *         &lt;element name="order" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="price" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="sl" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="tp" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="profit" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="balance" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "deal", propOrder = {
    "dealNo",
    "time",
    "type",
    "order",
    "size",
    "price",
    "sl",
    "tp",
    "profit",
    "balance"
})
public class Deal {

    protected int dealNo;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar time;
    protected DealType type;
    protected int order;
    protected double size;
    protected double price;
    protected double sl;
    protected double tp;
    protected double profit;
    protected double balance;

    /**
     * Gets the value of the dealNo property.
     * 
     */
    public int getDealNo() {
        return dealNo;
    }

    /**
     * Sets the value of the dealNo property.
     * 
     */
    public void setDealNo(int value) {
        this.dealNo = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTime(XMLGregorianCalendar value) {
        this.time = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link DealType }
     *     
     */
    public DealType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link DealType }
     *     
     */
    public void setType(DealType value) {
        this.type = value;
    }

    /**
     * Gets the value of the order property.
     * 
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     */
    public void setOrder(int value) {
        this.order = value;
    }

    /**
     * Gets the value of the size property.
     * 
     */
    public double getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     */
    public void setSize(double value) {
        this.size = value;
    }

    /**
     * Gets the value of the price property.
     * 
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     */
    public void setPrice(double value) {
        this.price = value;
    }

    /**
     * Gets the value of the sl property.
     * 
     */
    public double getSl() {
        return sl;
    }

    /**
     * Sets the value of the sl property.
     * 
     */
    public void setSl(double value) {
        this.sl = value;
    }

    /**
     * Gets the value of the tp property.
     * 
     */
    public double getTp() {
        return tp;
    }

    /**
     * Sets the value of the tp property.
     * 
     */
    public void setTp(double value) {
        this.tp = value;
    }

    /**
     * Gets the value of the profit property.
     * 
     */
    public double getProfit() {
        return profit;
    }

    /**
     * Sets the value of the profit property.
     * 
     */
    public void setProfit(double value) {
        this.profit = value;
    }

    /**
     * Gets the value of the balance property.
     * 
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Sets the value of the balance property.
     * 
     */
    public void setBalance(double value) {
        this.balance = value;
    }

}
