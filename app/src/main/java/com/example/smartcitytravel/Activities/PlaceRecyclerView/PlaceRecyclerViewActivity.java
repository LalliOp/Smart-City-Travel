package com.example.smartcitytravel.Activities.PlaceRecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcitytravel.AWSService.DataModel.PlaceModel.Place;
import com.example.smartcitytravel.AWSService.DataModel.PlaceModel.PlaceResult;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.ItemDecoration.GridSpaceItemDecoration;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.PlaceRecyclerViewAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPlaceRecyclerViewBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceRecyclerViewActivity extends AppCompatActivity {
    private ActivityPlaceRecyclerViewBinding binding;
    private Util util;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceRecyclerViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        connection = new Connection();
        util = new Util();

        setToolbar();
        checkConnectionAndGetPlaces();
        retryConnection();
    }

    //add toolbar in activity and customize status bar color
    public void setToolbar() {
        util.setStatusBarColor(PlaceRecyclerViewActivity.this, R.color.theme_dark);
        util.addToolbar(PlaceRecyclerViewActivity.this, binding.toolbarLayout.toolbar, getIntent().getExtras().getString("destination_name"));

    }

    public void checkConnectionAndGetPlaces() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(PlaceRecyclerViewActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            binding.recyclerViewLayout.setVisibility(View.VISIBLE);

                            getPopularPlaces();
                            getRestaurantPlaces();
                            getFamousSpots();
                            getHotelPlaces();
                        } else {
                            binding.noConnectionLayout.setVisibility(View.VISIBLE);
                        }
                        binding.connectionLoadingBar.setVisibility(View.GONE);
                    }
                });
            }
        });
        executor.shutdown();
    }

    //run when user click on retry icon
    public void retryConnection() {
        binding.retryConnectionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.connectionLoadingBar.setVisibility(View.VISIBLE);
                binding.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndGetPlaces();
            }
        });

    }

    //get popular places from database
    public void getPopularPlaces() {
        Call<PlaceResult> callablePopularPlaceResult = HttpClient.getInstance().getPopularPlaceList();
        callablePopularPlaceResult.enqueue(new Callback<PlaceResult>() {
            @Override
            public void onResponse(Call<PlaceResult> call, Response<PlaceResult> response) {
                if (response.body() != null) {
                    List<Place> popularPlaceList = response.body().getPlaceList();
                    showPopularPlaces((ArrayList<Place>) popularPlaceList);
                    binding.popularLoadingBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(PlaceRecyclerViewActivity.this, "Unable to get places", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<PlaceResult> call, Throwable t) {
                Toast.makeText(PlaceRecyclerViewActivity.this, "Unable to get places", Toast.LENGTH_LONG).show();

            }
        });

    }

    //create recyclerview and show popular places
    public void showPopularPlaces(ArrayList<Place> popularPlaceList) {
        PlaceRecyclerViewAdapter placeRecyclerViewAdapter = new PlaceRecyclerViewAdapter(this, popularPlaceList);

        binding.popularRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.popularRecyclerView.setHasFixedSize(true);
        binding.popularRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.popularRecyclerView.addItemDecoration(new GridSpaceItemDecoration(0, 0, 15, 0));
    }

    public void createRecommendationRecyclerView(PlaceRecyclerViewAdapter placeRecyclerViewAdapter, GridSpaceItemDecoration gridSpaceItemDecoration) {

        binding.recommendationRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.recommendationRecyclerView.setHasFixedSize(true);
        binding.recommendationRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recommendationRecyclerView.addItemDecoration(gridSpaceItemDecoration);
    }

    //get restaurant places from database
    public void getRestaurantPlaces() {
        Call<PlaceResult> callableRestaurantPlaceResult = HttpClient.getInstance().getPlaceList("Restaurant");

        callableRestaurantPlaceResult.enqueue(new Callback<PlaceResult>() {
            @Override
            public void onResponse(Call<PlaceResult> call, Response<PlaceResult> response) {
                if (response.body() != null) {
                    List<Place> restaurantPlaceList = response.body().getPlaceList();
                    showRestaurantPlaces((ArrayList<Place>) restaurantPlaceList);
                    binding.restaurantLoadingBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(PlaceRecyclerViewActivity.this, "Unable to get places", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<PlaceResult> call, Throwable t) {
                Toast.makeText(PlaceRecyclerViewActivity.this, "Unable to get places", Toast.LENGTH_LONG).show();

            }
        });
    }

    //create recyclerview and show restaurant places
    public void showRestaurantPlaces(ArrayList<Place> restaurantPlaceList) {
        PlaceRecyclerViewAdapter placeRecyclerViewAdapter = new PlaceRecyclerViewAdapter(this, restaurantPlaceList);

        binding.restaurantRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.restaurantRecyclerView.setHasFixedSize(true);
        binding.restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.restaurantRecyclerView.addItemDecoration(new GridSpaceItemDecoration(0, 0, 15, 0));
    }

    //get famous spots from database
    public void getFamousSpots() {
        Call<PlaceResult> callableFamousSpotResult = HttpClient.getInstance().getPlaceList("Tourism_spot");

        callableFamousSpotResult.enqueue(new Callback<PlaceResult>() {
            @Override
            public void onResponse(Call<PlaceResult> call, Response<PlaceResult> response) {
                if (response.body() != null) {
                    List<Place> famousSpotList = response.body().getPlaceList();
                    showFamousSpots((ArrayList<Place>) famousSpotList);
                    binding.famousSpotLoadingBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(PlaceRecyclerViewActivity.this, "Unable to get places", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<PlaceResult> call, Throwable t) {
                Toast.makeText(PlaceRecyclerViewActivity.this, "Unable to get places", Toast.LENGTH_LONG).show();

            }
        });
    }

    //create recyclerview and show famous spots
    public void showFamousSpots(ArrayList<Place> famousSpotList) {
        PlaceRecyclerViewAdapter placeRecyclerViewAdapter = new PlaceRecyclerViewAdapter(this, famousSpotList);

        binding.famousSpotRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.famousSpotRecyclerView.setHasFixedSize(true);
        binding.famousSpotRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.famousSpotRecyclerView.addItemDecoration(new GridSpaceItemDecoration(0, 0, 15, 0));
    }

    //get hotel places from database
    public void getHotelPlaces() {
        Call<PlaceResult> callableHotelPlaceResult = HttpClient.getInstance().getPlaceList("Hotel");

        callableHotelPlaceResult.enqueue(new Callback<PlaceResult>() {
            @Override
            public void onResponse(Call<PlaceResult> call, Response<PlaceResult> response) {
                if (response.body() != null) {
                    List<Place> hotelPlaceList = response.body().getPlaceList();
                    showHotelPlaces((ArrayList<Place>) hotelPlaceList);
                    binding.hotelLoadingBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(PlaceRecyclerViewActivity.this, "Unable to get places", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<PlaceResult> call, Throwable t) {
                Toast.makeText(PlaceRecyclerViewActivity.this, "Unable to get places", Toast.LENGTH_LONG).show();

            }
        });
    }

    //create recyclerview and show hotel places
    public void showHotelPlaces(ArrayList<Place> hotelPlaceList) {
        PlaceRecyclerViewAdapter placeRecyclerViewAdapter = new PlaceRecyclerViewAdapter(this, hotelPlaceList);

        binding.hotelRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.hotelRecyclerView.setHasFixedSize(true);
        binding.hotelRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.hotelRecyclerView.addItemDecoration(new GridSpaceItemDecoration(0, 0, 15, 0));
    }
}