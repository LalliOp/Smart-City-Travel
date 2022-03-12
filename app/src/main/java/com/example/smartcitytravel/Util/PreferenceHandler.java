package com.example.smartcitytravel.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smartcitytravel.R;

public class PreferenceHandler {
    public PreferenceHandler() {

    }

    //set email of logged in account or empty which system can use to know whether user is login or not and which user is login and act accordingly
    //only for non-google account
    public void setLoginEmailPreference(String value, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.LOGIN_EMAIL_KEY), value);
        editor.apply();
    }

    //get email of logged in account or empty which system can use to know whether user is login or not and which user is login and act accordingly
    //only for non-google account
    public String getLoginEmailPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.getString(context.getString(R.string.LOGIN_EMAIL_KEY), "");

    }

    //clear preference. No record left.
    //only for non-google account
    public void clearLoginEmailPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    //save email which is used during reset password process
    public void saveEmailOfResetPasswordProcess(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(context.getString(R.string.RESET_PASSWORD_EMAIL_KEY), value);
        editor.apply();
    }

    //get email which is used during reset password process
    public String getEmailOfResetPasswordProcess(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.PREFERENCE),
                Context.MODE_PRIVATE);
        return sharedPreferences.getString(context.getString(R.string.RESET_PASSWORD_EMAIL_KEY), "");

    }
}
