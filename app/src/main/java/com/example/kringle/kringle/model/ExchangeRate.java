package com.example.kringle.kringle.model;

import java.util.List;

public class ExchangeRate {

    String status;
    List<ExchangeRateData> data;

    public String getStatus() {
        return status;
    }

    public List<ExchangeRateData> getData() {
        return data;
    }
}
