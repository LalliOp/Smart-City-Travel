package com.example.smartcitytravel.Activities.EditProfile.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.AWSService.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;

public class UpdateProfileImageBroadcast extends BroadcastReceiver {
    private PreferenceHandler preferenceHandler;
    private ActivityHomeBinding binding;

    public UpdateProfileImageBroadcast(ActivityHomeBinding binding) {
        this.binding = binding;
        preferenceHandler = new PreferenceHandler();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        User user = preferenceHandler.getLoginAccountPreference(context);
        setUpdatedProfileImage(context, user);

    }

    // set profile new image
    public void setUpdatedProfileImage(Context context, User user) {
        View headerLayout = binding.navigationView.getHeaderView(0);

        Glide.with(context)
                .load(user.getImage_url())
                .timeout(60000)
                .into((ImageView) headerLayout.findViewById(R.id.profileImg));
    }
}
