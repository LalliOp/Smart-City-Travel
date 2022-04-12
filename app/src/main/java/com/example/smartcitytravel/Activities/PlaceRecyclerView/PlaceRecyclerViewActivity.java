package com.example.smartcitytravel.Activities.PlaceRecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcitytravel.AWSService.DataModel.PlaceModel.Place;
import com.example.smartcitytravel.AWSService.DataModel.PlaceModel.PlaceResult;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Activities.PlaceRecyclerView.ItemDecoration.GridSpaceItemDecoration;
import com.example.smartcitytravel.Activities.PlaceRecyclerView.RecyclerView.PlaceRecyclerViewAdapter;
import com.example.smartcitytravel.R;
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
    private Toast noConnectionToast;
    private boolean popularPlacesAvailable;
    private boolean restaurantPlacesAvailable;
    private boolean famousSpotsAvailable;
    private boolean hotelPlacesAvailable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceRecyclerViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        getParentActivityIntent();
        setToolbar();
        checkConnectionAndGetPlaces();
    }

    //initialize variables
    public void initialize() {
        connection = new Connection();
        util = new Util();
        noConnectionToast = new Toast(this);

        popularPlacesAvailable = false;
        restaurantPlacesAvailable = false;
        famousSpotsAvailable = false;
        hotelPlacesAvailable = false;
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
                            binding.popularLayout.setVisibility(View.VISIBLE);
                            binding.restaurantLayout.setVisibility(View.VISIBLE);
                            binding.famousSpotLayout.setVisibility(View.VISIBLE);
                            binding.hotelLayout.setVisibility(View.VISIBLE);

                            getPopularPlaces();
                            getRestaurantPlaces();
                            getFamousSpots();
                            getHotelPlaces();
                        } else {
                            binding.noConnectionLayout.setVisibility(View.VISIBLE);
                            retryConnection();
                        }
                        binding.loadingBar.setVisibility(View.GONE);

                    }
                });
            }
        });
        executor.shutdown();
    }

    //run when user click on retry icon
    public void retryConnection() {
        binding.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.loadingBar.setVisibility(View.VISIBLE);
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

                    popularPlacesAvailable = true;
                } else {
                    displayNoConnectionMessage();
                    retryPopularListener();
                    popularPlacesAvailable = false;
                }
                binding.popularLoadingBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<PlaceResult> call, Throwable t) {
                displayNoConnectionMessage();
                retryPopularListener();
                binding.popularLoadingBar.setVisibility(View.GONE);
                popularPlacesAvailable = false;

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

    //retry to get all places which is unable to get when click on popular retry button
    public void retryPopularListener() {
        binding.popularNoConnectionLayout.retryConnectionImg.setVisibility(View.VISIBLE);
        binding.popularNoConnectionLayout.retryConnectionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryGetPlaces();
            }
        });
    }

    public void createRecommendationRecyclerView(PlaceRecyclerViewAdapter
                                                         placeRecyclerViewAdapter, GridSpaceItemDecoration gridSpaceItemDecoration) {

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
                    restaurantPlacesAvailable = true;
                } else {
                    displayNoConnectionMessage();
                    retryRestaurantListener();
                    restaurantPlacesAvailable = false;

                }
                binding.restaurantLoadingBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<PlaceResult> call, Throwable t) {
                binding.restaurantLoadingBar.setVisibility(View.GONE);
                restaurantPlacesAvailable = false;
                retryRestaurantListener();
                displayNoConnectionMessage();
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

    //retry to get all places which is unable to get when click on restaurant retry button
    public void retryRestaurantListener() {
        binding.restaurantNoConnectionLayout.retryConnectionImg.setVisibility(View.VISIBLE);
        binding.restaurantNoConnectionLayout.retryConnectionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryGetPlaces();

            }
        });
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
                    famousSpotsAvailable = true;
                } else {
                    displayNoConnectionMessage();
                    retryFamousSpotListener();
                    famousSpotsAvailable = false;
                }
                binding.famousSpotLoadingBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<PlaceResult> call, Throwable t) {
                binding.famousSpotLoadingBar.setVisibility(View.GONE);
                famousSpotsAvailable = false;
                retryFamousSpotListener();
                displayNoConnectionMessage();
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

    //retry to get all places which is unable to get when click on famous spots retry button
    public void retryFamousSpotListener() {
        binding.famousSpotNoConnectionLayout.retryConnectionImg.setVisibility(View.VISIBLE);
        binding.famousSpotNoConnectionLayout.retryConnectionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryGetPlaces();

            }
        });
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
                    hotelPlacesAvailable = true;
                } else {
                    displayNoConnectionMessage();
                    retryHotelListener();
                    hotelPlacesAvailable = false;
                }
                binding.hotelLoadingBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(Call<PlaceResult> call, Throwable t) {
                displayNoConnectionMessage();
                retryHotelListener();
                binding.hotelLoadingBar.setVisibility(View.GONE);
                hotelPlacesAvailable = false;
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

    //retry to get all places which is unable to get when click on hotel retry button
    public void retryHotelListener() {
        binding.hotelNoConnectionLayout.retryConnectionImg.setVisibility(View.VISIBLE);
        binding.hotelNoConnectionLayout.retryConnectionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryGetPlaces();
            }
        });
    }

    // retry to get places
    public void retryGetPlaces() {
        if (!popularPlacesAvailable) {
            binding.popularLoadingBar.setVisibility(View.VISIBLE);
            binding.popularNoConnectionLayout.retryConnectionImg.setVisibility(View.GONE);
            getPopularPlaces();
        }
        if (!restaurantPlacesAvailable) {
            binding.restaurantLoadingBar.setVisibility(View.VISIBLE);
            binding.restaurantNoConnectionLayout.retryConnectionImg.setVisibility(View.GONE);
            getRestaurantPlaces();
        }
        if (!famousSpotsAvailable) {
            binding.famousSpotLoadingBar.setVisibility(View.VISIBLE);
            binding.famousSpotNoConnectionLayout.retryConnectionImg.setVisibility(View.GONE);
            getFamousSpots();
        }
        if (!hotelPlacesAvailable) {
            binding.hotelLoadingBar.setVisibility(View.VISIBLE);
            binding.hotelNoConnectionLayout.retryConnectionImg.setVisibility(View.GONE);
            getHotelPlaces();

        }

    }

    // show only one no connection msg when multiple connection failed at a same time to get places
    public void displayNoConnectionMessage() {
        try {
            noConnectionToast.cancel();
            noConnectionToast.getView().isShown();
        } catch (Exception ignored) {
            noConnectionToast = Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT);
            noConnectionToast.show();
        }
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