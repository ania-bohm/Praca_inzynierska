package com.annabohm.pracainzynierska;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageItemHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    Context context;
    ArrayList<Message> messageList;
    String currentUserId;

    public ChatAdapter(Context context, ArrayList<Message> messageList, String currentUserId){
        this.messageList = messageList;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.message_item_right, parent, false);
            return new ChatAdapter.MessageItemHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.message_item_left, parent, false);
            return new ChatAdapter.MessageItemHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageItemHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageContentTextView.setText(message.getMessageContent());
        holder.messageCreatedAtTextView.setText(message.getCreatedAt().toString());
        holder.messageSenderTextView.setText(message.getMessageSenderName());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
       if(currentUserId.equals(messageList.get(position).getMessageSenderId())){
           return MSG_TYPE_RIGHT;
       } else {
           return MSG_TYPE_LEFT;
       }
    }

    public static class MessageItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        public TextView messageContentTextView;
        public TextView messageSenderTextView;
        public TextView messageCreatedAtTextView;

        public MessageItemHolder(@NonNull View itemView) {
            super(itemView);

            messageContentTextView = itemView.findViewById(R.id.messageContentTextView);
            messageSenderTextView = itemView.findViewById(R.id.messageSenderTextView);
            messageCreatedAtTextView = itemView.findViewById(R.id.messageCreatedAtTextView);
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        }
    }
}
