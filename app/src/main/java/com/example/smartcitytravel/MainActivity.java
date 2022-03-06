package com.example.smartcitytravel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Home.HomeActivity;
import com.example.smartcitytravel.Login.LoginActivity;
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
    private Util util;
    private Connection connection;
    private PreferenceHandler preferenceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        util = new Util();
        connection = new Connection();
        preferenceHandler = new PreferenceHandler();

        checkUserAlreadySignIn();
    }

    //Check whether user is already sign in
    public void checkUserAlreadySignIn() {
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (googleSignInAccount != null) {
            checkConnectionAndSaveGoogleAccount(googleSignInAccount);
            moveToHomeActivity();
        } else if (!preferenceHandler.getLoginEmailPreference(this).isEmpty()) {
            moveToHomeActivity();
        } else {
            moveToLoginActivity();
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
                googleSignInAccount.getEmail().toLowerCase(), "0", "1");

        createAccountCallable.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (response.body().getAccount_status() == 0) {
                    preferenceHandler.setLoginEmailPreference("", MainActivity.this);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Unable to save google account details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //first check internet connection and then save google account details
    public void checkConnectionAndSaveGoogleAccount(GoogleSignInAccount googleSignInAccount) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean connectionAvailable = connection.isConnectionAvailable(MainActivity.this);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (connectionAvailable) {
                            saveGoogleAccount(googleSignInAccount);
                        } else {
                            Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }
}

