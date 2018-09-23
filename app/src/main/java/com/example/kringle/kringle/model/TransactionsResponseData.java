package com.example.kringle.kringle.model;

import android.os.Parcel;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class TransactionsResponseData {

//    @SerializedName("txid")
//    Integer txid;

    @SerializedName("amount")
    int amount;

    @SerializedName("address_from")
    String address_from;

    @SerializedName("address_to")
    String address_to;

    @SerializedName("incoming")
    int incoming;

    @SerializedName("timestamp")
    int timestamp;

    @SerializedName("status")
    String status;

//    public int getTxid() {
//        return txid;
//    }

    public int getAmount() {
        return amount;
    }

    public String getAddress_from() {
        return address_from;
    }

    public String getAddress_to() {
        return address_to;
    }

    public int getIncoming() {
        return incoming;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }
}
