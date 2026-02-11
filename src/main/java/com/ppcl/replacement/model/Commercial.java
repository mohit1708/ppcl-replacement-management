package com.ppcl.replacement.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Commercial {
    private int id;
    private int printerId;
    private int reqId;
    private BigDecimal existingCost;
    private BigDecimal newCost;
    private BigDecimal existingRental;
    private BigDecimal newRental;
    private BigDecimal costDifference;
    private BigDecimal rentalDifference;
    private String justification;
    private String addedBy;
    private Timestamp addedAt;
    private String updatedBy;
    private Timestamp updatedAt;
    private int status;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getPrinterId() {
        return printerId;
    }

    public void setPrinterId(final int printerId) {
        this.printerId = printerId;
    }

    public int getReqId() {
        return reqId;
    }

    public void setReqId(final int reqId) {
        this.reqId = reqId;
    }

    public BigDecimal getExistingCost() {
        return existingCost;
    }

    public void setExistingCost(final BigDecimal existingCost) {
        this.existingCost = existingCost;
    }

    public BigDecimal getNewCost() {
        return newCost;
    }

    public void setNewCost(final BigDecimal newCost) {
        this.newCost = newCost;
    }

    public BigDecimal getExistingRental() {
        return existingRental;
    }

    public void setExistingRental(final BigDecimal existingRental) {
        this.existingRental = existingRental;
    }

    public BigDecimal getNewRental() {
        return newRental;
    }

    public void setNewRental(final BigDecimal newRental) {
        this.newRental = newRental;
    }

    public BigDecimal getCostDifference() {
        return costDifference;
    }

    public void setCostDifference(final BigDecimal costDifference) {
        this.costDifference = costDifference;
    }

    public BigDecimal getRentalDifference() {
        return rentalDifference;
    }

    public void setRentalDifference(final BigDecimal rentalDifference) {
        this.rentalDifference = rentalDifference;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(final String addedBy) {
        this.addedBy = addedBy;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(final Timestamp addedAt) {
        this.addedAt = addedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(final String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }
}
