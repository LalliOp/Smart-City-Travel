package com.example.smartcitytravel.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartcitytravel.DataModel.User;
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
        editor.putString("password", user.getPassword());
        editor.putString("email", user.getEmail());
        editor.putString("image_url", user.getImage_url());
        editor.putBoolean("google_account", user.getGoogle_account());
        editor.apply();
    }

    //get detail of logged in account
    public User getLoggedInAccountPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        User user = new User();
        user.setUserId(sharedPreferences.getString("userId", ""));
        user.setName(sharedPreferences.getString("name", ""));
        user.setEmail(sharedPreferences.getString("email", ""));
        user.setPassword(sharedPreferences.getString("password", ""));
        user.setImage_url(sharedPreferences.getString("image_url", ""));
        user.setGoogle_account(sharedPreferences.getBoolean("google_account", false));

        return user;

    }

    //get account type of logged in account
    public Boolean getAccountTypePreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("google_account", false);
    }

    //clear detail of logged in account
    public void clearLoggedInAccountPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    //check preference exist or not
    public Boolean checkLoggedInAccountPreferenceExist(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.contains("userId");
    }

    //update name of logged in account
    public void updateNamePreference(String name, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.apply();
    }

    //update password of logged in account
    public void updatePasswordPreference(String password, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("password", password);
        editor.apply();
    }

    //update profile image of logged in account
    public void updateImagePreference(String image_url, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("image_url", image_url);
        editor.apply();
    }

    //get name of logged in account
    public String getNamePreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.getString("name", "");
    }

    //get password of logged in account
    public String getPasswordPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.getString("password", "");
    }

    //get profile image of logged in account
    public String getImagePreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);

        return sharedPreferences.getString("image_url", "");
    }

    //get userId of logged in account
    public String getUserIdPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);

        return sharedPreferences.getString("userId", "");
    }
}
