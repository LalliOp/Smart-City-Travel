package com.example.smartcitytravel.AWSService.DataModel;

public class User {
    private Integer userId;
    private String name;
    private String email;
    private String image_url;
    private Integer google_account;

    public User(){

    }

    public User(Integer userId, String name, String email, String image_url, Integer google_account) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.image_url = image_url;
        this.google_account = google_account;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Integer getGoogle_account() {
        return google_account;
    }

    public void setGoogle_account(Integer google_account) {
        this.google_account = google_account;
    }
}
