package com.example.smartcitytravel.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.smartcitytravel.R;

public class PreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.fragment_preference, rootKey);

        deleteAccount();
    }

    // when user click on delete account preference, dialog show to confirm delete account
    public void deleteAccount() {
        Preference deleteAccountPreference = findPreference("delete_account");
        deleteAccountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                showDeleteAccountDialog("Delete Account",
                        "Do you want to permanently delete your account?");
                return true;
            }
        });
    }

    //create layout of dialog and set title and message in dialog textview
    public View createDeleteAccountLayout(String title, String message) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);

        TextView titleTxt = dialogView.findViewById(R.id.titleTxt);
        titleTxt.setText(title);

        TextView messageTxt = dialogView.findViewById(R.id.messageTxt);
        messageTxt.setText(message);

        return dialogView;
    }

    //create and show delete account dialog
    public void showDeleteAccountDialog(String title, String message) {
        View dialogView = createDeleteAccountLayout(title, message);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
                        Toast.makeText(requireContext(), "DELETE", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
    }
}