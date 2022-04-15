package com.example.smartcitytravel.Activities.PlaceDetail;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.AWSService.DataModel.PlaceModel.Place;
import com.example.smartcitytravel.AWSService.Http.HttpClient;
import com.example.smartcitytravel.Activities.PlaceDetail.SliderViewAdapter.ImageSliderViewAdapter;
import com.example.smartcitytravel.Activities.PlaceDetail.ViewPager2Adapter.PlaceDetailPagerAdapter;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPlaceDetailBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import io.reactivex.rxjava3.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceDetailActivity extends AppCompatActivity {
    private ActivityPlaceDetailBinding binding;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();

        setToolbar();
        getPlaceDetail();

    }

    //add toolbar in activity and customize status bar color
    public void setToolbar() {
        util.setStatusBarColor(PlaceDetailActivity.this, R.color.theme_dark);
        util.addToolbar(PlaceDetailActivity.this, binding.toolbarLayout.toolbar, "Detail");

    }

    // get detail of place from database
    public void getPlaceDetail() {
        int placeId = getIntent().getExtras().getInt("placeId");

        Call<Place> callablePlaceDetail = HttpClient.getInstance().getPlaceDetail(String.valueOf(placeId));

        callablePlaceDetail.enqueue(new Callback<Place>() {
            @Override
            public void onResponse(Call<Place> call, Response<Place> response) {
                Place place = response.body();
                if (place != null) {
                    binding.placeName.setText(place.getName());

                    showImageSliderView(place.getImageUrl());
                    showPlaceDetailTabs(place);

                } else {
                    Toast.makeText(PlaceDetailActivity.this, "Unable to get place details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Place> call, Throwable t) {
                Toast.makeText(PlaceDetailActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //create and show place images slider
    //change images after few seconds
    public void showImageSliderView(String imageUrl) {

        ArrayList<String> imageList = new ArrayList<>();
        String url1 = imageUrl;
        String url2 = "https://qphs.fs.quoracdn.net/main-qimg-8e203d34a6a56345f86f1a92570557ba.webp";
        String url3 = "https://bizzbucket.co/wp-content/uploads/2020/08/Life-in-The-Metro-Blog-Title-22.png";
        imageList.add(url1);
        imageList.add(url2);
        imageList.add(url3);

        ImageSliderViewAdapter imageSliderViewAdapter = new ImageSliderViewAdapter(this, imageList);
        binding.imageSliderView.setSliderAdapter(imageSliderViewAdapter);
        binding.imageSliderView.setAutoCycle(true);
        binding.imageSliderView.startAutoCycle();
    }

    //create viewpager2 and tab layout
    //show tabs which show place detail
    public void showPlaceDetailTabs(Place place) {
        PlaceDetailPagerAdapter placeDetailPagerAdapter = new PlaceDetailPagerAdapter(this, place);

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