package com.ppcl.replacement.model;

/**
 * Represents consolidated order item (grouped by location + model)
 * Used for displaying in printer booking form
 */
public class OrderItem {
    private String locationName;
    private int printerModelId;
    private String newPrinterModel;
    private String printerType;
    private int quantity;
    private String contactPerson;
    private String contactNumber;
    private int suggestedBranchId;
    private String defaultDeliveryDate;

    // Getters and Setters
//    public int getLocationId() {
//        return locationId;
//    }
//
//    public void setLocationId(int locationId) {
//        this.locationId = locationId;
//    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(final String locationName) {
        this.locationName = locationName;
    }

    public int getPrinterModelId() {
        return printerModelId;
    }

    public void setPrinterModelId(final int printerModelId) {
        this.printerModelId = printerModelId;
    }

    public String getNewPrinterModel() {
        return newPrinterModel;
    }

    public void setNewPrinterModel(final String newPrinterModel) {
        this.newPrinterModel = newPrinterModel;
    }

    public String getPrinterType() {
        return printerType;
    }

    public void setPrinterType(final String printerType) {
        this.printerType = printerType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
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

    public int getSuggestedBranchId() {
        return suggestedBranchId;
    }

    public void setSuggestedBranchId(final int suggestedBranchId) {
        this.suggestedBranchId = suggestedBranchId;
    }

    public String getDefaultDeliveryDate() {
        return defaultDeliveryDate;
    }

    public void setDefaultDeliveryDate(final String defaultDeliveryDate) {
        this.defaultDeliveryDate = defaultDeliveryDate;
    }
}
