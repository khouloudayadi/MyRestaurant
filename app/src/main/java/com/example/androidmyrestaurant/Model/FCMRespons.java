package com.example.androidmyrestaurant.Model;

import java.util.List;

public class FCMRespons {
    private long multicast_id ;
    private int success,faillure;
    private List<FCMResult> results;

    public FCMRespons() {
    }

    public long getMulticast_id() {
        return multicast_id;
    }

    public void setMulticast_id(long multicast_id) {
        this.multicast_id = multicast_id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFaillure() {
        return faillure;
    }

    public void setFaillure(int faillure) {
        this.faillure = faillure;
    }

    public List<FCMResult> getResults() {
        return results;
    }

    public void setResults(List<FCMResult> results) {
        this.results = results;
    }
}
