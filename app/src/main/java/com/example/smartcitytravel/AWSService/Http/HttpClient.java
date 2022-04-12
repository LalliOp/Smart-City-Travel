package com.example.smartcitytravel.AWSService.Http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {
    private static HttpRequest instance;

    public static HttpRequest getInstance() {
        if (instance == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(1000, TimeUnit.MILLISECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://jpzuu7f43m.execute-api.ap-south-1.amazonaws.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .client(okHttpClient)
                    .build();

            instance = retrofit.create(HttpRequest.class);
        }
        return instance;
    }
}
