package com.example.smartcitytravel.Login;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.VerifyAccountResult;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Dialogs.Dialog;
import com.example.smartcitytravel.Home.HomeActivity;
import com.example.smartcitytravel.SignUp.SignUpActivity;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private boolean validate_email;
    private boolean validate_password;
    private Util util;

    //run when launch() function is called by GoogleSignUpActivityResult
    private ActivityResultLauncher<Intent> GoogleSignUpActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    hideGoogleSignUpLoading();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        getGoogleSignUpResult(result);
                        moveToHomeActivity();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        setEmail();
        login();
        signUpWithGoogle();
        signUp();
    }

    // set email in email field in login activity when create account from signup activity and move to login activity
    public void setEmail() {
        if (getIntent().getExtras() != null) {
            String email = getIntent().getExtras().getString("email");
            binding.emailEdit.setText(email);
        }

    }

    /* called when user click on google icon
     * allow user to signUp or signIn with google account*/
    public void signUpWithGoogle() {
        binding.googleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGoogleSignUpLoading();

                GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, googleSignInOptions);

                Intent signInIntent = googleSignInClient.getSignInIntent();

                GoogleSignUpActivityResult.launch(signInIntent);

            }
        });

    }

    //Return object whether user successfully signIn or signUp with google account or throw exception
    public void getGoogleSignUpResult(ActivityResult result) {
        Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

        try {
            GoogleSignInAccount googleSignInAccount = googleSignInAccountTask.getResult(ApiException.class);
            Toast.makeText(LoginActivity.this, googleSignInAccount.getEmail(), Toast.LENGTH_SHORT).show();
        } catch (ApiException e) {
            Toast.makeText(LoginActivity.this, e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    // show progress bar when user click on signUp with Google Account
    public void showGoogleSignUpLoading() {
        binding.googleSignUpLoading.setVisibility(View.VISIBLE);
        binding.emailEdit.setEnabled(false);
        binding.passwordEdit.setEnabled(false);
    }

    //hide progress bar when signUp with Google Account is complete or user press back button
    public void hideGoogleSignUpLoading() {
        binding.googleSignUpLoading.setVisibility(View.GONE);
        binding.emailEdit.setEnabled(true);
        binding.passwordEdit.setEnabled(true);
    }

    //TODO:complete login
    public void login() {
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.emailEdit.getText().toString().isEmpty()) {
                    showEmailEmptyError("Error! Empty Email");
                } else {
                    hideEmailEmptyError();
                }
                if (binding.passwordEdit.getText().toString().isEmpty()) {
                    showPasswordEmptyError("Error! Empty Password");
                } else {
                    hidePasswordEmptyError();
                }
                if (!binding.emailEdit.getText().toString().isEmpty() &&
                        !binding.passwordEdit.getText().toString().isEmpty()) {
                    checkConnectionAndVerifyAccount();
                }

            }
        });
    }

    //Move from Login Activity to SignUp Activity when user click signup from here text
    public void signUp() {
        binding.signUpHereTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    //Move from Login Activity to Home Activity
    public void moveToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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

    //show error msg and hide error icon in password field when password field is empty
    public void showPasswordEmptyError(String errorMsg) {
        binding.passwordLayout.setEndIconTintList(util.iconRedColor(this));
        binding.passwordLayout.setError(errorMsg);
        binding.passwordLayout.setErrorIconDrawable(null);
    }

    //hide msg in password field when password field is not empty
    public void hidePasswordEmptyError() {
        binding.passwordLayout.setEndIconTintList(util.iconWhiteColor(this));
        binding.passwordLayout.setError(null);
    }

    public void verifySignIn() {
        showSignInLoadingBar();
        Call<VerifyAccountResult> verifyAccountResultCallable = HttpClient.getInstance().verifyAccount(binding.emailEdit.getText().toString().toLowerCase(),
                binding.passwordEdit.getText().toString());

        verifyAccountResultCallable.enqueue(new Callback<VerifyAccountResult>() {
            @Override
            public void onResponse(Call<VerifyAccountResult> call, Response<VerifyAccountResult> response) {
                VerifyAccountResult result = response.body();
                if (result.getAccount_status() == 1) {
                    moveToHomeActivity();
                } else if (result.getAccount_status() == 0) {
                    showPasswordEmptyError("Error! " + result.getErrorMsg());
                } else if (result.getAccount_status() == -1) {
                    createErrorDialog("Account","There are no account exist with this email");
                }
                hideSignInLoadingBar();
            }

            @Override
            public void onFailure(Call<VerifyAccountResult> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Unable to sign in", Toast.LENGTH_SHORT).show();
                hideSignInLoadingBar();
            }
        });
    }

    //check internet connection and then verify account by database
    public void checkConnectionAndVerifyAccount() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = util.isConnectionAvailable(LoginActivity.this);

                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionAvailable) {
                            verifySignIn();
                        } else {
                            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    public void createErrorDialog(String title, String message) {
        Dialog dialog = new Dialog(title, message);
        dialog.show(getSupportFragmentManager(), "dialog");
        dialog.setCancelable(false);
    }

    // show progress bar when user click on login button
    public void showSignInLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        binding.emailEdit.setEnabled(false);
        binding.passwordEdit.setEnabled(false);
        binding.loginBtn.setEnabled(false);
    }

    //hide progressbar when login complete and move to home activity or error occurs
    public void hideSignInLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        binding.emailEdit.setEnabled(true);
        binding.passwordEdit.setEnabled(true);
        binding.loginBtn.setEnabled(true);
    }
}