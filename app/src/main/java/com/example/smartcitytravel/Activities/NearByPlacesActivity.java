package com.example.smartcitytravel.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.DataModel.PlaceLocation;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.GridPlaceAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.GridSpaceItemDecoration;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityNearByPlacesBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NearByPlacesActivity extends AppCompatActivity {
    private ActivityNearByPlacesBinding binding;
    private Util util;
    private FirebaseFirestore db;
    private boolean firstTimeNearbyPlaces;
    private GridPlaceAdapter gridPlaceAdapter;
    private boolean locationPermissionAllowed;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;
    private static final long MIN_TIME_BTW_UPDATES = 60000;
    private LocationManager locationManager;
    private Location currentLocation;
    private boolean lastKnownLocationAccess;
    private Connection connection;


    // whenever location is changed or location is on or off
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onFlushComplete(int requestCode) {
            Toast.makeText(NearByPlacesActivity.this, "onFlush", Toast.LENGTH_SHORT).show();
            LocationListener.super.onFlushComplete(requestCode);
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {
            checkLocationPermission();
            if (locationPermissionAllowed) {
                currentLocation = location;
                if (lastKnownLocationAccess) {
                    lastKnownLocationAccess = false;
                } else {
                    hideLocationPermissionButton();

                    requestLocationPermission();
                    checkConnectionAndGetNearByPlaces();
                    Toast.makeText(NearByPlacesActivity.this, "LOCATION: " + location.getLongitude(), Toast.LENGTH_SHORT).show();

                }
            } else {
                showLocationPermissionButton();
            }

            Toast.makeText(NearByPlacesActivity.this, "onChanged", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Toast.makeText(NearByPlacesActivity.this, "ENABLED", Toast.LENGTH_SHORT).show();

            checkLocationPermission();
            if (locationPermissionAllowed) {
                hideLocationSettingsButton();

            }
            LocationListener.super.onProviderEnabled(provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Toast.makeText(NearByPlacesActivity.this, "DISABLED", Toast.LENGTH_SHORT).show();

            checkLocationPermission();
            if (locationPermissionAllowed) {
                showLocationSettingsButton();

            }
            LocationListener.super.onProviderDisabled(provider);
        }
    };

    private final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult
            (new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Boolean fineLocationPermission = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationPermission = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (fineLocationPermission != null && fineLocationPermission
                            || coarseLocationPermission != null && coarseLocationPermission) {
                        locationPermissionAllowed = true;
                    } else {
                        locationPermissionAllowed = false;

                    }
                }
            });

    @Override
    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNearByPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        requestLocationPermission();
        getNearByPlacesWithOldLocation();
        setToolBarTheme();
        openLocationSetting();
        changeRange();


    }

    @Override
    protected void onResume() {
        registerForLocationUpdates();
        super.onResume();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        db = FirebaseFirestore.getInstance();
        firstTimeNearbyPlaces = true;
        locationPermissionAllowed = false;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        lastKnownLocationAccess = false;
        connection = new Connection();

    }

    //change range when user click range button
    public void selectRange() {
        String selectedRange = binding.rangeTxt.getText().toString();
        switch (selectedRange) {
            case "3":
                binding.rangeTxt.setText("5");
                break;
            case "5":
                binding.rangeTxt.setText("7");
                break;
            case "7":
                binding.rangeTxt.setText("3");
                break;
        }
    }

    // style and customize toolbar and theme
    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, "Nearby Places");
    }

    //when click on range button, range changed and again get all nearby places to current location
    public void changeRange() {
        binding.rangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRange();
                checkConnectionAndGetNearByPlaces();
            }
        });
    }

    //get location of all nearby places to current location
    public void getNearByPlaces() {
        if (locationPermissionAllowed) {
            db.collection("place")
                    .whereEqualTo("City", getCityName())
                    .get()
                    .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots != null || !queryDocumentSnapshots.isEmpty()) {
                                if (queryDocumentSnapshots.size() == 0) {
                                    binding.noPlaceTxt.setVisibility(View.VISIBLE);
                                    binding.PlaceRecyclerView.setVisibility(View.GONE);

                                    if (!firstTimeNearbyPlaces) {
                                        gridPlaceAdapter.clearData();
                                    }
                                } else {


                                    ArrayList<Place> nearByPlaceList = new ArrayList<>();
                                    double selectedRange = Double.parseDouble(binding.rangeTxt.getText().toString());

                                    for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                        PlaceLocation placeLocation = querySnapshot.toObject(PlaceLocation.class);
                                        placeLocation.setPlaceId(querySnapshot.getId());

                                        double distanceBetween = calculateDistance(Double.parseDouble(placeLocation.getLatitude()), Double.parseDouble(placeLocation.getLongitude()));

                                        if (distanceBetween <= selectedRange) {
                                            Place place = querySnapshot.toObject(Place.class);
                                            place.setPlaceId(querySnapshot.getId());
                                            nearByPlaceList.add(place);
                                        }

                                    }
                                    if (firstTimeNearbyPlaces) {
                                        setAdapter(nearByPlaceList);
                                        firstTimeNearbyPlaces = false;
                                    } else {
                                        gridPlaceAdapter.setNewData(nearByPlaceList);

                                    }
                                    binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
                                    binding.noPlaceTxt.setVisibility(View.GONE);
                                    binding.rangeLayout.setVisibility(View.VISIBLE);
                                    binding.PlaceRecyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    });

        }

    }

    @SuppressLint("MissingPermission")
    public void getNearByPlacesWithOldLocation() {
        if (locationPermissionAllowed) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (currentLocation != null) {
                checkConnectionAndGetNearByPlaces();
                lastKnownLocationAccess = true;
            }
        }
    }

    // check whether connection exist or not. If exist, get nearby places
    public void checkConnectionAndGetNearByPlaces() {
        binding.CheckConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
        binding.PlaceRecyclerView.setVisibility(View.GONE);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(NearByPlacesActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            binding.rangeBtn.setVisibility(View.VISIBLE);
                            getNearByPlaces();
                        } else {
                            binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.VISIBLE);
                            binding.rangeLayout.setVisibility(View.GONE);
                            retryConnection();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    //run when user click on retry icon
    public void retryConnection() {
        binding.CheckConnectionLayout.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.CheckConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
                binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndGetNearByPlaces();
            }
        });

    }

    // initialize recyclerview
    public void setAdapter(ArrayList<Place> nearByPlaceList) {
        gridPlaceAdapter = new GridPlaceAdapter(this, nearByPlaceList);

        binding.PlaceRecyclerView.setAdapter(gridPlaceAdapter);
        binding.PlaceRecyclerView.addItemDecoration(new GridSpaceItemDecoration(20, 26, 10, 0));

    }

    // calculate distance between to two coordinates
    public double calculateDistance(double placeLatitude, double placeLongitude) {
        Location placeLocation = new Location("place_location");
        placeLocation.setLatitude(placeLatitude);
        placeLocation.setLongitude(placeLongitude);

        double distanceInMeters = currentLocation.distanceTo(placeLocation);
        return distanceInMeters * 0.001; //convert distance from meters into kilometers

    }

    //get the name of current location city
    public String getCityName() {
        Geocoder geocoder = new Geocoder(NearByPlacesActivity.this, Locale.getDefault());
        try {
            ArrayList<Address> cityList = (ArrayList<Address>) geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (cityList.get(0).getLocality() != null) {
                return cityList.get(0).getLocality();

            } else {
                return "";
            }
        } catch (IOException e) {
            return "";
        }
    }


    // when user lick on enable location button, open location setting to on or off location
    public void openLocationSetting() {
        binding.locationSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
    }

    // request location permission. If permission is denied, Ask for location permission
    public void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});

        } else {
            locationPermissionAllowed = true;
        }

    }

    // check whether location permission is allowed or not
    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionAllowed = true;
        } else {
            locationPermissionAllowed = false;
        }
    }

    // show when location permission is denied
    // it show button which navigate to settings page which can navigate to permission page
    public void showLocationPermissionButton() {
        binding.PlaceRecyclerView.setVisibility(View.GONE);
        binding.rangeLayout.setVisibility(View.GONE);
        binding.locationPermissionBtn.setVisibility(View.VISIBLE);
        binding.locationPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationPermission();
            }
        });
    }

    //hide when location permission is allowed
    public void hideLocationPermissionButton() {
        binding.locationPermissionBtn.setVisibility(View.GONE);
    }

    // app detail settings page are open, where we navigate to permissions page
    public void openLocationPermission() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    //show when location settings is off
    void showLocationSettingsButton() {
        binding.PlaceRecyclerView.setVisibility(View.GONE);
        binding.rangeLayout.setVisibility(View.GONE);
        binding.locationSettingBtn.setVisibility(View.VISIBLE);
    }

    //hide when location settings is on
    void hideLocationSettingsButton() {
        binding.locationSettingBtn.setVisibility(View.GONE);
    }

    // register to get current location
    @SuppressLint("MissingPermission")
    public void registerForLocationUpdates() {
        checkLocationPermission();
        if (locationPermissionAllowed) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BTW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BTW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            hideLocationPermissionButton();
        } else {
            showLocationPermissionButton();
        }
    }

    // return to previous activity when user click on up button (which is back button on top life side)
    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }
}