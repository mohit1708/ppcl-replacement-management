package com.ppcl.replacement.model;

import java.sql.Timestamp;

public class ReasonMaster {
    private int id;
    private String name;
    private int status;
    private Timestamp createdAt;

    public ReasonMaster() {
    }

    public ReasonMaster(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
