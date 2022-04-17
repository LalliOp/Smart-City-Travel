package com.example.smartcitytravel.Activities.EditProfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.AWSService.DataModel.User;
import com.example.smartcitytravel.Activities.EditProfile.WorkManager.ImageUpdateWorkManager;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private Util util;
    private Color color;
    private Validation validation;
    private PreferenceHandler preferenceHandler;
    private Connection connection;
    private User user;
    private String changeName;
    private boolean newProfileImageSelected;
    private boolean newProfileName;
    private Uri imageUri;
    private Toast noConnectionToast;

    //run when launch() function is called
    //get image from gallery and save new image
    private ActivityResultLauncher<Intent> imagePickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result != null && result.getData() != null) {
                        imageUri = result.getData().getData();
                        setProfileImage(imageUri.toString());
                        newProfileImageSelected = true;
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

        binding.saveBtn.setEnabled(false);
        initialize();
        setToolBarTheme();
        setUserProfile();
        openGallery();
        nameChangeListener();
        save();


    }

    //initialize variables
    public void initialize() {
        util = new Util();
        color = new Color();
        validation = new Validation();
        preferenceHandler = new PreferenceHandler();
        connection = new Connection();
        noConnectionToast = new Toast(this);

        changeName = "";
        user = preferenceHandler.getLoginAccountPreference(this);
        newProfileImageSelected = false;
        newProfileName = false;
    }


    // style and customize toolbar and theme
    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_dark);
        util.addToolbar(this, binding.toolbarLayout.toolbar, "Edit Profile");
    }

    //set name, email and image profile of user
    public void setUserProfile() {
        setProfileImage(user.getImage_url());
        setName();
        setEmail();
    }

    //get name from activity intent and set name in name field
    public void setName() {
        String name = util.capitalizedName(user.getName());
        binding.fullNameEdit.setText(name);
    }

    //get email from activity intent and set email in email field
    public void setEmail() {
        binding.emailEdit.setText(user.getEmail());
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
        binding.changeProfileImgLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                imagePickerActivityResultLauncher.launch(galleryIntent);


            }
        });
    }

    //check internet connection and then upload profile image and update in database
    public void checkConnectionAndUpdateProfileImage(Uri imageUri) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(EditProfileActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            startUpdateImageWorkManager(imageUri);
                            Toast.makeText(EditProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();

                            newProfileImageSelected = false;
                            setSaveButtonState();
                        } else {
                            displayNoConnectionMessage();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    // start background service to update profile image
    public void startUpdateImageWorkManager(Uri imageUri) {
        Data data = new Data.Builder()
                .putString("image_url", imageUri.toString())
                .putString("userId", user.getUserId())
                .build();

        WorkRequest imageUpdateWorkRequest = new OneTimeWorkRequest.
                Builder(ImageUpdateWorkManager.class)
                .setInputData(data)
                .build();

        WorkManager.getInstance(EditProfileActivity.this)
                .enqueue(imageUpdateWorkRequest);
    }

    //validate and save record in database
    public void save() {
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(EditProfileActivity.this);
                if (validateFullName() && newProfileName) {
                    checkConnectionAndUpdateProfileName();
                }
                if (newProfileImageSelected) {
                    checkConnectionAndUpdateProfileImage(imageUri);
                }
            }
        });
    }

    //check full name field contain valid and allowed characters
    public boolean validateFullName() {
        String fullName = binding.fullNameEdit.getText().toString();
        String errorMessage = validation.validateFullName(fullName);
        if (errorMessage.isEmpty()) {
            removeFullNameError();
            return true;
        } else {
            showFullNameError(errorMessage);
            return false;
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

    // check for changes in profile name
    public void nameChangeListener() {

        binding.fullNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changeName = s.toString();
                if (changeName.equalsIgnoreCase(user.getName())) {
                    newProfileName = false;
                } else {
                    newProfileName = true;
                }

                if (newProfileImageSelected || newProfileName) {
                    binding.saveBtn.setEnabled(true);
                } else {
                    binding.saveBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //check internet connection exist or not. If exist update profile name
    public void checkConnectionAndUpdateProfileName() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(EditProfileActivity.this);
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isInternetAvailable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            updateProfileName();
                        } else {
                            displayNoConnectionMessage();
                            binding.saveBtn.setEnabled(true);
                            hideLoadingBar();
                        }
                    }
                });

            }
        });
    }

    // update account profile name in database and update in Ui too
    public void updateProfileName() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user").document(user.getUserId())
                .update("name", changeName.toLowerCase())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        user.setName(changeName);
                        binding.fullNameEdit.setText(user.getName());
                        preferenceHandler.updateNameLoginAccountPreference(user.getName(), EditProfileActivity.this);
                        sendUpdateProfileNameBroadcast();
                        newProfileName = false;

                        Toast.makeText(EditProfileActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                        setSaveButtonState();

                        hideLoadingBar();
                    }
                });

    }

    // show progress bar when user click on save button
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(this);
    }

    //hide progressbar when update name in database is complete error occurs
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(this);
    }


    // send broadcast to update profile name
    public void sendUpdateProfileNameBroadcast() {
        Intent updateProfileNameIntent = new Intent("com.example.smartcitytravel.UPDATE_PROFILE_NAME");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateProfileNameIntent);
    }

    // show only one no connection msg when multiple connection failed
    public void displayNoConnectionMessage() {
        try {
            noConnectionToast.cancel();
            noConnectionToast.getView().isShown();
        } catch (Exception ignored) {
            noConnectionToast = Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT);
            noConnectionToast.show();
        }
    }

    // enable and disable save button
    public void setSaveButtonState() {
        if (!newProfileImageSelected && !newProfileName) {
            binding.saveBtn.setEnabled(false);
        } else {
            binding.saveBtn.setEnabled(true);

        }
    }

    // return to previous activity when user click on up button (which is back button on top life side)
    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }
}