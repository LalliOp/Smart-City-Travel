package com.example.smartcitytravel.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartcitytravel.DataModel.Message;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.RecyclerView.ChatAdapter;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;
import com.example.smartcitytravel.databinding.ActivityLiveChatBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class LiveChatActivity extends AppCompatActivity {
    private ActivityLiveChatBinding binding;
    private ArrayList<Message> messageList;
    private DatabaseReference reference;
    private PreferenceHandler preferenceHandler;
    private User user;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        setToolBarTheme();

        getChat();
        sendMessage();

    }

    public void initialize() {
        messageList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Group Chat");
        util = new Util();
        preferenceHandler = new PreferenceHandler();
        user = preferenceHandler.getLoggedInAccountPreference(this);
    }

    public void setToolBarTheme() {
        util.setStatusBarColor(this, R.color.theme_light);
        util.addToolbar(this, binding.toolbarLayout.getRoot(), "Group Chat");
    }

    private void sendMessage() {
        binding.sendMessageImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = binding.enterMessageEdit.getText().toString();
                if (!messageText.isEmpty()) {

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm aa", Locale.getDefault());
                    String currentTime = timeFormat.format(new Date());
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
                    String currentDate = dateFormat.format(new Date());

                    String id = reference.push().getKey();
                    Message message = new Message(id, user.getUserId(), user.getName(), messageText, currentDate, currentTime);
                    reference.child(id).setValue(message).addOnCompleteListener(task -> {
                        binding.enterMessageEdit.setText("");
                    });
                }
            }
        });

    }

    private void getChat() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Chat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messageList.add(message);
                }
                setAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.chatLoadingBar.setVisibility(View.GONE);
            }
        });
    }

    private void setAdapter() {
        ChatAdapter chatAdapter = new ChatAdapter(this, messageList);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        binding.chatLoadingBar.setVisibility(View.GONE);
    }


}
