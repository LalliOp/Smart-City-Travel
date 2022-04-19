package com.example.smartcitytravel.AWSService.Http;

import com.example.smartcitytravel.AWSService.DataModel.PinCodeResult;
import com.example.smartcitytravel.AWSService.DataModel.Place;
import com.example.smartcitytravel.AWSService.DataModel.PlaceModel.PlaceResult;
import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.DataModel.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface HttpRequest {
    @POST("create-account")
    Call<Result> createAccount(@Query("name") String name, @Query("email") String email,
                               @Query("password") String password, @Query("google_account") String google_account,
                               @Query("image_url") String image_url);

    @GET("verify-account")
    Call<Result> verifyAccount(@Query("email") String email, @Query("password") String password);

    @GET("verify-email")
    Call<Result> verifyEmail(@Query("email") String email);

    @GET("send-pin-code")
    Call<PinCodeResult> sendPinCode(@Query("email") String email);

    @POST("change-password")
    Call<Result> changePassword(@Query("email") String email, @Query("password") String password);

    @GET("get-account")
    Call<User> getAccount(@Query("email") String email);

    @POST("update-profile-name")
    Call<Result> updateProfileName(@Query("email") String email, @Query("name") String name);

    @POST("update-profile-image")
    Call<Result> updateProfileImage(@Query("email") String email, @Query("image_url") String image_url);

    @GET("get-popular-place-list")
    Call<PlaceResult> getPopularPlaceList();

    @GET("get-place-list")
    Call<PlaceResult> getPlaceList(@Query("place_type") String placeType);

    @GET("get-place-detail")
    Call<Place> getPlaceDetail(@Query("placeId") String placeId);

}
