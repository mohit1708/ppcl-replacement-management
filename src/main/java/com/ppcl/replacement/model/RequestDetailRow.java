package com.ppcl.replacement.model;

import java.sql.Timestamp;

public class RequestDetailRow {
    private int id;
    private String clientId;
    private String clientName;
    private String requesterName;
    private String requesterRole;
    private String accountManager;
    private int printerCount;
    private Timestamp createdAt;
    private String currentStage;
    private String currentStageName;
    private boolean firstActionTaken;
    private boolean editable;
    private String status;

    // Stage owner info
    private String stageOwnerName;

    // Sign-in user (Account Manager from CLIENT_ACCESS)
    private String signInUserId;

    // TAT and Last Action fields
    private Timestamp lastActionDate;
    private String lastActionBy;
    private int stageTatDays;
    private int overallTatDays;
    private int targetTatDays;
    private double tatPercentage;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
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

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(final String requesterName) {
        this.requesterName = requesterName;
    }

    public String getRequesterRole() {
        return requesterRole;
    }

    public void setRequesterRole(final String requesterRole) {
        this.requesterRole = requesterRole;
    }

    public String getAccountManager() {
        return accountManager;
    }

    public void setAccountManager(final String accountManager) {
        this.accountManager = accountManager;
    }

    public int getPrinterCount() {
        return printerCount;
    }

    public void setPrinterCount(final int printerCount) {
        this.printerCount = printerCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(final String currentStage) {
        this.currentStage = currentStage;
    }

    public boolean isFirstActionTaken() {
        return firstActionTaken;
    }

    public void setFirstActionTaken(final boolean firstActionTaken) {
        this.firstActionTaken = firstActionTaken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getCurrentStageName() {
        return currentStageName;
    }

    public void setCurrentStageName(final String currentStageName) {
        this.currentStageName = currentStageName;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    public String getStageOwnerName() {
        return stageOwnerName;
    }

    public void setStageOwnerName(final String stageOwnerName) {
        this.stageOwnerName = stageOwnerName;
    }

    public Timestamp getLastActionDate() {
        return lastActionDate;
    }

    public void setLastActionDate(final Timestamp lastActionDate) {
        this.lastActionDate = lastActionDate;
    }

    public String getLastActionBy() {
        return lastActionBy;
    }

    public void setLastActionBy(final String lastActionBy) {
        this.lastActionBy = lastActionBy;
    }

    public int getStageTatDays() {
        return stageTatDays;
    }

    public void setStageTatDays(final int stageTatDays) {
        this.stageTatDays = stageTatDays;
    }

    public int getOverallTatDays() {
        return overallTatDays;
    }

    public void setOverallTatDays(final int overallTatDays) {
        this.overallTatDays = overallTatDays;
    }

    public int getTargetTatDays() {
        return targetTatDays;
    }

    public void setTargetTatDays(final int targetTatDays) {
        this.targetTatDays = targetTatDays;
    }

    public double getTatPercentage() {
        return tatPercentage;
    }

    public void setTatPercentage(final double tatPercentage) {
        this.tatPercentage = tatPercentage;
    }

    public String getSignInUserId() {
        return signInUserId;
    }

    public void setSignInUserId(final String signInUserId) {
        this.signInUserId = signInUserId;
    }
}
