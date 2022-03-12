package com.example.smartcitytravel.LiveChat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.databinding.ActivityLiveChatBinding;

public class LiveChatActivity extends AppCompatActivity {
    private ActivityLiveChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

}