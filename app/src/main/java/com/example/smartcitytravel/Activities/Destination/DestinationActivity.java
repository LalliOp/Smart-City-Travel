package com.example.smartcitytravel.Activities.Destination;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Activities.PlaceList.PlaceListActivity;
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

        util = new Util();

        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, "Destination");

        showLahoreDestination();
        showIslamabadDestination();
    }

    // move to place activity which show lahore famous places
    public void showLahoreDestination() {
        binding.lahoreCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPlaceRecyclerViewActivity("Lahore");
            }
        });
    }

    //move to place activity which show islamabad famous places
    public void showIslamabadDestination() {
        binding.islamabadCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPlaceRecyclerViewActivity("Islamabad");
            }
        });
    }

    //move to place activity which show different places of selected city
    public void moveToPlaceRecyclerViewActivity(String destination_name) {
        Intent intent = new Intent(this, PlaceListActivity.class);
        intent.putExtra("destination_name", destination_name);
        startActivity(intent);
    }

    // return to previous activity when user click on up button (which is back button on top life side)
    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

}