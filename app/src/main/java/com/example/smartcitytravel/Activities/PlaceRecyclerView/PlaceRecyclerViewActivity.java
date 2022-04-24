package com.example.smartcitytravel.Activities.PlaceRecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcitytravel.Activities.ItemDecoration.GridSpaceItemDecoration;
import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.PlaceRecyclerViewAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPlaceRecyclerViewBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaceRecyclerViewActivity extends AppCompatActivity {
    private ActivityPlaceRecyclerViewBinding binding;
    private Util util;
    private Connection connection;
    private Toast noConnectionToast;
    private boolean popularPlacesAvailable;
    private boolean restaurantPlacesAvailable;
    private boolean famousSpotsAvailable;
    private boolean hotelPlacesAvailable;
    private CollectionReference placeCollection;
    private String city;


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
        placeCollection = FirebaseFirestore.getInstance().collection("place");
        city = getIntent().getExtras().getString("destination_name");

        popularPlacesAvailable = false;
        restaurantPlacesAvailable = false;
        famousSpotsAvailable = false;
        hotelPlacesAvailable = false;
    }

    //add toolbar in activity and customize status bar color
    public void setToolbar() {
        util.setStatusBarColor(PlaceRecyclerViewActivity.this, R.color.theme_light);
        util.addToolbar(PlaceRecyclerViewActivity.this, binding.toolbarLayout.toolbar, city);
    }

    //check internet connection exist or not. If exist load places
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
                            binding.checkConnectionLayout.noConnectionLayout.setVisibility(View.VISIBLE);
                            retryConnection();
                        }
                        binding.checkConnectionLayout.loadingBar.setVisibility(View.GONE);

                    }
                });
            }
        });
        executor.shutdown();
    }

    //run when user click on retry icon
    public void retryConnection() {
        binding.checkConnectionLayout.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.checkConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
                binding.checkConnectionLayout.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndGetPlaces();
            }
        });

    }

    //get popular places from database and pass to recycler view
    public void getPopularPlaces() {
        placeCollection.whereEqualTo("City", city)
                .orderBy("Rating", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Place> popularPlaceList = new ArrayList<>();

                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Place place = querySnapshot.toObject(Place.class);
                                place.setPlaceId(querySnapshot.getId());

                                popularPlaceList.add(place);

                            }

//                            Collections.shuffle(popularPlaceList);
//
//                            ArrayList<Place> placeList = new ArrayList<>(popularPlaceList.subList(0, popularPlaceList.size() - 1));

                            showPopularPlaces(popularPlaceList);

                            popularPlacesAvailable = true;

                        } else {
                            displayNoConnectionMessage();
                            retryPopularListener();
                            popularPlacesAvailable = false;
                        }
                        binding.popularLoadingBar.setVisibility(View.GONE);
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

    //get restaurant places from database and pass to recycler view
    public void getRestaurantPlaces() {
        placeCollection.whereEqualTo("City", city)
                .whereEqualTo("Place_type", "Restaurant")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Place> restaurantPlaceList = new ArrayList<>();

                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Place place = querySnapshot.toObject(Place.class);
                                place.setPlaceId(querySnapshot.getId());

                                restaurantPlaceList.add(place);

                            }

//                            Collections.shuffle(restaurantPlaceList);
//
//                            ArrayList<Place> placeList = new ArrayList<>(restaurantPlaceList.subList(0, restaurantPlaceList.size() - 1));

                            showRestaurantPlaces(restaurantPlaceList);

                            restaurantPlacesAvailable = true;

                        } else {
                            displayNoConnectionMessage();
                            retryRestaurantListener();
                            restaurantPlacesAvailable = false;
                        }
                        binding.restaurantLoadingBar.setVisibility(View.GONE);
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
        placeCollection.whereEqualTo("City", city)
                .whereEqualTo("Place_type", "Tourism_spot")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Place> famousSpotList = new ArrayList<>();

                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Place place = querySnapshot.toObject(Place.class);
                                place.setPlaceId(querySnapshot.getId());

                                famousSpotList.add(place);
                            }

//                            Collections.shuffle(famousSpotList);
//
//                            ArrayList<Place> placeList = new ArrayList<>(famousSpotList.subList(0, famousSpotList.size() - 1));

                            showFamousSpots(famousSpotList);

                            famousSpotsAvailable = true;

                        } else {
                            displayNoConnectionMessage();
                            retryFamousSpotListener();
                            famousSpotsAvailable = false;
                        }
                        binding.famousSpotLoadingBar.setVisibility(View.GONE);
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

    //get hotel places from database and pass to recycler view
    public void getHotelPlaces() {
        placeCollection.whereEqualTo("City", city)
                .whereEqualTo("Place_type", "Hotel")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Place> hotelPlaceList = new ArrayList<>();

                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Place place = querySnapshot.toObject(Place.class);
                                place.setPlaceId(querySnapshot.getId());

                                hotelPlaceList.add(place);

                            }

//                            Collections.shuffle(hotelPlaceList);
//
//                            ArrayList<Place> placeList = new ArrayList<>(hotelPlaceList.subList(0, hotelPlaceList.size() - 1));

                            showHotelPlaces(hotelPlaceList);

                            hotelPlacesAvailable = true;

                        } else {
                            displayNoConnectionMessage();
                            retryHotelListener();
                            hotelPlacesAvailable = false;
                        }
                        binding.hotelLoadingBar.setVisibility(View.GONE);
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