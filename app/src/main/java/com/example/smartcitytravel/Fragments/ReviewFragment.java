package com.example.smartcitytravel.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.Activities.SeeAllReview.SeeAllReviewActivity;
import com.example.smartcitytravel.Activities.WriteReview.WriteReviewActivity;
import com.example.smartcitytravel.DataModel.PlaceDetail;
import com.example.smartcitytravel.DataModel.Review;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.RecyclerView.ReviewAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.FragmentReviewBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewFragment extends Fragment {
    private FragmentReviewBinding binding;
    private PlaceDetail placeDetail;
    private Util util;
    private FirebaseFirestore db;
    private PreferenceHandler preferenceHandler;
    private User user;
    private Review review;
    private Connection connection;

    // called by launch() and pass write review activity as intent
    private ActivityResultLauncher<Intent> writeReviewResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == 100) {

                        review.setReviewId(result.getData().getExtras().getString("reviewId"));
                        review.setRating(result.getData().getExtras().getFloat("rating"));
                        review.setFeedback(result.getData().getExtras().getString("review"));


                        double avgRating = result.getData().getExtras().getDouble("avgRating");

                        if (avgRating != -1.0) {
                            binding.avgRatingBar.setRating((float) avgRating);
                            binding.avgRatingTxt.setText(String.valueOf(avgRating));
                        }

                        setReview(review);

                        binding.writeReviewOption.writeReviewBtn.setVisibility(View.GONE);
                        binding.editReviewOption.editReviewLayout.setVisibility(View.VISIBLE);

                        editReview(review);
                    }
                }
            }
    );

    public ReviewFragment(PlaceDetail placeDetail) {
        this.placeDetail = placeDetail;
        util = new Util();
        preferenceHandler = new PreferenceHandler();
        db = FirebaseFirestore.getInstance();
        review = new Review();
        connection = new Connection();
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
        checkConnectionAndLoadReviews();
    }

    // load other people reviews and set recycler view
    public void loadUsersReview() {
        db.collection("review")
                .whereEqualTo("placeId", placeDetail.getPlaceId())
                .whereNotEqualTo("userId", user.getUserId())
                .get()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Review> allReviewList = new ArrayList<>();
                            ArrayList<User> userList = new ArrayList<>();

                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Review review = querySnapshot.toObject(Review.class);

                                if (!review.getFeedback().isEmpty()) {
                                    allReviewList.add(review);
                                }
                            }

                            if (allReviewList.size() == 0) {
                                binding.checkConnectionLayout.loadingBar.setVisibility(View.GONE);
                                binding.UILayout.setVisibility(View.VISIBLE);
                            } else if (allReviewList.size() <= 2) {
                                getUsersInfo(allReviewList);
                            } else {
                                ArrayList<Review> reviewList = new ArrayList<>(allReviewList.subList(0, 2));
                                getUsersInfo(reviewList);
                                binding.seeAllReviewButton.setVisibility(View.VISIBLE);
                                moveToSeeAllReview();
                            }

                        } else {
                            binding.checkConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.UILayout.setVisibility(View.VISIBLE);
                        }


                    }
                });
    }

    // create and show review recycler view
    public void createUserReviewRecyclerView(ArrayList<Review> reviewList, ArrayList<User> userList) {
        ReviewAdapter reviewAdapter = new ReviewAdapter(
                requireActivity(), reviewList, userList, true);

        binding.userReviewRecyclerView.setAdapter(reviewAdapter);
        binding.userReviewRecyclerView.setHasFixedSize(true);
        binding.userReviewRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    // get info of users who review place and then create recyclerview
    public void getUsersInfo(ArrayList<Review> reviewList) {
        ArrayList<User> userList = new ArrayList<>();
        db.collection("user")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (Review review : reviewList) {

                                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                    User user = querySnapshot.toObject(User.class);
                                    user.setUserId(querySnapshot.getId());

                                    if (review.getUserId().equals(user.getUserId())) {
                                        userList.add(user);
                                        break;
                                    }
                                }

                            }
                            createUserReviewRecyclerView(reviewList, userList);
                            binding.checkConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.UILayout.setVisibility(View.VISIBLE);
                        }

                    }
                });
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
                        writeReviewResultLauncher.launch(intent);
                    }
                }, 100);

            }
        });
    }

    // call when user already review or rate place
    // hide feedback textview if review feedback is empty otherwise not
    public void setReview(Review review) {

        if (review.getFeedback().isEmpty()) {
            binding.editReviewOption.reviewLayout.reviewTxt.setVisibility(View.GONE);
        } else {
            binding.editReviewOption.reviewLayout.reviewTxt.setVisibility(View.VISIBLE);
            binding.editReviewOption.reviewLayout.reviewTxt.setText(review.getFeedback());
        }

        limitReviewLength();

        binding.editReviewOption.reviewLayout.nameTxt.setText(util.capitalizedName(user.getName()));
        binding.editReviewOption.reviewLayout.ratingBar.setRating(review.getRating());

        Glide.with(requireActivity())
                .load(user.getImage_url())
                .timeout(60000)
                .into(binding.editReviewOption.reviewLayout.profileImg);

    }

    // limit length of review to 3 lines
    public void limitReviewLength() {
        if (binding.editReviewOption.reviewLayout.reviewTxt.getMaxLines() > 3) {
            binding.editReviewOption.reviewLayout.reviewTxt.setMaxLines(3);
            binding.editReviewOption.reviewLayout.reviewTxt.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    // expand or collapse review when user click on review when review is greater than 3
    public void expandOrCollapseReview() {
        binding.editReviewOption.reviewLayout.reviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.editReviewOption.reviewLayout.reviewTxt.getMaxLines() == 3) {
                    binding.editReviewOption.reviewLayout.reviewTxt.setMaxLines(Integer.MAX_VALUE);
                } else if (binding.editReviewOption.reviewLayout.reviewTxt.getMaxLines() > 3) {
                    binding.editReviewOption.reviewLayout.reviewTxt.setMaxLines(3);
                    binding.editReviewOption.reviewLayout.reviewTxt.setEllipsize(TextUtils.TruncateAt.END);
                }
            }
        });
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
                intent.putExtra("placeId", review.getPlaceId());
                writeReviewResultLauncher.launch(intent);
            }
        });
    }

    //make binding null which garbage collector auto collect and remove binding object with end of fragment
    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    // move to see all review activity to show all reviews
    public void moveToSeeAllReview() {
        binding.seeAllReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), SeeAllReviewActivity.class);
                intent.putExtra("placeId", placeDetail.getPlaceId());
                intent.putExtra("userId", user.getUserId());
                startActivity(intent);
            }
        });

    }


    // check connection exist or not. If exist than load reviews
    public void checkConnectionAndLoadReviews() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(requireActivity());

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            loadReview();
                            setRating();
                            expandOrCollapseReview();
                            loadUsersReview();
                        } else {
                            binding.checkConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.checkConnectionLayout.noConnectionLayout.setVisibility(View.VISIBLE);
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
        binding.checkConnectionLayout.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.checkConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
                binding.checkConnectionLayout.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndLoadReviews();
            }
        });

    }
}