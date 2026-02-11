package com.ppcl.replacement.model;

public class PrinterDetail {
    private int id;
    private int agrProdId;
    private int pModelId;
    private String serial;
    private String modelName;
    private String model;
    private int clientBrId;
    private int clientBranchId;
    private String city;
    private String location;

    // Existing replacement request ID if this printer is already in a request
    private Integer existingRequestId;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getAgrProdId() {
        return agrProdId;
    }

    public void setAgrProdId(final int agrProdId) {
        this.agrProdId = agrProdId;
    }

    public int getPModelId() {
        return pModelId;
    }

    public void setPModelId(final int pModelId) {
        this.pModelId = pModelId;
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

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public int getClientBrId() {
        return clientBrId;
    }

    public void setClientBrId(final int clientBrId) {
        this.clientBrId = clientBrId;
    }

    public int getClientBranchId() {
        return clientBranchId;
    }

    public void setClientBranchId(final int clientBranchId) {
        this.clientBranchId = clientBranchId;
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

    public Integer getExistingRequestId() {
        return existingRequestId;
    }

    public void setExistingRequestId(final Integer existingRequestId) {
        this.existingRequestId = existingRequestId;
    }
}
