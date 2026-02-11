package com.ppcl.replacement.model;

public class CartridgeDetail {
    private int id;
    private String type;
    private String model;
    private int quantity;

    public CartridgeDetail() {
    }

    public CartridgeDetail(final int id, final String type, final String model, final int quantity) {
        this.id = id;
        this.type = type;
        this.model = model;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }
}
