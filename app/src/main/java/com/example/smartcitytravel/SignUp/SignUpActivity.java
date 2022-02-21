package com.example.smartcitytravel.SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Home.HomeActivity;
import com.example.smartcitytravel.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private boolean validate_full_name;
    private boolean validate_email;
    private boolean validate_password;
    private boolean validate_confirm_password;
    private boolean validate_match_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeValidator();
        registerAccount();

    }

    //initialize validate variable for each field which help us to know which field contain error or not
    private void initializeValidator() {
        validate_full_name = false;
        validate_email = false;
        validate_password = false;
        validate_confirm_password = false;
        validate_match_password = false;
    }

    //run when user click on register button
    //check all fields contain valid and allowed characters and move to login activity if no error occur
    private void registerAccount() {
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateFullName();
                validateEmail();
                validatePassword();
                matchPasswordAndConfirmPassword();
                MoveToLoginActivity();
            }
        });
    }

    //check full name field contain valid and allowed characters
    private void validateFullName() {
        String fullName = binding.fullNameEdit.getText().toString();
        String nameRegex = "[a-zA-Z\\s]+";
        if (fullName.isEmpty()) {
            binding.fullNameLayout.setError("Error! Enter your name");
            validate_full_name = false;
        } else if (!fullName.matches(nameRegex)) {
            binding.fullNameLayout.setError("Error! Only alphabets and spaces are acceptable");
            validate_full_name = false;
        } else {
            binding.fullNameLayout.setError(null);
            validate_full_name = true;
        }
    }

    //check email field contain valid and allowed characters
    private void validateEmail() {
        String email = binding.emailEdit.getText().toString();
        String emailRegex = "^[A-Za-z0-9.]+@[A-Za-z.]+$";
        if (email.isEmpty()) {
            binding.emailLayout.setError("Error! Enter email");
            validate_email = false;
        } else if (!email.matches(emailRegex)) {
            binding.emailLayout.setError("Error! Invalid Email");
            validate_email = false;
        } else {
            binding.emailLayout.setError(null);
            validate_email = true;
        }
    }

    //check password field contain valid and allowed characters
    private void validatePassword() {
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
            binding.passwordLayout.setError("Error! Enter password");
            binding.passwordLayout.setErrorIconDrawable(null);
            validate_password = false;
        } else if (password.length() < 8) {
            binding.passwordLayout.setError("Error! Password should contain 8 or more characters");
            binding.passwordLayout.setErrorIconDrawable(null);
            validate_password = false;
        } else if (password.contains(" ")) {
            binding.passwordLayout.setError("Error! Password should not contain spaces");
            binding.passwordLayout.setErrorIconDrawable(null);
            validate_password = false;
        } else if (!containOneDigit) {
            binding.passwordLayout.setError("Error! Password should contain at least one digit");
            binding.passwordLayout.setErrorIconDrawable(null);
            validate_password = false;
        } else if (!containOneUpperCaseLetter) {
            binding.passwordLayout.setError("Error! Password should contain at least one uppercase letter");
            binding.passwordLayout.setErrorIconDrawable(null);
            validate_password = false;
        } else if (!containOneLowerCaseLetter) {
            binding.passwordLayout.setError("Error! Password should contain at least one lowercase letter");
            binding.passwordLayout.setErrorIconDrawable(null);
            validate_password = false;
        } else {
            binding.passwordLayout.setError(null);
            validate_password = true;
        }
    }

    //check confirm password field
    //and match confirm password with password
    private void matchPasswordAndConfirmPassword() {
        String password = binding.passwordEdit.getText().toString();
        String confirmPassword = binding.confirmPasswordEdit.getText().toString();
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordLayout.setError("ERROR! Re-enter password");
            binding.confirmPasswordLayout.setErrorIconDrawable(null);
            validate_confirm_password = false;
        } else if (!password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError("ERROR! Password not matched");
            binding.confirmPasswordLayout.setErrorIconDrawable(null);

            binding.passwordLayout.setError(" ");
            binding.passwordLayout.setErrorIconDrawable(null);

            validate_confirm_password = false;
            validate_match_password = false;
        } else if (password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError(null);
            validate_confirm_password = true;
            validate_match_password = true;
        }
    }

    //Move from SignUp Activity to Login Activity after checking each field contain valid characters
    private void MoveToLoginActivity() {
        if (validate_full_name && validate_email && validate_password && validate_confirm_password
                && validate_match_password) {

            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }
}