package com.example.smartcitytravel.AWSService.Http;

import com.example.smartcitytravel.AWSService.DataModel.CreateAccountResult;
import com.example.smartcitytravel.AWSService.DataModel.VerifyAccountResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HttpRequest {
    @POST("create-account")
    Call<CreateAccountResult> createAccount(@Query("name") String name, @Query("email") String email, @Query("password") String password);

    @GET("verify-account")
    Call<VerifyAccountResult> verifyAccount(@Query("email") String email, @Query("password") String password);

}
