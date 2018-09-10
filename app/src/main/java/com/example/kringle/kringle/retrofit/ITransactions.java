package com.example.kringle.kringle.retrofit;


import com.example.kringle.kringle.model.TransactionsRequest;
import com.example.kringle.kringle.model.TransactionsResponse;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ITransactions {

    @Multipart
    @POST("abror")
    Call<TransactionsResponse> getTransactions(

            @Part("id") String id,
            @Part("timestamp") int timestamp,
            @Part("address") String address,
            @Part("amount") String amount,
            @Part("currency") String currency

    );

}