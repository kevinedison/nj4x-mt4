
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for mt4EATesterConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mt4EATesterConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="model" type="{http://tester.nj4x.com/}testModel" minOccurs="0"/>
 *         &lt;element name="spread" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="doOptimization" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="dateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="dateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="positions" type="{http://tester.nj4x.com/}eaTestPositions" minOccurs="0"/>
 *         &lt;element name="initialDeposit" type="{http://tester.nj4x.com/}deposit" minOccurs="0"/>
 *         &lt;element name="fitness" type="{http://tester.nj4x.com/}eaTestOptimizedParameter" minOccurs="0"/>
 *         &lt;element name="useGeneticAlgorithm" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="terminalHomeDirectorySuffix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mt4EATesterConfiguration", propOrder = {
    "model",
    "spread",
    "doOptimization",
    "dateFrom",
    "dateTo",
    "positions",
    "initialDeposit",
    "fitness",
    "useGeneticAlgorithm",
    "terminalHomeDirectorySuffix"
})
public class Mt4EATesterConfiguration {

    protected TestModel model;
    protected int spread;
    protected boolean doOptimization;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTo;
    protected EaTestPositions positions;
    protected Deposit initialDeposit;
    protected EaTestOptimizedParameter fitness;
    protected boolean useGeneticAlgorithm;
    protected String terminalHomeDirectorySuffix;

    /**
     * Gets the value of the model property.
     * 
     * @return
     *     possible object is
     *     {@link TestModel }
     *     
     */
    public TestModel getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     * 
     * @param value
     *     allowed object is
     *     {@link TestModel }
     *     
     */
    public void setModel(TestModel value) {
        this.model = value;
    }

    /**
     * Gets the value of the spread property.
     * 
     */
    public int getSpread() {
        return spread;
    }

    /**
     * Sets the value of the spread property.
     * 
     */
    public void setSpread(int value) {
        this.spread = value;
    }

    /**
     * Gets the value of the doOptimization property.
     * 
     */
    public boolean isDoOptimization() {
        return doOptimization;
    }

    /**
     * Sets the value of the doOptimization property.
     * 
     */
    public void setDoOptimization(boolean value) {
        this.doOptimization = value;
    }

    /**
     * Gets the value of the dateFrom property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateFrom() {
        return dateFrom;
    }

    /**
     * Sets the value of the dateFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateFrom(XMLGregorianCalendar value) {
        this.dateFrom = value;
    }

    /**
     * Gets the value of the dateTo property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTo() {
        return dateTo;
    }

    /**
     * Sets the value of the dateTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTo(XMLGregorianCalendar value) {
        this.dateTo = value;
    }

    /**
     * Gets the value of the positions property.
     * 
     * @return
     *     possible object is
     *     {@link EaTestPositions }
     *     
     */
    public EaTestPositions getPositions() {
        return positions;
    }

    /**
     * Sets the value of the positions property.
     * 
     * @param value
     *     allowed object is
     *     {@link EaTestPositions }
     *     
     */
    public void setPositions(EaTestPositions value) {
        this.positions = value;
    }

    /**
     * Gets the value of the initialDeposit property.
     * 
     * @return
     *     possible object is
     *     {@link Deposit }
     *     
     */
    public Deposit getInitialDeposit() {
        return initialDeposit;
    }

    /**
     * Sets the value of the initialDeposit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Deposit }
     *     
     */
    public void setInitialDeposit(Deposit value) {
        this.initialDeposit = value;
    }

    /**
     * Gets the value of the fitness property.
     * 
     * @return
     *     possible object is
     *     {@link EaTestOptimizedParameter }
     *     
     */
    public EaTestOptimizedParameter getFitness() {
        return fitness;
    }

    /**
     * Sets the value of the fitness property.
     * 
     * @param value
     *     allowed object is
     *     {@link EaTestOptimizedParameter }
     *     
     */
    public void setFitness(EaTestOptimizedParameter value) {
        this.fitness = value;
    }

    /**
     * Gets the value of the useGeneticAlgorithm property.
     * 
     */
    public boolean isUseGeneticAlgorithm() {
        return useGeneticAlgorithm;
    }

    /**
     * Sets the value of the useGeneticAlgorithm property.
     * 
     */
    public void setUseGeneticAlgorithm(boolean value) {
        this.useGeneticAlgorithm = value;
    }

    /**
     * Gets the value of the terminalHomeDirectorySuffix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerminalHomeDirectorySuffix() {
        return terminalHomeDirectorySuffix;
    }

    /**
     * Sets the value of the terminalHomeDirectorySuffix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerminalHomeDirectorySuffix(String value) {
        this.terminalHomeDirectorySuffix = value;
    }

}
