package com.example.smartcitytravel.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartcitytravel.Activities.Destination.DestinationActivity;
import com.example.smartcitytravel.Activities.LiveChat.LiveChatActivity;
import com.example.smartcitytravel.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        openLiveChat();
        moveToDestinationActivity();
    }

    //make binding null which garbage collector auto collect and remove binding object with end of fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //run when user click on live chat image
    //open live chat activity
    public void openLiveChat() {
        binding.liveChatImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToLiveChatActivity();
            }
        });
    }

    //move from home fragment to live chat activity
    public void moveToLiveChatActivity() {
        Intent intent = new Intent(getActivity(), LiveChatActivity.class);
        startActivity(intent);
    }

    //when user click on destination button, open destination activity
    public void moveToDestinationActivity() {
        binding.destinationCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DestinationActivity.class);
                startActivity(intent);
            }
        });
    }
}