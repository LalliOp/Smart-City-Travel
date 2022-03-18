package com.example.smartcitytravel.Activities.SignUp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Activities.Login.LoginActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
import com.example.smartcitytravel.databinding.ActivitySignUpBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        util = new Util();
        connection = new Connection();
        color = new Color();
        validation = new Validation();

        util.setStatusBarColor(SignUpActivity.this, R.color.dark_grey);
        setLoadingBarColor();
        initializeValidator();
        registerAccount();

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
                    checkConnectionAndCreateAccount();
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

    // check all fields are valid. If valid create account by passing user account info to database
    // and move to Login Activity if account created successfully
    public void createAccount() {
        Call<Result> createAccountCallable = HttpClient.getInstance().createAccount(binding.fullNameEdit.getText().toString().toLowerCase(),
                binding.emailEdit.getText().toString().toLowerCase(), binding.passwordEdit.getText().toString(), "0",
                getString(R.string.default_profile_image_url));

        createAccountCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Result result = response.body();
                if (result.getAccount_status() == 0) {
                    Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    moveToLoginActivity();
                } else if (result.getAccount_status() == 1) {
                    util.createErrorDialog(SignUpActivity.this, "Account", result.getMessage());

                } else if (result.getAccount_status() == 3) {
                    util.createErrorDialog(SignUpActivity.this, "Account", "Google account exist with this email. " + result.getMessage());
                }
                hideLoadingBar();
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Unable to create account", Toast.LENGTH_SHORT).show();
                hideLoadingBar();
            }
        });
    }

    //Move from SignUp Activity to Login Activity
    //pass new created account email to Login Activity
    public void moveToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("email", binding.emailEdit.getText().toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    //change default loading bar color
    public void setLoadingBarColor() {
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_white)));

    }

    // show progress bar when user click on register button
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(SignUpActivity.this);
    }

    //hide progressbar when signup is complete and move to Login Activity or error occurs
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(SignUpActivity.this);
    }

    //check internet connection and then create account in database
    public void checkConnectionAndCreateAccount() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(SignUpActivity.this);
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
                            createAccount();
                        } else {
                            Toast.makeText(SignUpActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

}