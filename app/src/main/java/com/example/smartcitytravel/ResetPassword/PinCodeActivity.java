package com.example.smartcitytravel.ResetPassword;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPinCodeBinding;

public class PinCodeActivity extends AppCompatActivity {
    private ActivityPinCodeBinding binding;
    private Util util;
    private int pin_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        getPinCode();
    }

    // get pin code which is passed by Email Activity
    public void getPinCode() {
        if (getIntent().getExtras() != null) {
            pin_code = getIntent().getExtras().getInt("pin_code");
        }
    }
}