package com.example.smartcitytravel.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
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
    private Connection connection;
    private PreferenceHandler preferenceHandler;
    private Util util;
    private Uri imageUri;
    private String email;

    public ImageUpdateWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        connection = new Connection();
        preferenceHandler = new PreferenceHandler();
        util = new Util();
        imageUri = Uri.parse(getInputData().getString("image_url"));
        email = getInputData().getString("email");

    }

    @NonNull
    @Override
    public Result doWork() {
        uploadProfileImage();
        return Result.success();

    }

    //check internet connection and then upload image in firebase cloud and update image in database
    public void uploadProfileImage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("profile-images");

        Random random1 = new Random();
        Random random2 = new Random();
        String imageName = random1.nextInt() + random2.nextInt() + "";

        StorageReference imageReference = storageReference.child(imageName);
        UploadTask uploadImage = imageReference.putFile(imageUri);

        uploadImage.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Unable to update image", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Unable to update image", Toast.LENGTH_SHORT).show();
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
                if (result != null && result.getAccount_status() == 0) {
                    preferenceHandler.updateImageLoginAccountPreference(downloadImageUri.toString(), getApplicationContext());
                    sendUpdateProfileBroadcast();
                } else {
                    Toast.makeText(getApplicationContext(), "Unable to update profile image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.smartcitytravel.AWSService.DataModel.Result> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to update profile image", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // send broadcast to update profile
    public void sendUpdateProfileBroadcast() {
        Intent updateProfileIntent = new Intent("com.example.smartcitytravel.UPDATE_PROFILE");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateProfileIntent);
    }

}
