package com.ppcl.replacement.model;

/**
 * Model class for Printer details shown in the popup modal
 * Maps to REPLACEMENT_PRINTER_DETAILS table
 */
public class PrinterDetailRow {

    private int id;
    private int replacementRequestId;

    // Existing printer info
    private int existingModelId;
    private String existingModelName;
    private String existingSerial;

    // New printer info
    private Integer newModelSelectedId;
    private String newModelSelectedText;
    private String newModelSource;

    // Location info
    private int clientDotId;
    private String clientCity;
    private String clientBranch;

    // Contact info
    private String contactPersonName;
    private String contactPersonNumber;
    private String contactPersonEmail;

    // Additional
    private String recommendedComments;

    /**
     * Get new printer model display text
     * Returns selected model name if ID exists, otherwise returns manual text
     */
    public String getNewModelDisplay() {
        if (newModelSelectedId != null && newModelSelectedId > 0) {
            return newModelSelectedText;
        }
        return newModelSelectedText != null ? newModelSelectedText : "TBD";
    }

    /**
     * Get location display combining city and branch
     */
    public String getLocationDisplay() {
        final StringBuilder sb = new StringBuilder();
        if (clientCity != null && !clientCity.isEmpty()) {
            sb.append(clientCity);
        }
        if (clientBranch != null && !clientBranch.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" / ");
            }
            sb.append(clientBranch);
        }
        return sb.length() > 0 ? sb.toString() : "N/A";
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

    public int getExistingModelId() {
        return existingModelId;
    }

    public void setExistingModelId(final int existingModelId) {
        this.existingModelId = existingModelId;
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

    public Integer getNewModelSelectedId() {
        return newModelSelectedId;
    }

    public void setNewModelSelectedId(final Integer newModelSelectedId) {
        this.newModelSelectedId = newModelSelectedId;
    }

    public String getNewModelSelectedText() {
        return newModelSelectedText;
    }

    public void setNewModelSelectedText(final String newModelSelectedText) {
        this.newModelSelectedText = newModelSelectedText;
    }

    public String getNewModelSource() {
        return newModelSource;
    }

    public void setNewModelSource(final String newModelSource) {
        this.newModelSource = newModelSource;
    }

    public int getClientDotId() {
        return clientDotId;
    }

    public void setClientDotId(final int clientDotId) {
        this.clientDotId = clientDotId;
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

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(final String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getContactPersonNumber() {
        return contactPersonNumber;
    }

    public void setContactPersonNumber(final String contactPersonNumber) {
        this.contactPersonNumber = contactPersonNumber;
    }

    public String getContactPersonEmail() {
        return contactPersonEmail;
    }

    public void setContactPersonEmail(final String contactPersonEmail) {
        this.contactPersonEmail = contactPersonEmail;
    }

    public String getRecommendedComments() {
        return recommendedComments;
    }

    public void setRecommendedComments(final String recommendedComments) {
        this.recommendedComments = recommendedComments;
    }
}

