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
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    CollectionReference events = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Events");
    NavController navController;
    private ArrayList<Event> eventList;
    private ArrayList<String> eventIdList;
    private Context context;
    DateHandler dateHandler = new DateHandler("dd/MM/yyyy", "HH:mm");

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

        pastEventNameTextView.setText(event.getEventName());
        assert eventDateStart != null;
        pastEventDateStartTextView.setText(dateHandler.convertDateToString(eventDateStart));
        assert eventTimeStart != null;
        pastEventTimeStartTextView.setText(dateHandler.convertTimeToString(eventTimeStart));
        assert eventDateFinish != null;
        pastEventDateFinishTextView.setText(dateHandler.convertDateToString(eventDateFinish));
        assert eventTimeFinish != null;
        pastEventTimeFinishTextView.setText(dateHandler.convertTimeToString(eventTimeFinish));

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
        this.eventList.addAll(eventList);
    }

    public void setEventIdList(ArrayList<String> eventIdList) {
        this.eventIdList.clear();
        this.eventIdList.addAll(eventIdList);
    }

    public void deleteItem(int position) {
        events.document(eventIdList.get(position)).delete();
    }

    @Override
    public void remove(@Nullable Event object) {
        super.remove(object);
    }

}
