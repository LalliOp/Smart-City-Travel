package com.example.smartcitytravel.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.Activities.WriteReview.WriteReviewActivity;
import com.example.smartcitytravel.DataModel.PlaceDetail;
import com.example.smartcitytravel.DataModel.Review;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.ReviewRecyclerViewAdapter;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.FragmentReviewBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ReviewFragment extends Fragment {
    private FragmentReviewBinding binding;
    private PlaceDetail placeDetail;
    private Util util;
    private FirebaseFirestore db;
    private PreferenceHandler preferenceHandler;
    private User user;

    public ReviewFragment(PlaceDetail placeDetail) {
        this.placeDetail = placeDetail;
        util = new Util();
        preferenceHandler = new PreferenceHandler();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = preferenceHandler.getLoggedInAccountPreference(requireActivity());

        loadReview();
        setRating();


    }

    // create and show review recycler view
    public void createReviewRecyclerView(ArrayList<Review> reviewList) {
        ReviewRecyclerViewAdapter reviewRecyclerViewAdapter = new ReviewRecyclerViewAdapter(requireActivity(), reviewList);

        binding.reviewRecyclerView.setAdapter(reviewRecyclerViewAdapter);
        binding.reviewRecyclerView.setHasFixedSize(true);
        binding.reviewRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    // set rating
    public void setRating() {
        binding.avgRatingTxt.setText(placeDetail.getRating().toString());
        binding.avgRatingBar.setRating(placeDetail.getRating());

    }

    // move to write review activity to rate and give feedback
    public void writeReview() {
        binding.writeReviewOption.writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(requireActivity(), WriteReviewActivity.class);
                        intent.putExtra("placeId", placeDetail.getPlaceId());
                        requireActivity().startActivity(intent);
                    }
                }, 100);

            }
        });
    }

    // call when user already review or rate place
    public void setReview(Review review) {

        binding.editReviewOption.reviewLayout.nameTxt.setText(util.capitalizedName(user.getName()));
        binding.editReviewOption.reviewLayout.reviewTxt.setText(review.getFeedback());
        binding.editReviewOption.reviewLayout.ratingBar.setRating(review.getRating());

        Glide.with(requireActivity())
                .load(user.getImage_url())
                .timeout(60000)
                .into(binding.editReviewOption.reviewLayout.profileImg);

    }

    // check whether user already review place or not. Update UI based on review existence
    public void loadReview() {
        db.collection("review")
                .whereEqualTo("userId", user.getUserId())
                .whereEqualTo("placeId", placeDetail.getPlaceId())
                .get()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Review review = null;
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                review = querySnapshot.toObject(Review.class);
                                review.setReviewId(querySnapshot.getId());

                                setReview(review);
                            }
                            editReview(review);
                            binding.editReviewOption.editReviewLayout.setVisibility(View.VISIBLE);
                        } else {
                            writeReview();
                            binding.writeReviewOption.writeReviewBtn.setVisibility(View.VISIBLE);
                        }

                        binding.loadingBar.setVisibility(View.GONE);
                        binding.UILayout.setVisibility(View.VISIBLE);
                    }
                });
    }

    //move to write review activity when user click on edit button under review

    public void editReview(Review review) {
        binding.editReviewOption.editReviewTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), WriteReviewActivity.class);
                intent.putExtra("edit", true);
                intent.putExtra("reviewId", review.getReviewId());
                intent.putExtra("review", review.getFeedback());
                intent.putExtra("rating", review.getRating());
                requireActivity().startActivity(intent);
            }
        });
    }

    //make binding null which garbage collector auto collect and remove binding object with end of fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



}