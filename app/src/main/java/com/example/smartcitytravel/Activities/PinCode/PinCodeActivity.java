package com.example.smartcitytravel.Activities.PinCode;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.PinCodeResult;
import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Activities.ResetPassword.NewPasswordActivity;
import com.example.smartcitytravel.Activities.SuccessfulAccountMessage.SuccessfulAccountMessageActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPinCodeBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PinCodeActivity extends AppCompatActivity {
    private ActivityPinCodeBinding binding;
    private Util util;
    private Connection connection;
    private int pin_code;
    private String email;
    private boolean fromSignUpActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        setLoadingBarColor();
        setTitle();
        getEmail();
        boldEmail();
        util.setStatusBarColor(PinCodeActivity.this, R.color.black);
        sendPinCode();
        continueButtonClickListener();
        resendCode();
    }

    //initialize variables
    public void initialize() {
        pin_code = 0;
        util = new Util();
        connection = new Connection();
        fromSignUpActivity = false;
    }

    //change default loading bar color
    public void setLoadingBarColor() {
        ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2));
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(colorStateList);
    }

    //set title of activity
    public void setTitle() {
        String title = getIntent().getExtras().getString("title");
        binding.titleTxt.setText(title);

    }

    // get email which is passed by Email Activity or SignUp Activity
    public void getEmail() {
        if (getIntent().getExtras().getString("signup_email") != null) {
            email = getIntent().getExtras().getString("signup_email");
            fromSignUpActivity = true;
        } else {
            email = getIntent().getExtras().getString("email");
            fromSignUpActivity = false;
        }
    }

    //add bold style email in header text
    public void boldEmail() {
        String normalTxt = binding.pincodeTxt.getText().toString();
        SpannableString styleTxt = new SpannableString(normalTxt + " " + email);
        styleTxt.setSpan(new StyleSpan(Typeface.BOLD), 34, styleTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.pincodeTxt.setText(styleTxt);
    }

    //check pin code field contain valid data
    //also check entered pin code is right or not and move to New Password Activity
    public void validatePinCode() {
        String pinCode = binding.pinCodeEdit.getText().toString();
        if (pinCode.isEmpty()) {
            showPinCodeError(" ");
        } else if (Integer.parseInt(pinCode) == pin_code) {
            removePinCodeError();
            if (fromSignUpActivity) {
                createAccount();
            } else {
                moveToNewPasswordActivity();
            }

        } else if (Integer.parseInt(pinCode) != pin_code) {
            showPinCodeError(" ");
            util.createErrorDialog(PinCodeActivity.this, "Error", "Incorrect pin code entered");
        }
    }

    //show error msg and error icon color in pin code field
    public void showPinCodeError(String errorMsg) {
        binding.pincodeLayout.setErrorIconDrawable(null);
        binding.pincodeLayout.setError(errorMsg);
    }

    //hide error icon color and msg in pin code field when no error occurs
    public void removePinCodeError() {
        binding.pincodeLayout.setError(null);
    }

    // call when user click on continue button
    // verify and validate pin code and move to New Password Activity
    public void continueButtonClickListener() {
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(PinCodeActivity.this);
                validatePinCode();
            }
        });
    }

    //Move from PinCode Activity to NewPassword Activity
    public void moveToNewPasswordActivity() {
        Intent intent = new Intent(this, NewPasswordActivity.class);
        intent.putExtra("email", email);
        startActivity(intent);

    }

    public void resendCode() {
        binding.resendCodeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPinCode();
            }
        });
    }

    //send pin code to email user enter
    public void sendPinCode() {
        Call<PinCodeResult> pinCodeCallable = HttpClient.getInstance().sendPinCode(email);

        pinCodeCallable.enqueue(new Callback<PinCodeResult>() {
            @Override
            public void onResponse(@NonNull Call<PinCodeResult> call, @NonNull Response<PinCodeResult> response) {
                PinCodeResult result = response.body();
                if (result != null) {
                    pin_code = result.getPin_code();
                    Toast.makeText(PinCodeActivity.this, pin_code + "", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(PinCodeActivity.this, "Unable to send pin code. Try resend code", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(@NonNull Call<PinCodeResult> call, @NonNull Throwable t) {
                Toast.makeText(PinCodeActivity.this, "No Connection", Toast.LENGTH_SHORT).show();

            }
        });
    }

    // check all fields are valid. If valid create account by passing user account info to database
    // and move to Login Activity if account created successfully
    public void createAccount() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(PinCodeActivity.this);
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        String name = getIntent().getExtras().getString("signup_name");
        String password = getIntent().getExtras().getString("signup_password");
        String normalizedFullName = name.toLowerCase();
        normalizedFullName = normalizedFullName.trim().replaceAll("\\s{2,}", " ");

        Call<Result> createAccountCallable = HttpClient.getInstance().createAccount(normalizedFullName,
                email.toLowerCase(), password, "0",
                getString(R.string.default_profile_image_url));

        createAccountCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                Result result = response.body();
                if (result != null) {
                    if (result.getStatus() == 0) {
                        moveToSuccessfulAccountCreationActivity();
                    }
                } else {
                    Toast.makeText(PinCodeActivity.this, "Unable to create account", Toast.LENGTH_SHORT).show();
                }
                hideLoadingBar();
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(PinCodeActivity.this, "No Connection", Toast.LENGTH_SHORT).show();
                hideLoadingBar();
            }
        });
    }

    // show progress bar when user click on register button
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(PinCodeActivity.this);
    }

    //hide progressbar when signup is complete and move to Login Activity or error occurs
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(PinCodeActivity.this);
    }

    //move to successful account creation activity
    public void moveToSuccessfulAccountCreationActivity() {
        Intent intent = new Intent(this, SuccessfulAccountMessageActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("message", "Account Created Successfully");
        startActivity(intent);
    }
}