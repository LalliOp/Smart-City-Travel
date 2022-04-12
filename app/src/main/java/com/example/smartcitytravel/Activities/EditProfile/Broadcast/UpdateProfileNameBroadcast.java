package com.example.smartcitytravel.Activities.EditProfile.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.example.smartcitytravel.AWSService.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;

public class UpdateProfileNameBroadcast extends BroadcastReceiver {
    private PreferenceHandler preferenceHandler;
    private ActivityHomeBinding binding;
    private Util util;

    public UpdateProfileNameBroadcast(ActivityHomeBinding binding) {
        this.binding = binding;
        preferenceHandler = new PreferenceHandler();
        util = new Util();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        User user = preferenceHandler.getLoginAccountPreference(context);
        setNewProfileName(user);

    }

    // set new profile name
    public void setNewProfileName(User user) {
        View headerLayout = binding.navigationView.getHeaderView(0);

        TextView nameTxt = headerLayout.findViewById(R.id.profileNameTxt);
        nameTxt.setText(util.capitalizedName(user.getName()));

    }
}
