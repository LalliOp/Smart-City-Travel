package com.example.smartcitytravel.AWSService.Http;

import com.example.smartcitytravel.AWSService.DataModel.Result;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HttpRequest {
    @POST("create-account")
    Call<Result> createUserAccount(@Query("name") String name, @Query("email") String email, @Query("password") String password);

}
