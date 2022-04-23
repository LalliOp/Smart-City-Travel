package com.example.smartcitytravel.Activities.WriteReview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.DataModel.Review;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityWriteReviewBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class WriteReviewActivity extends AppCompatActivity {
    private ActivityWriteReviewBinding binding;
    private Util util;
    private PreferenceHandler preferenceHandler;
    private User user;
    private Toast updateMessageToast;
    private boolean toEdit;
    private boolean updateReview;
    private boolean updateRating;
    private boolean backPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        util = new Util();
        preferenceHandler = new PreferenceHandler();
        user = preferenceHandler.getLoggedInAccountPreference(this);
        updateMessageToast = new Toast(this);

        updateReview = false;
        updateRating = false;
        backPressed = false;

        getIntentData();
        setToolbar();
        setUserProfile();
        submitButtonClickListener();
        reviewChangeListener();
        ratingChangeListener();
    }

    // get values pass by review fragment when click on edit button
    public void getIntentData() {
        if (getIntent().getExtras() != null) {
            toEdit = getIntent().getExtras().getBoolean("edit");

            setReviewAndRating();
        } else {
            toEdit = false;
        }
    }

    //set review and rating in their respective field
    //call when come to this activity from review fragment when click on edit button
    public void setReviewAndRating() {
        String review = getIntent().getExtras().getString("review");
        float rating = getIntent().getExtras().getFloat("rating");

        binding.reviewEdit.setText(review);
        binding.ratingBar.setRating(rating);
    }

    // called when user click on submit button
    public void submitButtonClickListener() {
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadReview();
            }
        });


    }

    // add review in database
    public void uploadReview() {
        showLoadingBar();

        util.hideKeyboard(WriteReviewActivity.this);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (toEdit) {
            String reviewId = getIntent().getExtras().getString("reviewId");
            if (updateReview) {
                updateReview(db, reviewId);
            }
            if (updateRating) {
                updateRating(db, reviewId);
            }
        } else {
            writeReview(db);
        }
    }

    // call when writing review first time
    public void writeReview(FirebaseFirestore db) {
        String placeId = getIntent().getExtras().getString("placeId");

        Review review = new Review(user.getUserId(), placeId, binding.ratingBar.getRating(),
                binding.reviewEdit.getText().toString());

        db.collection("review")
                .add(review)
                .addOnSuccessListener(this, new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        hideLoadingBar();
                        Toast.makeText(WriteReviewActivity.this, "Review posted", Toast.LENGTH_SHORT).show();

                        finishActivity();
                    }
                });
    }

    // update old review
    public void updateReview(FirebaseFirestore db, String reviewId) {
        db.collection("review")
                .document(reviewId)
                .update("feedback", binding.reviewEdit.getText().toString())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        hideLoadingBar();
                        displayUpdateMessage();

                        finishActivity();
                    }
                });
    }

    // update old rating
    public void updateRating(FirebaseFirestore db, String reviewId) {

        db.collection("review")
                .document(reviewId)
                .update("rating", binding.ratingBar.getRating())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        hideLoadingBar();
                        displayUpdateMessage();

                        finishActivity();
                    }
                });
    }

    // called when change in review occurs
    public void reviewChangeListener() {
        binding.reviewEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateReview = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    // called when rating change
    public void ratingChangeListener() {
        binding.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                updateRating = true;
            }
        });

    }

    //add toolbar in activity and customize status bar color
    public void setToolbar() {
        util.setStatusBarColor(this, R.color.theme_light);
        if (toEdit) {
            util.addToolbar(this, binding.toolbarLayout.toolbar, "Edit Review");
        } else {
            util.addToolbar(this, binding.toolbarLayout.toolbar, "Write A Review");

        }

    }

    //set profile name and image
    public void setUserProfile() {
        binding.nameTxt.setText(util.capitalizedName(user.getName()));

        Glide.with(this)
                .load(user.getImage_url())
                .into(binding.profileImg);

    }

    // show progress bar
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(this);
    }

    //hide progressbar
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(this);
    }

    // show message when updated
    public void displayUpdateMessage() {
        try {
            updateMessageToast.cancel();
            updateMessageToast.getView().isShown();
        } catch (Exception ignored) {
            updateMessageToast = Toast.makeText(this, "Review updated", Toast.LENGTH_SHORT);
            updateMessageToast.show();
        }
    }

    // back to previous activity when user click on up button (which is back button on top life side)
    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            backPressed = true;
            util.hideKeyboard(this);
            showUpdateReviewConfirmationDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    //create and show update review confirmation dialog
    public void showUpdateReviewConfirmationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);

        TextView titleTxt = dialogView.findViewById(R.id.titleTxt);
        titleTxt.setText("Confirm");

        TextView messageTxt = dialogView.findViewById(R.id.messageTxt);
        messageTxt.setText("Do you want to save changes?");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploadReview();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
    }

    // finish activity if user click back or up button
    public void finishActivity() {
        if (backPressed) {
            finish();
        }
    }

    //show apply changes confirmation dialog if changes are left to apply otherwise just exit activity
    @Override
    public void onBackPressed() {
        if (updateReview || updateRating) {
            backPressed = true;
            util.hideKeyboard(this);
            showUpdateReviewConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }
}