package com.example.kringle.kringle.retrofit;

import com.example.kringle.kringle.model.LogOut;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ILogOut {

    @DELETE("auth")
    Call<LogOut> logout(
            @Header("Authorization") String authorization,
            @Query("id") int id,
            @Query("timestamp") int timestamp);

}
