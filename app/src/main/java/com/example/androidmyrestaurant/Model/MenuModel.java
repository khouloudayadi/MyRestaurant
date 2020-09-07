package com.example.androidmyrestaurant.Model;


import java.util.List;

public class MenuModel {

    private boolean success;
    private String message;
    private List<Menu> result;

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

    public List<Menu> getResult() {
        return result;
    }

    public void setResult(List<Menu> result) {
        this.result = result;
    }

    public MenuModel(boolean success, String message, List<Menu> result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }
}
