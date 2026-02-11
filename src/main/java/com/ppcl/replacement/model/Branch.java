package com.ppcl.replacement.model;

/**
 * Represents a dispatch branch for printer booking
 */
public class Branch {
    private int id;
    private String name;

    // Constructors
    public Branch() {
    }

    public Branch(final int id, final String name) {
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
}
