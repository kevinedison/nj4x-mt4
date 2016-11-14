
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for optimizationRun complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="optimizationRun">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="expertParameters" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="profit" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="totalTrades" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="profitFactor" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="expectedPayoff" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="drawdownValue" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="drawdownPct" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "optimizationRun", propOrder = {
    "expertParameters",
    "profit",
    "totalTrades",
    "profitFactor",
    "expectedPayoff",
    "drawdownValue",
    "drawdownPct"
})
public class OptimizationRun {

    protected String expertParameters;
    protected double profit;
    protected double totalTrades;
    protected double profitFactor;
    protected double expectedPayoff;
    protected double drawdownValue;
    protected double drawdownPct;

    /**
     * Gets the value of the expertParameters property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpertParameters() {
        return expertParameters;
    }

    /**
     * Sets the value of the expertParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpertParameters(String value) {
        this.expertParameters = value;
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
     * Gets the value of the totalTrades property.
     * 
     */
    public double getTotalTrades() {
        return totalTrades;
    }

    /**
     * Sets the value of the totalTrades property.
     * 
     */
    public void setTotalTrades(double value) {
        this.totalTrades = value;
    }

    /**
     * Gets the value of the profitFactor property.
     * 
     */
    public double getProfitFactor() {
        return profitFactor;
    }

    /**
     * Sets the value of the profitFactor property.
     * 
     */
    public void setProfitFactor(double value) {
        this.profitFactor = value;
    }

    /**
     * Gets the value of the expectedPayoff property.
     * 
     */
    public double getExpectedPayoff() {
        return expectedPayoff;
    }

    /**
     * Sets the value of the expectedPayoff property.
     * 
     */
    public void setExpectedPayoff(double value) {
        this.expectedPayoff = value;
    }

    /**
     * Gets the value of the drawdownValue property.
     * 
     */
    public double getDrawdownValue() {
        return drawdownValue;
    }

    /**
     * Sets the value of the drawdownValue property.
     * 
     */
    public void setDrawdownValue(double value) {
        this.drawdownValue = value;
    }

    /**
     * Gets the value of the drawdownPct property.
     * 
     */
    public double getDrawdownPct() {
        return drawdownPct;
    }

    /**
     * Sets the value of the drawdownPct property.
     * 
     */
    public void setDrawdownPct(double value) {
        this.drawdownPct = value;
    }

}
