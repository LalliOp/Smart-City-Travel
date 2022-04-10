package com.example.smartcitytravel.Activities.PlaceDetail.ViewPager2Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.smartcitytravel.Activities.PlaceDetail.Fragments.DescriptionFragment;
import com.example.smartcitytravel.Activities.PlaceDetail.Fragments.FeedbackFragment;
import com.example.smartcitytravel.Activities.PlaceDetail.Fragments.NavigationFragment;

import io.reactivex.rxjava3.annotations.NonNull;

public class PlaceDetailPagerAdapter extends FragmentStateAdapter {

    public PlaceDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @androidx.annotation.NonNull
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DescriptionFragment();
            case 1:
                return new NavigationFragment();
            case 2:
                return new FeedbackFragment();
            default:
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}