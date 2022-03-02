package com.example.smartcitytravel.Util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.smartcitytravel.R;

import java.io.IOException;
import java.net.InetAddress;

public class Util {
    public Util() {
    }

    //check whether system is connected with internet source (WIFI and Network) regardless of internet is working or not
    //then call isInternetAvailable() to check whether internet connection is working or not
    public boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        } else {
            return isInternetAvailable();
        }

    }

    //try to connect with google server to confirm internet connection is working
    public boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return address.isReachable(1000);
        } catch (IOException e) {
            return false;
        }
    }

    //create error color which will used as icon color when error shown
    public ColorStateList iconRedColor(Context context) {
        int redColor = context.getResources().getColor(R.color.red);
        return ColorStateList.valueOf(redColor);
    }

    //create normal color which will used as icon color when no error occurs
    public ColorStateList iconWhiteColor(Context context) {
        int whiteColor = context.getResources().getColor(R.color.white);
        return ColorStateList.valueOf(whiteColor);
    }

}
