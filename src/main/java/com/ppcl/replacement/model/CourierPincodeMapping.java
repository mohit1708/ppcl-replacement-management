package com.ppcl.replacement.model;

import java.sql.Timestamp;

/**
 * Courier Pincode Mapping DTO
 * Maps to COURIER_PINCODE_MAPPING table
 */
public class CourierPincodeMapping {

    private int id;
    private int courierId;
    private String courierName;
    private int pincode;        // NUMBER in DB
    private String city;
    private String state;
    private String region;
    private int status;         // 1=ACTIVE, 0=INACTIVE

    private Timestamp creationDateTime;
    private Timestamp updateDateTime;
    private String createdBy;
    private String modifiedBy;

    // For error tracking (transient - not stored in DB)
    private boolean hasError;
    private String errorMessage;

    public CourierPincodeMapping() {
    }

    // ==================== Getters/Setters ====================

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getCourierId() {
        return courierId;
    }

    public void setCourierId(final int courierId) {
        this.courierId = courierId;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(final String courierName) {
        this.courierName = courierName;
    }

    public int getPincode() {
        return pincode;
    }

    public void setPincode(final int pincode) {
        this.pincode = pincode;
    }

    // Convenience setter for String pincode
    public void setPincode(final String pincode) {
        if (pincode != null && !pincode.isEmpty()) {
            this.pincode = Integer.parseInt(pincode);
        }
    }

    // Convenience getter for String pincode (for JSON serialization)
    public String getPincodeStr() {
        return String.valueOf(pincode);
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public int getStatusCode() {
        return status;
    }

    public void setStatusCode(final int status) {
        this.status = status;
    }

    // Convenience methods for status as String (for JSON/UI)
    public String getStatus() {
        return status == 1 ? "ACTIVE" : "INACTIVE";
    }

    public void setStatus(final String status) {
        this.status = "ACTIVE".equalsIgnoreCase(status) ? 1 : 0;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public Timestamp getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(final Timestamp creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    // Alias for compatibility
    public Timestamp getCreatedAt() {
        return creationDateTime;
    }

    public Timestamp getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(final Timestamp updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(final String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(final boolean hasError) {
        this.hasError = hasError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
