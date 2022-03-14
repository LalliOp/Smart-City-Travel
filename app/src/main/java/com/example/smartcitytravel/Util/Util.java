package com.example.smartcitytravel.Util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.FragmentActivity;

import com.example.smartcitytravel.Dialogs.ErrorDialog;

import org.apache.commons.lang3.StringUtils;

public class Util {
    public Util() {
    }

    // hide keyboard from screen
    public void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusView = activity.getCurrentFocus();
        if (focusView != null) {
            inputManager.hideSoftInputFromWindow(focusView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // user unable to touch any view. Disable all views
    public void makeScreenNotTouchable(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    // user able to touch any view. Enable all views
    public void makeScreenTouchable(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    //show error message in dialog
    public void createErrorDialog(FragmentActivity activity, String title, String message) {
        ErrorDialog errorDialog = new ErrorDialog(title, message);
        errorDialog.show(activity.getSupportFragmentManager(), "error_dialog");
        errorDialog.setCancelable(false);
    }

    //make first word of name capital
    public String capitalizedName(String full_name) {
        String capitalizedName = "";
        String[] split_full_name = full_name.split("\\s+");
        for (String name : split_full_name) {
            name = StringUtils.capitalize(name);

            capitalizedName = capitalizedName + name + " ";
        }
        return capitalizedName;
    }

}
