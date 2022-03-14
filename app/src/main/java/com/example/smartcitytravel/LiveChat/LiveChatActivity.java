package com.example.smartcitytravel.LiveChat;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.databinding.ActivityLiveChatBinding;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.fragments.OpenChannelFragment;

public class LiveChatActivity extends AppCompatActivity {
    private ActivityLiveChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SendBirdUIKit.connect(new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e == null) {
                    Toast.makeText(LiveChatActivity.this, user.getUserId() + " " + user.getNickname(), Toast.LENGTH_LONG).show();
                    OpenChannelFragment openChannelFragment = createOpenChannelFragment(
                            "sendbird_open_channel_15294_4dffc38743c22483aefb1ea23d83a4b9aaca5915");
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(binding.fragmentContainer.getId(), openChannelFragment)
                            .commit();

                    binding.chatLoadingBar.setVisibility(View.GONE);

                } else {
                    Toast.makeText(LiveChatActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    protected OpenChannelFragment createOpenChannelFragment(@NonNull String channelUrl) {
        return new OpenChannelFragment.Builder(channelUrl)
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(false)
                .build();
    }

}