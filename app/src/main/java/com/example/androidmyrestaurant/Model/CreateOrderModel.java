package com.example.androidmyrestaurant.Model;

import java.util.List;

public class CreateOrderModel {
    private boolean success;
    private String message;
    private List<createOrder> result;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<createOrder> getResult() {
        return result;
    }

    public void setResult(List<createOrder> result) {
        this.result = result;
    }
}
