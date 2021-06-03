package com.annabohm.pracainzynierska;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        final Message message = messageList.get(position);
        holder.messageContentTextView.setText(message.getMessageContent());
        final DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        holder.messageCreatedAtTextView.setText(dateFormatterPrint.format(message.getCreatedAt()));
        holder.messageSenderTextView.setText(message.getMessageSenderName());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
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
        ChatAdapter.ItemClickListener itemClickListener;
        public TextView messageContentTextView;
        public TextView messageSenderTextView;
        public TextView messageCreatedAtTextView;

        public MessageItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            messageContentTextView = itemView.findViewById(R.id.messageContentTextView);
            messageSenderTextView = itemView.findViewById(R.id.messageSenderTextView);
            messageCreatedAtTextView = itemView.findViewById(R.id.messageCreatedAtTextView);
        }

        public void setItemClickListener(ChatAdapter.ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(R.string.message_select_action);
            menu.add(0, 0, getAdapterPosition(), R.string.message_delete);
            menu.add(0, 0, getAdapterPosition(), R.string.message_cancel);
        }
    }
}
