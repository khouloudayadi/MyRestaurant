package com.example.androidmyrestaurant.Model;

public class Transaction {

    private String id,status,type, currencyIsCode, amountn, merchantAccountId,subMerchantAccountId;
    private String masterMerchantAccountId,orderId,createAt,updateAt;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrencyIsCode() {
        return currencyIsCode;
    }

    public void setCurrencyIsCode(String currencyIsCode) {
        this.currencyIsCode = currencyIsCode;
    }

    public String getAmountn() {
        return amountn;
    }

    public void setAmountn(String amountn) {
        this.amountn = amountn;
    }

    public String getMerchantAccountId() {
        return merchantAccountId;
    }

    public void setMerchantAccountId(String merchantAccountId) {
        this.merchantAccountId = merchantAccountId;
    }

    public String getSubMerchantAccountId() {
        return subMerchantAccountId;
    }

    public void setSubMerchantAccountId(String subMerchantAccountId) {
        this.subMerchantAccountId = subMerchantAccountId;
    }

    public String getMasterMerchantAccountId() {
        return masterMerchantAccountId;
    }

    public void setMasterMerchantAccountId(String masterMerchantAccountId) {
        this.masterMerchantAccountId = masterMerchantAccountId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }
}
