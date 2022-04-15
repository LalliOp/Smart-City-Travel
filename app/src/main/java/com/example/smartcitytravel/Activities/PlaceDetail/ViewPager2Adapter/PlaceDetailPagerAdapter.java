package com.example.smartcitytravel.Activities.PlaceDetail.ViewPager2Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.smartcitytravel.AWSService.DataModel.PlaceModel.Place;
import com.example.smartcitytravel.Activities.PlaceDetail.Fragments.DescriptionFragment;
import com.example.smartcitytravel.Activities.PlaceDetail.Fragments.FeedbackFragment;
import com.example.smartcitytravel.Activities.PlaceDetail.Fragments.NavigationFragment;

import io.reactivex.rxjava3.annotations.NonNull;

public class PlaceDetailPagerAdapter extends FragmentStateAdapter {
    private Place place;

    public PlaceDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity, Place place) {
        super(fragmentActivity);
        this.place = place;
    }

    @androidx.annotation.NonNull
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DescriptionFragment(place);
            case 1:
                return new NavigationFragment(place);
            case 2:
                return new FeedbackFragment(place);
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}