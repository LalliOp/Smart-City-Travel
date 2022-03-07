package com.example.smartcitytravel.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartcitytravel.Login.LoginActivity;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.databinding.FragmentHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private PreferenceHandler preferenceHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceHandler = new PreferenceHandler();

        logout();
    }

    //make binding null which garbage collector auto collect and remove binding object with end of fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void logout() {
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!preferenceHandler.getLoginEmailPreference(getActivity()).isEmpty()) {
                    preferenceHandler.setLoginEmailPreference("", getActivity());

                    moveToLoginActivity();
                } else {
                    GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build();

                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), googleSignInOptions);

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
        });
    }

    //Move Home Fragment to Login Activity
    public void moveToLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }


}