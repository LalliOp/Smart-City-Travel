package com.example.smartcitytravel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.Result;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Home.HomeActivity;
import com.example.smartcitytravel.Login.LoginActivity;
import com.example.smartcitytravel.Util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        util = new Util();
        sharedPreferences = getSharedPreferences(getString(R.string.MY_PREFERENCE), MODE_PRIVATE);
        editor = sharedPreferences.edit();
        firstTimeRunSetup();
        checkUserAlreadySignIn();
    }

    //This run only one time when app run very first time after installation
    public void firstTimeRunSetup() {
        final String FIRST_RUN_KEY = "first_run";
        boolean firstRun = sharedPreferences.getBoolean(FIRST_RUN_KEY, true);
        if (firstRun) {

            Toast.makeText(this, "FIRST TIME RUN", Toast.LENGTH_SHORT).show();
            editor.putBoolean(FIRST_RUN_KEY, false);
            editor.apply();
        }

    }

    //Check whether user is already sign in
    public void checkUserAlreadySignIn() {
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        int login_type = sharedPreferences.getInt(getString(R.string.LOGIN_TYPE_KEY), -1);

        if (googleSignInAccount != null) {
            checkConnectionAndSaveGoogleAccount(googleSignInAccount);
            moveToHomeActivity();
        } else if (login_type == 0) {
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
                    editor.putString(getString(R.string.LOGIN_KEY), "");
                    editor.putInt(getString(R.string.LOGIN_TYPE_KEY), 1);
                    editor.apply();
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
                Boolean connectionAvailable = util.isConnectionAvailable(MainActivity.this);

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

