package com.example.smartcitytravel.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Login.LoginActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(getString(R.string.MY_PREFERENCE), MODE_PRIVATE);
        editor = sharedPreferences.edit();

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int login_type = sharedPreferences.getInt(getString(R.string.LOGIN_TYPE_KEY), -1);
                if (login_type == 0) {
                    editor.putString(getString(R.string.LOGIN_KEY), "");
                    editor.putInt(getString(R.string.LOGIN_TYPE_KEY), -1);
                    editor.apply();

                    moveToLoginActivity();
                } else {
                    GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build();

                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(HomeActivity.this, googleSignInOptions);

                    googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                moveToLoginActivity();
                            }

                        }
                    });
                }
            }
        });

    }

    //Move from Home Activity to Login Activity
    public void moveToLoginActivity() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
