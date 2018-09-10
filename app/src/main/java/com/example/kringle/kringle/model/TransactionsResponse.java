package com.example.kringle.kringle.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TransactionsResponse {

    @SerializedName("data_type")
    @Expose
    private String data_type;

    @SerializedName("surname")
    @Expose
    private String surname;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("timestamp")
    @Expose
    private int timestamp;

    @SerializedName("address")
    @Expose
    private String address;

    @SerializedName("amount")
    @Expose
    private String amount;

    @SerializedName("currency")
    @Expose
    private String currency;

    public String getData_type() {
        return data_type;
    }

    public String getSurname() {
        return surname;
    }

    public String getId() {
        return id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getAddress() {
        return address;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
