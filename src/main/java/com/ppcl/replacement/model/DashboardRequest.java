package com.ppcl.replacement.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model class for Dashboard replacement request row
 * Contains all fields needed for the detailed request list view
 */
public class DashboardRequest {

    // Primary identifiers
    private int id;
    private String displayId;           // Display ID from database (no formatting)

    // Client information
    private int clientId;
    private String clientName;
    private String clientCity;
    private String clientBranch;

    // Owner information
    private int currentOwnerId;
    private String currentOwnerUserId;
    private String currentOwnerName;
    private String currentOwnerDepartment;

    // Stage information
    private int currentStageId;
    private String currentStageCode;
    private String currentStageDescription;

    // Account Manager information (conditional based on SOURCE)
    private String accountManagerUserId;
    private String accountManagerName;

    // Requester information
    private int requesterUserId;
    private String requesterUserIdStr;
    private String requesterName;

    // Request details
    private String status;
    private String source;
    private Integer serviceCallId;
    private int reasonId;
    private String reasonName;
    private String replacementType;

    // TAT information
    private BigDecimal tatPercentage;
    private String tatStatus;           // "Within TAT" or "Beyond TAT"

    // Printer count
    private int printerCount;

    // Dates
    private Timestamp creationDateTime;
    private Timestamp updateDateTime;

    /**
     * Return display ID - just returns the database ID without formatting
     */
    public String getDisplayId() {
        return displayId != null ? displayId : String.valueOf(id);
    }

    /**
     * Calculate TAT status from percentage
     */
    public String getTatStatus() {
        if (tatStatus != null) {
            return tatStatus;
        }
        if (tatPercentage != null) {
            return tatPercentage.compareTo(BigDecimal.valueOf(100)) >= 0
                    ? "Beyond TAT" : "Within TAT";
        }
        return "Unknown";
    }

    /**
     * Check if request is within TAT
     */
    public boolean isWithinTat() {
        return tatPercentage == null || tatPercentage.compareTo(BigDecimal.valueOf(100)) < 0;
    }

    /**
     * Get CSS badge class based on status
     */
    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status.toUpperCase()) {
            case "OPEN":
            case "PENDING":
                return "badge-warning";
            case "COMPLETED":
                return "badge-success";
            case "REJECTED":
                return "badge-danger";
            default:
                return "badge-secondary";
        }
    }

    /**
     * Get CSS badge class for TAT status
     */
    public String getTatBadgeClass() {
        return isWithinTat() ? "badge-success" : "badge-danger";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void setDisplayId(final String displayId) {
        this.displayId = displayId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(final int clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    public String getClientCity() {
        return clientCity;
    }

    public void setClientCity(final String clientCity) {
        this.clientCity = clientCity;
    }

    public String getClientBranch() {
        return clientBranch;
    }

    public void setClientBranch(final String clientBranch) {
        this.clientBranch = clientBranch;
    }

    public int getCurrentOwnerId() {
        return currentOwnerId;
    }

    public void setCurrentOwnerId(final int currentOwnerId) {
        this.currentOwnerId = currentOwnerId;
    }

    public String getCurrentOwnerUserId() {
        return currentOwnerUserId;
    }

    public void setCurrentOwnerUserId(final String currentOwnerUserId) {
        this.currentOwnerUserId = currentOwnerUserId;
    }

    public String getCurrentOwnerName() {
        return currentOwnerName;
    }

    public void setCurrentOwnerName(final String currentOwnerName) {
        this.currentOwnerName = currentOwnerName;
    }

    public String getCurrentOwnerDepartment() {
        return currentOwnerDepartment;
    }

    public void setCurrentOwnerDepartment(final String currentOwnerDepartment) {
        this.currentOwnerDepartment = currentOwnerDepartment;
    }

    public int getCurrentStageId() {
        return currentStageId;
    }

    public void setCurrentStageId(final int currentStageId) {
        this.currentStageId = currentStageId;
    }

    public String getCurrentStageCode() {
        return currentStageCode;
    }

    public void setCurrentStageCode(final String currentStageCode) {
        this.currentStageCode = currentStageCode;
    }

    public String getCurrentStageDescription() {
        return currentStageDescription;
    }

    public void setCurrentStageDescription(final String currentStageDescription) {
        this.currentStageDescription = currentStageDescription;
    }

    public String getAccountManagerUserId() {
        return accountManagerUserId;
    }

    public void setAccountManagerUserId(final String accountManagerUserId) {
        this.accountManagerUserId = accountManagerUserId;
    }

    public String getAccountManagerName() {
        return accountManagerName;
    }

    public void setAccountManagerName(final String accountManagerName) {
        this.accountManagerName = accountManagerName;
    }

    public int getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(final int requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public String getRequesterUserIdStr() {
        return requesterUserIdStr;
    }

    public void setRequesterUserIdStr(final String requesterUserIdStr) {
        this.requesterUserIdStr = requesterUserIdStr;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(final String requesterName) {
        this.requesterName = requesterName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public Integer getServiceCallId() {
        return serviceCallId;
    }

    public void setServiceCallId(final Integer serviceCallId) {
        this.serviceCallId = serviceCallId;
    }

    public int getReasonId() {
        return reasonId;
    }

    public void setReasonId(final int reasonId) {
        this.reasonId = reasonId;
    }

    public String getReasonName() {
        return reasonName;
    }

    public void setReasonName(final String reasonName) {
        this.reasonName = reasonName;
    }

    public String getReplacementType() {
        return replacementType;
    }

    public void setReplacementType(final String replacementType) {
        this.replacementType = replacementType;
    }

    public BigDecimal getTatPercentage() {
        return tatPercentage;
    }

    public void setTatPercentage(final BigDecimal tatPercentage) {
        this.tatPercentage = tatPercentage;
    }

    public void setTatStatus(final String tatStatus) {
        this.tatStatus = tatStatus;
    }

    public int getPrinterCount() {
        return printerCount;
    }

    public void setPrinterCount(final int printerCount) {
        this.printerCount = printerCount;
    }

    public Timestamp getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(final Timestamp creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Timestamp getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(final Timestamp updateDateTime) {
        this.updateDateTime = updateDateTime;
    }
}

