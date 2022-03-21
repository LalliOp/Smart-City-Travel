package com.example.smartcitytravel.Activities.PlaceRecyclerView;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcitytravel.ItemDecoration.GridSpaceItemDecoration;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.PlaceRecyclerViewAdapter;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPlaceRecyclerViewBinding;

import java.util.ArrayList;

public class PlaceRecyclerViewActivity extends AppCompatActivity {
    private ActivityPlaceRecyclerViewBinding binding;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceRecyclerViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        util.setStatusBarColor(PlaceRecyclerViewActivity.this, R.color.theme_dark);
        util.addToolbar(PlaceRecyclerViewActivity.this, binding.toolbarLayout.toolbar, getIntent().getExtras().getString("destination_name"));

        ArrayList<Place> placeArrayList = new ArrayList<>();
        placeArrayList.add(new Place("Avengers", R.drawable.home_background));
        placeArrayList.add(new Place("Venom", R.drawable.home_background));
        placeArrayList.add(new Place("Batman Begins", R.drawable.home_background));
        placeArrayList.add(new Place("Jumanji", R.drawable.home_background));
        placeArrayList.add(new Place("Good Deeds", R.drawable.home_background));
        placeArrayList.add(new Place("Hulk", R.drawable.home_background));
        placeArrayList.add(new Place("Avatar", R.drawable.home_background));

        PlaceRecyclerViewAdapter placeRecyclerViewAdapter = new PlaceRecyclerViewAdapter(this, placeArrayList);
        GridSpaceItemDecoration gridSpaceItemDecoration = new GridSpaceItemDecoration(0, 0, 15, 0);

        createPopularRecyclerView(placeRecyclerViewAdapter, gridSpaceItemDecoration);
        createRecommendationRecyclerView(placeRecyclerViewAdapter, gridSpaceItemDecoration);
        createRestaurantRecyclerView(placeRecyclerViewAdapter, gridSpaceItemDecoration);
        createPlaceRecyclerView(placeRecyclerViewAdapter, gridSpaceItemDecoration);
        createHotelRecyclerView(placeRecyclerViewAdapter, gridSpaceItemDecoration);
    }

    public void createPopularRecyclerView(PlaceRecyclerViewAdapter placeRecyclerViewAdapter, GridSpaceItemDecoration gridSpaceItemDecoration) {

        binding.popularRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.popularRecyclerView.setHasFixedSize(true);
        binding.popularRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.popularRecyclerView.addItemDecoration(gridSpaceItemDecoration);
    }

    public void createRecommendationRecyclerView(PlaceRecyclerViewAdapter placeRecyclerViewAdapter, GridSpaceItemDecoration gridSpaceItemDecoration) {

        binding.recommendationRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.recommendationRecyclerView.setHasFixedSize(true);
        binding.recommendationRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recommendationRecyclerView.addItemDecoration(gridSpaceItemDecoration);
    }

    public void createRestaurantRecyclerView(PlaceRecyclerViewAdapter placeRecyclerViewAdapter, GridSpaceItemDecoration gridSpaceItemDecoration) {

        binding.restaurantRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.restaurantRecyclerView.setHasFixedSize(true);
        binding.restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.restaurantRecyclerView.addItemDecoration(gridSpaceItemDecoration);
    }

    public void createPlaceRecyclerView(PlaceRecyclerViewAdapter placeRecyclerViewAdapter, GridSpaceItemDecoration gridSpaceItemDecoration) {

        binding.placeRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.placeRecyclerView.setHasFixedSize(true);
        binding.placeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.placeRecyclerView.addItemDecoration(gridSpaceItemDecoration);
    }

    public void createHotelRecyclerView(PlaceRecyclerViewAdapter placeRecyclerViewAdapter, GridSpaceItemDecoration gridSpaceItemDecoration) {

        binding.hotelRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.hotelRecyclerView.setHasFixedSize(true);
        binding.hotelRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.hotelRecyclerView.addItemDecoration(gridSpaceItemDecoration);
    }
}