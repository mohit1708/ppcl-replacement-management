package com.ppcl.replacement.model;

public class ServiceCall {
    private int callId;
    private String clientId;
    private String clientName;
    private String callBy;
    private String contactNo;
    private String pModel;
    private String pSerial;
    private int brId;

    // Getters and Setters
    public int getCallId() {
        return callId;
    }

    public void setCallId(final int callId) {
        this.callId = callId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    public String getCallBy() {
        return callBy;
    }

    public void setCallBy(final String callBy) {
        this.callBy = callBy;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(final String contactNo) {
        this.contactNo = contactNo;
    }

    public String getPModel() {
        return pModel;
    }

    public void setPModel(final String pModel) {
        this.pModel = pModel;
    }

    public String getPSerial() {
        return pSerial;
    }

    public void setPSerial(final String pSerial) {
        this.pSerial = pSerial;
    }

    public int getBrId() {
        return brId;
    }

    public void setBrId(final int brId) {
        this.brId = brId;
    }
}
