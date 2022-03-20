package com.example.smartcitytravel.Activities.Home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.bumptech.glide.Glide;
import com.example.smartcitytravel.AWSService.DataModel.User;
import com.example.smartcitytravel.Activities.EditProfile.EditProfileActivity;
import com.example.smartcitytravel.Activities.Login.LoginActivity;
import com.example.smartcitytravel.Fragments.AboutUsFragment;
import com.example.smartcitytravel.Fragments.HomeFragment;
import com.example.smartcitytravel.Fragments.SettingsFragment;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Connection;
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
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setLoadingBarColor();
        createHomeFragment(savedInstanceState);
        navigationDrawerToggle();
        selectFragmentFromDrawer();
        editUserProfile();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setUserProfile();
    }

    //initialize variables
    public void initialize() {
        util = new Util();
        preferenceHandler = new PreferenceHandler();
        connection = new Connection();
    }

    // set name , email and image of user profile
    public void setUserProfile() {
        User user = preferenceHandler.getLoginAccountPreference(HomeActivity.this);

        View headerLayout = binding.navigationView.getHeaderView(0);

        TextView nameTxt = headerLayout.findViewById(R.id.profileNameTxt);
        nameTxt.setText(util.capitalizedName(user.getName()));

        TextView emailTxt = headerLayout.findViewById(R.id.profileEmailTxt);
        emailTxt.setText(user.getEmail());

        Glide.with(HomeActivity.this)
                .load(user.getImage_url())
                .into((ImageView) headerLayout.findViewById(R.id.profileImg));
    }

    //change fragment base on selected activity
    public void selectFragmentFromDrawer() {
        binding.navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.home_menu:
                                getSupportFragmentManager().beginTransaction()
                                        .setReorderingAllowed(true)
                                        .addToBackStack("home")
                                        .replace(binding.fragmentContainer.getId(), new HomeFragment())
                                        .commit();
                                break;
                            case R.id.settings_menu:
                                getSupportFragmentManager().beginTransaction()
                                        .setReorderingAllowed(true)
                                        .replace(binding.fragmentContainer.getId(), new SettingsFragment())
                                        .commit();
                                break;
                            case R.id.about_us_menu:
                                getSupportFragmentManager().beginTransaction()
                                        .setReorderingAllowed(true)
                                        .replace(binding.fragmentContainer.getId(), new AboutUsFragment())
                                        .commit();
                                break;
                            case R.id.logout_menu:
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
                    .addToBackStack("home")
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
        Integer account_type = preferenceHandler.getLoginAccountTypePreference(HomeActivity.this);
        if (account_type == 0) {
            preferenceHandler.clearLoginAccountPreference(HomeActivity.this);

            moveToLoginActivity();
        } else if (account_type == 1) {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
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
                Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
                startActivity(intent);

                binding.drawerLayout.closeDrawer(GravityCompat.START);

            }
        });

    }


}
