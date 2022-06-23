package com.example.smartcitytravel.Activities;

import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.smartcitytravel.DataModel.PlaceDetail;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.SliderViewAdapter.ImageSliderViewAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.ViewPager2Adapter.PlaceDetailPagerAdapter;
import com.example.smartcitytravel.WorkManager.AddToFavoriteWorkManager;
import com.example.smartcitytravel.WorkManager.RemoveFromFavoriteWorkManager;
import com.example.smartcitytravel.databinding.ActivityPlaceDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaceDetailActivity extends AppCompatActivity {
    private ActivityPlaceDetailBinding binding;
    private Util util;
    private Connection connection;
    private FirebaseFirestore db;
    private String userId;
    private PreferenceHandler preferenceHandler;
    private String placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initialize();
        setToolbar();
        checkConnectionAndGetPlaceDetail();
        fillFavoriteIcon();
        blankFavoriteIcon();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        connection = new Connection();
        db = FirebaseFirestore.getInstance();
        preferenceHandler = new PreferenceHandler();
        userId = preferenceHandler.getUserIdPreference(this);
        placeId = getIntent().getExtras().getString("placeId");
    }

    //add toolbar in activity and customize status bar color
    public void setToolbar() {
        util.setStatusBarColor(PlaceDetailActivity.this, R.color.theme_light);
        util.addToolbar(PlaceDetailActivity.this, binding.toolbarLayout.toolbar, "Detail");

    }

    //change favorite icon to filled pink color and add place in favorite
    public void fillFavoriteIcon() {
        binding.favoriteBorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.favoriteBorder.setVisibility(View.GONE);
                binding.favoriteFilled.setVisibility(View.VISIBLE);

                checkConnectionAndAddToFavorite();
            }
        });

    }

    // add place in favorite in database
    public void startAddToFavoriteWorkManager() {
        Data data = new Data.Builder()
                .putString("placeId", placeId)
                .putString("userId", userId)
                .build();

        WorkRequest addToFavoriteWorkRequest = new OneTimeWorkRequest.
                Builder(AddToFavoriteWorkManager.class)
                .setInputData(data)
                .build();

        WorkManager.getInstance(this)
                .enqueue(addToFavoriteWorkRequest);
    }

    //check connection exist or not. If exist then add place to favorite
    public void checkConnectionAndAddToFavorite() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean isInternetAvailable = connection.isConnectionSourceAndInternetAvailable(PlaceDetailActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInternetAvailable) {
                            startAddToFavoriteWorkManager();
                        } else {
                            Toast.makeText(PlaceDetailActivity.this, "Unable to add in favorites", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        executor.shutdown();
    }

    //change favorite icon to blank icon and remove place from favorite
    public void blankFavoriteIcon() {
        binding.favoriteFilled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.favoriteBorder.setVisibility(View.VISIBLE);
                binding.favoriteFilled.setVisibility(View.GONE);

                checkConnectionAndRemoveFromFavorite();
            }
        });
    }

    // remove favorite place from database
    public void startRemoveFromFavoriteWorkManager() {
        Data data = new Data.Builder()
                .putString("placeId", placeId)
                .putString("userId", userId)
                .build();

        WorkRequest removeFromFavoriteWorkRequest = new OneTimeWorkRequest.
                Builder(RemoveFromFavoriteWorkManager.class)
                .setInputData(data)
                .build();

        WorkManager.getInstance(this)
                .enqueue(removeFromFavoriteWorkRequest);


    }

    //check connection exist or not. If exist then remove place from favorites
    public void checkConnectionAndRemoveFromFavorite() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean isInternetAvailable = connection.isConnectionSourceAndInternetAvailable(PlaceDetailActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInternetAvailable) {
                            startRemoveFromFavoriteWorkManager();
                        } else {
                            Toast.makeText(PlaceDetailActivity.this, "Unable to remove from favorites", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        executor.shutdown();
    }

    //check from database whether this place is in favorite place list of user and change favorite icon based on that
    public void isPlaceFavorite() {
        db.collection("favorite")
                .whereEqualTo("userId", userId)
                .whereEqualTo("placeId", placeId)
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            binding.favoriteBorder.setVisibility(View.GONE);
                            binding.favoriteFilled.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    //check internet connection exist or not. If exist get place details
    public void checkConnectionAndGetPlaceDetail() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(PlaceDetailActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            getPlaceDetail();
                            isPlaceFavorite();
                        } else {
                            binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.VISIBLE);
                            retryConnection();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    //run when user click on retry icon
    public void retryConnection() {
        binding.CheckConnectionLayout.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.CheckConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
                binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndGetPlaceDetail();
            }
        });

    }

    // get detail of place from database
    public void getPlaceDetail() {


        db.collection("place")
                .document(placeId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            PlaceDetail placeDetail = documentSnapshot.toObject(PlaceDetail.class);
                            placeDetail.setPlaceId(documentSnapshot.getId());

                            binding.placeName.setText(placeDetail.getName());
                            binding.cityTxt.setText(placeDetail.getCity());

                            showImageSliderView(placeDetail);
                            createPlaceDetailTabs(placeDetail);
                            binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.UILayout.setVisibility(View.VISIBLE);

                        }
                    }
                });


    }

    //create and show place images slider
    //change images after few seconds
    public void showImageSliderView(PlaceDetail placeDetail) {

        ArrayList<String> imageList = new ArrayList<>();
        imageList.add(placeDetail.getImage1());
        imageList.add(placeDetail.getImage2());
        imageList.add(placeDetail.getImage3());

        ImageSliderViewAdapter imageSliderViewAdapter = new ImageSliderViewAdapter(this, imageList);
        binding.imageSliderView.setSliderAdapter(imageSliderViewAdapter);
        binding.imageSliderView.setAutoCycle(true);
        binding.imageSliderView.startAutoCycle();

    }

    //create viewpager2 and tab layout
    //show tabs which show place detail
    public void createPlaceDetailTabs(PlaceDetail placeDetail) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        PlaceDetailPagerAdapter placeDetailPagerAdapter = new PlaceDetailPagerAdapter(this, placeDetail, locationManager);
        binding.placeDetailViewPager2.setAdapter(placeDetailPagerAdapter);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.tabLayout, binding.placeDetailViewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Description");
                        break;
                    case 1:
                        tab.setText("Navigation");
                        break;
                    case 2:
                        tab.setText("Review");
                        break;
                }
            }
        });
        tabLayoutMediator.attach();
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