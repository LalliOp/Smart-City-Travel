package com.example.smartcitytravel.Activities.PlaceRecyclerView;

public class Place {
    private String name;
    private int imageUrl;

    public Place() {
    }

    public Place(String name, int imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(int imageUrl) {
        this.imageUrl = imageUrl;
    }
}
