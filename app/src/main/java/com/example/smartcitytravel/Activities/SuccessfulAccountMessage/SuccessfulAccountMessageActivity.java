package com.example.smartcitytravel.Activities.SuccessfulAccountMessage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Activities.Login.LoginActivity;
import com.example.smartcitytravel.databinding.ActivitySuccessfulAccountMessageBinding;

public class SuccessfulAccountMessageActivity extends AppCompatActivity {
    private ActivitySuccessfulAccountMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessfulAccountMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showMessage();
        continueButtonListener();
    }

    //show successful message passed by PinCode or New Password Activity
    public void showMessage() {
        String message = getIntent().getExtras().getString("message");
        binding.successfulMessageTxt.setText(message);
    }

    // when button pressed, move to Login Activity
    public void continueButtonListener() {
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToLoginActivity();
            }
        });
    }

    //move to Login Activity
    public void moveToLoginActivity() {
        String email = getIntent().getExtras().getString("email");

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("email", email);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}