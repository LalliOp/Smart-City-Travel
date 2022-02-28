package com.example.smartcitytravel.AWSService.DataModel;

import com.google.gson.annotations.SerializedName;

public class Result {
    private boolean successful;
    private String errorMsg;

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
