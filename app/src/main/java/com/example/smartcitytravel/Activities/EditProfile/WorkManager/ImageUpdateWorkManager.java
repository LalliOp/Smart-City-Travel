package com.example.smartcitytravel.Activities.EditProfile.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageUpdateWorkManager extends Worker {
    private PreferenceHandler preferenceHandler;
    private Uri imageUri;
    private String email;
    private Connection connection;

    public ImageUpdateWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        preferenceHandler = new PreferenceHandler();
        imageUri = Uri.parse(getInputData().getString("image_url"));
        email = getInputData().getString("email");
        connection = new Connection();

    }

    @NonNull
    @Override
    public Result doWork() {
        uploadProfileImage();
        return Result.success();
    }

    //upload image in firebase cloud and update image in database
    public void uploadProfileImage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("profile-images");

        String imageName = getImageName();

        StorageReference imageReference = storageReference.child(imageName);
        UploadTask uploadImage = imageReference.putFile(imageUri);

        uploadImage.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();

                } else {
                    return imageReference.getDownloadUrl();
                }

            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadImageUri = task.getResult();
                    updateProfileImage(downloadImageUri);
                } else {
                }
            }
        });
    }

    //update profile image in database
    public void updateProfileImage(Uri downloadImageUri) {
        Call<com.example.smartcitytravel.AWSService.DataModel.Result> callableProfileImage = HttpClient.getInstance().updateProfileImage(email,
                downloadImageUri.toString());
        callableProfileImage.enqueue(new Callback<com.example.smartcitytravel.AWSService.DataModel.Result>() {
            @Override
            public void onResponse(Call<com.example.smartcitytravel.AWSService.DataModel.Result> call, Response<com.example.smartcitytravel.AWSService.DataModel.Result> response) {
                com.example.smartcitytravel.AWSService.DataModel.Result result = response.body();
                if (result != null && result.getStatus() == 0) {
                    preferenceHandler.updateImageLoginAccountPreference(downloadImageUri.toString(), getApplicationContext());
                    sendUpdateProfileImageBroadcast();
                }
            }

            @Override
            public void onFailure(Call<com.example.smartcitytravel.AWSService.DataModel.Result> call, Throwable t) {
            }
        });

    }

    // send broadcast to update profile image
    public void sendUpdateProfileImageBroadcast() {
        Intent updateProfileImageIntent = new Intent("com.example.smartcitytravel.UPDATE_PROFILE_IMAGE");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateProfileImageIntent);
    }

    //create name for image file
    public String getImageName() {
        int startIndex = imageUri.getLastPathSegment().lastIndexOf("/");
        int endIndex = imageUri.getLastPathSegment().lastIndexOf(".");

        String rawImgName = imageUri.getLastPathSegment().substring(startIndex, endIndex);
        rawImgName = rawImgName.replace("/", "");

        Random randomNumber1 = new Random();
        Random randomNumber2 = new Random();

        return rawImgName + randomNumber1.nextInt() + randomNumber2.nextInt();
    }

}
