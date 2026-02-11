package com.ppcl.replacement.model;

public class User {
    private int id;
    private int empId;
    private String userId;
    private String name;
    private String mobile;
    private String email;
    private int brId;
    private int deptId;
    private String dept;
    private String role;
    private int roleId;

    public User() {
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(final int roleId) {
        this.roleId = roleId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(final int empId) {
        this.empId = empId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(final String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public int getBrId() {
        return brId;
    }

    public void setBrId(final int brId) {
        this.brId = brId;
    }

    public int getDeptId() {
        return deptId;
    }

    public void setDeptId(final int deptId) {
        this.deptId = deptId;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(final String dept) {
        this.dept = dept;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", empId=" + empId +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", brId=" + brId +
                ", deptId=" + deptId +
                ", dept='" + dept + '\'' +
                ", role='" + role + '\'' +
                ", roleId=" + roleId +
                '}';
    }
}
