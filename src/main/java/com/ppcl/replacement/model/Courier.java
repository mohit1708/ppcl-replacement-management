package com.ppcl.replacement.model;

import java.sql.Timestamp;

/**
 * Model class for COURIER_MASTER table.
 * Holds courier details along with login-related fields
 */
public class Courier {

    // basic courier fields
    private int id;
    private String name;
    private String contactPerson;
    private String address;
    private Long mobile;
    private String email;
    private int cityId;
    private String website;
    private Timestamp createdAt;
    private String status;

    // login-related fields (stored in COURIER_MASTER itself)
    private String password;
    private String firstLoginFlag;
    private String otp;
    private Timestamp otpGeneratedTime;
    private Timestamp otpExpiryTime;
    private int otpAttemptCount;
    private Timestamp lastLoginTime;
    private String accountStatus;

    public Courier() {
    }

    public Courier(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    // --- getters & setters ---

    public int getId() { return id; }
    public void setId(final int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(final String contactPerson) { this.contactPerson = contactPerson; }

    public String getAddress() { return address; }
    public void setAddress(final String address) { this.address = address; }

    public Long getMobile() { return mobile; }
    public void setMobile(final Long mobile) { this.mobile = mobile; }

    public String getEmail() { return email; }
    public void setEmail(final String email) { this.email = email; }

    public int getCityId() { return cityId; }
    public void setCityId(final int cityId) { this.cityId = cityId; }

    public String getWebsite() { return website; }
    public void setWebsite(final String website) { this.website = website; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(final Timestamp createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }

    public String getPassword() { return password; }
    public void setPassword(final String password) { this.password = password; }

    public String getFirstLoginFlag() { return firstLoginFlag; }
    public void setFirstLoginFlag(final String firstLoginFlag) { this.firstLoginFlag = firstLoginFlag; }

    /** Returns true if courier hasn't changed the initial auto-generated password yet. */
    public boolean isFirstLogin() { return "Y".equalsIgnoreCase(firstLoginFlag); }

    public String getOtp() { return otp; }
    public void setOtp(final String otp) { this.otp = otp; }

    public Timestamp getOtpGeneratedTime() { return otpGeneratedTime; }
    public void setOtpGeneratedTime(final Timestamp otpGeneratedTime) { this.otpGeneratedTime = otpGeneratedTime; }

    public Timestamp getOtpExpiryTime() { return otpExpiryTime; }
    public void setOtpExpiryTime(final Timestamp otpExpiryTime) { this.otpExpiryTime = otpExpiryTime; }

    public int getOtpAttemptCount() { return otpAttemptCount; }
    public void setOtpAttemptCount(final int otpAttemptCount) { this.otpAttemptCount = otpAttemptCount; }

    public Timestamp getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(final Timestamp lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(final String accountStatus) { this.accountStatus = accountStatus; }

    /** Account is active when ACCOUNT_STATUS = 'A'. */
    public boolean isAccountActive() { return "A".equalsIgnoreCase(accountStatus); }

    /** Login is considered created when a password exists in the DB. */
    public boolean hasLoginCreated() { return password != null && !password.isEmpty(); }
}
