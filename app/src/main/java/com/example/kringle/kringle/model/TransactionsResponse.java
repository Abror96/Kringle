package com.example.kringle.kringle.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TransactionsResponse {

    @SerializedName("status")
    String status;

    @SerializedName("data")
    List<TransactionsResponseData> data;

    public String getStatus() {
        return status;
    }

    public List<TransactionsResponseData> getData() {
        return data;
    }
}
