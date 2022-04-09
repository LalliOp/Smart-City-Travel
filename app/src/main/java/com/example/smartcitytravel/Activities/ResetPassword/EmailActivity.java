package com.example.smartcitytravel.Activities.ResetPassword;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
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
    private Validation validation;
    private boolean validate_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        util.setStatusBarColor(EmailActivity.this, R.color.black);
        setLoadingBarColor();
        continueButtonClickListener();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        connection = new Connection();
        color = new Color();
        preferenceHandler = new PreferenceHandler();
        validation = new Validation();
        validate_email = false;
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
        String errorMessage = validation.validateEmail(email);
        if (errorMessage.isEmpty()) {
            hideEmailError();

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
    public void hideEmailError() {
        binding.emailLayout.setError(null);
        validate_email = true;
    }

    //check internet connection and then verify email by database
    public void checkConnectionAndVerifyEmail() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(EmailActivity.this);
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
                            verifyEmail();
                        } else {
                            Toast.makeText(EmailActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    //check whether email exist or not
    public void verifyEmail() {
        Call<Result> verifyEmailCallable = HttpClient.getInstance().verifyEmail(binding.emailEdit.getText().toString().toLowerCase());

        verifyEmailCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                Result result = response.body();
                if (result.getStatus() == -1) {
                    util.createErrorDialog(EmailActivity.this, "Account", result.getMessage());
                } else if (result.getStatus() == 0) {
                    util.createErrorDialog(EmailActivity.this, "Account",
                            "Account exist with google. " + result.getMessage());
                } else if (result.getStatus() == 1) {
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

    //change default loading bar color
    public void setLoadingBarColor() {
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2)));
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