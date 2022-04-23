package com.example.smartcitytravel.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.Activities.PlaceDetail.PlaceDetailActivity;
import com.example.smartcitytravel.databinding.PlaceViewBinding;

import java.util.ArrayList;

public class PlaceRecyclerViewAdapter extends RecyclerView.Adapter<PlaceRecyclerViewAdapter.PlaceViewHolder> {

    private final ArrayList<Place> placeArrayList;
    private final Context context;

    public PlaceRecyclerViewAdapter(Context context, ArrayList<Place> placeArrayList) {
        this.context = context;
        this.placeArrayList = placeArrayList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceRecyclerViewAdapter.PlaceViewHolder(PlaceViewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.binding.placeName.setText(placeArrayList.get(position).getName());
        holder.binding.placeRatingTxt.setText(placeArrayList.get(position).getRating().toString());
        Glide.with(context)
                .load(placeArrayList.get(position).getImage1())
                .timeout(60000)
                .into(holder.binding.placeImg);

        holder.binding.placeLoadingBar.setVisibility(View.GONE);

        moveToPlaceDetailActivity(holder, position);

    }

    @Override
    public int getItemCount() {
        return placeArrayList.size();
    }

    //move to place detail activity
    public void moveToPlaceDetailActivity(PlaceRecyclerViewAdapter.PlaceViewHolder holder, int position) {
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlaceDetailActivity.class);
                intent.putExtra("placeId", placeArrayList.get(position).getPlaceId());
                context.startActivity(intent);
            }
        });
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final PlaceViewBinding binding;

        public PlaceViewHolder(@NonNull PlaceViewBinding placeViewBinding) {
            super(placeViewBinding.getRoot());
            this.binding = placeViewBinding;

        }
    }
}
