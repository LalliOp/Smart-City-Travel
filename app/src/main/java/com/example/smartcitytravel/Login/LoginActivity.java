package com.example.smartcitytravel.Login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Home.HomeActivity;
import com.example.smartcitytravel.ResetPassword.EmailActivity;
import com.example.smartcitytravel.SignUp.SignUpActivity;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
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
    private Util util;
    private Connection connection;
    private PreferenceHandler preferenceHandler;
    private Color color;

    //run when launch() function is called by GoogleSignUpActivityResult
    private ActivityResultLauncher<Intent> GoogleSignInActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    hideGoogleSignInLoading();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        getGoogleSignInResult(result);
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
        connection = new Connection();
        preferenceHandler = new PreferenceHandler();
        color = new Color();

        setEmail();
        login();
        signInWithGoogle();
        signUp();
        resetPassword();
    }

    // set email in email field in login activity when create account from signup activity and move to login activity
    // get email which is passed by SignUp Activity
    public void setEmail() {
        if (getIntent().getExtras() != null) {
            String email = getIntent().getExtras().getString("email");
            binding.emailEdit.setText(email);
        }

    }

    /* called when user click on google icon
     * allow user to signUp or signIn with google account*/
    public void signInWithGoogle() {
        binding.googleImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnectionAndSignInWithGoogle();
            }
        });

    }

    //check internet connection and then start google sign in service
    public void checkConnectionAndSignInWithGoogle() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = connection.isConnectionAvailable(LoginActivity.this);

                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionAvailable) {
                            initializeGoogleSignIn();
                        } else {
                            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    // starting google sign in service
    public void initializeGoogleSignIn() {
        showGoogleSignInLoading();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, googleSignInOptions);

        Intent signInIntent = googleSignInClient.getSignInIntent();

        GoogleSignInActivityResult.launch(signInIntent);
    }

    //Return object whether user successfully signIn or signUp with google account or throw exception
    public void getGoogleSignInResult(ActivityResult result) {
        Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

        try {
            GoogleSignInAccount googleSignInAccount = googleSignInAccountTask.getResult(ApiException.class);
            checkConnectionAndSaveGoogleAccount(googleSignInAccount);

        } catch (ApiException e) {
            Toast.makeText(LoginActivity.this, "Unable to Sign In with Google", Toast.LENGTH_SHORT).show();
        }
    }

    // show progress bar when user click on signUp with Google Account
    public void showGoogleSignInLoading() {
        binding.googleSignInLoading.setVisibility(View.VISIBLE);
        binding.emailEdit.setEnabled(false);
        binding.passwordEdit.setEnabled(false);
    }

    //hide progress bar when signUp with Google Account is complete or user press back button
    public void hideGoogleSignInLoading() {
        binding.googleSignInLoading.setVisibility(View.GONE);
        binding.emailEdit.setEnabled(true);
        binding.passwordEdit.setEnabled(true);
    }

    // save google account details in database when user login with google account
    public void saveGoogleAccount(GoogleSignInAccount googleSignInAccount) {

        Call<Result> createAccountCallable = HttpClient.getInstance().createAccount(googleSignInAccount.getDisplayName().toLowerCase(),
                googleSignInAccount.getEmail().toLowerCase(), "0", "1");

        createAccountCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (response.body().getAccount_status() == 0) {
                    preferenceHandler.setLoginEmailPreference("", LoginActivity.this);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Unable to save google account details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //first check internet connection and then save google account details
    public void checkConnectionAndSaveGoogleAccount(GoogleSignInAccount googleSignInAccount) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = connection.isConnectionAvailable(LoginActivity.this);

                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionAvailable) {
                            saveGoogleAccount(googleSignInAccount);
                        } else {
                            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    //run by user click on login button
    //check email and password is not empty, check internet connection and then verify account by database
    public void login() {
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(LoginActivity.this);
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
        binding.emailLayout.setErrorIconTintList(color.iconRedColor(this));
        binding.emailLayout.setError(errorMsg);
    }

    //hide error icon color and msg in email field when email field is not empty
    public void hideEmailEmptyError() {
        binding.emailLayout.setError(null);
    }

    //show error msg and hide error icon in password field when password field is empty
    public void showPasswordEmptyError(String errorMsg) {
        binding.passwordLayout.setEndIconTintList(color.iconRedColor(this));
        binding.passwordLayout.setError(errorMsg);
        binding.passwordLayout.setErrorIconDrawable(null);
    }

    //hide msg in password field when password field is not empty
    public void hidePasswordEmptyError() {
        binding.passwordLayout.setEndIconTintList(color.iconWhiteColor(this));
        binding.passwordLayout.setError(null);
    }

    //check whether account exist or not
    public void verifyLogin() {
        showLoginLoadingBar();
        Call<Result> verifyAccountResultCallable = HttpClient.getInstance().verifyAccount(binding.emailEdit.getText().toString().toLowerCase(),
                binding.passwordEdit.getText().toString());

        verifyAccountResultCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                Result result = response.body();
                if (result.getAccount_status() == 1) {
                    preferenceHandler.setLoginEmailPreference(binding.emailEdit.getText().toString().toLowerCase(), LoginActivity.this);
                    moveToHomeActivity();
                } else if (result.getAccount_status() == 0) {
                    showPasswordEmptyError("Error! " + result.getMessage());
                } else if (result.getAccount_status() == -1) {
                    util.createErrorDialog(LoginActivity.this, "Account", "There are no account exist with this email");
                } else if (result.getAccount_status() == 2) {
                    util.createErrorDialog(LoginActivity.this, "Account", "Google account exist with this email. " + result.getMessage());
                }
                hideLoginLoadingBar();
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Unable to sign in", Toast.LENGTH_SHORT).show();
                hideLoginLoadingBar();
            }
        });
    }

    //check internet connection and then verify account by database
    public void checkConnectionAndVerifyAccount() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = connection.isConnectionAvailable(LoginActivity.this);
                LoginActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionAvailable) {
                            verifyLogin();
                        } else {
                            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    // show progress bar when user click on login button
    public void showLoginLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(LoginActivity.this);
    }

    //hide progressbar when login complete and move to home activity or error occurs
    public void hideLoginLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(LoginActivity.this);
    }

    //move to reset password process
    public void resetPassword() {
        binding.restPasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, EmailActivity.class);
                startActivity(intent);
            }
        });

    }
}