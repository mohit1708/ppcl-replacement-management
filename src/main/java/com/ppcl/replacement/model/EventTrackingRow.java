package com.ppcl.replacement.model;

import java.sql.Timestamp;

/**
 * Model class representing a row in RPLCE_FLOW_EVENT_TRACKING
 * Used for the Event Tracking detail view
 */
public class EventTrackingRow {
    private int id;
    private int replacementRequestId;

    // Stage info
    private int stageId;
    private String stageCode;
    private String stageDescription;

    // Owner info
    private int ownerId;
    private String ownerUserId;
    private String ownerName;

    // Department info (from owner's EMP record)
    private int departmentId;
    private String departmentName;

    // Timing
    private Timestamp startAt;
    private Timestamp endAt;
    private String comments;

    // TAT
    private Double tatPercentage;

    // Client info (from replacement request)
    private String clientName;
    private String clientCity;

    // Request info
    private String requestStatus;
    private String reasonName;

    // Computed fields
    public boolean isWithinTat() {
        return tatPercentage == null || tatPercentage < 100;
    }

    public String getTatStatus() {
        if (tatPercentage == null) {
            return "Within TAT";
        }
        return tatPercentage < 100 ? "Within TAT" : "Beyond TAT";
    }

    public String getTatBadgeClass() {
        return isWithinTat() ? "badge-success" : "badge-danger";
    }

    public String getFormattedTatPercentage() {
        if (tatPercentage == null) {
            return "-";
        }
        return String.format("%.1f%%", tatPercentage);
    }

    public boolean isCompleted() {
        return endAt != null;
    }

    public String getStatusBadgeClass() {
        return isCompleted() ? "badge-secondary" : "badge-primary";
    }

    public String getEventStatus() {
        return isCompleted() ? "Completed" : "In Progress";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getReplacementRequestId() {
        return replacementRequestId;
    }

    public void setReplacementRequestId(final int replacementRequestId) {
        this.replacementRequestId = replacementRequestId;
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(final int stageId) {
        this.stageId = stageId;
    }

    public String getStageCode() {
        return stageCode;
    }

    public void setStageCode(final String stageCode) {
        this.stageCode = stageCode;
    }

    public String getStageDescription() {
        return stageDescription;
    }

    public void setStageDescription(final String stageDescription) {
        this.stageDescription = stageDescription;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(final int ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(final String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(final int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(final String departmentName) {
        this.departmentName = departmentName;
    }

    public Timestamp getStartAt() {
        return startAt;
    }

    public void setStartAt(final Timestamp startAt) {
        this.startAt = startAt;
    }

    public Timestamp getEndAt() {
        return endAt;
    }

    public void setEndAt(final Timestamp endAt) {
        this.endAt = endAt;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public Double getTatPercentage() {
        return tatPercentage;
    }

    public void setTatPercentage(final Double tatPercentage) {
        this.tatPercentage = tatPercentage;
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

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(final String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getReasonName() {
        return reasonName;
    }

    public void setReasonName(final String reasonName) {
        this.reasonName = reasonName;
    }
}

