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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.smartcitytravel.DataModel.Message;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.ChatAdapter;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityLiveChatBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LiveChatActivity extends AppCompatActivity {
    private ActivityLiveChatBinding binding;
    private PreferenceHandler preferenceHandler;
    private FirebaseFirestore db;
    private boolean locationPermissionAllowed;
    private LocationManager locationManager;
    private Location currentLocation;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;
    private static final long MIN_TIME_BTW_UPDATES = 60000;
    private User user;
    private Util util;
    private String city;
    private ChatAdapter chatAdapter;
    private boolean initializedChat;
    private boolean registerChatUpdate;
    private Connection connection;
    private ArrayList<Message> errorMessageList;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            currentLocation = location;
            getChatUpdates();
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            if (locationPermissionAllowed) {
                hideLocationSettingsButton();
                if (binding.chatRecyclerView.getVisibility() == View.GONE) {
                    binding.chatRecyclerView.setVisibility(View.VISIBLE);
                    binding.messageLayout.setVisibility(View.VISIBLE);
                    binding.loadingBar.setVisibility(View.VISIBLE);
                }
            }

            LocationListener.super.onProviderEnabled(provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            if (locationPermissionAllowed && currentLocation == null) {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setToolBarTheme();
        requestLocationPermission();
        clickSendMessage();
        openLocationSetting();
    }

    @Override
    protected void onResume() {
        checkLocationPermission();
        registerForLocationUpdates();
        getCurrentLocation();
        getChatUpdates();
        super.onResume();
    }

    public void initialize() {
        util = new Util();
        preferenceHandler = new PreferenceHandler();
        user = preferenceHandler.getLoggedInAccountPreference(this);
        db = FirebaseFirestore.getInstance();
        initializedChat = false;
        locationPermissionAllowed = false;
        registerChatUpdate = false;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        connection = new Connection();
        errorMessageList = new ArrayList<>();
    }

    // style and customize status bar and toolbar
    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.getRoot(), "Group Chat");
    }

    //set city name as title
    void setToolbarTitle(String title) {
        if (city != null) {
            getSupportActionBar().setTitle(title);
        }

    }

    // call when user click on send message button
    private void clickSendMessage() {
        binding.sendMessageImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationPermissionAllowed) {
                    String messageText = binding.enterMessageEdit.getText().toString();
                    if (!messageText.isEmpty()) {
                        checkConnectionAndSendMessage(messageText);
                    }
                }
            }
        });

    }

    //create message object
    public Message createMessage(String messageText) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        return new Message(user.getUserId(), user.getName(), messageText, currentDate, currentTime, city);
    }

    // check connection and send message
    public void checkConnectionAndSendMessage(String messageText) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(LiveChatActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Message message = createMessage(messageText);
                        binding.enterMessageEdit.setText("");
                        if (internetAvailable) {
                            uploadNewMessage(message);
                        } else {
                            addErrorMessageInChatAdapter(message);
                            uploadErrorMessageListScheduler();
                        }
                    }
                });

            }
        });
        executor.shutdown();
    }

    // upload new message in database
    public void uploadNewMessage(Message message) {
        message.setError(false);
        db
                .collection("group_chat")
                .add(message);
    }

    // show error message into chat adapter
    public void addErrorMessageInChatAdapter(Message message) {
        message.setError(true);
        errorMessageList.add(message);

        if (chatAdapter != null) {
            chatAdapter.addSingleData(message);
        } else {
            setAdapter(errorMessageList);
        }
        binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.getData().size());
        Toast.makeText(LiveChatActivity.this, "Unable to send message", Toast.LENGTH_SHORT).show();
    }

    //upload error message to database
    // run after every 10 seconds till messages uploaded
    public void uploadErrorMessageListScheduler() {
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                boolean internetAvailable = connection.isConnectionSourceAndInternetAvailable(LiveChatActivity.this);
                if (internetAvailable) {
                    if (errorMessageList.size() >= 1) {
                        for (int index = 0; index < errorMessageList.size(); index++) {
                            db.collection("group_chat")
                                    .add(errorMessageList.get(index))
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            if (documentReference != null) {
                                                chatAdapter.removeErrorData();
                                                binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.getData().size());
                                            }

                                        }
                                    });
                        }
                        errorMessageList.clear();
                    }
                }
                if (errorMessageList.size() == 0) {
                    scheduledExecutor.shutdown();
                }
            }
        }, 0, 10, TimeUnit.SECONDS);


    }

    //whenever data changes in database. It will call
    private void getChatUpdates() {
        getCityName();
        setToolbarTitle(city + " Chat");
        if (locationPermissionAllowed && city != null && !registerChatUpdate) {
            binding.loadingBar.setVisibility(View.VISIBLE);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            db.collection("group_chat")
                    .whereEqualTo("city", city)
                    .whereEqualTo("date", currentDate)
                    .orderBy("time")
                    .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (value != null) {
                                ArrayList<Message> messageList = new ArrayList<>();

                                if (!initializedChat) {
                                    initializedChat = true;
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                                        Message message = queryDocumentSnapshot.toObject(Message.class);
                                        messageList.add(message);
                                    }
                                    setAdapter(messageList);
                                } else {
                                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                            Message message = documentChange.getDocument().toObject(Message.class);
                                            messageList.add(message);
                                        }

                                    }
                                    chatAdapter.addData(messageList);
                                    binding.chatRecyclerView.smoothScrollToPosition(chatAdapter.getData().size());

                                }
                                registerChatUpdate = true;
                                binding.loadingBar.setVisibility(View.GONE);
                            }

                        }
                    });
        }
    }

    // set recyclerview with messageList
    private void setAdapter(ArrayList<Message> messageList) {
        chatAdapter = new ChatAdapter(this, messageList);
        binding.chatRecyclerView.setAdapter(chatAdapter);
    }

    // get current location
    @SuppressLint("MissingPermission")
    public void getCurrentLocation() {
        if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
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
            hideLocationPermissionButton();
        } else {
            locationPermissionAllowed = false;
            showLocationPermissionButton();
        }
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
        binding.chatRecyclerView.setVisibility(View.GONE);
        binding.messageLayout.setVisibility(View.GONE);
        binding.locationSettingBtn.setVisibility(View.VISIBLE);
    }

    //hide when location settings is on
    void hideLocationSettingsButton() {
        binding.locationSettingBtn.setVisibility(View.GONE);
        binding.messageLayout.setVisibility(View.VISIBLE);
    }

    // show when location permission is denied
    // it show button which navigate to settings page which can navigate to permission page
    public void showLocationPermissionButton() {
        binding.chatRecyclerView.setVisibility(View.GONE);
        binding.messageLayout.setVisibility(View.GONE);
        binding.locationPermissionBtn.setVisibility(View.VISIBLE);
        binding.locationPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLocationPermission();
            }
        });
    }

    //get the name of current location city
    public void getCityName() {
        if (currentLocation != null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {

                ArrayList<Address> cityList = (ArrayList<Address>) geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                if (cityList.get(0).getLocality() != null) {
                    city = cityList.get(0).getLocality();
                }
            } catch (IOException ignored) {
            }
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

    // register to get current location
    @SuppressLint("MissingPermission")
    public void registerForLocationUpdates() {
        if (locationPermissionAllowed) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BTW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BTW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
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
