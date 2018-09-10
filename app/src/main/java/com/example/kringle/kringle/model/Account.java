package com.example.kringle.kringle.model;

import java.util.List;

public class Account {

    private String status;
    private List<AccountData> data;

    public String getStatus() {
        return status;
    }

    public List<AccountData> getData() {
        return data;
    }
}
