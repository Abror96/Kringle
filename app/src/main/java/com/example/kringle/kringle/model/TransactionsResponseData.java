package com.example.kringle.kringle.model;

public class TransactionsResponseData {

    int txid;
    int amount;
    String address_from;
    String address_to;
    int incoming;
    int timestamp;
    String status;

    public int getTxid() {
        return txid;
    }

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
