package com.ppcl.replacement.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReplacementRequest {
    private int id;
    private String clientId;
    private String clientName;
    private String replacementType;
    private int reasonId;
    private String reasonName;
    private String source;
    private String requesterUserId;
    private String requesterName;
    private String currentStage;
    private String currentOwnerRole;
    private String currentOwnerUserId;
    private Integer currentEventId;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private List<ReplacementPrinter> printers;
    private Integer serviceCallId;  // New
    private String comments;
    private int tlLeadId;

    private String contactPerson;
    private String contactNumber;
    private String contactEmail;

    public int getTlLeadId() {
        return tlLeadId;
    }

    public void setTlLeadId(final int tlLeadId) {
        this.tlLeadId = tlLeadId;
    }

    public Integer getServiceCallId() {
        return serviceCallId;
    }

    public void setServiceCallId(final Integer serviceCallId) {
        this.serviceCallId = serviceCallId;
    }


    public ReplacementRequest() {
        printers = new ArrayList<>();
    }

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

    public String getReplacementType() {
        return replacementType;
    }

    public void setReplacementType(final String replacementType) {
        this.replacementType = replacementType;
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

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public String getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(final String requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(final String requesterName) {
        this.requesterName = requesterName;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(final String currentStage) {
        this.currentStage = currentStage;
    }

    public String getCurrentOwnerRole() {
        return currentOwnerRole;
    }

    public void setCurrentOwnerRole(final String currentOwnerRole) {
        this.currentOwnerRole = currentOwnerRole;
    }

    public String getCurrentOwnerUserId() {
        return currentOwnerUserId;
    }

    public void setCurrentOwnerUserId(final String currentOwnerUserId) {
        this.currentOwnerUserId = currentOwnerUserId;
    }

    public Integer getCurrentEventId() {
        return currentEventId;
    }

    public void setCurrentEventId(final Integer currentEventId) {
        this.currentEventId = currentEventId;
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

    public List<ReplacementPrinter> getPrinters() {
        return printers;
    }

    public void setPrinters(final List<ReplacementPrinter> printers) {
        this.printers = printers;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
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
}
