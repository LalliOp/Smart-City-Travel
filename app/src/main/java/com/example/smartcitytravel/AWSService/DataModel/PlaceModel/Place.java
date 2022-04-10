package com.example.smartcitytravel.AWSService.DataModel.PlaceModel;

public class Place {
    private Integer placeId;
    private String name;
    private String imageUrl;
    private Float rating;

    public Place() {
    }

    public Place(Integer placeId, String name, String imageUrl, Float rating) {
        this.placeId = placeId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    public Integer getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Integer placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }
}
