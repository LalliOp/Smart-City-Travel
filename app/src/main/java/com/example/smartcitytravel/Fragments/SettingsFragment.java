package com.example.smartcitytravel.Fragments;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smartcitytravel.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey);
        Preference deleteAccountPreference=findPreference("delete_account");
        deleteAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                Toast.makeText(getActivity(), "PREFERENCE", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}