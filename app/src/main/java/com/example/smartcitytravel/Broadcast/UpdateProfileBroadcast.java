package com.example.smartcitytravel.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.AWSService.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;

public class UpdateProfileBroadcast extends BroadcastReceiver {
    private PreferenceHandler preferenceHandler;
    private ActivityHomeBinding binding;
    private Util util;

    public UpdateProfileBroadcast(ActivityHomeBinding binding) {
        this.binding = binding;
        preferenceHandler = new PreferenceHandler();
        util = new Util();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        User user = preferenceHandler.getLoginAccountPreference(context);
        setUserProfile(context, user);

    }
    // set name , email and image of user profile
    public void setUserProfile(Context context, User user) {
        View headerLayout = binding.navigationView.getHeaderView(0);

        TextView nameTxt = headerLayout.findViewById(R.id.profileNameTxt);
        nameTxt.setText(util.capitalizedName(user.getName()));

        Glide.with(context)
                .load(user.getImage_url())
                .timeout(60000)
                .into((ImageView) headerLayout.findViewById(R.id.profileImg));
    }
}
