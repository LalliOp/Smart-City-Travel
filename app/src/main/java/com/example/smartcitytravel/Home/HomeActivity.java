package com.example.smartcitytravel.Home;

import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.example.smartcitytravel.Fragments.HomeFragment;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        createHomeFragment(savedInstanceState);
        navigationDrawer();


    }

    public void navigationDrawer() {
        setSupportActionBar(binding.toolbar.getRoot());

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.getRoot(),
                binding.toolbar.getRoot(), R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
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

    //create home fragment which is start screen in home activity
    public void createHomeFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            HomeFragment homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(binding.fragmentContainer.getId(), homeFragment)
                    .commit();
        }
    }
}
