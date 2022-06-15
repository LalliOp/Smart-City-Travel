package com.example.smartcitytravel.Activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcitytravel.DataModel.Review;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.ReviewAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivitySeeAllReviewBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeeAllReviewActivity extends AppCompatActivity {
    private ActivitySeeAllReviewBinding binding;
    private Util util;
    private FirebaseFirestore db;
    private Connection connection;
    private boolean loading;
    private String placeId;
    private String userId;
    private DocumentSnapshot lastLoadedReview;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeeAllReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setToolBarTheme();
        checkConnectionAndLoadUsersReview();

    }

    //initialize variables
    public void initialize() {
        util = new Util();
        db = FirebaseFirestore.getInstance();
        connection = new Connection();
        placeId = getIntent().getExtras().getString("placeId");
        userId = getIntent().getExtras().getString("userId");
        lastLoadedReview = null;
        loading = false;
    }

    // style and customize toolbar and theme
    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, "All Reviews");
    }

    //check connection exist or not. If exist then load reviews
    public void checkConnectionAndLoadUsersReview() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(SeeAllReviewActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            startLoadUsersReview();
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

    // load other people reviews and set recycler view
    public void startLoadUsersReview() {
        ArrayList<Review> reviewList = new ArrayList<>();

        db.collection("review")
                .whereEqualTo("placeId", placeId)
                .whereNotEqualTo("userId", userId)
                .orderBy("userId")
                .limit(15)
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            lastLoadedReview = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Review review = querySnapshot.toObject(Review.class);

                                if (!review.getFeedback().isEmpty()) {
                                    reviewList.add(review);
                                }
                            }
                            startLoadUsersInfo(reviewList);


                        }

                    }
                });
    }

    // get info of users who review place then create recyclerview
    public void startLoadUsersInfo(ArrayList<Review> reviewList) {
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

                            createReviewRecyclerView(reviewList, userList);
                            binding.checkConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.seeAllReviewRecyclerView.setVisibility(View.VISIBLE);
                        }

                    }
                });
    }

    // create and show review recycler view
    public void createReviewRecyclerView(ArrayList<Review> reviewList, ArrayList<User> userList) {
        reviewAdapter = new ReviewAdapter(
                this, reviewList, userList, false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        binding.seeAllReviewRecyclerView.setAdapter(reviewAdapter);
        binding.seeAllReviewRecyclerView.setLayoutManager(linearLayoutManager);
        binding.seeAllReviewRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (linearLayoutManager.findLastCompletelyVisibleItemPosition()
                        == reviewAdapter.getItemCount() - 1 && !loading) {
                    loading = true;
                    checkConnectionAndLoadMoreUsersReview();
                }
            }
        });

    }

    //show progress bar when more reviews are loading
    public void showLoadMoreProgressBar() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                binding.loadMoreProgressBar.setVisibility(View.VISIBLE);

            }
        });
    }

    // hide progress bar when reviews are loaded or no review found
    public void hideLoadMoreProgressBar() {
        binding.loadMoreProgressBar.setVisibility(View.GONE);

    }

    // invisible progress bar when reviews are loaded or no review found
    public void inVisibleLoadMoreProgressBar() {
        binding.loadMoreProgressBar.setVisibility(View.INVISIBLE);

    }

    // check internet connection exist or not. If exist load more review from database
    public void checkConnectionAndLoadMoreUsersReview() {
        boolean isConnectionSourceAvailable = connection.isConnectionSourceAvailable(this);
        if (isConnectionSourceAvailable) {
            showLoadMoreProgressBar();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isInternetAvailable();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            loadMoreUsersReview();
                        } else {
                            Toast.makeText(SeeAllReviewActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            inVisibleLoadMoreProgressBar();
                            binding.loadMoreNoConnectionImg.setVisibility(View.VISIBLE);
                            tryLoadMoreReviewConnection();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    // call when recycler view reach end to get more reviews
    public void loadMoreUsersReview() {
        ArrayList<Review> moreReviewList = new ArrayList<>();

        db.collection("review")
                .whereNotEqualTo("userId", userId)
                .whereEqualTo("placeId", placeId)
                .orderBy("userId")
                .startAfter(lastLoadedReview)
                .limit(15)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            lastLoadedReview = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {

                                Review review = querySnapshot.toObject(Review.class);

                                if (!review.getFeedback().isEmpty()) {
                                    moreReviewList.add(review);
                                }
                            }
                            loadMoreUsersInfo(moreReviewList);
                        } else {
                            hideLoadMoreProgressBar();
                        }
                    }
                });

    }

    //called when more review are loaded to load user info related to that reviews
    public void loadMoreUsersInfo(ArrayList<Review> moreReviewList) {
        ArrayList<User> moreUserList = new ArrayList<>();
        db.collection("user")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (Review review : moreReviewList) {

                                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                    User user = querySnapshot.toObject(User.class);
                                    user.setUserId(querySnapshot.getId());

                                    if (review.getUserId().equals(user.getUserId())) {
                                        moreUserList.add(user);
                                        break;
                                    }
                                }

                            }
                            reviewAdapter.setData(moreReviewList, moreUserList);
                            hideLoadMoreProgressBar();
                            loading = false;
                        }

                    }
                });
    }


    //run when user click on retry icon
    public void retryConnection() {
        binding.checkConnectionLayout.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.checkConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
                binding.checkConnectionLayout.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndLoadUsersReview();
            }
        });
    }

    //run when user click on retry icon which show when system unable to load more reviews
    public void tryLoadMoreReviewConnection() {
        binding.loadMoreNoConnectionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.loadMoreNoConnectionImg.setVisibility(View.GONE);
                showLoadMoreProgressBar();

                checkConnectionAndLoadMoreUsersReview();
            }
        });
    }
}