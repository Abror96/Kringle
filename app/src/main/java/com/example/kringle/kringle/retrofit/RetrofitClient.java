package com.example.kringle.kringle.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit ourInstance;

    public static Retrofit getInstance() {
        if (ourInstance == null) {

            ourInstance = new Retrofit.Builder()
                    .baseUrl("https://tbc009.net/api/v1/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return ourInstance;

    }

    private RetrofitClient() {

    }

}
