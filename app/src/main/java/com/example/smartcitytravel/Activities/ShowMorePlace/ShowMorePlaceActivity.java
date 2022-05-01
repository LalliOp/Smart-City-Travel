package com.example.smartcitytravel.Activities.ShowMorePlace;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcitytravel.Activities.ItemDecoration.GridSpaceItemDecoration;
import com.example.smartcitytravel.DataModel.Place;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.ShowMorePlaceRecyclerViewAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityShowMorePlaceBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShowMorePlaceActivity extends AppCompatActivity {
    private ActivityShowMorePlaceBinding binding;
    private Util util;
    private ShowMorePlaceRecyclerViewAdapter placeRecyclerViewAdapter;
    private boolean loading;
    private DocumentSnapshot lastLoadedPlace;
    private String placeType;
    private int initialLoaded;
    private Connection connection;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowMorePlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setToolBarTheme();

        createRecyclerView();
        autoScrollToEnd();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        loading = false;
        lastLoadedPlace = null;
        initialLoaded = 0;
        placeType = getIntent().getExtras().getString("placeType");
        connection = new Connection();
        db = FirebaseFirestore.getInstance();

    }

    // style and customize toolbar and theme
    public void setToolBarTheme() {
        String title = getIntent().getExtras().getString("title");

        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.toolbar, title);
    }

    //create recyclerview and show places
    public void createRecyclerView() {
        ArrayList<Place> placeList = (ArrayList<Place>) getIntent().getExtras().getSerializable("placeList");

        placeRecyclerViewAdapter = new ShowMorePlaceRecyclerViewAdapter(this,
                placeList);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        binding.placeRecyclerView.setAdapter(placeRecyclerViewAdapter);
        binding.placeRecyclerView.setLayoutManager(gridLayoutManager);
        binding.placeRecyclerView.addItemDecoration(new GridSpaceItemDecoration(20, 26, 10, 0));
        binding.placeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (gridLayoutManager.findLastCompletelyVisibleItemPosition()
                        == placeRecyclerViewAdapter.getItemCount() - 1 && !loading && initialLoaded != -1) {
                    loading = true;
                    binding.loadMoreProgressBar.setVisibility(View.VISIBLE);
                    checkConnectionAndLoadPlaces();
                }
            }
        });

    }

    //check connection exist or not. If exist load places
    public void checkConnectionAndLoadPlaces() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(ShowMorePlaceActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (internetAvailable) {
                            if (initialLoaded == 1) {
                                loadMorePlaces();
                            } else if (initialLoaded == 0) {
                                startLoadPlaces();
                            }
                        } else {
                            Toast.makeText(ShowMorePlaceActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            binding.loadMoreNoConnectionImg.setVisibility(View.VISIBLE);
                            binding.loadMoreProgressBar.setVisibility(View.INVISIBLE);
                            retryConnection();
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    //run when user click on retry icon which show when system unable to load places
    public void retryConnection() {
        binding.loadMoreNoConnectionImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.loadMoreProgressBar.setVisibility(View.VISIBLE);
                binding.loadMoreNoConnectionImg.setVisibility(View.GONE);

                checkConnectionAndLoadPlaces();
            }
        });
    }

    // load places first time when recyclerview reach end
    public void startLoadPlaces() {
        db.collection("place")
                .whereEqualTo("Place_type", placeType)
                .orderBy(placeType)
                .limit(30)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Place> placeList = new ArrayList<>();

                            lastLoadedPlace = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Place place = querySnapshot.toObject(Place.class);
                                place.setPlaceId(querySnapshot.getId());

                                boolean differentPlace = true;
                                for (Place adapterPlace : placeRecyclerViewAdapter.getData()) {
                                    if (adapterPlace.getPlaceId().equals(place.getPlaceId())) {
                                        differentPlace = false;
                                        break;
                                    }
                                }
                                if (differentPlace) {
                                    placeList.add(place);
                                }
                            }

                            placeRecyclerViewAdapter.setData(placeList);
                            initialLoaded = 1;
                            loading = false;

                        } else {
                            initialLoaded = -1;
                        }
                        binding.loadMoreProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    //load more places when recyclerview reach end
    public void loadMorePlaces() {
        db.collection("place")
                .whereEqualTo("Place_type", placeType)
                .orderBy(placeType)
                .startAfter(lastLoadedPlace)
                .limit(30)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            ArrayList<Place> placeList = new ArrayList<>();

                            lastLoadedPlace = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Place place = querySnapshot.toObject(Place.class);
                                place.setPlaceId(querySnapshot.getId());

                                boolean differentPlace = true;
                                for (Place adapterPlace : placeRecyclerViewAdapter.getData()) {
                                    if (adapterPlace.getPlaceId().equals(place.getPlaceId())) {
                                        differentPlace = false;
                                        break;
                                    }
                                }
                                if (differentPlace) {
                                    placeList.add(place);
                                }
                            }

                            placeRecyclerViewAdapter.setData(placeList);
                            loading = false;
                        } else {
                            loading = true;
                        }
                        binding.loadMoreProgressBar.setVisibility(View.GONE);

                    }
                });
    }

    // move to end of page
    public void autoScrollToEnd() {
        binding.scrollView.post(new Runnable() {
            @Override
            public void run() {
                binding.scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    // back to previous activity user click on up button (which is back button on top life side)
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