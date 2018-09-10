package com.example.kringle.kringle.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TransactionsRequest {

    private String id;
    private String timestamp;
    private String address;
    private String amount;
    private String currency;

    public TransactionsRequest(String id, String timestamp, String address, String amount, String currency) {
        this.id = id;
        this.timestamp = timestamp;
        this.address = address;
        this.amount = amount;
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
