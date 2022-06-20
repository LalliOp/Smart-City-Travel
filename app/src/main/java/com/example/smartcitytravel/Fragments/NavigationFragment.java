package com.example.smartcitytravel.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartcitytravel.DataModel.PlaceDetail;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.GpsTracker;
import com.example.smartcitytravel.databinding.FragmentNavigationBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NavigationFragment extends Fragment implements OnMapReadyCallback {
    private FragmentNavigationBinding binding;
    private final PlaceDetail placeDetail;
    private final GpsTracker gpsTracker;
    private final LatLng currentLocationLatLng;
    private final LatLng placeLatLng;
    private final Context context;

    public NavigationFragment(Context context, PlaceDetail placeDetail) {
        this.context = context;
        this.placeDetail = placeDetail;
        this.gpsTracker = new GpsTracker(context);
        currentLocationLatLng = gpsTracker.getLocationLatLng();
        placeLatLng = new LatLng(Double.parseDouble(placeDetail.getLatitude()), Double.parseDouble(placeDetail.getLongitude()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNavigationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setMapFragment();
        binding.navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://maps.google.com/maps?daddr=" + placeDetail.getLatitude() + "," + placeDetail.getLongitude());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);

            }
        });


        super.onViewCreated(view, savedInstanceState);
    }

    //get map fragment and allow mao to attach with this fragment
    public void setMapFragment() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        configureMap(googleMap);
        setLocationOnMap(googleMap);

    }

    //set marker of location on map
    public void setLocationOnMap(GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(currentLocationLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.ic_my_location, 100, 100)))
                .title("Current Location"));

        googleMap.addMarker(new MarkerOptions()
                .position(placeLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.ic_destination_location, 100, 100)))
                .title(placeDetail.getName()));
    }

    @SuppressLint("PotentialBehaviorOverride")
    // create basic settings and options for map
    public void configureMap(GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10.7f));

        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    //use to resize the icon size
    public Bitmap resizeMapIcons(int res, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), res);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    //make binding null which garbage collector auto collect and remove binding object with end of fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}