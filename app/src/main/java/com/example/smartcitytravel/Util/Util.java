package com.example.smartcitytravel.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.smartcitytravel.R;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
            return !address.toString().equals("");
        } catch (UnknownHostException e) {
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

    //set email of logged in account or empty which system can use to know whether user is login or not and which user is login and act accordingly
    //only for non-google account
    public void setLoginEmailPreference(String value, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.LOGIN_PREFERENCE), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(context.getString(R.string.LOGIN_EMAIL_KEY), value);
        editor.apply();
    }

    //get email of logged in account or empty which system can use to know whether user is login or not and which user is login and act accordingly
    //only for non-google account
    public String getLoginEmailPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.LOGIN_PREFERENCE), Context.MODE_PRIVATE);
        return sharedPreferences.getString(context.getString(R.string.LOGIN_EMAIL_KEY), "");

    }

}
