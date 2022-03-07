package com.example.smartcitytravel.ResetPassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityEmailBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailActivity extends AppCompatActivity {
    private ActivityEmailBinding binding;
    private Util util;
    private Connection connection;
    private Color color;
    private PreferenceHandler preferenceHandler;
    private boolean validate_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        connection = new Connection();
        color = new Color();
        preferenceHandler = new PreferenceHandler();
        validate_email = false;

        continueButtonClickListener();
    }

    // call when user click on continue button
    // check email ,send pin code and move to pin code activity
    public void continueButtonClickListener() {
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(EmailActivity.this);
                validateEmail();
                if (validate_email) {
                    checkConnectionAndVerifyEmail();
                }


            }
        });
    }

    //check email field contain valid and allowed characters
    public void validateEmail() {
        String email = binding.emailEdit.getText().toString();
        String emailRegex = "^[A-Za-z0-9.]+@[A-Za-z.]+$";
        if (email.isEmpty()) {
            showEmailError("Error! Empty Email");
            validate_email = false;
        } else if (!email.matches(emailRegex)) {
            showEmailError("Error! Invalid Email");
            validate_email = false;
        } else {
            hideEmailError();
            validate_email = true;
        }
    }

    //show error msg and error icon color in email field
    public void showEmailError(String errorMsg) {
        binding.emailLayout.setErrorIconTintList(color.iconRedColor(this));
        binding.emailLayout.setError(errorMsg);
    }

    //hide error icon color and msg in email field when no error occurs
    public void hideEmailError() {
        binding.emailLayout.setError(null);
    }

    //check internet connection and then verify email by database
    public void checkConnectionAndVerifyEmail() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = connection.isConnectionAvailable(EmailActivity.this);
                EmailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionAvailable) {
                            verifyEmail();
                        } else {
                            Toast.makeText(EmailActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    //check whether email exist or not
    public void verifyEmail() {
        showLoadingBar();
        Call<Result> verifyEmailCallable = HttpClient.getInstance().verifyEmail(binding.emailEdit.getText().toString().toLowerCase());

        verifyEmailCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                Result result = response.body();
                if (result.getAccount_status() == -1) {
                    util.createErrorDialog(EmailActivity.this, "Account", result.getMessage());
                } else if (result.getAccount_status() == 0) {
                    util.createErrorDialog(EmailActivity.this, "Account",
                            "Account exist with google. " + result.getMessage());
                } else if (result.getAccount_status() == 1) {
                    preferenceHandler.saveEmailOfResetPasswordProcess(EmailActivity.this, binding.emailEdit.getText().toString().toLowerCase());
                    moveToPinCodeActivity();
                }
                hideLoadingBar();
            }

            @Override
            public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                Toast.makeText(EmailActivity.this, "Unable to verify email", Toast.LENGTH_SHORT).show();
                hideLoadingBar();
            }
        });


    }

    // show progress bar when user click on continue button
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(EmailActivity.this);

    }

    //hide progressbar when move to next activity or error occurs
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(EmailActivity.this);

    }

    //Move from Email Activity to Pin Code Activity
    public void moveToPinCodeActivity() {
        Intent intent = new Intent(this, PinCodeActivity.class);
        startActivity(intent);
    }

}