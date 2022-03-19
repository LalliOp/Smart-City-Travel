package com.example.smartcitytravel.Activities.EditProfile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;

    //run when launch() function is called
    //get image from gallery
    private ActivityResultLauncher<Intent> imagePickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result != null) {
                        String imageUrl = result.getData().getData().toString();
                        setProfileImage(imageUrl);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setProfileImage(getIntent().getStringExtra("profile_img_url"));
        setName();
        openGallery();
        returnToLastActivity();
    }

    //get name from activity intent and set name in name field
    public void setName() {
        binding.fullNameEdit.setText(getIntent().getStringExtra("name"));
    }

    // get user profile image from activity intent and show profile image
    public void setProfileImage(String image_url) {
        Glide.with(this)
                .load(image_url)
                .into(binding.profileImg);
    }

    //open gallery to select image
    public void openGallery() {
        binding.changeProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                imagePickerActivityResultLauncher.launch(galleryIntent);

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