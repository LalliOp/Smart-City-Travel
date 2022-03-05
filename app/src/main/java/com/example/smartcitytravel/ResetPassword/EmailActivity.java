package com.example.smartcitytravel.ResetPassword;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityEmailBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                if (binding.emailEdit.getText().toString().isEmpty()) {
                    showEmailEmptyError("Error! Empty Email");
                } else {
                    hideEmailEmptyError();
                    checkConnectionAndVerifyAccount();
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

    //check internet connection and then verify account by database
    public void checkConnectionAndVerifyAccount() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = util.isConnectionAvailable(EmailActivity.this);
                EmailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionAvailable) {
                            verifyLogin();
                        } else {
                            Toast.makeText(EmailActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    //check whether account exist or not
    public void verifyLogin() {
        showLoginLoadingBar();
//        Call<Result> verifyAccountResultCallable = HttpClient.getInstance().verifyAccount(binding.emailEdit.getText().toString().toLowerCase(),
//                binding.passwordEdit.getText().toString());
//
//        verifyAccountResultCallable.enqueue(new Callback<Result>() {
//            @Override
//            public void onResponse(Call<Result> call, Response<Result> response) {
//                Result result = response.body();
//                hideLoginLoadingBar();
//            }
//
//            @Override
//            public void onFailure(Call<Result> call, Throwable t) {
//                Toast.makeText(EmailActivity.this, "Unable to sign in", Toast.LENGTH_SHORT).show();
//                hideLoginLoadingBar();
//            }
//        });
    }

    // show progress bar when user click on login button
    public void showLoginLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        binding.emailEdit.setEnabled(false);
        binding.continueBtn.setEnabled(false);
    }

    //hide progressbar when login complete and move to home activity or error occurs
    public void hideLoginLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        binding.emailEdit.setEnabled(true);
        binding.continueBtn.setEnabled(true);
    }
}