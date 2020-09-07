package com.example.androidmyrestaurant.Model;

import java.util.List;

public class TokenModel {
    private boolean success;
    private String message;
    private List<Token> result;

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

    public List<Token> getResult() {
        return result;
    }

    public void setResult(List<Token> result) {
        this.result = result;
    }
}
