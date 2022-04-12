package com.example.smartcitytravel.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartcitytravel.AWSService.DataModel.User;
import com.example.smartcitytravel.R;

public class PreferenceHandler {
    public PreferenceHandler() {

    }

    //set detail of logged in account
    public void setLoginAccountPreference(User user, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("userId", user.getUserId());
        editor.putString("name", user.getName());
        editor.putString("email", user.getEmail());
        editor.putString("image_url", user.getImage_url());
        editor.putInt("google_account", user.getGoogle_account());
        editor.apply();
    }

    //get detail of logged in account
    public User getLoginAccountPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        User user = new User();
        user.setUserId(sharedPreferences.getInt("userId", -1));
        user.setName(sharedPreferences.getString("name", ""));
        user.setEmail(sharedPreferences.getString("email", ""));
        user.setImage_url(sharedPreferences.getString("image_url", ""));
        user.setGoogle_account(sharedPreferences.getInt("google_account", -1));

        return user;

    }

    //get account type of logged in account
    public Integer getLoginAccountTypePreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.getInt("google_account", -1);
    }

    //clear detail of logged in account
    public void clearLoginAccountPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    //update name of logged in account
    public void updateNameLoginAccountPreference(String name, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.apply();
    }

    //update profile image of logged in account
    public void updateImageLoginAccountPreference(String image_url, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("image_url", image_url);
        editor.apply();
    }
}
