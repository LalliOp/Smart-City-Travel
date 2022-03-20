package com.example.smartcitytravel;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Activities.Home.HomeActivity;
import com.example.smartcitytravel.Activities.Login.LoginActivity;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private Connection connection;
    private PreferenceHandler preferenceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connection = new Connection();
        preferenceHandler = new PreferenceHandler();
        Util util = new Util();

        util.setStatusBarColor(MainActivity.this, R.color.white);
        checkUserAlreadySignIn();
    }

    //Check whether user is already sign in
    public void checkUserAlreadySignIn() {
        Integer google_account = preferenceHandler.getLoginAccountTypePreference(MainActivity.this);

        if (google_account == -1) {
            moveToLoginActivity();
        } else if (google_account == 0) {
            moveToHomeActivity();
        } else if (google_account == 1) {
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
            checkConnectionAndSaveGoogleAccount(googleSignInAccount);
            moveToHomeActivity();
        }
    }

    //Move from SplashScreen to Login Activity
    public void moveToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    //Move from SplashScreen to Home Activity
    public void moveToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // save google account details in database when user login with google account
    public void saveGoogleAccount(GoogleSignInAccount googleSignInAccount) {

        Call<Result> createAccountCallable = HttpClient.getInstance().createAccount(googleSignInAccount.getDisplayName().toLowerCase(),
                googleSignInAccount.getEmail().toLowerCase(), "0", "1",
                googleSignInAccount.getPhotoUrl().toString());

        createAccountCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
            }
        });
    }

    //first check internet connection and then save google account details
    public void checkConnectionAndSaveGoogleAccount(GoogleSignInAccount googleSignInAccount) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(MainActivity.this);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            saveGoogleAccount(googleSignInAccount);
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }
}

