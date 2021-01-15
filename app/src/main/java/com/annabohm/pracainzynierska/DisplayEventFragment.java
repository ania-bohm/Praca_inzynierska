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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class DisplayEventFragment extends Fragment {

    NavController navController;
    ImageView toDoListButton;
    ImageView shoppingListButton;
    ImageView editEventButton;
    Context context;
    TextView showEventNameTextView;
    TextView showEventDateStartTextView;
    TextView showEventTimeStartTextView;
    TextView showEventDateFinishTextView;
    TextView showEventTimeFinishTextView;
    TextView showEventDescriptionTextView;
    static final String KEY_EVENT_NAME = "event_name";
    static final String KEY_EVENT_DATE_START = "event_date_start";
    static final String KEY_EVENT_TIME_START = "event_time_start";
    static final String KEY_EVENT_DATE_FINISH = "event_date_finish";
    static final String KEY_EVENT_TIME_FINISH = "event_time_finish";
    static final String KEY_EVENT_DESCRIPTION = "event_description";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference = db.collection("Events").document("My first event!");
    CollectionReference collectionReference = db.collection("Events");

    public DisplayEventFragment() {
        // Required empty public constructor
    }

    public static DisplayEventFragment newInstance(String param1, String param2) {
        DisplayEventFragment fragment = new DisplayEventFragment();
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
        ((MainActivity)getActivity()).setDrawerLocked();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_event, container, false);
    }

    private View.OnClickListener toDoListOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToToDoList);
        }
    };

    private View.OnClickListener shoppingListOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToShoppingList);
        }
    };

    private View.OnClickListener editEventOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToEditEvent);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        toDoListButton = view.findViewById(R.id.toDoListButton);
        shoppingListButton = view.findViewById(R.id.shoppingListButton);
        editEventButton = view.findViewById(R.id.editEventButton);
        showEventNameTextView = view.findViewById(R.id.showEventNameTextView);
        showEventDateStartTextView = view.findViewById(R.id.showEventDateStartTextView);
        showEventTimeStartTextView = view.findViewById(R.id.showEventTimeStartTextView);
        showEventDateFinishTextView = view.findViewById(R.id.showEventDateFinishTextView);
        showEventTimeFinishTextView = view.findViewById(R.id.showEventTimeFinishTextView);
        showEventDescriptionTextView = view.findViewById(R.id.showEventDescriptionTextView);
        toDoListButton.setOnClickListener(toDoListOnClickListener);
        shoppingListButton.setOnClickListener(shoppingListOnClickListener);
        editEventButton.setOnClickListener(editEventOnClickListener);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Event event = new Event();
                if (documentSnapshot.exists()) {
                    event = documentSnapshot.toObject(Event.class);
//                    "Sat Jun 01 12:53:10 IST 2013";
//                    "Thu Jan 14 00:00:00 GMT+00:00 2021"
//                    "EE MMM dd HH:mm:ss z yyyy";
                    DateFormat dateFormatterRead = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                    DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
                    DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");
                    Date eventDateStart = null;
                    Date eventDateFinish = null;
                    Date eventTimeStart = null;
                    Date eventTimeFinish = null;

                    try {
//                        eventDateStart = dateFormatterRead.parse(documentSnapshot.getDate(KEY_EVENT_DATE_START).toString());
//                        eventDateFinish = dateFormatterRead.parse(documentSnapshot.getDate(KEY_EVENT_DATE_FINISH).toString());
//                        eventTimeStart = dateFormatterRead.parse(documentSnapshot.getDate(KEY_EVENT_TIME_START).toString());
//                        eventTimeFinish = dateFormatterRead.parse(documentSnapshot.getDate(KEY_EVENT_TIME_FINISH).toString());

                        eventDateStart = dateFormatterRead.parse(event.getEventDateStart().toString());
                        eventDateFinish = dateFormatterRead.parse(event.getEventDateFinish().toString());
                        eventTimeStart = dateFormatterRead.parse(event.getEventTimeStart().toString());
                        eventTimeFinish = dateFormatterRead.parse(event.getEventTimeFinish().toString());
                    } catch (ParseException e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, e.toString());
                    }

                    showEventNameTextView.setText(event.getEventName());
                    showEventDateStartTextView.setText(dateFormatterPrint.format(eventDateStart));
                    showEventTimeStartTextView.setText(timeFormatterPrint.format(eventTimeStart));
                    showEventDateFinishTextView.setText(dateFormatterPrint.format(eventDateFinish));
                    showEventTimeFinishTextView.setText(timeFormatterPrint.format(eventTimeFinish));
                    showEventDescriptionTextView.setText(event.getEventDescription());
                    //Map<String, Object> events = documentSnapshot.getData();
                } else {
                    Toast.makeText(context, "Document does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Reading data from Firestore failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}