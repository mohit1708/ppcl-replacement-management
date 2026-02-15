package com.ppcl.replacement.model;

import java.util.Date;

public class PrinterPullback {
    private int id;
    private int replacementReqId;
    private Integer callId;
    private Integer clientDotId;
    private String location;
    private Integer printerModel;
    private String serialNo;
    private String pickedBy;
    private Integer status;
    private Integer courierId;
    private String courierName;
    private String consignmentNo;
    private Date dispatchDate;
    private Date arrivalDate;
    private String receipt;
    private String destinationBranch;
    private String transportMode;
    private String contactPerson;
    private String contactNumber;
    private String comments;
    private int printer;
    private int powerCable;
    private int lanCable;
    private int tray;
    private Integer emptyCartridge;
    private Integer unusedCartridge;
    private String pullbackMode;
    private Integer replacementPrinterDetailsId;

    // Display fields (joined from other tables)
    private String clientName;
    private String clientAddress;
    private String clientContactNumber;
    private String printerModelName;
    private String statusName;
    private String replacementReqNo;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getReplacementReqId() {
        return replacementReqId;
    }

    public void setReplacementReqId(final int replacementReqId) {
        this.replacementReqId = replacementReqId;
    }

    public Integer getCallId() {
        return callId;
    }

    public void setCallId(final Integer callId) {
        this.callId = callId;
    }

    public Integer getClientDotId() {
        return clientDotId;
    }

    public void setClientDotId(final Integer clientDotId) {
        this.clientDotId = clientDotId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public Integer getPrinterModel() {
        return printerModel;
    }

    public void setPrinterModel(final Integer printerModel) {
        this.printerModel = printerModel;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(final String serialNo) {
        this.serialNo = serialNo;
    }

    public String getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(final String pickedBy) {
        this.pickedBy = pickedBy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(final Integer status) {
        this.status = status;
    }

    public Integer getCourierId() {
        return courierId;
    }

    public void setCourierId(final Integer courierId) {
        this.courierId = courierId;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(final String courierName) {
        this.courierName = courierName;
    }

    public String getConsignmentNo() {
        return consignmentNo;
    }

    public void setConsignmentNo(final String consignmentNo) {
        this.consignmentNo = consignmentNo;
    }

    public Date getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(final Date dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(final Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(final String receipt) {
        this.receipt = receipt;
    }

    public String getDestinationBranch() {
        return destinationBranch;
    }

    public void setDestinationBranch(final String destinationBranch) {
        this.destinationBranch = destinationBranch;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(final String transportMode) {
        this.transportMode = transportMode;
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

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public int getPrinter() {
        return printer;
    }

    public void setPrinter(final int printer) {
        this.printer = printer;
    }

    public int getPowerCable() {
        return powerCable;
    }

    public void setPowerCable(final int powerCable) {
        this.powerCable = powerCable;
    }

    public int getLanCable() {
        return lanCable;
    }

    public void setLanCable(final int lanCable) {
        this.lanCable = lanCable;
    }

    public int getTray() {
        return tray;
    }

    public void setTray(final int tray) {
        this.tray = tray;
    }

    public Integer getEmptyCartridge() {
        return emptyCartridge;
    }

    public void setEmptyCartridge(final Integer emptyCartridge) {
        this.emptyCartridge = emptyCartridge;
    }

    public Integer getUnusedCartridge() {
        return unusedCartridge;
    }

    public void setUnusedCartridge(final Integer unusedCartridge) {
        this.unusedCartridge = unusedCartridge;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(final String clientName) {
        this.clientName = clientName;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(final String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getClientContactNumber() {
        return clientContactNumber;
    }

    public void setClientContactNumber(final String clientContactNumber) {
        this.clientContactNumber = clientContactNumber;
    }

    public String getPrinterModelName() {
        return printerModelName;
    }

    public void setPrinterModelName(final String printerModelName) {
        this.printerModelName = printerModelName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(final String statusName) {
        this.statusName = statusName;
    }

    public String getReplacementReqNo() {
        return replacementReqNo;
    }

    public void setReplacementReqNo(final String replacementReqNo) {
        this.replacementReqNo = replacementReqNo;
    }

    public String getPullbackMode() {
        return pullbackMode;
    }

    public void setPullbackMode(final String pullbackMode) {
        this.pullbackMode = pullbackMode;
    }

    public Integer getReplacementPrinterDetailsId() {
        return replacementPrinterDetailsId;
    }

    public void setReplacementPrinterDetailsId(final Integer replacementPrinterDetailsId) {
        this.replacementPrinterDetailsId = replacementPrinterDetailsId;
    }
}
