
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mt4EATesterLimits complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mt4EATesterLimits">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="limitedBalance" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="limitedProfit" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="limitedMarginLevel" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="limitedMaxDrawdown" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="limitedConsecutiveLoss" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="limitedConsecutiveLossDeals" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="limitedConsecutiveWin" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="limitedConsecutiveWinDeals" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mt4EATesterLimits", propOrder = {
    "limitedBalance",
    "limitedProfit",
    "limitedMarginLevel",
    "limitedMaxDrawdown",
    "limitedConsecutiveLoss",
    "limitedConsecutiveLossDeals",
    "limitedConsecutiveWin",
    "limitedConsecutiveWinDeals"
})
public class Mt4EATesterLimits {

    protected double limitedBalance;
    protected double limitedProfit;
    protected double limitedMarginLevel;
    protected double limitedMaxDrawdown;
    protected double limitedConsecutiveLoss;
    protected int limitedConsecutiveLossDeals;
    protected double limitedConsecutiveWin;
    protected int limitedConsecutiveWinDeals;

    /**
     * Gets the value of the limitedBalance property.
     * 
     */
    public double getLimitedBalance() {
        return limitedBalance;
    }

    /**
     * Sets the value of the limitedBalance property.
     * 
     */
    public void setLimitedBalance(double value) {
        this.limitedBalance = value;
    }

    /**
     * Gets the value of the limitedProfit property.
     * 
     */
    public double getLimitedProfit() {
        return limitedProfit;
    }

    /**
     * Sets the value of the limitedProfit property.
     * 
     */
    public void setLimitedProfit(double value) {
        this.limitedProfit = value;
    }

    /**
     * Gets the value of the limitedMarginLevel property.
     * 
     */
    public double getLimitedMarginLevel() {
        return limitedMarginLevel;
    }

    /**
     * Sets the value of the limitedMarginLevel property.
     * 
     */
    public void setLimitedMarginLevel(double value) {
        this.limitedMarginLevel = value;
    }

    /**
     * Gets the value of the limitedMaxDrawdown property.
     * 
     */
    public double getLimitedMaxDrawdown() {
        return limitedMaxDrawdown;
    }

    /**
     * Sets the value of the limitedMaxDrawdown property.
     * 
     */
    public void setLimitedMaxDrawdown(double value) {
        this.limitedMaxDrawdown = value;
    }

    /**
     * Gets the value of the limitedConsecutiveLoss property.
     * 
     */
    public double getLimitedConsecutiveLoss() {
        return limitedConsecutiveLoss;
    }

    /**
     * Sets the value of the limitedConsecutiveLoss property.
     * 
     */
    public void setLimitedConsecutiveLoss(double value) {
        this.limitedConsecutiveLoss = value;
    }

    /**
     * Gets the value of the limitedConsecutiveLossDeals property.
     * 
     */
    public int getLimitedConsecutiveLossDeals() {
        return limitedConsecutiveLossDeals;
    }

    /**
     * Sets the value of the limitedConsecutiveLossDeals property.
     * 
     */
    public void setLimitedConsecutiveLossDeals(int value) {
        this.limitedConsecutiveLossDeals = value;
    }

    /**
     * Gets the value of the limitedConsecutiveWin property.
     * 
     */
    public double getLimitedConsecutiveWin() {
        return limitedConsecutiveWin;
    }

    /**
     * Sets the value of the limitedConsecutiveWin property.
     * 
     */
    public void setLimitedConsecutiveWin(double value) {
        this.limitedConsecutiveWin = value;
    }

    /**
     * Gets the value of the limitedConsecutiveWinDeals property.
     * 
     */
    public int getLimitedConsecutiveWinDeals() {
        return limitedConsecutiveWinDeals;
    }

    /**
     * Sets the value of the limitedConsecutiveWinDeals property.
     * 
     */
    public void setLimitedConsecutiveWinDeals(int value) {
        this.limitedConsecutiveWinDeals = value;
    }

}
