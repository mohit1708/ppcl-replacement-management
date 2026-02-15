package com.ppcl.replacement.model;

public class Client {
    private int id;
    private int brId;
    private String clientId;
    private String name;
    private String branch;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String contactPerson;
    private String mobileNo;
    private String emailId1;
    private String emailId2;
    private String dbsCategory;

    public Client() {
    }

    public Client(final int id, final String clientId, final String name) {
        this.id = id;
        this.clientId = clientId;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(final String branch) {
        this.branch = branch;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
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

    public String getPincode() {
        return pincode;
    }

    public void setPincode(final String pincode) {
        this.pincode = pincode;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(final String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmailId1() {
        return emailId1;
    }

    public void setEmailId1(final String emailId1) {
        this.emailId1 = emailId1;
    }

    public String getEmailId2() {
        return emailId2;
    }

    public void setEmailId2(final String emailId2) {
        this.emailId2 = emailId2;
    }

    public String getDbsCategory() {
        return dbsCategory;
    }

    public void setDbsCategory(final String dbsCategory) {
        this.dbsCategory = dbsCategory;
    }

        public int getBrId() {
        return brId;
    }

    public void setBrId(int brId) {
        this.brId = brId;
    }

}
