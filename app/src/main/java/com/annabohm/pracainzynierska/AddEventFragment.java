package com.annabohm.pracainzynierska;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class AddEventFragment extends Fragment {

    static final String KEY_EVENT_NAME = "event_name";
    static final String KEY_EVENT_DATE_START = "event_date_start";
    static final String KEY_EVENT_TIME_START = "event_time_start";
    static final String KEY_EVENT_DATE_FINISH = "event_date_finish";
    static final String KEY_EVENT_TIME_FINISH = "event_time_finish";
    static final String KEY_EVENT_DESCRIPTION = "event_description";
    NavController navController;
    Button eventReadyButton;
    Button eventCancelButton;
    Button eventImageButton;
    Context context;
    EditText eventNameEditText;
    EditText eventDateStartEditText;
    EditText eventTimeStartEditText;
    EditText eventDateFinishEditText;
    EditText eventTimeFinishEditText;
    EditText eventLocationEditText;
    EditText eventDescriptionEditText;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private View.OnClickListener eventReadyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String eventName = eventNameEditText.getText().toString();
            String eventDescription = eventDescriptionEditText.getText().toString();
            String eventLocation = eventLocationEditText.getText().toString();
            String dateStartValue = eventDateStartEditText.getText().toString();
            String dateFinishValue = eventDateFinishEditText.getText().toString();
            String timeStartValue = eventTimeStartEditText.getText().toString();
            String timeFinishValue = eventTimeFinishEditText.getText().toString();
            if (eventName.trim().isEmpty() || eventDescription.trim().isEmpty() || dateStartValue.trim().isEmpty() || dateFinishValue.trim().isEmpty() || timeStartValue.trim().isEmpty() || timeFinishValue.trim().isEmpty()) {
                Toast.makeText(context, "Please fill all the fields!", Toast.LENGTH_SHORT).show();
                return;
            }
            //
            DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat timeFormatter = new SimpleDateFormat("HH:mm");
            Date eventDateStart = null;
            Date eventDateFinish = null;
            Date eventTimeStart = null;
            Date eventTimeFinish = null;

            try {
                eventDateStart = dateFormatter.parse(dateStartValue);
                eventDateFinish = dateFormatter.parse(dateFinishValue);
                eventTimeStart = timeFormatter.parse(timeStartValue);
                eventTimeFinish = timeFormatter.parse(timeFinishValue);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
                Log.i(TAG, e.toString());
            }
            //
//            Map<String, Object> eventMap = new HashMap<>();
//            eventMap.put(KEY_EVENT_NAME, eventName);
//            eventMap.put(KEY_EVENT_DATE_START, eventDateStart);
//            eventMap.put(KEY_EVENT_TIME_START, eventTimeStart);
//            eventMap.put(KEY_EVENT_DATE_FINISH, eventDateFinish);
//            eventMap.put(KEY_EVENT_TIME_FINISH, eventTimeFinish);
//            eventMap.put(KEY_EVENT_DESCRIPTION, eventDescription);
            Event event = new Event(eventName, eventDateStart, eventTimeStart, eventDateFinish, eventTimeFinish, eventLocation, eventDescription);

            //alternatively - db.document("Events/My first event!");
            //String.valueOf(System.currentTimeMillis()) - as documentPath
            //db.collection("Events").add(eventMap);
            db.collection("Events").add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(context, "Event saved successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Event save failed!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                }
            });

//            db.collection("Events").document("My first event!").set(event).addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    Toast.makeText(context, "Event saved successfully", Toast.LENGTH_SHORT).show();
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(context, "Event save failed!", Toast.LENGTH_SHORT).show();
//                    Log.d(TAG, e.toString());
//                }
//            });
            navController.popBackStack();
        }
    };
    private View.OnClickListener eventCancelOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };

    public AddEventFragment() {
        // Required empty public constructor
    }

    public static AddEventFragment newInstance(String param1, String param2) {
        AddEventFragment fragment = new AddEventFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setDrawerLocked();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        eventReadyButton = view.findViewById(R.id.eventReadyButton);
        eventCancelButton = view.findViewById(R.id.eventCancelButton);
        eventImageButton = view.findViewById(R.id.eventImageButton);
        eventReadyButton.setOnClickListener(eventReadyOnClickListener);
        eventCancelButton.setOnClickListener(eventCancelOnClickListener);
        eventNameEditText = view.findViewById(R.id.eventNameEditText);
        eventDateStartEditText = view.findViewById(R.id.eventDateStartEditText);
        eventTimeStartEditText = view.findViewById(R.id.eventTimeStartEditText);
        eventDateFinishEditText = view.findViewById(R.id.eventDateFinishEditText);
        eventTimeFinishEditText = view.findViewById(R.id.eventTimeFinishEditText);
        eventLocationEditText = view.findViewById(R.id.eventLocationEditText);
        eventDescriptionEditText = view.findViewById(R.id.eventDescriptionEditText);
    }
}