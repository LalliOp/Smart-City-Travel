package com.example.smartcitytravel.ResetPassword;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.databinding.ActivityNewPasswordBinding;

public class NewPasswordActivity extends AppCompatActivity {

    private ActivityNewPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}