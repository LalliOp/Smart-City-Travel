package com.example.smartcitytravel.ResetPassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.PinCodeResult;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPinCodeBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PinCodeActivity extends AppCompatActivity {
    private ActivityPinCodeBinding binding;
    private Util util;
    private Connection connection;
    private PreferenceHandler preferenceHandler;
    private int pin_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        connection = new Connection();
        preferenceHandler = new PreferenceHandler();

        getPinCode();
        Toast.makeText(PinCodeActivity.this, pin_code + "", Toast.LENGTH_LONG).show();
        continueButtonClickListener();
        resendCode();
    }

    // get pin code which is passed by Email Activity
    public void getPinCode() {
        if (getIntent().getExtras() != null) {
            pin_code = getIntent().getExtras().getInt("pin_code");
        }
    }

    //check pin code field contain valid data
    //also check entered pin code is right or not and move to New Password Activity
    public void validatePinCode() {
        String pinCode = binding.pinCodeEdit.getText().toString();
        if (pinCode.isEmpty()) {
            showPinCodeError("Error! Empty Field");
        } else if (Integer.parseInt(pinCode) == pin_code) {
            removePinCodeError();
            moveToNewPasswordActivity();
        } else {
            showPinCodeError("Error! Invalid Code");
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
        startActivity(intent);

    }

    public void resendCode() {
        binding.resendCodeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnectionAndResendCode();
            }
        });
    }

    //check internet connection and then resend code to email
    public void checkConnectionAndResendCode() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = connection.isConnectionAvailable(PinCodeActivity.this);
                PinCodeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionAvailable) {
                            send_pin_code();
                        } else {
                            Toast.makeText(PinCodeActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    //send pin code to email user enter
    public void send_pin_code() {
        showLoadingBar();
        Call<PinCodeResult> pinCodeCallable = HttpClient.getInstance().sendPinCode(
                preferenceHandler.getEmailOfResetPasswordProcess(PinCodeActivity.this));

        pinCodeCallable.enqueue(new Callback<PinCodeResult>() {
            @Override
            public void onResponse(@NonNull Call<PinCodeResult> call, @NonNull Response<PinCodeResult> response) {
                PinCodeResult result = response.body();
                if (result != null) {
                    pin_code = result.getPin_code();
                    Toast.makeText(PinCodeActivity.this, pin_code + "", Toast.LENGTH_LONG).show();

                }
                hideLoadingBar();
            }

            @Override
            public void onFailure(@NonNull Call<PinCodeResult> call, @NonNull Throwable t) {
                Toast.makeText(PinCodeActivity.this, "Unable to send pin code", Toast.LENGTH_SHORT).show();
                hideLoadingBar();
            }
        });
    }

    // show progress bar when user click on resend code
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(PinCodeActivity.this);

    }

    //hide progressbar when code is resend or error occurs
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(PinCodeActivity.this);

    }
}