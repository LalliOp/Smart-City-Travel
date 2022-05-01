package com.example.smartcitytravel.ViewPager2Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.smartcitytravel.Fragments.DescriptionFragment;
import com.example.smartcitytravel.Fragments.NavigationFragment;
import com.example.smartcitytravel.Fragments.ReviewFragment;
import com.example.smartcitytravel.DataModel.PlaceDetail;


public class PlaceDetailPagerAdapter extends FragmentStateAdapter {
    private final PlaceDetail placeDetail;

    public PlaceDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity, PlaceDetail placeDetail) {
        super(fragmentActivity);
        this.placeDetail = placeDetail;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DescriptionFragment(placeDetail);
            case 1:
                return new NavigationFragment(placeDetail);
            case 2:
                return new ReviewFragment(placeDetail);
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}