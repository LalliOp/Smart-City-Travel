package com.example.smartcitytravel.AWSService.Http;

import com.example.smartcitytravel.AWSService.DataModel.Result;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HttpRequest {
    @POST("create-account")
    Call<Result> createAccount(@Query("name") String name, @Query("email") String email,
                               @Query("password") String password, @Query("google_account") String google_account);

    @GET("verify-account")
    Call<Result> verifyAccount(@Query("email") String email, @Query("password") String password);

}
