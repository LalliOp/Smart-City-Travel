package com.example.smartcitytravel.AWSService.DataModel.PlaceModel;

public class Place {
    private Integer placeId;
    private String name;
    private String imageUrl;
    private Float rating;
    private Integer no_of_votes;
    private String longitude;
    private String latitude;

    public Place() {
    }

    public Place(Integer placeId, String name, String imageUrl, Float rating, Integer no_of_votes, String longitude, String latitude) {
        this.placeId = placeId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.no_of_votes = no_of_votes;
        this.longitude = longitude;
        this.latitude = latitude;
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

    public Integer getNo_of_votes() {
        return no_of_votes;
    }

    public void setNo_of_votes(Integer no_of_votes) {
        this.no_of_votes = no_of_votes;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
