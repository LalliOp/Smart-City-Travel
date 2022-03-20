package com.example.smartcitytravel.Activities.EditProfile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.DataModel.User;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private Util util;
    private Color color;
    private Validation validation;
    private PreferenceHandler preferenceHandler;
    private Connection connection;
    private User user;
    private String changeName;

    //run when launch() function is called
    //get image from gallery
    private ActivityResultLauncher<Intent> imagePickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result != null && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        binding.loadingImg.setVisibility(View.VISIBLE);
                        checkConnectionAndUpdateProfileImage(imageUri);

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

        changeName = "";
        user = preferenceHandler.getLoginAccountPreference(this);
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
        binding.changeProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");
                imagePickerActivityResultLauncher.launch(galleryIntent);
            }
        });
    }

    //save new profile image in firebase store
    public void uploadProfileImage(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("profile-images");

        Random random1 = new Random();
        Random random2 = new Random();
        String imageName = imageUri.getAuthority() + random1.nextInt() + random2.nextInt();

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
                    Uri imageUri = task.getResult();
                    updateProfileImage(imageUri);
                    setProfileImage(imageUri.toString());
                    binding.loadingImg.setVisibility(View.GONE);
                }
            }
        });
    }

    //update profile image in database
    public void updateProfileImage(Uri imageUri) {
        Call<Result> callableProfileImage = HttpClient.getInstance().updateProfileImage(user.getEmail(),
                imageUri.toString());
        callableProfileImage.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Result result = response.body();

                if (result != null && result.getAccount_status() == 0) {
                    preferenceHandler.updateImageLoginAccountPreference(imageUri.toString(), EditProfileActivity.this);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Unable to update profile image", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Unable to update profile image", Toast.LENGTH_SHORT).show();

            }
        });
    }

    //check internet connection and then upload image in firebase cloud and update image in database
    public void checkConnectionAndUpdateProfileImage(Uri image_uri) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(EditProfileActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            uploadProfileImage(image_uri);
                        } else {
                            Toast.makeText(EditProfileActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    //validate and save record in database
    public void save() {
        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(EditProfileActivity.this);
                if (validateFullName()) {
                    user.setName(changeName);
                    preferenceHandler.updateNameLoginAccountPreference(user.getName(), EditProfileActivity.this);
                    checkConnectionAndChangeName();
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

    //check internet connection and then change profile name in database
    public void checkConnectionAndChangeName() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(this);
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isInternetAvailable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            updateProfileName();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    // update account profile name in database and update in Ui too
    public void updateProfileName() {
        Call<Result> callableUpdateName = HttpClient.getInstance().updateProfileName(user.getEmail(), user.getName());

        callableUpdateName.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Result result = response.body();
                if (result != null) {
                    binding.fullNameEdit.setText(user.getName());
                }
                hideLoadingBar();
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Unable to update profile name", Toast.LENGTH_SHORT).show();
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

}