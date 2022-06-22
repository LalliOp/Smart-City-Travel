package com.example.smartcitytravel.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class GpsTracker implements LocationListener {
    private Context context;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean canGetLocation;
    Location location;
    double latitude;
    double longitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BTW_UPDATES = 1000 * 60 * 1;
    private LocationManager locationManager;


    public GpsTracker(Context context) {
        this.context = context;
        this.isGPSEnabled = false;
        this.isNetworkEnabled = false;
        this.canGetLocation = false;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    }

    @SuppressLint("MissingPermission")
    public void registerForLocationUpdates() {
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BTW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BTW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
    }

    public Location getLocation() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            canGetLocation = false;
            return null;
        } else {
            canGetLocation = true;
            registerForLocationUpdates();

            if (isNetworkEnabled) {
                location = networkProvider();

            }
            if (isGPSEnabled) {
                location = GPSProvider();
            }
            return location;
        }
    }

    @SuppressLint("MissingPermission")
    public Location networkProvider() {
        if (locationManager != null) {
            location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }
        return location;
    }

    @SuppressLint("MissingPermission")
    public Location GPSProvider() {
        if (locationManager != null) {
            location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }
        return location;
    }

    public LatLng getLocationLatLng() {
        getLocation();
        return new LatLng(latitude, longitude);
    }

    public boolean canGetLocation() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            canGetLocation = false;
        } else {
            canGetLocation = true;
        }
        return this.canGetLocation;
    }

    // open location setting to on or off location
    public void openLocationSetting() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);
    }

    //call when location changed
    @Override
    public void onLocationChanged(@NonNull Location location) {
        getLocation();

    }

    // call on gps enable
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        canGetLocation = true;
        LocationListener.super.onProviderEnabled(provider);
    }

    //call on gps disable
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        canGetLocation = false;
        LocationListener.super.onProviderDisabled(provider);
    }
}
