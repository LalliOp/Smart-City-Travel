package com.example.smartcitytravel.Activities;

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
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.DataModel.User;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private Util util;
    private Connection connection;
    private Color color;
    private Validation validation;
    private PreferenceHandler preferenceHandler;
    private boolean validate_email;
    private boolean validate_password;
    private CollectionReference userCollection;

    //run when launch() function is called by GoogleSignUpActivityResult
    private final ActivityResultLauncher<Intent> GoogleSignInActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        getGoogleSignInResult(result);
                    } else {
                        hideLoadingBar();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        util.setStatusBarColorDrawable(LoginActivity.this, R.drawable.login_statusbar_gardient);
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

    //initialize variables
    public void initialize() {
        util = new Util();
        connection = new Connection();
        color = new Color();
        validation = new Validation();
        preferenceHandler = new PreferenceHandler();
        userCollection = FirebaseFirestore.getInstance().collection("user");
    }

    // set email in email field in login activity when create account from successful account creation activity and move to login activity
    // get email which is passed by successful account creation Activity
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
            showLoadingBar();
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
                            hideLoadingBar();
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
            verifyEmail(googleSignInAccount);

        } catch (ApiException e) {
            Toast.makeText(LoginActivity.this, "Unable to Sign In with Google", Toast.LENGTH_SHORT).show();
            hideLoadingBar();
        }
    }

    // save google account details in database when user login with google account
    public void saveGoogleAccount(GoogleSignInAccount googleSignInAccount) {
        String profile_image_url;
        if (googleSignInAccount.getPhotoUrl() != null) {
            profile_image_url = googleSignInAccount.getPhotoUrl().toString();
        } else {
            profile_image_url = getString(R.string.default_profile_image_url);

        }

        User user = new User(googleSignInAccount.getDisplayName().toLowerCase(), googleSignInAccount.getEmail().toLowerCase(),
                "0", profile_image_url, true);

        userCollection.add(user).addOnSuccessListener(this, new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                if (!documentReference.getId().isEmpty()) {
                    preferenceHandler.setLoginAccountPreference(user, LoginActivity.this);
                    moveToHomeActivity();

                } else {
                    Toast.makeText(LoginActivity.this, "Unable to save google account details", Toast.LENGTH_SHORT).show();
                    hideLoadingBar();
                }
            }
        });

    }

    //check whether google email already exist or not
    //if account already exist get details otherwise create new account with google info
    public void verifyEmail(GoogleSignInAccount googleSignInAccount) {
        userCollection.whereEqualTo("email", googleSignInAccount.getEmail().toLowerCase())
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                User user = querySnapshot.toObject(User.class);
                                user.setUserId(querySnapshot.getId());

                                preferenceHandler.setLoginAccountPreference(user, LoginActivity.this);
                                moveToHomeActivity();
                            }

                        } else {
                            saveGoogleAccount(googleSignInAccount);
                        }
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
                    checkConnectionAndVerifyLogin();
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

    //check connection exist or not and call verifyLogin() if connection exist
    public void checkConnectionAndVerifyLogin() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(LoginActivity.this);
        if (isConnectionSourceAvailable) {
            showLoadingBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isInternetAvailable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            verifyLogin();
                        } else {
                            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            hideLoadingBar();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    //check whether account exist or not and save account info in preference
    public void verifyLogin() {

        userCollection.whereEqualTo("email", binding.emailEdit.getText().toString().toLowerCase())
                .get().addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                String userInputPassword = binding.passwordEdit.getText().toString();

                                User user = querySnapshot.toObject(User.class);
                                user.setUserId(querySnapshot.getId());
                                if (user.getGoogle_account()) {
                                    util.createErrorDialog(LoginActivity.this, "Account", "Account already exist with google. Try sign in with google");
                                } else {
                                    if (user.getPassword().equals(userInputPassword)) {
                                        preferenceHandler.setLoginAccountPreference(user, LoginActivity.this);
                                        moveToHomeActivity();
                                    } else {
                                        util.createErrorDialog(LoginActivity.this, "Password", "Incorrect Password");
                                    }
                                }

                            }
                        } else {
                            util.createErrorDialog(LoginActivity.this, "Account", "No account exist with this email");
                        }
                        hideLoadingBar();

                    }
                });
    }

    //change default loading bar color
    public void setLoadingBarColor() {
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2)));
    }

    // show progress bar
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(LoginActivity.this);
    }

    //hide progressbar
    public void hideLoadingBar() {
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