package com.example.smartcitytravel.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.FragmentReviewBinding;

public class UpdateReviewBroadcast extends BroadcastReceiver {
    private final PreferenceHandler preferenceHandler;
    private final FragmentReviewBinding binding;
    private final Util util;

    public UpdateReviewBroadcast(FragmentReviewBinding binding) {
        this.binding = binding;
        preferenceHandler = new PreferenceHandler();
        util = new Util();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
