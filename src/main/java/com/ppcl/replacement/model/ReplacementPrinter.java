package com.ppcl.replacement.model;

import java.sql.Timestamp;

public class ReplacementPrinter {
    private int id;
    private int reqId;
    private int clientBrId;
    private String location;
    private String city;
    private Integer agrProdId;
    private Integer existingPModelId;
    private String existingModelName;
    private String existingSerial;
    private Integer recommendedPModelId;
    private String recommendedModelName;
    private String recommendedModelText;
    private int printerType;
    private Integer newPModelId;
    private String newModelName;
    private String newSerial;
    private int replaceWithExistingComm;
    private String commercialComment;
    private String agreementNoMapped;
    private String contactPerson;
    private String contactNumber;
    private String contactEmail;
    private Integer printerOrderId;
    private Integer printerOrderItemId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer newPModelSelectedId;  // NEW (dropdown selection)
    private String newModelText;          // NEW (manual text)
    private String newPModelSource;       // NEW (DROPDOWN/MANUAL/NONE)
    private boolean isNew;
    private String printerStage;  // 0-PENDING,1-ALLOTED, 2-DISPATCHED, 3-DELIVERED, 4-INSTALLED, 5-RETURNED
    // Delivery tracking fields from PRINTER_ORDER_ITEM_ALLOT
    private String deliveryStatus;
    private String deliveredDate;
    private String agreementDate;

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String getAgreementDate() {
        return agreementDate;
    }

    public void setAgreementDate(String agreementDate) {
        this.agreementDate = agreementDate;
    }

    public String getPrinterStage() {
        return printerStage;
    }

    public void setPrinterStage(String printerStage) {
        this.printerStage = printerStage;
    }

    public ReplacementPrinter() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getReqId() {
        return reqId;
    }

    public void setReqId(final int reqId) {
        this.reqId = reqId;
    }

    public int getClientBrId() {
        return clientBrId;
    }

    public void setClientBrId(final int clientBrId) {
        this.clientBrId = clientBrId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public Integer getAgrProdId() {
        return agrProdId;
    }

    public void setAgrProdId(final Integer agrProdId) {
        this.agrProdId = agrProdId;
    }

    public Integer getExistingPModelId() {
        return existingPModelId;
    }

    public void setExistingPModelId(final Integer existingPModelId) {
        this.existingPModelId = existingPModelId;
    }

    public String getExistingModelName() {
        return existingModelName;
    }

    public void setExistingModelName(final String existingModelName) {
        this.existingModelName = existingModelName;
    }

    public String getExistingSerial() {
        return existingSerial;
    }

    public void setExistingSerial(final String existingSerial) {
        this.existingSerial = existingSerial;
    }

    public Integer getRecommendedPModelId() {
        return recommendedPModelId;
    }

    public void setRecommendedPModelId(final Integer recommendedPModelId) {
        this.recommendedPModelId = recommendedPModelId;
    }

    public String getRecommendedModelName() {
        return recommendedModelName;
    }

    public void setRecommendedModelName(final String recommendedModelName) {
        this.recommendedModelName = recommendedModelName;
    }

    public String getRecommendedModelText() {
        return recommendedModelText;
    }

    public void setRecommendedModelText(final String recommendedModelText) {
        this.recommendedModelText = recommendedModelText;
    }

    public int getPrinterType() {
        return printerType;
    }

    public void setPrinterType(final int printerType) {
        this.printerType = printerType;
    }

    public Integer getNewPModelId() {
        return newPModelId;
    }

    public void setNewPModelId(final Integer newPModelId) {
        this.newPModelId = newPModelId;
    }

    public String getNewModelName() {
        return newModelName;
    }

    public void setNewModelName(final String newModelName) {
        this.newModelName = newModelName;
    }

    public String getNewSerial() {
        return newSerial;
    }

    public void setNewSerial(final String newSerial) {
        this.newSerial = newSerial;
    }

    public int getReplaceWithExistingComm() {
        return replaceWithExistingComm;
    }

    public void setReplaceWithExistingComm(final int replaceWithExistingComm) {
        this.replaceWithExistingComm = replaceWithExistingComm;
    }

    public String getCommercialComment() {
        return commercialComment;
    }

    public void setCommercialComment(final String commercialComment) {
        this.commercialComment = commercialComment;
    }

    public String getAgreementNoMapped() {
        return agreementNoMapped;
    }

    public void setAgreementNoMapped(final String agreementNoMapped) {
        this.agreementNoMapped = agreementNoMapped;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(final String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(final String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Integer getPrinterOrderId() {
        return printerOrderId;
    }

    public void setPrinterOrderId(final Integer printerOrderId) {
        this.printerOrderId = printerOrderId;
    }

    public Integer getPrinterOrderItemId() {
        return printerOrderItemId;
    }

    public void setPrinterOrderItemId(final Integer printerOrderItemId) {
        this.printerOrderItemId = printerOrderItemId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getNewPModelSelectedId() {
        return newPModelSelectedId;
    }

    public void setNewPModelSelectedId(final Integer newPModelSelectedId) {
        this.newPModelSelectedId = newPModelSelectedId;
    }

    public String getNewModelText() {
        return newModelText;
    }

    public void setNewModelText(final String newModelText) {
        this.newModelText = newModelText;
    }

    public String getNewPModelSource() {
        return newPModelSource;
    }

    public void setNewPModelSource(final String newPModelSource) {
        this.newPModelSource = newPModelSource;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(final String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(final String deliveredDate) {
        this.deliveredDate = deliveredDate;
    }
}
