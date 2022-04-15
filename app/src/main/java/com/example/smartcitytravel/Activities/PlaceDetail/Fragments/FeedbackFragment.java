package com.example.smartcitytravel.Activities.PlaceDetail.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcitytravel.AWSService.DataModel.PlaceModel.Place;
import com.example.smartcitytravel.Activities.PlaceDetail.Fragments.RecyclerView.FeedbackRecyclerViewAdapter;
import com.example.smartcitytravel.databinding.FragmentFeedbackBinding;

public class FeedbackFragment extends Fragment {
    private FragmentFeedbackBinding binding;
    private Place place;

    public FeedbackFragment(Place place) {
        this.place = place;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFeedbackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.avgRatingTxt.setText(place.getRating().toString());
        binding.avgRatingBar.setRating(place.getRating());

//        FeedbackRecyclerViewAdapter placeRecyclerViewAdapter = new FeedbackRecyclerViewAdapter(this, hotelPlaceList);
//
//        binding.feedbackRecyclerView.setAdapter(placeRecyclerViewAdapter);
//        binding.feedbackRecyclerView.setHasFixedSize(true);
//        binding.feedbackRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

    }

    //make binding null which garbage collector auto collect and remove binding object with end of fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}