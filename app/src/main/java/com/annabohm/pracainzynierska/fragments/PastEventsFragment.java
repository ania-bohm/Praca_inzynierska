package com.annabohm.pracainzynierska.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.annabohm.pracainzynierska.others.DateHandler;
import com.annabohm.pracainzynierska.datamodels.Event;
import com.annabohm.pracainzynierska.singletons.FirebaseAuthInstanceSingleton;
import com.annabohm.pracainzynierska.singletons.FirestoreInstanceSingleton;
import com.annabohm.pracainzynierska.activities.MainActivity;
import com.annabohm.pracainzynierska.adapters.PastEventsListAdapter;
import com.annabohm.pracainzynierska.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class PastEventsFragment extends Fragment {
    NavController navController;
    ListView pastEventsListListView;
    TextView pastEventsEmptyTextView;
    ArrayList<Event> eventList;
    ArrayList<String> eventIdList;
    PastEventsListAdapter pastEventsListAdapter;
    FirebaseAuthInstanceSingleton firebaseAuthInstanceSingleton = FirebaseAuthInstanceSingleton.getInstance();
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    CollectionReference events = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Events");
    CollectionReference eventAttendees = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("EventAttendees");
    CollectionReference attendeeEvents = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("AttendeeEvents");
    ArrayList<String> attendeesToDeleteIdList = new ArrayList<>();
    Context context;
    DateHandler dateHandler = new DateHandler("dd/MM/yyyy", "HH:mm");
    String authorId = firebaseAuthInstanceSingleton.getFirebaseAuthRef().getCurrentUser().getUid();

    public PastEventsFragment() {
    }

    public static PastEventsFragment newInstance() {
        return new PastEventsFragment();
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
        ((MainActivity) requireActivity()).setDrawerLocked();
        return inflater.inflate(R.layout.fragment_past_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        pastEventsListListView = view.findViewById(R.id.pastEventsListListView);
        pastEventsEmptyTextView = view.findViewById(R.id.pastEventsEmptyTextView);

        pastEventsEmptyTextView.setVisibility(View.GONE);

        eventList = new ArrayList<>();
        eventIdList = new ArrayList<>();

        pastEventsListAdapter = new PastEventsListAdapter(context, eventList, eventIdList, navController);
        pastEventsListListView.setAdapter(pastEventsListAdapter);

        registerForContextMenu(pastEventsListListView);
        pastEventsListListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        initialisePastEventsList();
    }

    public void initialisePastEventsList() {
        events.whereEqualTo("eventAuthor", authorId).orderBy("eventDateFinish", Query.Direction.DESCENDING).orderBy("eventTimeFinish", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Event pastEvent = documentSnapshot.toObject(Event.class);
                        Date dateNow = new Date();
                        assert pastEvent != null;
                        String newString = dateHandler.convertTimeToString(pastEvent.getEventTimeFinish());
                        String newStringNow = dateHandler.convertTimeToString(dateNow);

                        int hourNow = Integer.parseInt(newStringNow.substring(0, 2));
                        int hour = Integer.parseInt(newString.substring(0, 2));
                        int minuteNow = Integer.parseInt(newStringNow.substring(3, 5));
                        int minute = Integer.parseInt(newString.substring(3, 5));
                        String dateNowString = dateHandler.convertDateToString(dateNow);
                        String dateString = dateHandler.convertDateToString(pastEvent.getEventDateFinish());

                        if (pastEvent.getEventDateFinish().before(new Date())) {
                            eventList.add(pastEvent);
                            eventIdList.add(documentSnapshot.getId());
                            pastEventsListAdapter.notifyDataSetChanged();
                        } else if (dateString.equals(dateNowString)) {
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
                if (eventList.isEmpty() || eventIdList.isEmpty()) {
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
        requireActivity().getMenuInflater().inflate(R.menu.floating_context_menu_event, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteEvent:
                new AlertDialog.Builder(context)
                        .setTitle(R.string.event_delete_title)
                        .setMessage(R.string.event_delete_message)
                        .setNegativeButton(R.string.event_delete_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton(R.string.event_delete_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String eventIdToDelete = eventIdList.get(info.position);
                                getAttendeesToDeleteIdList(eventIdToDelete);
                                pastEventsListAdapter.deleteItem(info.position);
                                eventIdList.remove(info.position);
                                eventList.remove(info.position);
                                pastEventsListAdapter.notifyDataSetChanged();
                                Toast.makeText(context, R.string.event_delete_success, Toast.LENGTH_SHORT).show();
                                if (eventIdList.isEmpty()) {
                                    pastEventsEmptyTextView.setVisibility(View.VISIBLE);
                                }
                            }
                        })
                        .setIcon(R.drawable.ic_delete)
                        .show();
                return true;
            case R.id.cancelDeleteEvent:
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
                                    attendeesToDeleteIdList.add(Objects.requireNonNull(documentSnapshot.get("User")).toString());
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
                                            attendeesToDeleteIdList.add(Objects.requireNonNull(documentSnapshot.get("User")).toString());
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
                                                    attendeesToDeleteIdList.add(Objects.requireNonNull(documentSnapshot.get("User")).toString());
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