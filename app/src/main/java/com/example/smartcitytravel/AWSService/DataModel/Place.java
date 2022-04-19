package com.example.smartcitytravel.AWSService.DataModel;

public class Place {
    private String placeId;
    private String Name;
    private String Image1;
    private Float Rating;

    public Place() {
    }

    public Place(String name, String image1, Float rating) {
        Name = name;
        Image1 = image1;
        Rating = rating;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage1() {
        return Image1;
    }

    public void setImage1(String image1) {
        Image1 = image1;
    }

    public Float getRating() {
        return Rating;
    }

    public void setRating(Float rating) {
        Rating = rating;
    }
}
