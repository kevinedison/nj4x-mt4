
package com.jfx.net.tsapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for singleTestReport complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="singleTestReport">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="symbol" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="period" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="model" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="initialDeposit" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="spread" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parameters" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="barsInTest" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ticksModelled" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="mismatchedChartsErrors" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="modellingQualityPct" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="totalNetProfit" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="grossProfit" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="grossLoss" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="profitFactor" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="expectedPayoff" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="absoluteDrawdown" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="maximumDrawdown" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="relativeDrawdown" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="totalTrades" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="shortPositionsWon" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="longPositionsWon" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="profitTrades" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="lossTrades" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="maximumConsecutiveWins" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="maximumConsecutiveLosses" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="averageConsecutiveWins" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="averageConsecutiveLosses" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="deals" type="{http://tester.nj4x.com/}deal" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "singleTestReport", propOrder = {
    "symbol",
    "period",
    "model",
    "initialDeposit",
    "spread",
    "parameters",
    "barsInTest",
    "ticksModelled",
    "mismatchedChartsErrors",
    "modellingQualityPct",
    "totalNetProfit",
    "grossProfit",
    "grossLoss",
    "profitFactor",
    "expectedPayoff",
    "absoluteDrawdown",
    "maximumDrawdown",
    "relativeDrawdown",
    "totalTrades",
    "shortPositionsWon",
    "longPositionsWon",
    "profitTrades",
    "lossTrades",
    "maximumConsecutiveWins",
    "maximumConsecutiveLosses",
    "averageConsecutiveWins",
    "averageConsecutiveLosses",
    "deals"
})
public class SingleTestReport {

    protected String symbol;
    protected String period;
    protected String model;
    protected double initialDeposit;
    protected String spread;
    protected String parameters;
    protected int barsInTest;
    protected int ticksModelled;
    protected int mismatchedChartsErrors;
    protected double modellingQualityPct;
    protected double totalNetProfit;
    protected double grossProfit;
    protected double grossLoss;
    protected double profitFactor;
    protected double expectedPayoff;
    protected double absoluteDrawdown;
    protected double maximumDrawdown;
    protected double relativeDrawdown;
    protected int totalTrades;
    protected int shortPositionsWon;
    protected int longPositionsWon;
    protected int profitTrades;
    protected int lossTrades;
    protected int maximumConsecutiveWins;
    protected int maximumConsecutiveLosses;
    protected int averageConsecutiveWins;
    protected int averageConsecutiveLosses;
    @XmlElement(nillable = true)
    protected List<Deal> deals;

    /**
     * Gets the value of the symbol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Sets the value of the symbol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSymbol(String value) {
        this.symbol = value;
    }

    /**
     * Gets the value of the period property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPeriod() {
        return period;
    }

    /**
     * Sets the value of the period property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPeriod(String value) {
        this.period = value;
    }

    /**
     * Gets the value of the model property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModel(String value) {
        this.model = value;
    }

    /**
     * Gets the value of the initialDeposit property.
     * 
     */
    public double getInitialDeposit() {
        return initialDeposit;
    }

    /**
     * Sets the value of the initialDeposit property.
     * 
     */
    public void setInitialDeposit(double value) {
        this.initialDeposit = value;
    }

    /**
     * Gets the value of the spread property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpread() {
        return spread;
    }

    /**
     * Sets the value of the spread property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpread(String value) {
        this.spread = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParameters(String value) {
        this.parameters = value;
    }

    /**
     * Gets the value of the barsInTest property.
     * 
     */
    public int getBarsInTest() {
        return barsInTest;
    }

    /**
     * Sets the value of the barsInTest property.
     * 
     */
    public void setBarsInTest(int value) {
        this.barsInTest = value;
    }

    /**
     * Gets the value of the ticksModelled property.
     * 
     */
    public int getTicksModelled() {
        return ticksModelled;
    }

    /**
     * Sets the value of the ticksModelled property.
     * 
     */
    public void setTicksModelled(int value) {
        this.ticksModelled = value;
    }

    /**
     * Gets the value of the mismatchedChartsErrors property.
     * 
     */
    public int getMismatchedChartsErrors() {
        return mismatchedChartsErrors;
    }

    /**
     * Sets the value of the mismatchedChartsErrors property.
     * 
     */
    public void setMismatchedChartsErrors(int value) {
        this.mismatchedChartsErrors = value;
    }

    /**
     * Gets the value of the modellingQualityPct property.
     * 
     */
    public double getModellingQualityPct() {
        return modellingQualityPct;
    }

    /**
     * Sets the value of the modellingQualityPct property.
     * 
     */
    public void setModellingQualityPct(double value) {
        this.modellingQualityPct = value;
    }

    /**
     * Gets the value of the totalNetProfit property.
     * 
     */
    public double getTotalNetProfit() {
        return totalNetProfit;
    }

