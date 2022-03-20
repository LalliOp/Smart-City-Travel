package com.example.smartcitytravel.Activities.Login;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import com.example.smartcitytravel.AWSService.DataModel.User;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Activities.Home.HomeActivity;
import com.example.smartcitytravel.Activities.ResetPassword.EmailActivity;
import com.example.smartcitytravel.Activities.SignUp.SignUpActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Color;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.Util.Validation;
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
    private Color color;
    private Validation validation;
    private PreferenceHandler preferenceHandler;
    private boolean validate_email;
    private boolean validate_password;

    //run when launch() function is called by GoogleSignUpActivityResult
    private ActivityResultLauncher<Intent> GoogleSignInActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    hideGoogleSignInLoadingBar();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        getGoogleSignInResult(result);
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
        color = new Color();
        validation = new Validation();
        preferenceHandler = new PreferenceHandler();

        util.setStatusBarColor(LoginActivity.this, R.color.brown);
        setLoadingBarColor();
        initializeValidator();
        setEmail();
        login();
        signInWithGoogle();
        signUp();
        resetPassword();
    }

    //initialize validate variable for each edit field which help us to know which field contain error or not
    public void initializeValidator() {
        validate_email = false;
        validate_password = false;
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
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(LoginActivity.this);
        if (isConnectionSourceAvailable) {
            showGoogleSignInLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean isInternetAvailable = connection.isInternetAvailable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInternetAvailable) {
                            initializeGoogleSignIn();
                        } else {
                            hideGoogleSignInLoadingBar();
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

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, googleSignInOptions);

        Intent signInIntent = googleSignInClient.getSignInIntent();

        GoogleSignInActivityResult.launch(signInIntent);
    }

    //Return object whether user successfully signIn or signUp with google account or throw exception
    //move to home activity
    public void getGoogleSignInResult(ActivityResult result) {
        Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());

        try {
            GoogleSignInAccount googleSignInAccount = googleSignInAccountTask.getResult(ApiException.class);
            checkConnectionAndVerifyEmail(googleSignInAccount);

        } catch (ApiException e) {
            Toast.makeText(LoginActivity.this, "Unable to Sign In with Google", Toast.LENGTH_SHORT).show();
        }
    }

    // save google account details in database when user login with google account
    public void saveGoogleAccount(GoogleSignInAccount googleSignInAccount) {
        String profile_image_url;
        if (googleSignInAccount.getPhotoUrl() == null) {
            profile_image_url = getString(R.string.default_profile_image_url);
        } else {
            profile_image_url = googleSignInAccount.getPhotoUrl().toString();
        }
        Call<Result> createAccountCallable = HttpClient.getInstance().createAccount(googleSignInAccount.getDisplayName().toLowerCase(),
                googleSignInAccount.getEmail().toLowerCase(), "0", "1", profile_image_url);

        createAccountCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (response.body() != null) {
                    getAccountDetails(googleSignInAccount.getEmail().toLowerCase());
                }

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Unable to save google account details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //check internet connection and then verify email by database
    public void checkConnectionAndVerifyEmail(GoogleSignInAccount googleSignInAccount) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(LoginActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            verifyEmail(googleSignInAccount);
                        } else {
                            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    //check whether google email already exist or not
    //if account already exist get details otherwise create new account with google info
    public void verifyEmail(GoogleSignInAccount googleSignInAccount) {
        Call<Result> verifyEmailCallable = HttpClient.getInstance().verifyEmail(googleSignInAccount.getEmail());

        verifyEmailCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                Result result = response.body();
                if (result != null) {
                    if (result.getAccount_status() == -1) {
                        saveGoogleAccount(googleSignInAccount);
                    } else if (result.getAccount_status() == 1 || result.getAccount_status() == 0) {
                        getAccountDetails(googleSignInAccount.getEmail());
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "Unable to setup Account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Unable to verify email", Toast.LENGTH_SHORT).show();
            }
        });


    }

    // get account details from database and save in shared preference
    public void getAccountDetails(String email) {
        Call<User> CallableAccount = HttpClient.getInstance().getAccount(email);

        CallableAccount.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                if (user != null) {
                    moveToHomeActivity();
                    preferenceHandler.setLoginAccountPreference(user, LoginActivity.this);
                }

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Unable to get account details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //run by user click on login button
    //check email and password is not empty, check internet connection and then verify account by database
    public void login() {
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                util.hideKeyboard(LoginActivity.this);

                validateEmail();
                validatePassword();
                if (validate_email && validate_password) {
                    checkConnectionAndVerifyAccount();
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

    //password field is empty or not
    public void validatePassword() {
        if (binding.passwordEdit.getText().toString().isEmpty()) {
            showPasswordError("Error! Empty Password");
            validate_email = false;
        } else {
            hidePasswordError();
            validate_password = true;
        }
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

    //show error msg and hide error icon in password field
    public void showPasswordError(String errorMsg) {
        binding.passwordLayout.setEndIconTintList(color.iconRedColor(this));
        binding.passwordLayout.setError(errorMsg);
        binding.passwordLayout.setErrorIconDrawable(null);
    }

    //hide error msg in password field
    public void hidePasswordError() {
        binding.passwordLayout.setEndIconTintList(color.iconWhiteColor(this));
        binding.passwordLayout.setError(null);
    }

    //check whether account exist or not
    public void verifyLogin() {

        Call<Result> verifyAccountResultCallable = HttpClient.getInstance().verifyAccount(binding.emailEdit.getText().toString().toLowerCase(),
                binding.passwordEdit.getText().toString());

        verifyAccountResultCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                Result result = response.body();
                if (result != null) {
                    if (result.getAccount_status() == 1) {
                        getAccountDetails(binding.emailEdit.getText().toString().toLowerCase());
                    } else if (result.getAccount_status() == 0) {
                        util.createErrorDialog(LoginActivity.this, "Password", result.getMessage());
                    } else if (result.getAccount_status() == -1) {
                        util.createErrorDialog(LoginActivity.this, "Account", "No account exist with this email");
                    } else if (result.getAccount_status() == 2) {
                        util.createErrorDialog(LoginActivity.this, "Account", "Google account exist with this email. " + result.getMessage());
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "Unable to sign in", Toast.LENGTH_SHORT).show();
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
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(LoginActivity.this);
        if (isConnectionSourceAvailable) {
            showLoginLoadingBar();
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
                            verifyLogin();
                        } else {
                            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoginLoadingBar();

                        }

                    }
                });
            }
        });
        executor.shutdown();
    }

    //change default loading bar color
    public void setLoadingBarColor() {
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2)));
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

    // show progress bar when user click on google sign in button
    public void showGoogleSignInLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        binding.loadingProgressBar.loadingBarBackground.setVisibility(View.GONE);
        util.makeScreenNotTouchable(LoginActivity.this);
    }

    //hide progressbar when google sign in complete and move to home activity or error occurs
    public void hideGoogleSignInLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        binding.loadingProgressBar.loadingBarBackground.setVisibility(View.VISIBLE);
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