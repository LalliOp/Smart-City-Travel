package com.example.smartcitytravel.Dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smartcitytravel.Login.LoginActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LogoutDialog extends DialogFragment {
    private String title;
    private String message;
    private View dialogView;
    private PreferenceHandler preferenceHandler;
    private ActivityHomeBinding binding;
    private Util util;

    public LogoutDialog(ActivityHomeBinding binding, String title, String message) {
        this.binding = binding;
        this.title = title;
        this.message = message;
        preferenceHandler = new PreferenceHandler();
        util = new Util();
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog, null);

        setTitle();
        setMessage();
        return createDialog();

    }

    public void setTitle() {
        TextView titleTxt = dialogView.findViewById(R.id.titleTxt);
        titleTxt.setText(title);
    }

    public void setMessage() {
        TextView messageTxt = dialogView.findViewById(R.id.messageTxt);
        messageTxt.setText(message);
    }

    public AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                        showLogoutLoadingBar();
                        logout();
                    }
                });
        return builder.create();
    }

    // show progress bar when user click on logout button
    public void showLogoutLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(requireActivity());
    }

    //logout user from system whether google account or non google account
    public void logout() {
        if (!preferenceHandler.getLoginEmailPreference(getContext()).isEmpty()) {
            preferenceHandler.clearLoginEmailPreference(getContext());

            moveToLoginActivity();
        } else {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getContext(), googleSignInOptions);

            googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        moveToLoginActivity();
                    } else if (task.isCanceled()) {
                        Toast.makeText(getContext(), "Unable to logout", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    //Move Home Activity to Login Activity
    public void moveToLoginActivity() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
    }

}
