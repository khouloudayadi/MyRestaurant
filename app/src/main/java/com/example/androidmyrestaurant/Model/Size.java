package com.example.androidmyrestaurant.Model;

public class Size {
 private int id;
 private String description;
 private Float extraPrice;

    public Size(int id, String description, double extraPrice) {
        this.id = id;
        this.description = description;
        this.extraPrice = (float) extraPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(double extraPrice) {
        this.extraPrice = (float) extraPrice;
    }
}
