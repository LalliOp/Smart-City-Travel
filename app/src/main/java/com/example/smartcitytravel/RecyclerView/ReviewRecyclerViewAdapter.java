package com.example.smartcitytravel.RecyclerView;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.DataModel.Review;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ReviewViewBinding;

import java.util.ArrayList;

public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder> {
    private ArrayList<Review> reviewList;
    private ArrayList<User> userList;
    private Context context;
    private Util util;
    private boolean trimReviewLength;

    public ReviewRecyclerViewAdapter(Context context, ArrayList<Review> reviewList, ArrayList<User> userList, boolean trimReviewLength) {
        this.context = context;
        this.reviewList = reviewList;
        util = new Util();
        this.trimReviewLength = trimReviewLength;
        this.userList = userList;

    }

    @NonNull
    @Override
    public ReviewRecyclerViewAdapter.ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReviewViewHolder(ReviewViewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewRecyclerViewAdapter.ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        User user = userList.get(position);
        setUI(holder, review, user);

        if (trimReviewLength) {
            limitReviewLength(holder);
            expandOrCollapseReview(holder);
        }

    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ReviewViewBinding binding;

        public ReviewViewHolder(@NonNull ReviewViewBinding reviewViewBinding) {
            super(reviewViewBinding.getRoot());
            this.binding = reviewViewBinding;
        }
    }

    // set more data and notify in recyclerview
    public void setData(ArrayList<Review> reviewList, ArrayList<User> userList) {
        int insertPosition = this.reviewList.size();
        this.reviewList.addAll(reviewList);
        this.userList.addAll(userList);
        notifyItemInserted(insertPosition);
    }

    // set data in recycler view
    public void setUI(ReviewViewHolder holder, Review review, User user) {
        holder.binding.nameTxt.setText(util.capitalizedName(user.getName()));
        holder.binding.ratingBar.setRating(review.getRating());

        holder.binding.reviewTxt.setText(review.getFeedback());

        Glide.with(context)
                .load(user.getImage_url())
                .timeout(60000)
                .into(holder.binding.profileImg);
    }

    // limit length of review to 3 lines
    public void limitReviewLength(ReviewViewHolder holder) {
        if (holder.binding.reviewTxt.getMaxLines() > 3) {
            holder.binding.reviewTxt.setMaxLines(3);
            holder.binding.reviewTxt.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    // expand or collapse review when user click on review when review is greater than 3
    public void expandOrCollapseReview(ReviewViewHolder holder) {
        holder.binding.reviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.binding.reviewTxt.getMaxLines() == 3) {
                    holder.binding.reviewTxt.setMaxLines(Integer.MAX_VALUE);
                } else if (holder.binding.reviewTxt.getMaxLines() > 3) {
                    holder.binding.reviewTxt.setMaxLines(3);
                    holder.binding.reviewTxt.setEllipsize(TextUtils.TruncateAt.END);
                }
            }
        });
    }

}
