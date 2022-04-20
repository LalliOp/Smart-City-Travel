package com.example.smartcitytravel.Activities.LiveChat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.Connection;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityLiveChatBinding;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.handlers.InitResultHandler;
import com.sendbird.uikit.SendBirdUIKit;
import com.sendbird.uikit.adapter.SendBirdUIKitAdapter;
import com.sendbird.uikit.fragments.OpenChannelFragment;
import com.sendbird.uikit.interfaces.UserInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveChatActivity extends AppCompatActivity {
    private ActivityLiveChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PreferenceHandler preferenceHandler = new PreferenceHandler();
        com.example.smartcitytravel.DataModel.User user = preferenceHandler.getLoginAccountPreference(LiveChatActivity.this);
        Util util = new Util();

        checkConnectionAndCreateLiveChat(util, user);

    }

    //check internet connection and create live chat
    public void checkConnectionAndCreateLiveChat(Util util, com.example.smartcitytravel.DataModel.User user) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Connection connection = new Connection();
                Boolean connectionAvailable = connection.isConnectionSourceAndInternetAvailable(LiveChatActivity.this);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initLiveChat(util, user);
                        if (connectionAvailable) {
                            initLiveChat(util, user);
                        } else {
                            binding.chatLoadingBar.setVisibility(View.GONE);
                            showNoConnectionDialog("Connection Issue", "No Internet Connection");
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    // initialize live chat
    public void initLiveChat(Util util, com.example.smartcitytravel.DataModel.User user) {

        SendBirdUIKit.init(new SendBirdUIKitAdapter() {
            @Override
            public String getAppId() {
                return getString(R.string.SENDBIRD_APP_ID);
            }

            @Override
            public String getAccessToken() {
                return "";
            }

            @Override
            public UserInfo getUserInfo() {
                return new UserInfo() {
                    @Override
                    public String getUserId() {
                        return String.valueOf(user.getUserId());
                    }

                    @Override
                    public String getNickname() {
                        return util.capitalizedName(user.getName());
                    }

                    @Override
                    public String getProfileUrl() {
                        return user.getImage_url();
                    }
                };
            }

            @Override
            public InitResultHandler getInitResultHandler() {
                return new InitResultHandler() {
                    @Override
                    public void onMigrationStarted() {

                    }

                    @Override
                    public void onInitFailed(@NonNull SendBirdException e) {
                        Toast.makeText(LiveChatActivity.this, "Unable to connect to chat", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInitSucceed() {
                        connectUser();
                    }
                };
            }
        }, this);
    }

    // connect user to live chat server and create live chat
    public void connectUser() {
        SendBirdUIKit.connect(new SendBird.ConnectHandler() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e == null) {
                    OpenChannelFragment openChannelFragment = createLiveChatFragment(getString(R.string.lahore_channel));
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(binding.liveChatFragmentContainer.getId(), openChannelFragment)
                            .commit();

                    binding.chatLoadingBar.setVisibility(View.GONE);
                }
            }
        });
    }

    // create live chat
    public OpenChannelFragment createLiveChatFragment(@NonNull String channelUrl) {
        return new OpenChannelFragment.Builder(channelUrl)
                .setUseHeader(true)
                .setUseHeaderLeftButton(true)
                .setUseHeaderRightButton(true)
                .setUseUserProfile(true)
                .setUseMessageGroupUI(true)
                .build();
    }

    //create layout of dialog and set title and message in dialog textview
    public View createDialogLayout(String title, String message) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);

        TextView titleTxt = dialogView.findViewById(R.id.titleTxt);
        titleTxt.setText(title);

        TextView messageTxt = dialogView.findViewById(R.id.messageTxt);
        messageTxt.setText(message);

        return dialogView;
    }

    //create and show no connection dialog
    public void showNoConnectionDialog(String title, String message) {
        View dialogView = createDialogLayout(title, message);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setCancelable(false);
    }
}
