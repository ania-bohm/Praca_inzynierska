package com.annabohm.pracainzynierska;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class PastEventsFragment extends Fragment {

    NavController navController;
    ListView pastEventsListListView;
    TextView pastEventsEmptyTextView;
    ArrayList<Event> eventList;
    ArrayList<String> eventIdList;
    PastEventsListAdapter pastEventsListAdapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference eventAttendees = db.collection("EventAttendees");
    CollectionReference attendeeEvents = db.collection("AttendeeEvents");
    ArrayList<String> attendeesToDeleteIdList = new ArrayList<>();
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
        setHasOptionsMenu(true);
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
        pastEventsEmptyTextView = view.findViewById(R.id.pastEventsEmptyTextView);

        pastEventsEmptyTextView.setVisibility(View.GONE);

        registerForContextMenu(pastEventsListListView);
        pastEventsListListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        eventList = new ArrayList<>();
        eventIdList = new ArrayList<>();

        pastEventsListAdapter = new PastEventsListAdapter(context, eventList, eventIdList, navController);
        pastEventsListListView.setAdapter(pastEventsListAdapter);

        initialisePastEventsList();
    }

    public void initialisePastEventsList() {
        String authorId = firebaseAuth.getCurrentUser().getUid();
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
                } else {
                    pastEventsEmptyTextView.setVisibility(View.VISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.floating_context_menu_event, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteEvent:
                new AlertDialog.Builder(context)
                        .setTitle("Usunięcie wydarzenia")
                        .setMessage("Czy na pewno chcesz usunąć wydarzenie?")
                        .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String eventIdToDelete = eventIdList.get(info.position);
                                getAttendeesToDeleteIdList(eventIdToDelete);
                                pastEventsListAdapter.deleteItem(info.position);
                                eventIdList.remove(info.position);
                                eventList.remove(info.position);
                                pastEventsListAdapter.notifyDataSetChanged();
                                Toast.makeText(context, "Usunięto wydarzenie", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setIcon(R.drawable.ic_decline)
                        .show();
                return true;
            case R.id.cancelDeleteEvent:
                Toast.makeText(context, "Cancelled deleting", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void getAttendeesToDeleteIdList(final String eventToDeleteId) {
        eventAttendees.document(eventToDeleteId).collection("Invited").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        eventAttendees.document(eventToDeleteId).collection("Invited").document(documentSnapshot.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    attendeesToDeleteIdList.add(documentSnapshot.get("User").toString());
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
                eventAttendees.document(eventToDeleteId).collection("Confirmed").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                eventAttendees.document(eventToDeleteId).collection("Confirmed").document(documentSnapshot.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            attendeesToDeleteIdList.add(documentSnapshot.get("User").toString());
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
                        eventAttendees.document(eventToDeleteId).collection("Declined").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        eventAttendees.document(eventToDeleteId).collection("Declined").document(documentSnapshot.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    attendeesToDeleteIdList.add(documentSnapshot.get("User").toString());
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
                                updateEventAttendees(eventToDeleteId);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    public void updateEventAttendees(final String eventToDeleteId) {
        eventAttendees.document(eventToDeleteId).collection("Invited").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.exists()) {
                            eventAttendees.document(eventToDeleteId).collection("Invited").document(documentSnapshot.getId()).delete();
                        }
                    }
                }
                eventAttendees.document(eventToDeleteId).collection("Confirmed").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                if (documentSnapshot.exists()) {
                                    eventAttendees.document(eventToDeleteId).collection("Confirmed").document(documentSnapshot.getId()).delete();
                                }
                            }
                        }
                        eventAttendees.document(eventToDeleteId).collection("Declined").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot.exists()) {
                                            eventAttendees.document(eventToDeleteId).collection("Declined").document(documentSnapshot.getId()).delete();
                                        }
                                    }
                                }
                                updateAttendeeEvents(eventToDeleteId);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    public void updateAttendeeEvents(final String eventToDeleteId) {
        for (final String userId : attendeesToDeleteIdList) {
            attendeeEvents.document(userId).collection("Invited").whereEqualTo("Event", eventToDeleteId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                attendeeEvents.document(userId).collection("Invited").document(documentSnapshot.getId()).delete();
                            }
                        }
                    } else {
                        attendeeEvents.document(userId).collection("Confirmed").whereEqualTo("Event", eventToDeleteId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot.exists()) {
                                            attendeeEvents.document(userId).collection("Confirmed").document(documentSnapshot.getId()).delete();
                                        }
                                    }
                                } else {
                                    attendeeEvents.document(userId).collection("Declined").whereEqualTo("Event", eventToDeleteId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                    if (documentSnapshot.exists()) {
                                                        attendeeEvents.document(userId).collection("Declined").document(documentSnapshot.getId()).delete();
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
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        });
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
}