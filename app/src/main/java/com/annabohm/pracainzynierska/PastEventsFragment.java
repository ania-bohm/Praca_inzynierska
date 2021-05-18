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
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class PastEventsFragment extends Fragment {

    NavController navController;
    ListView pastEventsListListView;
    ArrayList<Event> eventList;
    ArrayList<String> eventIdList;
    PastEventsListAdapter pastEventsListAdapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    Context context;

    public PastEventsFragment() {
        // Required empty public constructor
    }

    public static PastEventsFragment newInstance(String param1, String param2) {
        PastEventsFragment fragment = new PastEventsFragment();
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
        return inflater.inflate(R.layout.fragment_past_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        pastEventsListListView = view.findViewById(R.id.pastEventsListListView);

        eventList = new ArrayList<>();
        eventIdList = new ArrayList<>();

        pastEventsListAdapter = new PastEventsListAdapter(context, eventList, eventIdList, navController);
        pastEventsListListView.setAdapter(pastEventsListAdapter);

        initialisePastEventsList();
    }

    public void initialisePastEventsList() {
        String authorId = firebaseAuth.getCurrentUser().getUid();
//        DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
//        DateFormat timeFormatter = new SimpleDateFormat("HH:mm");
//        Date eventDateFinish = new Date();
//        Date eventTimeFinish = new Date();
//
//        try {
//            eventDateFinish = dateFormatter.parse(eventDateFinish.toString());
//            eventTimeFinish = timeFormatter.parse(eventTimeFinish.toString());
//        } catch (java.text.ParseException e) {
//            e.printStackTrace();
//            Log.i(TAG, e.toString());
//        }
        events.whereEqualTo("eventAuthor", authorId).orderBy("eventDateFinish", Query.Direction.DESCENDING).orderBy("eventTimeFinish", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Event pastEvent = documentSnapshot.toObject(Event.class);
                        Date dateNow = new Date();
                        final DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");
                        String newString = timeFormatterPrint.format(pastEvent.getEventTimeFinish());
                        String newStringNow = timeFormatterPrint.format(dateNow);

                        int hourNow = Integer.parseInt(newStringNow.substring(0, 2));
                        int hour = Integer.parseInt(newString.substring(0, 2));
                        int minuteNow = Integer.parseInt(newStringNow.substring(3, 5));
                        int minute = Integer.parseInt(newString.substring(3, 5));

                        if (pastEvent.getEventDateFinish().before(new Date())) {
                            eventList.add(pastEvent);
                            eventIdList.add(documentSnapshot.getId());
                            pastEventsListAdapter.notifyDataSetChanged();
                        } else if (pastEvent.getEventDateFinish().equals(new Date())) {
                            if (hourNow == 0) {
                                if (hour == 0) {
                                    if (minute < minuteNow) {
                                        eventList.add(pastEvent);
                                        eventIdList.add(documentSnapshot.getId());
                                        pastEventsListAdapter.notifyDataSetChanged();
                                    }
                                }
                            } else if (hour == hourNow && minute < minuteNow) {
                                eventList.add(pastEvent);
                                eventIdList.add(documentSnapshot.getId());
                                pastEventsListAdapter.notifyDataSetChanged();
                            } else if (hour < hourNow) {
                                eventList.add(pastEvent);
                                eventIdList.add(documentSnapshot.getId());
                                pastEventsListAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }
}