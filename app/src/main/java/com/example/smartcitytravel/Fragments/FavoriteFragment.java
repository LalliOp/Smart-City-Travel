package com.example.smartcitytravel.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.smartcitytravel.Activities.GridSpaceItemDecoration;
import com.example.smartcitytravel.DataModel.Favorite;
import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.GridPlaceAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.FragmentFavoriteBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteFragment extends Fragment {
    private FragmentFavoriteBinding binding;
    private FirebaseFirestore db;
    private PreferenceHandler preferenceHandler;
    private String userId;
    private Util util;
    private GridPlaceAdapter placeRecyclerViewAdapter;
    private Connection connection;
    private boolean firstTime;

    public FavoriteFragment() {
        db = FirebaseFirestore.getInstance();
        preferenceHandler = new PreferenceHandler();
        util = new Util();
        connection = new Connection();
        firstTime = true;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userId = preferenceHandler.getUserIdPreference(requireContext());
        setToolBarTheme();
        checkConnectionAndGetFavoritePlaceList();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!firstTime) {
            getUpdatedFavoritePlaceList();
        }
        firstTime = false;
    }


    // style and customize toolbar and theme
    public void setToolBarTheme() {
        util.setStatusBarColor(requireActivity(), R.color.theme_light);
        util.addToolbarAndNoUpButton((AppCompatActivity) requireActivity(), binding.toolbarLayout.toolbar, "       Favorites");
    }

    // check connection exist or not. If exist then get favorite place list
    public void checkConnectionAndGetFavoritePlaceList() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(requireActivity());

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (internetAvailable) {
                            getFavoritePlaceList();
                        } else {
                            binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);
                            binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.VISIBLE);
                            retryConnection();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    //get user favorite places from database
    public void getFavoritePlaceList() {
        db.collection("favorite")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Favorite> favoriteList = new ArrayList<>();
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Favorite favorite = querySnapshot.toObject(Favorite.class);
                                favorite.setFavoriteId(querySnapshot.getId());

                                favoriteList.add(favorite);
                            }
                            getPlacesDetail(favoriteList);

                        } else {

                            binding.nothingToShowTxt.setVisibility(View.VISIBLE);
                            binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);

                        }
                    }
                });
    }

    // get detail of all favorite places
    public void getPlacesDetail(ArrayList<Favorite> favoriteList) {
        db.collection("place")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Place> placeList = new ArrayList<>();
                            for (Favorite favorite : favoriteList) {
                                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                    Place place = querySnapshot.toObject(Place.class);
                                    place.setPlaceId(querySnapshot.getId());

                                    if (favorite.getPlaceId().equals(place.getPlaceId())) {
                                        placeList.add(place);
                                        break;
                                    }
                                }
                            }

                            createRecyclerView(placeList);
                            binding.CheckConnectionLayout.loadingBar.setVisibility(View.GONE);

                        }

                    }
                });
    }

    //create recyclerview and show places
    public void createRecyclerView(ArrayList<Place> placeList) {
        placeRecyclerViewAdapter = new GridPlaceAdapter(requireContext(),
                placeList);

        binding.placeRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.placeRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2));
        binding.placeRecyclerView.addItemDecoration(new GridSpaceItemDecoration(20, 26, 10, 0));
    }

    //run when user click on retry icon
    public void retryConnection() {
        binding.CheckConnectionLayout.retryConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.CheckConnectionLayout.loadingBar.setVisibility(View.VISIBLE);
                binding.CheckConnectionLayout.noConnectionLayout.setVisibility(View.GONE);

                checkConnectionAndGetFavoritePlaceList();
            }
        });

    }

    //get updated favorite place list
    public void getUpdatedFavoritePlaceList() {
        db.collection("favorite")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Favorite> favoriteList = new ArrayList<>();
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Favorite favorite = querySnapshot.toObject(Favorite.class);
                                favorite.setFavoriteId(querySnapshot.getId());

                                favoriteList.add(favorite);
                            }
                            if (favoriteList.size() < placeRecyclerViewAdapter.getItemCount()) {
                                getUpdatePlacesDetail(favoriteList);
                            }

                        } else {
                            placeRecyclerViewAdapter.clearData();

                            binding.nothingToShowTxt.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    // get place detail of updated favorite places
    public void getUpdatePlacesDetail(ArrayList<Favorite> favoriteList) {
        db.collection("place")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Place> placeList = new ArrayList<>();
                            for (Favorite favorite : favoriteList) {
                                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                    Place place = querySnapshot.toObject(Place.class);
                                    place.setPlaceId(querySnapshot.getId());

                                    if (favorite.getPlaceId().equals(place.getPlaceId())) {
                                        placeList.add(place);
                                        break;
                                    }
                                }
                            }
                            placeRecyclerViewAdapter.removeData(placeList);

                        }

                    }
                });
    }

}