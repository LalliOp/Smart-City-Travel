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
        editor.putString("userId", user.getUserId());
        editor.putString("name", user.getName());
        editor.putString("email", user.getEmail());
        editor.putString("image_url", user.getImage_url());
        editor.putBoolean("google_account", user.getGoogle_account());
        editor.apply();
    }

    //get detail of logged in account
    public User getLoginAccountPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        User user = new User();
        user.setUserId(sharedPreferences.getString("userId", ""));
        user.setName(sharedPreferences.getString("name", ""));
        user.setEmail(sharedPreferences.getString("email", ""));
        user.setImage_url(sharedPreferences.getString("image_url", ""));
        user.setGoogle_account(sharedPreferences.getBoolean("google_account", false));

        return user;

    }

    //get account type of logged in account
    public Boolean getLoginAccountTypePreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("google_account", false);
    }

    //clear detail of logged in account
    public void clearLoginAccountPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    //check preference exist or not
    public Boolean checkLoginAccountPreferenceExist(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.contains("userId");
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

    //get name of logged in account
    public String getNameLoginAccountPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.getString("name", "");
    }

    //get profile image of logged in account
    public String getImageLoginAccountPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);

        return sharedPreferences.getString("image_url", "");
    }
}
