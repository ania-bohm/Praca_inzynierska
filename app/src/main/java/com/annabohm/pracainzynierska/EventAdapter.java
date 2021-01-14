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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class EventAdapter extends FirestoreRecyclerAdapter<Event, EventAdapter.EventHolder> {

    public EventAdapter(@NonNull FirestoreRecyclerOptions<Event> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull EventHolder holder, int position, @NonNull Event model) {
        Date eventDateStart = model.getEventDateStart();
        Date eventTimeStart = model.getEventTimeStart();
        Date eventTimeFinish = model.getEventTimeFinish();
        DateFormat dateFormatterRead = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
        DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");

        try {
            eventDateStart = dateFormatterRead.parse(eventDateStart.toString());
            eventTimeStart = dateFormatterRead.parse(eventTimeStart.toString());
            eventTimeFinish = dateFormatterRead.parse(eventTimeFinish.toString());
        } catch (java.text.ParseException e)
        {
            e.printStackTrace();
            Log.i(TAG, e.toString());
        }
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

    class EventHolder extends RecyclerView.ViewHolder{
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
        }
    }
}
