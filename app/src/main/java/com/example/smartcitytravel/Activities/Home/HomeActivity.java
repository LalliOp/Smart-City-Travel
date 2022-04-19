package com.example.smartcitytravel.Activities.Home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.AWSService.DataModel.User;
import com.example.smartcitytravel.Activities.EditProfile.Broadcast.UpdateProfileImageBroadcast;
import com.example.smartcitytravel.Activities.EditProfile.Broadcast.UpdateProfileNameBroadcast;
import com.example.smartcitytravel.Activities.EditProfile.EditProfileActivity;
import com.example.smartcitytravel.Activities.Home.Fragments.AboutUsFragment;
import com.example.smartcitytravel.Activities.Home.Fragments.HomeFragment;
import com.example.smartcitytravel.Activities.Home.Fragments.SettingsFragment;
import com.example.smartcitytravel.Activities.Login.LoginActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private Util util;
    private PreferenceHandler preferenceHandler;
    private UpdateProfileImageBroadcast updateProfileImageBroadcast;
    private UpdateProfileNameBroadcast updateProfileNameBroadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        User user = preferenceHandler.getLoginAccountPreference(HomeActivity.this);
        registerUpdateProfileImageBroadcastReceiver();
        registerUpdateProfileNameBroadcastReceiver();
        setLoadingBarColor();
        setUserProfile(user);
        createHomeFragment(savedInstanceState);
        navigationDrawerToggle();
        selectFragmentFromDrawer();
        editUserProfile();

    }


    //initialize variables
    public void initialize() {
        util = new Util();
        preferenceHandler = new PreferenceHandler();
    }

    // register to listen for profile image broadcast
    public void registerUpdateProfileImageBroadcastReceiver() {
        updateProfileImageBroadcast = new UpdateProfileImageBroadcast(binding);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.smartcitytravel.UPDATE_PROFILE_IMAGE");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateProfileImageBroadcast, intentFilter);
    }

    // register to listen for profile name broadcast
    public void registerUpdateProfileNameBroadcastReceiver() {
        updateProfileNameBroadcast = new UpdateProfileNameBroadcast(binding);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.smartcitytravel.UPDATE_PROFILE_NAME");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateProfileNameBroadcast, intentFilter);
    }

    // set name , email and image of user profile
    public void setUserProfile(User user) {

        View headerLayout = binding.navigationView.getHeaderView(0);

        TextView nameTxt = headerLayout.findViewById(R.id.profileNameTxt);
        nameTxt.setText(util.capitalizedName(user.getName()));

        TextView emailTxt = headerLayout.findViewById(R.id.profileEmailTxt);
        emailTxt.setText(user.getEmail());

        Glide.with(HomeActivity.this)
                .load(user.getImage_url())
                .timeout(60000)
                .into((ImageView) headerLayout.findViewById(R.id.profileImg));
    }

    //change or select fragment from navigation drawer
    public void selectFragmentFromDrawer() {
        binding.navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.home_menu) {
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(binding.fragmentContainer.getId(), new HomeFragment())
                                    .commit();
                        } else if (item.getItemId() == R.id.settings_menu) {
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(binding.fragmentContainer.getId(), new SettingsFragment())
                                    .commit();
                        } else if (item.getItemId() == R.id.about_us_menu) {
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(binding.fragmentContainer.getId(), new AboutUsFragment())
                                    .commit();
                        } else if (item.getItemId() == R.id.logout_menu) {
                            showLogoutDialog("Logout", "Do you want to logout?");
                        }

                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });

    }

    //open and close navigation drawer
    public void navigationDrawerToggle() {
        binding.navigationDrawerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    //create home fragment which is start screen in home activity
    //and select as selected fragment in navigation drawer
    public void createHomeFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(binding.fragmentContainer.getId(), new HomeFragment())
                    .commit();
            binding.navigationView.setCheckedItem(R.id.home_menu);
        }
    }

    //override default back press behavior
    //if drawer is open and user press back, close the drawer
    //otherwise default behaviour
    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    //create layout of dialog and set title and message in dialog textview
    public View createLogoutLayout(String title, String message) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);

        TextView titleTxt = dialogView.findViewById(R.id.titleTxt);
        titleTxt.setText(title);

        TextView messageTxt = dialogView.findViewById(R.id.messageTxt);
        messageTxt.setText(message);

        return dialogView;
    }

    //create and show logout dialog
    public void showLogoutDialog(String title, String message) {
        View dialogView = createLogoutLayout(title, message);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showLoadingBar();
                        logout();

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
    }

    //change default loading bar color
    public void setLoadingBarColor() {
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2)));
    }

    // show progress bar
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(this);
    }

    //logout user from system whether google account or non google account
    public void logout() {
        Boolean account_type = preferenceHandler.getLoginAccountTypePreference(HomeActivity.this);
        if (!account_type) {
            preferenceHandler.clearLoginAccountPreference(HomeActivity.this);

            moveToLoginActivity();

        } else {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

            googleSignInClient.signOut().addOnCompleteListener(this,new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        moveToLoginActivity();
                    } else if (task.isCanceled()) {
                        Toast.makeText(HomeActivity.this, "Unable to logout", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            preferenceHandler.clearLoginAccountPreference(HomeActivity.this);
        }
    }

    //Move Home Activity to Login Activity
    public void moveToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    //move to edit user profile activity
    public void editUserProfile() {
        View headerLayout = binding.navigationView.getHeaderView(0);
        ShapeableImageView editProfileImg = headerLayout.findViewById(R.id.editProfileImg);

        editProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
                        startActivity(intent);
                    }
                }, 50);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }, 1000);

            }
        });

    }

    //remove broadcast receiver when activity is destroyed
    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(updateProfileImageBroadcast);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(updateProfileNameBroadcast);
        super.onDestroy();
    }

}
