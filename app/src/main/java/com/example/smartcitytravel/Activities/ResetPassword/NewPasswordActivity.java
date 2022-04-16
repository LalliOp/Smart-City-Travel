package com.example.smartcitytravel.Activities.ResetPassword;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Activities.SuccessfulAccountMessage.SuccessfulAccountMessageActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivityNewPasswordBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewPasswordActivity extends AppCompatActivity {
    private ActivityNewPasswordBinding binding;
    private Color color;
    private Connection connection;
    private Util util;
    private Validation validation;
    private String email;
    private boolean validate_password;
    private boolean validate_confirm_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        getEmail();
        util.setStatusBarColor(NewPasswordActivity.this, R.color.black);
        setLoadingBarColor();
        initializeValidator();
        resetPassword();
    }

    //initialize variables
    public void initialize() {
        color = new Color();
        connection = new Connection();
        util = new Util();
        validation = new Validation();
    }

    //initialize validate variable for each field which help us to know which field contain error or not
    public void initializeValidator() {
        validate_password = false;
        validate_confirm_password = false;
    }

    // get email which is passed by PinCode Activity
    public void getEmail() {
        email = getIntent().getExtras().getString("email");
    }

    public void resetPassword() {
        binding.resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(NewPasswordActivity.this);
                validatePassword();
                validateMatchPasswordAndConfirmPassword();
                if (validate_password && validate_confirm_password) {
                    checkConnectionAndChangePassword();
                }
            }
        });
    }

    //check password field contain valid and allowed characters
    public void validatePassword() {
        String password = binding.passwordEdit.getText().toString();
        String errorMessage = validation.validatePassword(password);
        if (errorMessage.isEmpty()) {
            removePasswordError();

        } else {
            showPasswordError(errorMessage);
        }
    }

    //change default loading bar color
    public void setLoadingBarColor() {
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2)));
    }

    //show error msg and hide error icon in password field
    public void showPasswordError(String errorMsg) {
        binding.passwordLayout.setEndIconTintList(color.iconRedColor(this));
        binding.passwordLayout.setError(errorMsg);
        binding.passwordLayout.setErrorIconDrawable(null);
        validate_password = false;
    }

    //hide msg in password field when no error occurs
    public void removePasswordError() {
        binding.passwordLayout.setEndIconTintList(color.iconWhiteColor(this));
        binding.passwordLayout.setError(null);
        validate_password = true;
    }

    //check confirm password field
    //and match confirm password with password
    public void validateMatchPasswordAndConfirmPassword() {
        String password = binding.passwordEdit.getText().toString();
        String confirmPassword = binding.confirmPasswordEdit.getText().toString();
        int errorCode = validation.matchPasswordAndConfirmPassword(password, confirmPassword);
        if (errorCode == 2) {
            showConfirmPasswordError("ERROR! Re-enter password");
        } else if (errorCode == 1) {
            showConfirmPasswordError("ERROR! Password not matched");
            showPasswordError(" ");
        } else if (errorCode == 0) {
            removeConfirmPasswordError();
        } else {
            showConfirmPasswordError("ERROR! Invalid password");
        }
    }

    //show error msg and hide error icon in confirm password field
    public void showConfirmPasswordError(String errorMsg) {
        binding.confirmPasswordLayout.setEndIconTintList(color.iconRedColor(this));
        binding.confirmPasswordLayout.setError(errorMsg);
        binding.confirmPasswordLayout.setErrorIconDrawable(null);
        validate_confirm_password = false;
    }

    //hide msg in confirm password field when no error occurs
    public void removeConfirmPasswordError() {
        binding.confirmPasswordLayout.setEndIconTintList(color.iconWhiteColor(this));
        binding.confirmPasswordLayout.setError(null);
        validate_confirm_password = true;
    }

    // check internet connection exist or not. If exist change password in database
    public void checkConnectionAndChangePassword() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(NewPasswordActivity.this);
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
                            changePassword();
                        } else {
                            Toast.makeText(NewPasswordActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });

            }
        });
    }

    //change account password in database
    public void changePassword() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {

                                db.collection("user")
                                        .document(querySnapshot.getId())
                                        .update("password", binding.passwordEdit.getText().toString())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                moveToSuccessfulAccountMessageActivity();
                                                hideLoadingBar();
                                            }
                                        });
                            }
                        }
                    }
                });

    }

    // show progress bar when user click on reset password button
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(NewPasswordActivity.this);
    }

    //hide progressbar when reset password is complete and move to Login Activity or error occurs
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(NewPasswordActivity.this);
    }

    //move to successful account message activity
    public void moveToSuccessfulAccountMessageActivity() {
        Intent intent = new Intent(this, SuccessfulAccountMessageActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("message", "Password Changed Successfully");
        startActivity(intent);
    }
}