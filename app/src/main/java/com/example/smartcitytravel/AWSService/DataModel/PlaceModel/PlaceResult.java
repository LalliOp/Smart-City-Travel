package com.example.smartcitytravel.AWSService.DataModel.PlaceModel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlaceResult {
    @SerializedName("places")
    private List<Place> placeList;

    public PlaceResult() {
    }

    public PlaceResult(List<Place> placeList) {
        this.placeList = placeList;
    }

    public List<Place> getPlaceList() {
        return placeList;
    }

    public void setPlaceList(List<Place> placeList) {
        this.placeList = placeList;
    }
}
