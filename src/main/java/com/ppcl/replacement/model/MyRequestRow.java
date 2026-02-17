package com.ppcl.replacement.model;

import java.sql.Timestamp;

public class MyRequestRow {
    private int id;
    private String clientId;
    private String clientName;
    private String signingBranch;
    private String branch;
    private String locationSummary;
    private int printerCount;
    private String currentStage;
    private String currentStageName;
    private String currentOwnerUserId;
    private String stageOwnerName;
    private Timestamp createdAt;
    private boolean editable;
    private boolean firstActionTaken;
    private String existingModel;
    private String newModel;
    private Integer serviceCallId;
    private String status;
    private int allotedPrinterCount;

    // TAT fields
    private long tatActualMinutes;
    private long tatDurationMinutes;
    private String tatUnit;
    private double tatPercentage;
    private String tatStatus; // WITHIN, WARNING, BREACH

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public int getAllotedPrinterCount() {
        return allotedPrinterCount;
    }

    public void setAllotedPrinterCount(int allotedPrinterCount) {
        this.allotedPrinterCount = allotedPrinterCount;
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

    public String getSigningBranch() {
        return signingBranch;
    }

    public void setSigningBranch(final String signingBranch) {
        this.signingBranch = signingBranch;
    }

    public String getLocationSummary() {
        return locationSummary;
    }

    public void setLocationSummary(final String locationSummary) {
        this.locationSummary = locationSummary;
    }

    public int getPrinterCount() {
        return printerCount;
    }

    public void setPrinterCount(final int printerCount) {
        this.printerCount = printerCount;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(final String currentStage) {
        this.currentStage = currentStage;
    }

    public String getCurrentStageName() {
        return currentStageName;
    }

    public void setCurrentStageName(final String currentStageName) {
        this.currentStageName = currentStageName;
    }

    public String getCurrentOwnerUserId() {
        return currentOwnerUserId;
    }

    public void setCurrentOwnerUserId(final String currentOwnerUserId) {
        this.currentOwnerUserId = currentOwnerUserId;
    }

    public String getStageOwnerName() {
        return stageOwnerName;
    }

    public void setStageOwnerName(final String stageOwnerName) {
        this.stageOwnerName = stageOwnerName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    public boolean isFirstActionTaken() {
        return firstActionTaken;
    }

    public void setFirstActionTaken(final boolean firstActionTaken) {
        this.firstActionTaken = firstActionTaken;
    }

    public String getExistingModel() {
        return existingModel;
    }

    public void setExistingModel(final String existingModel) {
        this.existingModel = existingModel;
    }

    public String getNewModel() {
        return newModel;
    }

    public void setNewModel(final String newModel) {
        this.newModel = newModel;
    }

    public Integer getServiceCallId() {
        return serviceCallId;
    }

    public void setServiceCallId(final Integer serviceCallId) {
        this.serviceCallId = serviceCallId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public long getTatActualMinutes() {
        return tatActualMinutes;
    }

    public void setTatActualMinutes(final long tatActualMinutes) {
        this.tatActualMinutes = tatActualMinutes;
    }

    public long getTatDurationMinutes() {
        return tatDurationMinutes;
    }

    public void setTatDurationMinutes(final long tatDurationMinutes) {
        this.tatDurationMinutes = tatDurationMinutes;
    }

    public String getTatUnit() {
        return tatUnit;
    }

    public void setTatUnit(final String tatUnit) {
        this.tatUnit = tatUnit;
    }

    public double getTatPercentage() {
        return tatPercentage;
    }

    public void setTatPercentage(final double tatPercentage) {
        this.tatPercentage = tatPercentage;
    }

    public String getTatStatus() {
        return tatStatus;
    }

    public void setTatStatus(final String tatStatus) {
        this.tatStatus = tatStatus;
    }

    // Helper methods for display (1 working day = 8 hours = 480 minutes)
    private static final long WORKING_MINUTES_PER_DAY = 8 * 60;

    public String getTatActualDisplay() {
        if (tatDurationMinutes <= 0) return "N/A";
        if ("HOURS".equals(tatUnit)) {
            final long hours = tatActualMinutes / 60;
            return hours + " hr" + (hours != 1 ? "s" : "");
        } else {
            final long days = tatActualMinutes / WORKING_MINUTES_PER_DAY;
            if (days == 0 && tatActualMinutes > 0) {
                final long hours = tatActualMinutes / 60;
                return hours > 0 ? hours + " hr" + (hours != 1 ? "s" : "") : tatActualMinutes + " min";
            }
            return days + " day" + (days != 1 ? "s" : "");
        }
    }

    public String getTatDurationDisplay() {
        if (tatDurationMinutes <= 0) return "N/A";
        if ("HOURS".equals(tatUnit)) {
            final long hours = tatDurationMinutes / 60;
            return hours + " hr" + (hours != 1 ? "s" : "");
        } else {
            final long days = tatDurationMinutes / WORKING_MINUTES_PER_DAY;
            return days + " day" + (days != 1 ? "s" : "");
        }
    }

    public String getTatBreachDisplay() {
        if (tatPercentage <= 100) return "";
        final long breachMinutes = tatActualMinutes - tatDurationMinutes;
        if ("HOURS".equals(tatUnit)) {
            final long hours = breachMinutes / 60;
            return "+" + hours + " hr" + (hours != 1 ? "s" : "") + " breach";
        } else {
            final long days = breachMinutes / WORKING_MINUTES_PER_DAY;
            return "+" + days + " day" + (days != 1 ? "s" : "") + " breach";
        }
    }
}
