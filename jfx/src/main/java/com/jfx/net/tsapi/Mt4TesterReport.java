
package com.jfx.net.tsapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mt4TesterReport complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mt4TesterReport">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="log" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="error" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="optimizationReport" type="{http://tester.nj4x.com/}optimizationReport" minOccurs="0"/>
 *         &lt;element name="singleTestReport" type="{http://tester.nj4x.com/}singleTestReport" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mt4TesterReport", propOrder = {
    "log",
    "error",
    "optimizationReport",
    "singleTestReport"
})
public class Mt4TesterReport {

    protected String log;
    protected String error;
    protected OptimizationReport optimizationReport;
    protected SingleTestReport singleTestReport;

    /**
     * Gets the value of the log property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLog() {
        return log;
    }

    /**
     * Sets the value of the log property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLog(String value) {
        this.log = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setError(String value) {
        this.error = value;
    }

    /**
     * Gets the value of the optimizationReport property.
     * 
     * @return
     *     possible object is
     *     {@link OptimizationReport }
     *     
     */
    public OptimizationReport getOptimizationReport() {
        return optimizationReport;
    }

    /**
     * Sets the value of the optimizationReport property.
     * 
     * @param value
     *     allowed object is
     *     {@link OptimizationReport }
     *     
     */
    public void setOptimizationReport(OptimizationReport value) {
        this.optimizationReport = value;
    }

    /**
     * Gets the value of the singleTestReport property.
     * 
     * @return
     *     possible object is
     *     {@link SingleTestReport }
     *     
     */
    public SingleTestReport getSingleTestReport() {
        return singleTestReport;
    }

    /**
     * Sets the value of the singleTestReport property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleTestReport }
     *     
     */
    public void setSingleTestReport(SingleTestReport value) {
        this.singleTestReport = value;
    }

}
