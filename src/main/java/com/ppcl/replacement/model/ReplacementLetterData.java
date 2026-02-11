package com.ppcl.replacement.model;

import java.util.List;

public class ReplacementLetterData {
    private int requestId;
    private String refNo;
    private String letterDate;
    private Client client;
    private List<ReplacementPrinter> printers;
    private List<CartridgeDetail> cartridges;
    private List<String> locations;
    private String signedBy;
    private String signedAt;
    private boolean isSigned;
    private boolean replacementLetterGenerated;
    private String signedLetterPath;

    public ReplacementLetterData() {
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(final int requestId) {
        this.requestId = requestId;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(final String refNo) {
        this.refNo = refNo;
    }

    public String getLetterDate() {
        return letterDate;
    }

    public void setLetterDate(final String letterDate) {
        this.letterDate = letterDate;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(final Client client) {
        this.client = client;
    }

    public List<ReplacementPrinter> getPrinters() {
        return printers;
    }

    public void setPrinters(final List<ReplacementPrinter> printers) {
        this.printers = printers;
    }

    public List<CartridgeDetail> getCartridges() {
        return cartridges;
    }

    public void setCartridges(final List<CartridgeDetail> cartridges) {
        this.cartridges = cartridges;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(final List<String> locations) {
        this.locations = locations;
    }

    public String getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(final String signedBy) {
        this.signedBy = signedBy;
    }

    public String getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(final String signedAt) {
        this.signedAt = signedAt;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public void setSigned(final boolean signed) {
        isSigned = signed;
    }

    public boolean isReplacementLetterGenerated() {
        return replacementLetterGenerated;
    }

    public void setReplacementLetterGenerated(final boolean replacementLetterGenerated) {
        this.replacementLetterGenerated = replacementLetterGenerated;
    }

    public String getSignedLetterPath() {
        return signedLetterPath;
    }

    public void setSignedLetterPath(final String signedLetterPath) {
        this.signedLetterPath = signedLetterPath;
    }
}
