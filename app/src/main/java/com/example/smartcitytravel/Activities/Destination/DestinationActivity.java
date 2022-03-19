package com.example.smartcitytravel.Activities.Destination;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityDestinationBinding;

public class DestinationActivity extends AppCompatActivity {
    private ActivityDestinationBinding binding;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDestinationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util=new Util();

        util.setStatusBarColor(this, R.color.theme_dark);
        addToolbar();
    }

    //customize and add toolbar
    void addToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar);
        getSupportActionBar().setTitle("Destination");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

}