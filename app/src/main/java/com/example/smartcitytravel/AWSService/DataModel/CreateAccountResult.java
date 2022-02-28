package com.example.smartcitytravel.AWSService.DataModel;

public class CreateAccountResult {
    private boolean successful;
    private String errorMsg;

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
