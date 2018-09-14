package com.example.kringle.kringle.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TransactionsPostRequest {

    private String id;
    private String timestamp;
    private String address;
    private String amount;
    private String currency;

    public TransactionsPostRequest(String id, String timestamp, String address, String amount, String currency) {
        this.id = id;
        this.timestamp = timestamp;
        this.address = address;
        this.amount = amount;
        this.currency = currency;
    }
}
