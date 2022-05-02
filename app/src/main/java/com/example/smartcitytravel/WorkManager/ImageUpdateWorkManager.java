package com.example.smartcitytravel.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.smartcitytravel.Util.PreferenceHandler;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;

public class ImageUpdateWorkManager extends Worker {
    private final PreferenceHandler preferenceHandler;
    private final Uri imageUri;
    private final String userId;
    private boolean updateUI;

    public ImageUpdateWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        preferenceHandler = new PreferenceHandler();
        imageUri = Uri.parse(getInputData().getString("image_url"));
        userId = getInputData().getString("userId");
        updateUI = getInputData().getBoolean("update_UI", false);

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

        String imageName = generateImageName();

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
                }
            }
        });
    }

    //update profile image in database
    public void updateProfileImage(Uri downloadImageUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(userId)
                .update("image_url", downloadImageUri.toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        preferenceHandler.updateImagePreference(downloadImageUri.toString(), getApplicationContext());

                        if (updateUI) {
                            sendUpdateProfileImageBroadcast();
                        }

                    }
                });
    }

    //create name for image file
    public String generateImageName() {
        int startIndex = imageUri.getLastPathSegment().lastIndexOf("/");
        int endIndex = imageUri.getLastPathSegment().lastIndexOf(".");

        String rawImgName = imageUri.getLastPathSegment().substring(startIndex, endIndex);
        rawImgName = rawImgName.replace("/", "");

        Random randomNumber1 = new Random();
        Random randomNumber2 = new Random();

        return rawImgName + randomNumber1.nextInt() + randomNumber2.nextInt();
    }

    // send broadcast to update profile image
    public void sendUpdateProfileImageBroadcast() {
        Intent updateProfileImageIntent = new Intent("com.example.smartcitytravel.UPDATE_PROFILE_IMAGE");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateProfileImageIntent);
    }

}
