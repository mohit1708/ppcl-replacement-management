package com.ppcl.replacement.model;

import java.sql.Timestamp;

/**
 * Model for Replacement Register view - Central register with signed letter tracking
 */
public class RegisterRequestRow {
    private int id;
    private String letterRef;          // Letter reference number (e.g., R-12345)
    private String clientId;
    private String clientName;
    private int printerCount;
    private Timestamp generationDate;  // Letter generation date
    private String currentStage;
    private String currentStageName;
    private String status;             // OPEN, PENDING, COMPLETED, REJECTED

    // Signed letter fields
    private String signedLetterPath;   // Path to uploaded signed letter PDF
    private Timestamp signedUploadDate;
    private boolean isLocked;          // True if signed copy uploaded (frozen)

    // Printers info for modal display
    private java.util.List<ReplacementPrinter> printers;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getLetterRef() {
        return letterRef;
    }

    public void setLetterRef(final String letterRef) {
        this.letterRef = letterRef;
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

    public int getPrinterCount() {
        return printerCount;
    }

    public void setPrinterCount(final int printerCount) {
        this.printerCount = printerCount;
    }

    public Timestamp getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(final Timestamp generationDate) {
        this.generationDate = generationDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getSignedLetterPath() {
        return signedLetterPath;
    }

    public void setSignedLetterPath(final String signedLetterPath) {
        this.signedLetterPath = signedLetterPath;
    }

    public Timestamp getSignedUploadDate() {
        return signedUploadDate;
    }

    public void setSignedUploadDate(final Timestamp signedUploadDate) {
        this.signedUploadDate = signedUploadDate;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(final boolean locked) {
        isLocked = locked;
    }

    public java.util.List<ReplacementPrinter> getPrinters() {
        return printers;
    }

    public void setPrinters(final java.util.List<ReplacementPrinter> printers) {
        this.printers = printers;
    }

    // Helper methods
    public String getDisplayStatus() {
        if (isLocked) {
            return "Signed & Locked";
        }
        return "Pending Signature";
    }

    public boolean hasSigned() {
        return signedLetterPath != null && !signedLetterPath.isEmpty();
    }
}
