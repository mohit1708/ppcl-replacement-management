package com.ppcl.replacement.model;

import java.sql.Timestamp;

public class PrinterHistory {
    private int agrProdId;
    private int modelId;
    private String modelName;
    private String serial;
    private Timestamp installationDate;
    private String clientName;
    private String city;
    private String location;
    private int serviceCallsCount;
    private int openServiceCalls;
    private int avgMonthlyPages;

    // Getters and Setters
    public int getAgrProdId() {
        return agrProdId;
    }

    public void setAgrProdId(final int agrProdId) {
        this.agrProdId = agrProdId;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(final int modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(final String modelName) {
        this.modelName = modelName;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(final String serial) {
        this.serial = serial;
    }

    public Timestamp getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(final Timestamp installationDate) {
        this.installationDate = installationDate;
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

    public int getServiceCallsCount() {
        return serviceCallsCount;
    }

    public void setServiceCallsCount(final int serviceCallsCount) {
        this.serviceCallsCount = serviceCallsCount;
    }

    public int getOpenServiceCalls() {
        return openServiceCalls;
    }

    public void setOpenServiceCalls(final int openServiceCalls) {
        this.openServiceCalls = openServiceCalls;
    }

    public int getAvgMonthlyPages() {
        return avgMonthlyPages;
    }

    public void setAvgMonthlyPages(final int avgMonthlyPages) {
        this.avgMonthlyPages = avgMonthlyPages;
    }
}
