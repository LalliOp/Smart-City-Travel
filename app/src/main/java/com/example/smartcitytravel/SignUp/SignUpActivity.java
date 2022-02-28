package com.example.smartcitytravel.SignUp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Login.LoginActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivitySignUpBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private Util util;
    private boolean validate_full_name;
    private boolean validate_email;
    private boolean validate_password;
    private boolean validate_confirm_password;
    private ColorStateList errorColorStateList;
    private ColorStateList normalColorStateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        initializeValidator();
        iconErrorColor();
        iconNormalColor();
        registerAccount();

    }

    //initialize validate variable for each field which help us to know which field contain error or not
    public void initializeValidator() {
        validate_full_name = false;
        validate_email = false;
        validate_password = false;
        validate_confirm_password = false;
    }

    //create error color which will used as icon color when error shown
    public void iconErrorColor() {
        int redColor = getResources().getColor(R.color.red);
        errorColorStateList = ColorStateList.valueOf(redColor);
    }

    //create normal color which will used as icon color when no error occurs
    public void iconNormalColor() {
        int whiteColor = getResources().getColor(R.color.white);
        normalColorStateList = ColorStateList.valueOf(whiteColor);
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
        String nameRegex = "[a-zA-Z\\s]+";
        if (fullName.isEmpty()) {
            showFullNameError("Error! Enter your name");
        } else if (!fullName.matches(nameRegex)) {
            showFullNameError("Error! Only alphabets and spaces are acceptable");
        } else {
            removeFullNameError();
        }
    }

    //show error msg and error icon color in full name field
    public void showFullNameError(String errorMsg) {
        binding.fullNameLayout.setErrorIconTintList(errorColorStateList);
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
        String emailRegex = "^[A-Za-z0-9.]+@[A-Za-z.]+$";
        if (email.isEmpty()) {
            showEmailError("Error! Enter email");
        } else if (!email.matches(emailRegex)) {
            showEmailError("Error! Invalid Email");
        } else {
            removeEmailError();
        }
    }

    //show error msg and error icon color in email field
    public void showEmailError(String errorMsg) {
        binding.emailLayout.setErrorIconTintList(errorColorStateList);
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
        binding.passwordLayout.setEndIconTintList(errorColorStateList);
        binding.passwordLayout.setError(errorMsg);
        binding.passwordLayout.setErrorIconDrawable(null);
        validate_password = false;
    }

    //hide msg in password field when no error occurs
    public void removePasswordError() {
        binding.passwordLayout.setEndIconTintList(normalColorStateList);
        binding.passwordLayout.setError(null);
        validate_password = true;
    }

    //check confirm password field
    //and match confirm password with password
    public void matchPasswordAndConfirmPassword() {
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
        binding.confirmPasswordLayout.setEndIconTintList(errorColorStateList);
        binding.confirmPasswordLayout.setError(errorMsg);
        binding.confirmPasswordLayout.setErrorIconDrawable(null);
        validate_confirm_password = false;
    }

    //hide msg in confirm password field when no error occurs
    public void removeConfirmPasswordError() {
        binding.confirmPasswordLayout.setEndIconTintList(normalColorStateList);
        binding.confirmPasswordLayout.setError(null);
        validate_confirm_password = true;
    }

    // check all fields are valid. If valid create account by passing user account info to database
    // and move to Login Activity if account created successfully
    public void createAccount() {
        showLoadingBar();

        Call<Result> createAccountCallable = HttpClient.getInstance().createUserAccount(binding.fullNameEdit.getText().toString().toLowerCase(),
                binding.emailEdit.getText().toString().toLowerCase(), binding.passwordEdit.getText().toString());
        createAccountCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Result result = response.body();
                if (result.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    MoveToLoginActivity();
                } else {
                    Toast.makeText(SignUpActivity.this, result.getErrorMsg(), Toast.LENGTH_SHORT).show();
                    showEmailError("Error! " + result.getErrorMsg());

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

    //Move from SignUp Activity to Login Activity after checking each field contain valid characters
    public void MoveToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("email", binding.emailEdit.getText().toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    // show progress bar
    public void showLoadingBar() {
        binding.signUpBarLayout.setVisibility(View.VISIBLE);
    }

    //hide progressbar
    public void hideLoadingBar() {
        binding.signUpBarLayout.setVisibility(View.GONE);
    }

    //check internet connection and create account
    public void checkConnectionAndCreateAccount() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = util.isConnectionAvailable(SignUpActivity.this);

                SignUpActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionAvailable) {
                            createAccount();
                        } else {
                            Toast.makeText(SignUpActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

}