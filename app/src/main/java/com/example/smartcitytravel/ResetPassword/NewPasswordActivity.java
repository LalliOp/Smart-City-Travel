package com.example.smartcitytravel.ResetPassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Login.LoginActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityNewPasswordBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewPasswordActivity extends AppCompatActivity {
    private ActivityNewPasswordBinding binding;
    private Color color;
    private Connection connection;
    private Util util;
    private PreferenceHandler preferenceHandler;
    private boolean validate_password;
    private boolean validate_confirm_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        color = new Color();
        connection = new Connection();
        util = new Util();
        preferenceHandler = new PreferenceHandler();

        util.setStatusBarColor(NewPasswordActivity.this, R.color.black);
        initializeValidator();
        resetPassword();
    }

    //initialize validate variable for each field which help us to know which field contain error or not
    public void initializeValidator() {
        validate_password = false;
        validate_confirm_password = false;
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
        boolean containOneDigit = false;
        boolean containOneUpperCaseLetter = false;
        boolean containOneLowerCaseLetter = false;

        for (char oneChar : password.toCharArray()) {
            if (!containOneDigit && Character.isDigit(oneChar)) {
                containOneDigit = true;
            }
            if (!containOneUpperCaseLetter && Character.isUpperCase(oneChar)) {
                containOneUpperCaseLetter = true;
            }
            if (!containOneLowerCaseLetter && Character.isLowerCase(oneChar)) {
                containOneLowerCaseLetter = true;
            }
            if (containOneDigit && containOneUpperCaseLetter && containOneLowerCaseLetter) {
                break;
            }
        }
        if (password.isEmpty()) {
            showPasswordError("Error! Enter password");
        } else if (password.length() < 8) {
            showPasswordError("Error! Password should contain 8 or more characters");
        } else if (password.contains(" ")) {
            showPasswordError("Error! Password should not contain spaces");
        } else if (!containOneDigit) {
            showPasswordError("Error! Password should contain at least one digit");
        } else if (!containOneUpperCaseLetter) {
            showPasswordError("Error! Password should contain at least one uppercase letter");
        } else if (!containOneLowerCaseLetter) {
            showPasswordError("Error! Password should contain at least one lowercase letter");
        } else {
            removePasswordError();
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
    public void validateMatchPasswordAndConfirmPassword() {
        String password = binding.passwordEdit.getText().toString();
        String confirmPassword = binding.confirmPasswordEdit.getText().toString();
        if (confirmPassword.isEmpty()) {
            showConfirmPasswordError("ERROR! Re-enter password");
        } else if (!password.equals(confirmPassword)) {
            showConfirmPasswordError("ERROR! Password not matched");
            showPasswordError(" ");
        } else if (password.equals(confirmPassword)) {
            removeConfirmPasswordError();
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

    //check internet connection and then change account password in database
    public void checkConnectionAndChangePassword() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(NewPasswordActivity.this);
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isInternetAvailable();

                NewPasswordActivity.this.runOnUiThread(new Runnable() {
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
        executor.shutdown();
    }

    //change account password in database
    public void changePassword() {
        Call<Result> changePasswordCallable = HttpClient.getInstance().changePassword(
                preferenceHandler.getEmailOfResetPasswordProcess(NewPasswordActivity.this),
                binding.passwordEdit.getText().toString());

        changePasswordCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Result result = response.body();
                if (result != null && result.getAccount_status() == 0) {
                    Toast.makeText(NewPasswordActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    moveToLoginActivity();
                } else {
                    Toast.makeText(NewPasswordActivity.this, "Unable to change password", Toast.LENGTH_SHORT).show();
                }
                hideLoadingBar();
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(NewPasswordActivity.this, "Unable to change password", Toast.LENGTH_SHORT).show();
                hideLoadingBar();
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

    //Move from NewPassword Activity to Login Activity
    //pass email to Login Activity
    public void moveToLoginActivity() {
        String email = preferenceHandler.getEmailOfResetPasswordProcess(NewPasswordActivity.this);
        preferenceHandler.saveEmailOfResetPasswordProcess(NewPasswordActivity.this, "");

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("email", email);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
}