    /**
     * Sets the value of the totalNetProfit property.
     * 
     */
    public void setTotalNetProfit(double value) {
        this.totalNetProfit = value;
    }

    /**
     * Gets the value of the grossProfit property.
     * 
     */
    public double getGrossProfit() {
        return grossProfit;
    }

    /**
     * Sets the value of the grossProfit property.
     * 
     */
    public void setGrossProfit(double value) {
        this.grossProfit = value;
    }

    /**
     * Gets the value of the grossLoss property.
     * 
     */
    public double getGrossLoss() {
        return grossLoss;
    }

    /**
     * Sets the value of the grossLoss property.
     * 
     */
    public void setGrossLoss(double value) {
        this.grossLoss = value;
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
     * Gets the value of the absoluteDrawdown property.
     * 
     */
    public double getAbsoluteDrawdown() {
        return absoluteDrawdown;
    }

    /**
     * Sets the value of the absoluteDrawdown property.
     * 
     */
    public void setAbsoluteDrawdown(double value) {
        this.absoluteDrawdown = value;
    }

    /**
     * Gets the value of the maximumDrawdown property.
     * 
     */
    public double getMaximumDrawdown() {
        return maximumDrawdown;
    }

    /**
     * Sets the value of the maximumDrawdown property.
     * 
     */
    public void setMaximumDrawdown(double value) {
        this.maximumDrawdown = value;
    }

    /**
     * Gets the value of the relativeDrawdown property.
     * 
     */
    public double getRelativeDrawdown() {
        return relativeDrawdown;
    }

    /**
     * Sets the value of the relativeDrawdown property.
     * 
     */
    public void setRelativeDrawdown(double value) {
        this.relativeDrawdown = value;
    }

    /**
     * Gets the value of the totalTrades property.
     * 
     */
    public int getTotalTrades() {
        return totalTrades;
    }

    /**
     * Sets the value of the totalTrades property.
     * 
     */
    public void setTotalTrades(int value) {
        this.totalTrades = value;
    }

    /**
     * Gets the value of the shortPositionsWon property.
     * 
     */
    public int getShortPositionsWon() {
        return shortPositionsWon;
    }

    /**
     * Sets the value of the shortPositionsWon property.
     * 
     */
    public void setShortPositionsWon(int value) {
        this.shortPositionsWon = value;
    }

    /**
     * Gets the value of the longPositionsWon property.
     * 
     */
    public int getLongPositionsWon() {
        return longPositionsWon;
    }

    /**
     * Sets the value of the longPositionsWon property.
     * 
     */
    public void setLongPositionsWon(int value) {
        this.longPositionsWon = value;
    }

    /**
     * Gets the value of the profitTrades property.
     * 
     */
    public int getProfitTrades() {
        return profitTrades;
    }

    /**
     * Sets the value of the profitTrades property.
     * 
     */
    public void setProfitTrades(int value) {
        this.profitTrades = value;
    }

    /**
     * Gets the value of the lossTrades property.
     * 
     */
    public int getLossTrades() {
        return lossTrades;
    }

    /**
     * Sets the value of the lossTrades property.
     * 
     */
    public void setLossTrades(int value) {
        this.lossTrades = value;
    }

    /**
     * Gets the value of the maximumConsecutiveWins property.
     * 
     */
    public int getMaximumConsecutiveWins() {
        return maximumConsecutiveWins;
    }

    /**
     * Sets the value of the maximumConsecutiveWins property.
     * 
     */
    public void setMaximumConsecutiveWins(int value) {
        this.maximumConsecutiveWins = value;
    }

    /**
     * Gets the value of the maximumConsecutiveLosses property.
     * 
     */
    public int getMaximumConsecutiveLosses() {
        return maximumConsecutiveLosses;
    }

    /**
     * Sets the value of the maximumConsecutiveLosses property.
     * 
     */
    public void setMaximumConsecutiveLosses(int value) {
        this.maximumConsecutiveLosses = value;
    }

    /**
     * Gets the value of the averageConsecutiveWins property.
     * 
     */
    public int getAverageConsecutiveWins() {
        return averageConsecutiveWins;
    }

    /**
     * Sets the value of the averageConsecutiveWins property.
     * 
     */
    public void setAverageConsecutiveWins(int value) {
        this.averageConsecutiveWins = value;
    }

    /**
     * Gets the value of the averageConsecutiveLosses property.
     * 
     */
    public int getAverageConsecutiveLosses() {
        return averageConsecutiveLosses;
    }

    /**
     * Sets the value of the averageConsecutiveLosses property.
     * 
     */
    public void setAverageConsecutiveLosses(int value) {
        this.averageConsecutiveLosses = value;
    }

    /**
     * Gets the value of the deals property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deals property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeals().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Deal }
     * 
     * 
     */
    public List<Deal> getDeals() {
        if (deals == null) {
            deals = new ArrayList<Deal>();
        }
        return this.deals;
    }

}
