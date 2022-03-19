package com.example.smartcitytravel.Activities.EditProfile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private Util util;
    private Color color;
    private Validation validation;

    //run when launch() function is called
    //get image from gallery
    private ActivityResultLauncher<Intent> imagePickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result != null && result.getData() != null) {
                        String imageUrl = result.getData().getData().toString();
                        setProfileImage(imageUrl);

                        binding.saveBtn.setEnabled(true);

                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        color = new Color();
        validation = new Validation();

        binding.saveBtn.setEnabled(false);
        util.setStatusBarColor(this, R.color.theme_dark);
        addToolbar();
        setProfileImage(getIntent().getStringExtra("profile_img_url"));
        setName();
        setEmail();
        openGallery();
        enableSaveButton();
        save();
    }

    void addToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    //get name from activity intent and set name in name field
    public void setName() {
        String name = getIntent().getStringExtra("name");
        name = util.capitalizedName(name);

        binding.fullNameEdit.setText(name);
    }

    //get email from activity intent and set email in email field
    public void setEmail() {
        String email = getIntent().getStringExtra("email");
        binding.emailEdit.setText(email);
        binding.emailLayout.setEnabled(false);
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

    //validate and save record in database
    public void save() {
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateFullName();
            }
        });
    }

    //check full name field contain valid and allowed characters
    public void validateFullName() {
        String fullName = binding.fullNameEdit.getText().toString();
        String errorMessage = validation.validateFullName(fullName);
        if (errorMessage.isEmpty()) {
            removeFullNameError();

        } else {
            showFullNameError(errorMessage);
        }
    }

    //show error msg and error icon color in full name field
    public void showFullNameError(String errorMsg) {
        binding.fullNameLayout.setErrorIconTintList(color.createColor(this, R.color.theme_light));
        binding.fullNameLayout.setError(errorMsg);
    }

    //hide error icon color and msg in full name field when no error occurs
    public void removeFullNameError() {
        binding.fullNameLayout.setError(null);
    }

    // enable or disable save button
    public void enableSaveButton() {
        String name = getIntent().getStringExtra("name");

        binding.fullNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equalsIgnoreCase(name)) {
                    binding.saveBtn.setEnabled(false);
                } else {
                    binding.saveBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}