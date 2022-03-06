package com.example.smartcitytravel.ResetPassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.PinCodeResult;
import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        continueButtonClickListener();
    }

    // call when user click on continue button
    // check email ,send pin code and move to pin code activity
    public void continueButtonClickListener() {
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(EmailActivity.this);
                if (binding.emailEdit.getText().toString().isEmpty()) {
                    showEmailEmptyError("Error! Empty Email");
                } else {
                    hideEmailEmptyError();
                    checkConnectionAndVerifyEmail();
                }
            }
        });
    }

    //show error msg and error icon color in email field when email field is empty
    public void showEmailEmptyError(String errorMsg) {
        binding.emailLayout.setErrorIconTintList(util.iconRedColor(this));
        binding.emailLayout.setError(errorMsg);
    }

    //hide error icon color and msg in email field when email field is not empty
    public void hideEmailEmptyError() {
        binding.emailLayout.setError(null);
    }

    //check internet connection and then verify email by database
    public void checkConnectionAndVerifyEmail() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = util.isConnectionAvailable(EmailActivity.this);
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
                    hideLoadingBar();
                } else if (result.getAccount_status() == 0) {
                    util.createErrorDialog(EmailActivity.this, "Account",
                            "Account exist with google. " + result.getMessage());
                    hideLoadingBar();
                } else if (result.getAccount_status() == 1) {
                    send_pin_code();
                }

            }

            @Override
            public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                Toast.makeText(EmailActivity.this, "Unable to verify email", Toast.LENGTH_SHORT).show();
                hideLoadingBar();
            }
        });


    }

    //send pin code to email user enter
    public void send_pin_code() {
        Call<PinCodeResult> pinCodeCallable = HttpClient.getInstance().sendPinCode(binding.emailEdit.getText().toString().toLowerCase());

        pinCodeCallable.enqueue(new Callback<PinCodeResult>() {
            @Override
            public void onResponse(@NonNull Call<PinCodeResult> call, @NonNull Response<PinCodeResult> response) {
                PinCodeResult result = response.body();
                if (result != null) {
                    moveToPinCodeActivity(result.getPin_code());
                    hideLoadingBar();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PinCodeResult> call, @NonNull Throwable t) {
                Toast.makeText(EmailActivity.this, "Unable to send pin code", Toast.LENGTH_SHORT).show();
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
    //pass pin code and email from this activity to PinCode Activity
    public void moveToPinCodeActivity(int pinCode) {
        Intent intent = new Intent(this, PinCodeActivity.class);
        intent.putExtra("pin_code", pinCode);
        intent.putExtra("email", binding.emailEdit.getText().toString().toLowerCase());
        startActivity(intent);

    }

}