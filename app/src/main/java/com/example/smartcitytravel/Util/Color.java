package com.example.smartcitytravel.Util;

import android.content.Context;
import android.content.res.ColorStateList;

import com.example.smartcitytravel.R;

public class Color {

    public Color() {

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

    //create color
    public ColorStateList createColor(Context context, int colorResId) {
        int redColor = context.getResources().getColor(colorResId);
        return ColorStateList.valueOf(redColor);
    }

}
