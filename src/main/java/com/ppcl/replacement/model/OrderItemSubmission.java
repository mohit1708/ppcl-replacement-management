package com.ppcl.replacement.model;

/**
 * Represents submitted order item from the printer booking form
 * Maps form fields to this class for database insertion
 */
public class OrderItemSubmission {
    private int locationId;
    private int printerModelId;
    private int quantity;
    private String printerType;
    private int dispatchBranchId;
    private String deliveryDate;
    private double printerPrice;
    private String contactPerson;
    private String contactNumber;
    private String cartridgePickup; // "yes" or "no"
    private int pickupQuantity;
    private String sendCartridge; // "yes" or "no"
    private int cartridgeQuantity;
    private String installation; // "yes" or "no"
    private String comments;

    // Getters and Setters
    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(final int locationId) {
        this.locationId = locationId;
    }

    public int getPrinterModelId() {
        return printerModelId;
    }

    public void setPrinterModelId(final int printerModelId) {
        this.printerModelId = printerModelId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public String getPrinterType() {
        return printerType;
    }

    public void setPrinterType(final String printerType) {
        this.printerType = printerType;
    }

    public int getDispatchBranchId() {
        return dispatchBranchId;
    }

    public void setDispatchBranchId(final int dispatchBranchId) {
        this.dispatchBranchId = dispatchBranchId;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(final String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public double getPrinterPrice() {
        return printerPrice;
    }

    public void setPrinterPrice(final double printerPrice) {
        this.printerPrice = printerPrice;
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

    public String getCartridgePickup() {
        return cartridgePickup;
    }

    public void setCartridgePickup(final String cartridgePickup) {
        this.cartridgePickup = cartridgePickup;
    }

    public int getPickupQuantity() {
        return pickupQuantity;
    }

    public void setPickupQuantity(final int pickupQuantity) {
        this.pickupQuantity = pickupQuantity;
    }

    public String getSendCartridge() {
        return sendCartridge;
    }

    public void setSendCartridge(final String sendCartridge) {
        this.sendCartridge = sendCartridge;
    }

    public int getCartridgeQuantity() {
        return cartridgeQuantity;
    }

    public void setCartridgeQuantity(final int cartridgeQuantity) {
        this.cartridgeQuantity = cartridgeQuantity;
    }

    public String getInstallation() {
        return installation;
    }

    public void setInstallation(final String installation) {
        this.installation = installation;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "OrderItemSubmission{" +
                "locationId=" + locationId +
                ", printerModelId=" + printerModelId +
                ", quantity=" + quantity +
                ", printerType='" + printerType + '\'' +
                ", dispatchBranchId=" + dispatchBranchId +
                ", deliveryDate='" + deliveryDate + '\'' +
                ", printerPrice=" + printerPrice +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", cartridgePickup='" + cartridgePickup + '\'' +
                ", pickupQuantity=" + pickupQuantity +
                ", sendCartridge='" + sendCartridge + '\'' +
                ", cartridgeQuantity=" + cartridgeQuantity +
                ", installation='" + installation + '\'' +
                ", comments='" + comments + '\'' +
                '}';
    }
}
