package com.example.smartcitytravel.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.DataModel.Review;
import com.example.smartcitytravel.databinding.ReviewViewBinding;

import java.util.ArrayList;

public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder> {
    private final ArrayList<Review> reviewList;
    private final Context context;

    public ReviewRecyclerViewAdapter(Context context, ArrayList<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReviewViewHolder(ReviewViewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
//        Review review = reviewList.get(position);
//
//        holder.binding.nameTxt.setText(review.getUserName());
//        holder.binding.ratingBar.setRating(review.getRating());
//        holder.binding.reviewTxt.setText(review.getFeedback());
//
//        Glide.with(context)
//                .load(review.getUserImageUrl())
//                .timeout(60000)
//                .into(holder.binding.profileImg);
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
}
