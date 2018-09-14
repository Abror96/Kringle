package com.example.kringle.kringle.retrofit;


import com.example.kringle.kringle.model.TransactionsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ITransactions {

    @GET("transactions")
    Call<TransactionsResponse> getTransactions(
        @Header("Authorization") String authorization,
        @Query("id") int id,
        @Query("timestamp") int timestamp,
        @Query("start") int start,
        @Query("limit") int limit);
}