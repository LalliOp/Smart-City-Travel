package com.example.smartcitytravel.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Connection {
    public Connection() {

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
            InetAddress googleAddress = InetAddress.getByName("www.google.com");
            InetAddress bingAddress = InetAddress.getByName("www.bing.com");
            if (!googleAddress.toString().equals("") || !bingAddress.toString().equals("")) {
                return true;
            } else {
                return false;
            }
        } catch (UnknownHostException e) {
            return false;
        }
    }

}
