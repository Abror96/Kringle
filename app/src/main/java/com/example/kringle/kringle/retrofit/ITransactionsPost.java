package com.example.kringle.kringle.retrofit;

import com.example.kringle.kringle.model.TransactionsPostResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ITransactionsPost {

    @POST("transactions")
    Call<TransactionsPostResponse> postTransactions(
            @Header("Authorization") String auth, @Body RequestBody body
    );

}
