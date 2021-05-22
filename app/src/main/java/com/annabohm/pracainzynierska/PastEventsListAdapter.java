package com.annabohm.pracainzynierska;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class PastEventsListAdapter extends ArrayAdapter<Event> implements View.OnClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    NavController navController;
    private ArrayList<Event> eventList;
    private ArrayList<String> eventIdList;
    private Context context;

    public PastEventsListAdapter(@NonNull Context context, @NonNull List<Event> eventList, List<String> eventIdList, NavController navController) {
        super(context, R.layout.past_event_list_item, eventList);
        this.eventList = (ArrayList) eventList;
        this.context = context;
        this.eventIdList = (ArrayList) eventIdList;
        this.navController = navController;
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Event event = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.past_event_list_item, parent, false);
        }
        TextView pastEventNameTextView = convertView.findViewById(R.id.pastEventNameTextView);
        TextView pastEventDateStartTextView = convertView.findViewById(R.id.pastEventDateStartTextView);
        TextView pastEventTimeStartTextView = convertView.findViewById(R.id.pastEventTimeStartTextView);
        TextView pastEventDateFinishTextView = convertView.findViewById(R.id.pastEventDateFinishTextView);
        TextView pastEventTimeFinishTextView = convertView.findViewById(R.id.pastEventTimeFinishTextView);
        LinearLayout pastEventLinearLayout = convertView.findViewById(R.id.pastEventLinearLayout);

        Date eventDateStart = event.getEventDateStart();
        Date eventTimeStart = event.getEventTimeStart();
        Date eventDateFinish = event.getEventDateFinish();
        Date eventTimeFinish = event.getEventTimeFinish();
        DateFormat dateFormatterRead = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");

        try {
            eventDateStart = dateFormatterRead.parse(eventDateStart.toString());
            eventTimeStart = dateFormatterRead.parse(eventTimeStart.toString());
            eventDateFinish = dateFormatterRead.parse(eventDateFinish.toString());
            eventTimeFinish = dateFormatterRead.parse(eventTimeFinish.toString());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            Log.i(TAG, e.toString());
        }

        pastEventNameTextView.setText(event.getEventName());
        pastEventDateStartTextView.setText(dateFormatterPrint.format(eventDateStart));
        pastEventTimeStartTextView.setText(timeFormatterPrint.format(eventTimeStart));
        pastEventDateFinishTextView.setText(dateFormatterPrint.format(eventDateFinish));
        pastEventTimeFinishTextView.setText(timeFormatterPrint.format(eventTimeFinish));

        pastEventLinearLayout.setLongClickable(true);

        pastEventLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventId = eventIdList.get(position);
                events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String path = documentSnapshot.getReference().getPath();
                            Bundle bundle = new Bundle();
                            bundle.putString("path", path);
                            navController.navigate(R.id.pastEventsToDisplayEvent, bundle);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });
            }
        });

        return convertView;
    }

    public void setEventList(ArrayList<Event> eventList) {
        this.eventList.clear();
        for (Event event : eventList) {
            this.eventList.add(event);
        }
    }

    public void setEventIdList(ArrayList<String> eventIdList) {
        this.eventIdList.clear();
        for (String eventId : eventIdList) {
            this.eventIdList.add(eventId);
        }
    }

    public void deleteItem(int position) {
        events.document(eventIdList.get(position)).delete();
    }

    @Override
    public void remove(@Nullable Event object) {
        super.remove(object);
    }

}
