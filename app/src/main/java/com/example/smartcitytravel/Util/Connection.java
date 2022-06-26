package com.example.smartcitytravel.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;

import khttp.KHttp;
import khttp.responses.Response;

public class Connection {
    public Connection() {

    }

    //check whether system is connected with internet source (WIFI and Network) regardless of internet is working or not
    //then call isInternetAvailable() to check whether internet connection is working or not
    public boolean isConnectionSourceAndInternetAvailable(Context context) {
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
            Response response = KHttp.get("https://www.google.com/",
                    new HashMap<String, String>(), new HashMap<String, String>(),
                    null, null, null, null, 1.5);

            return response.getStatusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    //check whether system is connected with internet source (WIFI and Network) regardless of internet is working or not
    public boolean isConnectionSourceAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();

    }

}
