package com.example.smartcitytravel.Activities.PlaceDetail;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.Activities.PlaceDetail.SliderViewAdapter.ImageSliderViewAdapter;
import com.example.smartcitytravel.Activities.PlaceDetail.ViewPager2Adapter.PlaceDetailPagerAdapter;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityPlaceDetailBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import io.reactivex.rxjava3.annotations.NonNull;

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
        showImageSliderView();
        showPlaceDetails();

    }

    //add toolbar in activity and customize status bar color
    public void setToolbar() {
        util.setStatusBarColor(PlaceDetailActivity.this, R.color.theme_dark);
        util.addToolbar(PlaceDetailActivity.this, binding.toolbarLayout.toolbar, "Detail");

    }

    //create and show place images slider
    //change images after few seconds
    public void showImageSliderView() {
        binding.placeName.setText("Place Name");
        binding.placeRating.setRating(3.5F);

        ArrayList<String> imageList = new ArrayList<>();
        String url1 = "https://www.geeksforgeeks.org/wp-content/uploads/gfg_200X200-1.png";
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
    public void showPlaceDetails() {
        PlaceDetailPagerAdapter placeDetailPagerAdapter = new PlaceDetailPagerAdapter(this);

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