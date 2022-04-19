package com.example.smartcitytravel.Activities.PlaceDetail.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.smartcitytravel.AWSService.DataModel.Place;
import com.example.smartcitytravel.databinding.FragmentNavigationBinding;

public class NavigationFragment extends Fragment {
    private FragmentNavigationBinding binding;
    private Place place;

    public NavigationFragment(Place place) {
        this.place = place;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNavigationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    //make binding null which garbage collector auto collect and remove binding object with end of fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}