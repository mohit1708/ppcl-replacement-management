package com.ppcl.replacement.model;

public class ReplacementPrinterAgrDTO {

    // --- Replacement Printer Details ---
    private Long replacementPrinterId;
    private Long replacementRequestId;
    private Long agrProdId;

    private Long existingPModelId;
    private String existingSerial;

    private String printerType;
    private Long pageCount;
    private String pageCountComment;

    private Long printerStageId;
    private String continueExistingCommercial;
    private String amCommercialComments;

    private String location;
    private String city;

    // --- AGR Product Commercials ---
    private AgrProd agrProd;

    // --- Getters & Setters ---

    public Long getReplacementPrinterId() {
        return replacementPrinterId;
    }

    public void setReplacementPrinterId(final Long replacementPrinterId) {
        this.replacementPrinterId = replacementPrinterId;
    }

    public Long getReplacementRequestId() {
        return replacementRequestId;
    }

    public void setReplacementRequestId(final Long replacementRequestId) {
        this.replacementRequestId = replacementRequestId;
    }

    public Long getAgrProdId() {
        return agrProdId;
    }

    public void setAgrProdId(final Long agrProdId) {
        this.agrProdId = agrProdId;
    }

    public Long getExistingPModelId() {
        return existingPModelId;
    }

    public void setExistingPModelId(final Long existingPModelId) {
        this.existingPModelId = existingPModelId;
    }

    public String getExistingSerial() {
        return existingSerial;
    }

    public void setExistingSerial(final String existingSerial) {
        this.existingSerial = existingSerial;
    }

    public String getPrinterType() {
        return printerType;
    }

    public void setPrinterType(final String printerType) {
        this.printerType = printerType;
    }

    public Long getPageCount() {
        return pageCount;
    }

    public void setPageCount(final Long pageCount) {
        this.pageCount = pageCount;
    }

    public String getPageCountComment() {
        return pageCountComment;
    }

    public void setPageCountComment(final String pageCountComment) {
        this.pageCountComment = pageCountComment;
    }

    public Long getPrinterStageId() {
        return printerStageId;
    }

    public void setPrinterStageId(final Long printerStageId) {
        this.printerStageId = printerStageId;
    }

    public String getContinueExistingCommercial() {
        return continueExistingCommercial;
    }

    public void setContinueExistingCommercial(final String continueExistingCommercial) {
        this.continueExistingCommercial = continueExistingCommercial;
    }

    public String getAmCommercialComments() {
        return amCommercialComments;
    }

    public void setAmCommercialComments(final String amCommercialComments) {
        this.amCommercialComments = amCommercialComments;
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

    public AgrProd getAgrProd() {
        return agrProd;
    }

    public void setAgrProd(final AgrProd agrProd) {
        this.agrProd = agrProd;
    }

    @Override
    public String toString() {
        return "ReplacementPrinterAgrDTO{" +
                "replacementPrinterId=" + replacementPrinterId +
                ", replacementRequestId=" + replacementRequestId +
                ", agrProdId=" + agrProdId +
                ", existingPModelId=" + existingPModelId +
                ", existingSerial='" + existingSerial + '\'' +
                ", printerType='" + printerType + '\'' +
                ", pageCount=" + pageCount +
                ", pageCountComment='" + pageCountComment + '\'' +
                ", printerStageId=" + printerStageId +
                ", continueExistingCommercial='" + continueExistingCommercial + '\'' +
                ", amCommercialComments='" + amCommercialComments + '\'' +
                ", location='" + location + '\'' +
                ", city='" + city + '\'' +
                ", agrProd=" + agrProd +
                '}';
    }
}
