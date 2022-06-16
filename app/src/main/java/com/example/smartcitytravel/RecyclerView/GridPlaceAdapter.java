package com.example.smartcitytravel.RecyclerView;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.Activities.PlaceDetailActivity;
import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.databinding.BiggerPlaceViewBinding;

import java.util.ArrayList;

public class GridPlaceAdapter extends RecyclerView.Adapter<GridPlaceAdapter.PlaceViewHolder> {
    private ArrayList<Place> placeList;
    private Context context;

    public GridPlaceAdapter(Context context, ArrayList<Place> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceViewHolder(BiggerPlaceViewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);

        setPlaceUI(holder, place);
        moveToPlaceDetailActivity(holder, place);

        setScaleAnimation(holder.binding.getRoot());
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    // set data in place UI
    public void setPlaceUI(PlaceViewHolder holder, Place place) {
        holder.binding.placeNameTxt.setText(place.getName());
        holder.binding.ratingTxt.setText(place.getRating().toString());
        Glide.with(context)
                .load(place.getImage1())
                .timeout(60000)
                .into(holder.binding.placeImg);

        holder.binding.loadingBar.setVisibility(View.GONE);

    }

    public void moveToPlaceDetailActivity(PlaceViewHolder holder, Place place) {
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlaceDetailActivity.class);
                intent.putExtra("placeId", place.getPlaceId());
                context.startActivity(intent);
            }
        });
    }

    // set more data and notify in recyclerview
    public void setData(ArrayList<Place> newPlaceList) {
        int insertPosition = this.placeList.size();
        this.placeList.addAll(newPlaceList);
        notifyItemInserted(insertPosition);
    }

    // data completely changed. Data maybe remove or new added
    public void setNewData(ArrayList<Place> newPlaceList) {
        this.placeList.clear();
        this.placeList.addAll(newPlaceList);
        notifyDataSetChanged();
    }

    // get data
    public ArrayList<Place> getData() {
        return placeList;
    }

    // remove all data
    public void clearData() {
        placeList.clear();
        notifyItemRangeRemoved(0, getItemCount());
    }

    // remove place which is not exist in new place list
    public void removeData(ArrayList<Place> newPlaceList) {
        for (int i = 0; i < this.placeList.size(); i++) {
            boolean placeExist = false;
            for (int j = 0; j < newPlaceList.size(); j++) {
                if (placeList.get(i).getPlaceId().equals(newPlaceList.get(j).getPlaceId())) {
                    placeExist = true;
                    break;
                }
            }
            if (!placeExist) {
                this.placeList.remove(i);
                notifyItemRemoved(i);
            }
        }

    }

    // zoom in animation
    private void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(300);
        view.startAnimation(anim);
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final BiggerPlaceViewBinding binding;

        public PlaceViewHolder(@NonNull BiggerPlaceViewBinding biggerPlaceViewBinding) {
            super(biggerPlaceViewBinding.getRoot());
            this.binding = biggerPlaceViewBinding;

        }
    }
}