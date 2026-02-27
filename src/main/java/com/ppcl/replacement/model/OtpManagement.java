package com.ppcl.replacement.model;

import java.sql.Timestamp;

/**
 * Model class for OTP Management table.
 * Represents OTP state for pickup and other features.
 * Links to REPLACEMENT_PULLBACK via pullbackId.
 */
public class OtpManagement {
    
    private Long id;
    private Long pullbackId;
    private String mobileNumber;
    private String otpValue;
    private Timestamp otpExpiryTime;
    private Integer attemptCount;
    private Timestamp blockUntilTime;
    private Timestamp otpValidatedAt;
    private String waSendStatus;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public OtpManagement() {
    }

    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPullbackId() {
        return pullbackId;
    }

    public void setPullbackId(Long pullbackId) {
        this.pullbackId = pullbackId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOtpValue() {
        return otpValue;
    }

    public void setOtpValue(String otpValue) {
        this.otpValue = otpValue;
    }

    public Timestamp getOtpExpiryTime() {
        return otpExpiryTime;
    }

    public void setOtpExpiryTime(Timestamp otpExpiryTime) {
        this.otpExpiryTime = otpExpiryTime;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Timestamp getBlockUntilTime() {
        return blockUntilTime;
    }

    public void setBlockUntilTime(Timestamp blockUntilTime) {
        this.blockUntilTime = blockUntilTime;
    }

    public Timestamp getOtpValidatedAt() {
        return otpValidatedAt;
    }

    public void setOtpValidatedAt(Timestamp otpValidatedAt) {
        this.otpValidatedAt = otpValidatedAt;
    }

    public String getWaSendStatus() {
        return waSendStatus;
    }

    public void setWaSendStatus(String waSendStatus) {
        this.waSendStatus = waSendStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
