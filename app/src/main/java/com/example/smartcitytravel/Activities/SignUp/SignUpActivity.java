package com.example.smartcitytravel.Activities.SignUp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Activities.PinCode.PinCodeActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private Util util;
    private Connection connection;
    private Color color;
    private Validation validation;
    private boolean validate_full_name;
    private boolean validate_email;
    private boolean validate_password;
    private boolean validate_confirm_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        util.setStatusBarColor(SignUpActivity.this, R.color.grey);
        initializeValidator();
        setLoadingBarColor();
        registerAccount();

    }

    //initialize variables
    public void initialize() {
        util = new Util();
        connection = new Connection();
        color = new Color();
        validation = new Validation();
    }

    //initialize validate variable for each edit field which help us to know which field contain error or not
    public void initializeValidator() {
        validate_full_name = false;
        validate_email = false;
        validate_password = false;
        validate_confirm_password = false;
    }

    //run when user click on register button
    //check all fields contain valid and allowed characters and move to login activity if no error occur
    public void registerAccount() {
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateFullName();
                validateEmail();
                validatePassword();
                matchPasswordAndConfirmPassword();

                if (validate_full_name && validate_email && validate_password && validate_confirm_password) {
                    checkConnectionAndVerifyEmail();
                }
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
        binding.fullNameLayout.setErrorIconTintList(color.iconRedColor(this));
        binding.fullNameLayout.setError(errorMsg);
        validate_full_name = false;
    }

    //hide error icon color and msg in full name field when no error occurs
    public void removeFullNameError() {
        binding.fullNameLayout.setError(null);
        validate_full_name = true;
    }

    //check email field contain valid and allowed characters
    public void validateEmail() {
        String email = binding.emailEdit.getText().toString();
        String errorMessage = validation.validateEmail(email);
        if (errorMessage.isEmpty()) {
            removeEmailError();

        } else {
            showEmailError(errorMessage);
        }
    }

    //show error msg and error icon color in email field
    public void showEmailError(String errorMsg) {
        binding.emailLayout.setErrorIconTintList(color.iconRedColor(this));
        binding.emailLayout.setError(errorMsg);
        validate_email = false;
    }

    //hide error icon color and msg in email field when no error occurs
    public void removeEmailError() {
        binding.emailLayout.setError(null);
        validate_email = true;
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
    public void matchPasswordAndConfirmPassword() {
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


    //Move from SignUp Activity to Pin code Activity
    //pass email to Pin code Activity
    public void moveToPinCodeActivity() {
        Intent intent = new Intent(this, PinCodeActivity.class);
        intent.putExtra("signup_name", binding.fullNameEdit.getText().toString());
        intent.putExtra("signup_email", binding.emailEdit.getText().toString());
        intent.putExtra("signup_password", binding.passwordEdit.getText().toString());
        intent.putExtra("title", "Verify Your Email");
        startActivity(intent);

    }

    // check internet connection exist or not. If exist than verify email by database
    public void checkConnectionAndVerifyEmail() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(SignUpActivity.this);
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
                            verifyEmail();
                        } else {
                            Toast.makeText(SignUpActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });

            }
        });
    }

    //check whether email exist or not
    public void verifyEmail() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").whereEqualTo("email", binding.emailEdit.getText().toString().toLowerCase())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                boolean google_account = querySnapshot.getBoolean("google_account");
                                if (google_account) {
                                    util.createErrorDialog(SignUpActivity.this, "Account", "Account already exist with google. Try sign in with google");
                                } else {
                                    util.createErrorDialog(SignUpActivity.this, "Account", "Account already exist with this email");
                                }
                            }

                        } else {
                            moveToPinCodeActivity();
                        }
                        hideLoadingBar();
                    }
                });

    }

    // show progress bar when user click on continue button
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(SignUpActivity.this);
    }

    //hide progressbar when move to next activity or error occurs
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(SignUpActivity.this);
    }

    //change default loading bar color
    public void setLoadingBarColor() {
        ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.light_white));
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(colorStateList);
    }
}