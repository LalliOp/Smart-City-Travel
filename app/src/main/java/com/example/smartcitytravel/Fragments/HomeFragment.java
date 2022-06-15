package com.example.smartcitytravel.Fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartcitytravel.Activities.DestinationActivity;
import com.example.smartcitytravel.Activities.LiveChatActivity;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Util util;

    public HomeFragment() {
        util = new Util();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        util.setStatusBarColor(requireActivity(), R.color.black);
        setLoadingBarColor();
        openLiveChat();
        moveToDestinationActivity();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideLoadingBar();
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
                showLoadingBar();
                Intent intent = new Intent(getActivity(), DestinationActivity.class);
                startActivity(intent);
            }
        });
    }

    //change default loading bar color
    public void setLoadingBarColor() {
        binding.loadingProgressBar.loadingBar.setIndeterminateTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_orange_2)));
    }

    // show progress bar when user click on destination button
    public void showLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.VISIBLE);
        util.makeScreenNotTouchable(requireActivity());
    }

    //hide progressbar when move to destination activity
    public void hideLoadingBar() {
        binding.loadingProgressBar.loadingBarLayout.setVisibility(View.GONE);
        util.makeScreenTouchable(requireActivity());
    }

    //make binding null which garbage collector auto collect and remove binding object with end of fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}