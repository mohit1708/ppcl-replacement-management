package com.ppcl.replacement.model;

import java.math.BigDecimal;

public class CurrentCommercial {
    private int agrProdId;
    private String serial;
    private String modelName;
    private String agreementNo;
    private String clientName;
    private String city;
    private String location;
    private BigDecimal blackRate;
    private BigDecimal colorRate;
    private int commerceType;

    // Last 6 months billing
    private BigDecimal month1Amt;
    private BigDecimal month2Amt;
    private BigDecimal month3Amt;
    private BigDecimal month4Amt;
    private BigDecimal month5Amt;
    private BigDecimal month6Amt;
    private BigDecimal total6Months;

    // Getters and Setters
    public int getAgrProdId() {
        return agrProdId;
    }

    public void setAgrProdId(final int agrProdId) {
        this.agrProdId = agrProdId;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(final String serial) {
        this.serial = serial;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(final String modelName) {
        this.modelName = modelName;
    }

    public String getAgreementNo() {
        return agreementNo;
    }

    public void setAgreementNo(final String agreementNo) {
        this.agreementNo = agreementNo;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public BigDecimal getBlackRate() {
        return blackRate;
    }

    public void setBlackRate(final BigDecimal blackRate) {
        this.blackRate = blackRate;
    }

    public BigDecimal getColorRate() {
        return colorRate;
    }

    public void setColorRate(final BigDecimal colorRate) {
        this.colorRate = colorRate;
    }

    public int getCommerceType() {
        return commerceType;
    }

    public void setCommerceType(final int commerceType) {
        this.commerceType = commerceType;
    }

    public BigDecimal getMonth1Amt() {
        return month1Amt;
    }

    public void setMonth1Amt(final BigDecimal month1Amt) {
        this.month1Amt = month1Amt;
    }

    public BigDecimal getMonth2Amt() {
        return month2Amt;
    }

    public void setMonth2Amt(final BigDecimal month2Amt) {
        this.month2Amt = month2Amt;
    }

    public BigDecimal getMonth3Amt() {
        return month3Amt;
    }

    public void setMonth3Amt(final BigDecimal month3Amt) {
        this.month3Amt = month3Amt;
    }

    public BigDecimal getMonth4Amt() {
        return month4Amt;
    }

    public void setMonth4Amt(final BigDecimal month4Amt) {
        this.month4Amt = month4Amt;
    }

    public BigDecimal getMonth5Amt() {
        return month5Amt;
    }

    public void setMonth5Amt(final BigDecimal month5Amt) {
        this.month5Amt = month5Amt;
    }

    public BigDecimal getMonth6Amt() {
        return month6Amt;
    }

    public void setMonth6Amt(final BigDecimal month6Amt) {
        this.month6Amt = month6Amt;
    }

    public BigDecimal getTotal6Months() {
        return total6Months;
    }

    public void setTotal6Months(final BigDecimal total6Months) {
        this.total6Months = total6Months;
    }
}
