package com.example.smartcitytravel.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.smartcitytravel.Fragments.AboutUsFragment;
import com.example.smartcitytravel.Fragments.HomeFragment;
import com.example.smartcitytravel.Fragments.SettingsFragment;
import com.example.smartcitytravel.Login.LoginActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private PreferenceHandler preferenceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceHandler = new PreferenceHandler();

        createHomeFragment(savedInstanceState);
        navigationDrawerToggle();
        selectFragmentFromDrawer();

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
                                logout();
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

    //logout user from system whether google account or non google account
    public void logout() {
        if (!preferenceHandler.getLoginEmailPreference(HomeActivity.this).isEmpty()) {
            preferenceHandler.setLoginEmailPreference("", HomeActivity.this);

            moveToLoginActivity();
        } else {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(HomeActivity.this, googleSignInOptions);

            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        moveToLoginActivity();
                    }

                }
            });
        }
    }

    //Move Home Activity to Login Activity
    public void moveToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
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
}
