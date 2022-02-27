package com.example.smartcitytravel.AWSService.Http;

import com.example.smartcitytravel.AWSService.DataModel.Response;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HttpRequest {
    @POST("create-account")
    Call<Response> createUserAccount(@Query("name") String name, @Query("email") String email, @Query("password") String password);

}
