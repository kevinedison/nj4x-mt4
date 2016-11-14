
package com.jfx.net.tsapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nj4XParams complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nj4XParams">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="jfxHost" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tenant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="strategy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="symbol" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parentTenant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="period" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="historyPeriod" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="jfxPort" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="charts" type="{http://ts.nj4x.com/}nj4XChartParams" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="asynchOrdersOperations" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nj4XParams", propOrder = {
    "jfxHost",
    "tenant",
    "strategy",
    "symbol",
    "parentTenant",
    "period",
    "historyPeriod",
    "jfxPort",
    "charts",
    "asynchOrdersOperations"
})
public class Nj4XParams {

    protected String jfxHost;
    protected String tenant;
    protected String strategy;
    protected String symbol;
    protected String parentTenant;
    protected int period;
    protected int historyPeriod;
    protected int jfxPort;
    @XmlElement(nillable = true)
    protected List<Nj4XChartParams> charts;
    protected boolean asynchOrdersOperations;

    /**
     * Gets the value of the jfxHost property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJfxHost() {
        return jfxHost;
    }

    /**
     * Sets the value of the jfxHost property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJfxHost(String value) {
        this.jfxHost = value;
    }

    /**
     * Gets the value of the tenant property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTenant() {
        return tenant;
    }

    /**
     * Sets the value of the tenant property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTenant(String value) {
        this.tenant = value;
    }

    /**
     * Gets the value of the strategy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStrategy() {
        return strategy;
    }

    /**
     * Sets the value of the strategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStrategy(String value) {
        this.strategy = value;
    }

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
     * Gets the value of the parentTenant property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentTenant() {
        return parentTenant;
    }

    /**
     * Sets the value of the parentTenant property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentTenant(String value) {
        this.parentTenant = value;
    }

    /**
     * Gets the value of the period property.
     * 
     */
    public int getPeriod() {
        return period;
    }

    /**
     * Sets the value of the period property.
     * 
     */
    public void setPeriod(int value) {
        this.period = value;
    }

    /**
     * Gets the value of the historyPeriod property.
     * 
     */
    public int getHistoryPeriod() {
        return historyPeriod;
    }

    /**
     * Sets the value of the historyPeriod property.
     * 
     */
    public void setHistoryPeriod(int value) {
        this.historyPeriod = value;
    }

    /**
     * Gets the value of the jfxPort property.
     * 
     */
    public int getJfxPort() {
        return jfxPort;
    }

    /**
     * Sets the value of the jfxPort property.
     * 
     */
    public void setJfxPort(int value) {
        this.jfxPort = value;
    }

    /**
     * Gets the value of the charts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the charts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCharts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Nj4XChartParams }
     * 
     * 
     */
    public List<Nj4XChartParams> getCharts() {
        if (charts == null) {
            charts = new ArrayList<Nj4XChartParams>();
        }
        return this.charts;
    }

    /**
     * Gets the value of the asynchOrdersOperations property.
     * 
     */
    public boolean isAsynchOrdersOperations() {
        return asynchOrdersOperations;
    }

    /**
     * Sets the value of the asynchOrdersOperations property.
     * 
     */
    public void setAsynchOrdersOperations(boolean value) {
        this.asynchOrdersOperations = value;
    }

}
