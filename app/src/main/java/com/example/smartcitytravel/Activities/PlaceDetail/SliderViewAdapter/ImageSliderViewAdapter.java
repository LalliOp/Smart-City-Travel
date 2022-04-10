package com.example.smartcitytravel.Activities.PlaceDetail.SliderViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.databinding.ImageSliderLayoutBinding;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

public class ImageSliderViewAdapter extends SliderViewAdapter<ImageSliderViewAdapter.ImageSliderHolder> {
    ArrayList<String> imageList;
    Context context;

    public ImageSliderViewAdapter(Context context, ArrayList<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @Override
    public ImageSliderHolder onCreateViewHolder(ViewGroup parent) {
        return new ImageSliderHolder(ImageSliderLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(ImageSliderHolder viewHolder, int position) {
        Glide.with(context)
                .load(imageList.get(position))
                .into(viewHolder.binding.placeImg);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    class ImageSliderHolder extends SliderViewAdapter.ViewHolder {
        private ImageSliderLayoutBinding binding;

        public ImageSliderHolder(ImageSliderLayoutBinding imageSliderLayoutBinding) {
            super(imageSliderLayoutBinding.getRoot());
            binding = imageSliderLayoutBinding;
        }
    }
}
