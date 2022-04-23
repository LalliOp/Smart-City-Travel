package com.example.smartcitytravel.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;

public class UpdateProfileImageBroadcast extends BroadcastReceiver {
    private final PreferenceHandler preferenceHandler;
    private final ActivityHomeBinding binding;

    public UpdateProfileImageBroadcast(ActivityHomeBinding binding) {
        this.binding = binding;
        preferenceHandler = new PreferenceHandler();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        setUpdatedProfileImage(context);

    }

    // set profile new image
    public void setUpdatedProfileImage(Context context) {
        View headerLayout = binding.navigationView.getHeaderView(0);

        String imageURL = preferenceHandler.getImagePreference(context);

        Glide.with(context)
                .load(imageURL)
                .timeout(60000)
                .into((ImageView) headerLayout.findViewById(R.id.profileImg));
    }
}
