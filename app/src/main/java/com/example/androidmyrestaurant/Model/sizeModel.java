package com.example.androidmyrestaurant.Model;


import java.util.List;

public class sizeModel {
    private boolean success;
    private String message;
    private List<Size> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Size> getResult() {
        return result;
    }

    public void setResult(List<Size> result) {
        this.result = result;
    }
}
