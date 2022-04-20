package com.example.smartcitytravel.Activities.PlaceDetail;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Activities.PlaceDetail.SliderViewAdapter.ImageSliderViewAdapter;
import com.example.smartcitytravel.Activities.PlaceDetail.ViewPager2Adapter.PlaceDetailPagerAdapter;
import com.example.smartcitytravel.DataModel.PlaceDetail;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPlaceDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaceDetailActivity extends AppCompatActivity {
    private ActivityPlaceDetailBinding binding;
    private Util util;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        connection = new Connection();

        setToolbar();
        checkConnectionAndGetPlaceDetail();

    }

    //add toolbar in activity and customize status bar color
    public void setToolbar() {
        util.setStatusBarColor(PlaceDetailActivity.this, R.color.theme_light);
        util.addToolbar(PlaceDetailActivity.this, binding.toolbarLayout.toolbar, "Detail");

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
                        } else {
                            binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.VISIBLE);
                            retryConnection();
                        }
                    }
                });

            }
        });
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
        String placeId = getIntent().getExtras().getString("placeId");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("place")
                .document(placeId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            PlaceDetail placeDetail = documentSnapshot.toObject(PlaceDetail.class);
                            binding.placeName.setText(placeDetail.getName());

                            showImageSliderView(placeDetail);
                            showPlaceDetailTabs(placeDetail);
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
    public void showPlaceDetailTabs(PlaceDetail placeDetail) {
        PlaceDetailPagerAdapter placeDetailPagerAdapter = new PlaceDetailPagerAdapter(this, placeDetail);

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
                        tab.setText("Feedback");
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