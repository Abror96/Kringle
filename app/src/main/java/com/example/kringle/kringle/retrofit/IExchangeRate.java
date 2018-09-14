package com.example.kringle.kringle.retrofit;

import com.example.kringle.kringle.model.ExchangeRate;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface IExchangeRate {

    @GET("exchangerate")
    Call<ExchangeRate> getExchangeRate(
        @Header("Authorization") String authorization,
        @Query("id") int id,
        @Query("timestamp") int timestamp
    );

}
