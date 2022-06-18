package com.example.smartcitytravel.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.DataModel.PlaceLocation;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.GridPlaceAdapter;
import com.example.smartcitytravel.Util.GpsTracker;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityNearByPlacesBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class NearByPlacesActivity extends AppCompatActivity {
    private ActivityNearByPlacesBinding binding;
    private Util util;
    private FirebaseFirestore db;
    private boolean firstTimeNearbyPlaces;
    private GridPlaceAdapter gridPlaceAdapter;
    private GpsTracker gpsTracker;
    private boolean locationPermissionAllowed;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNearByPlacesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setToolBarTheme();
        requestLocationPermission();
        locationSetting();
        changeRange();

    }

    @Override
    protected void onResume() {
        locationSettingButtonVisibility();
        locationPermissionButtonVisibility();
        checkLocationPermission();
        getNearByPlaces();
        super.onResume();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        db = FirebaseFirestore.getInstance();
        firstTimeNearbyPlaces = true;
        gpsTracker = new GpsTracker(this);
        locationPermissionAllowed = false;

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
                locationPermissionButtonVisibility();
                locationSettingButtonVisibility();
                getNearByPlaces();
            }
        });
    }

    //get location of all nearby places to current location
    public void getNearByPlaces() {
        if (gpsTracker.canGetLocation() && locationPermissionAllowed) {
            gpsTracker.getLocation();
            db
                    .collection("place")
                    .get()
                    .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots != null || !queryDocumentSnapshots.isEmpty()) {
                                LatLng currentLocationLatLng = gpsTracker.getLocationLatLng();
                                ArrayList<Place> nearByPlaceList = new ArrayList<>();
                                double selectedRange = Double.parseDouble(binding.rangeTxt.getText().toString());

                                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                    PlaceLocation placeLocation = querySnapshot.toObject(PlaceLocation.class);
                                    placeLocation.setPlaceId(querySnapshot.getId());

                                    LatLng placeLatLng = LatLng
                                            .newBuilder()
                                            .setLatitude(Double.parseDouble(placeLocation.getLatitude()))
                                            .setLongitude(Double.parseDouble(placeLocation.getLongitude()))
                                            .build();

                                    double placeDistance = calculateDistance(currentLocationLatLng, placeLatLng);

                                    if (placeDistance <= selectedRange) {
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
                            }
                        }
                    });


        }

    }

    // initialize recyclerview
    public void setAdapter(ArrayList<Place> nearByPlaceList) {
        gridPlaceAdapter = new GridPlaceAdapter(this, nearByPlaceList);

        binding.PlaceRecyclerView.setAdapter(gridPlaceAdapter);
        binding.PlaceRecyclerView.addItemDecoration(new GridSpaceItemDecoration(20, 26, 10, 0));

    }

    public double calculateDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.getLatitude();
        double lat2 = EndP.getLatitude();
        double lon1 = StartP.getLongitude();
        double lon2 = EndP.getLongitude();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    // show or hide enable location button based on location setting is on or off
    // get nearby places if location is enable
    public void locationSettingButtonVisibility() {
        if (gpsTracker.canGetLocation()) {
            binding.PlaceRecyclerView.setVisibility(View.VISIBLE);
            binding.rangeLayout.setVisibility(View.VISIBLE);
            binding.locationSettingBtn.setVisibility(View.GONE);
        } else {
            binding.PlaceRecyclerView.setVisibility(View.GONE);
            binding.rangeLayout.setVisibility(View.GONE);
            binding.locationSettingBtn.setVisibility(View.VISIBLE);

        }
    }

    // when user lick on enable location button, open location setting to on or off location
    public void locationSetting() {
        binding.locationSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker.openLocationSetting();
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

    //show or hide button which navigate user to settings page for allowing location permission
    public void locationPermissionButtonVisibility() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            binding.PlaceRecyclerView.setVisibility(View.GONE);
            binding.rangeLayout.setVisibility(View.GONE);
            binding.locationPermissionBtn.setVisibility(View.VISIBLE);
            binding.locationPermissionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openLocationPermission();
                }
            });
        } else {
            binding.locationPermissionBtn.setVisibility(View.GONE);
            binding.PlaceRecyclerView.setVisibility(View.VISIBLE);
            binding.rangeLayout.setVisibility(View.VISIBLE);

        }
    }

    // app detail settings page are open, where we navigate to permissions page
    public void openLocationPermission() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
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
}