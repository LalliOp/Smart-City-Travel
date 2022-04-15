package com.example.smartcitytravel.Activities.PlaceDetail.Fragments.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcitytravel.databinding.FeedbackViewBinding;

import java.util.ArrayList;

public class FeedbackRecyclerViewAdapter extends RecyclerView.Adapter<FeedbackRecyclerViewAdapter.FeedbackViewHolder> {
    private ArrayList<String> feedbackList;
    private Context context;

    public FeedbackRecyclerViewAdapter(Context context, ArrayList<String> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FeedbackViewHolder(FeedbackViewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private FeedbackViewBinding binding;

        public FeedbackViewHolder(@NonNull FeedbackViewBinding feedbackViewBinding) {
            super(feedbackViewBinding.getRoot());
            this.binding = feedbackViewBinding;
        }
    }
}
