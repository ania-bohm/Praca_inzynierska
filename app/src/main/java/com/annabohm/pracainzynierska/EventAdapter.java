package com.annabohm.pracainzynierska;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class EventAdapter extends FirestoreRecyclerAdapter<Event, EventAdapter.EventHolder> {
    OnItemClickListener listener;

    public EventAdapter(@NonNull FirestoreRecyclerOptions<Event> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull EventHolder holder, int position, @NonNull Event model) {
        Date eventDateStart = model.getEventDateStart();
        Date eventTimeStart = model.getEventTimeStart();
        Date eventTimeFinish = model.getEventTimeFinish();
        DateFormat dateFormatterRead = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");

        try {
            eventDateStart = dateFormatterRead.parse(eventDateStart.toString());
            eventTimeStart = dateFormatterRead.parse(eventTimeStart.toString());
            eventTimeFinish = dateFormatterRead.parse(eventTimeFinish.toString());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            Log.i(TAG, e.toString());
        }
        holder.miniEventLinearLayout.setBackgroundResource(model.getEventImage());
        holder.miniEventNameTextView.setText(model.getEventName());
        holder.miniEventDateStartTextView.setText(dateFormatterPrint.format(eventDateStart));
        holder.miniEventTimeStartTextView.setText(timeFormatterPrint.format(eventTimeStart));
        holder.miniEventTimeFinishTextView.setText(timeFormatterPrint.format(eventTimeFinish));
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mini_event, parent, false);
        return new EventHolder(view);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    class EventHolder extends RecyclerView.ViewHolder {
        LinearLayout miniEventLinearLayout;
        TextView miniEventNameTextView;
        TextView miniEventDateStartTextView;
        TextView miniEventTimeStartTextView;
        TextView miniEventTimeFinishTextView;

        public EventHolder(@NonNull View itemView) {
            super(itemView);
            miniEventLinearLayout = itemView.findViewById(R.id.miniEventLinearLayout);
            miniEventNameTextView = itemView.findViewById(R.id.miniEventNameTextView);
            miniEventDateStartTextView = itemView.findViewById(R.id.miniEventDateStartTextView);
            miniEventTimeStartTextView = itemView.findViewById(R.id.miniEventTimeStartTextView);
            miniEventTimeFinishTextView = itemView.findViewById(R.id.miniEventTimeFinishTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }
}
