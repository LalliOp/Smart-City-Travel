package com.example.smartcitytravel.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcitytravel.DataModel.Message;
import com.example.smartcitytravel.DataModel.User;
import com.example.smartcitytravel.R;
import com.example.smartcitytravel.Util.PreferenceHandler;
import com.example.smartcitytravel.Util.Util;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    static final int RECEIVE_MESSAGE_TYPE = 0;
    static final int SENDER_MESSAGE_TYPE = 1;
    private Context context;
    private ArrayList<Message> messageList;
    private PreferenceHandler preferenceHandler;
    private User user;
    private Util util;

    public ChatAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;

        preferenceHandler = new PreferenceHandler();
        user = preferenceHandler.getLoggedInAccountPreference(context);
        util = new Util();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_MESSAGE_TYPE) {
            return (new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_send, parent, false)));
        } else {
            return (new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_receive, parent, false)));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.userNameTxt.setText(util.capitalizedName(messageList.get(position).getSenderName()));
        holder.messageTxt.setText(messageList.get(position).getMessage());
        holder.timeTxt.setText(messageList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageTxt, userNameTxt, timeTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.userNameTxt);
            messageTxt = itemView.findViewById(R.id.messageTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (messageList.get(position).getSenderID().equals(user
                .getUserId())) {
            return SENDER_MESSAGE_TYPE;
        } else {
            return RECEIVE_MESSAGE_TYPE;

        }
    }
}
