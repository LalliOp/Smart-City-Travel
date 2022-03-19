package com.example.smartcitytravel.Activities.EditProfile;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.databinding.ActivityEditProfileBinding;

import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private ActivityResultLauncher<String[]> permissionActivityResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                }
            });
    //    private ActivityResultLauncher<String> permissionActivityResult = registerForActivityResult(
//            new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
//                @Override
//                public void onActivityResult(Boolean result) {
//                    if (result) {
//                        Toast.makeText(EditProfileActivity.this, "WORKING", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(EditProfileActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
    //run when launch() function is called by GoogleSignUpActivityResult
    private ActivityResultLauncher<String> imagePickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        Glide.with(EditProfileActivity.this)
                                .load(result)
                                .into(binding.profileImg);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setProfileImage();
        openGallery();
        returnToLastActivity();
    }

    // get user profile image from activity intent and show profile image
    public void setProfileImage() {
        Glide.with(this)
                .load(getIntent().getStringExtra("profile_img_url"))
                .into(binding.profileImg);
    }

    public void openGallery() {
        binding.changeProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                Intent galleryIntent = new Intent(Intent.ACTION_PICK, );
//                galleryIntent.setType("image/*");
//                imagePickerActivityResultLauncher.launch("images/*");
                permissionActivityResult.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.CAMERA});

            }
        });
    }

    //exit this activity and move back to last activity
    public void returnToLastActivity() {
        binding.backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}