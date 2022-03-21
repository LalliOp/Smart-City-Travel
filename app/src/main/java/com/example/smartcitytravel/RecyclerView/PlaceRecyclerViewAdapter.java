package com.example.smartcitytravel.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.Activities.PlaceRecyclerView.Place;
import com.example.smartcitytravel.databinding.PlaceViewBinding;

import java.util.ArrayList;

public class PlaceRecyclerViewAdapter extends RecyclerView.Adapter<PlaceRecyclerViewAdapter.PlaceViewHolder> {

    private ArrayList<Place> placeArrayList;
    private Context context;

    public PlaceRecyclerViewAdapter(Context context, ArrayList<Place> placeArrayList) {
        this.context = context;
        this.placeArrayList = placeArrayList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceRecyclerViewAdapter.PlaceViewHolder(PlaceViewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.binding.placeName.setText(placeArrayList.get(position).getName());
        Glide.with(context)
                .load(placeArrayList.get(position).getImageUrl())
                .into(holder.binding.placeImg);

        holder.binding.placeLoadingBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return placeArrayList.size();
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        private PlaceViewBinding binding;

        public PlaceViewHolder(@NonNull PlaceViewBinding placeViewBinding) {
            super(placeViewBinding.getRoot());
            this.binding = placeViewBinding;

        }
    }
}
