package com.annabohm.pracainzynierska;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class MainFragment extends Fragment {
    NavController navController;
    ImageView addEventButton;
    //RecyclerView yourEventsRecyclerView;
    RecyclerView yourCurrentEventsRecyclerView;
    RecyclerView allEventsRecyclerView;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference attendeeEvents = db.collection("AttendeeEvents");
    CollectionReference eventAttendees = db.collection("EventAttendees");
    ArrayList<String> attendeesToDeleteIdList = new ArrayList<>();
    //EventAdapter yourEventsAdapter;
    ConfirmedEventAdapter allEventsAdapter;
    ConfirmedEventAdapter yourCurrentEventsAdapter;
    HashMap<String, Event> confirmedEvents;
    HashMap<String, Event> yourCurrentEvents;
    Context context;

    private View.OnClickListener addEventOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.mainToAddEvent);
        }
    };

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        // Inflate the layout for this fragment
        ((MainActivity) getActivity()).setDrawerUnlocked();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    private void setUpAllEventsRecyclerView(final View view) {
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        attendeeEvents.document(currentUserId).collection("Confirmed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                    if (documentSnapshot.exists()) {
                        final String eventId = documentSnapshot.get("Event").toString();
                        events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                //
                                Event eventToCheck = documentSnapshot.toObject(Event.class);
                                Date dateNow = new Date();
                                final DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");
                                String newString = timeFormatterPrint.format(eventToCheck.getEventTimeFinish());
                                String newStringNow = timeFormatterPrint.format(dateNow);

                                int hourNow = Integer.parseInt(newStringNow.substring(0, 2));
                                int hour = Integer.parseInt(newString.substring(0, 2));
                                int minuteNow = Integer.parseInt(newStringNow.substring(3, 5));
                                int minute = Integer.parseInt(newString.substring(3, 5));

                                if (eventToCheck.getEventDateFinish().after(new Date())) {
                                    confirmedEvents.put(eventId, documentSnapshot.toObject(Event.class));
                                    allEventsAdapter.notifyDataSetChanged();
                                } else if (eventToCheck.getEventDateFinish().equals(new Date())) {
                                    if (hourNow == 0) {
                                        if (hour == 0) {
                                            if (minute > minuteNow) {
                                                confirmedEvents.put(eventId, documentSnapshot.toObject(Event.class));
                                                allEventsAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    } else if (hour == hourNow && minute > minuteNow) {
                                        confirmedEvents.put(eventId, documentSnapshot.toObject(Event.class));
                                        allEventsAdapter.notifyDataSetChanged();
                                    } else if (hour > hourNow) {
                                        confirmedEvents.put(eventId, documentSnapshot.toObject(Event.class));
                                        allEventsAdapter.notifyDataSetChanged();
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
                allEventsRecyclerView.setAdapter(allEventsAdapter);
                allEventsAdapter.setOnItemClickListener(new ConfirmedEventAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(final int position) {
                        String eventId = allEventsAdapter.getEventId(position);

                        events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String path = documentSnapshot.getReference().getPath();
//                                    Toast.makeText(getContext(), "Position clicked: " + position, Toast.LENGTH_SHORT).show();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("path", path);
                                    navController.navigate(R.id.mainToDisplayEvent, bundle);
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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    private void setUpYourCurrentEventsRecyclerView(final View view) {
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        events.orderBy("eventDateStart", Query.Direction.ASCENDING).whereEqualTo("eventAuthor", currentUserId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                    if (documentSnapshot.exists()) {
                        final String eventId = documentSnapshot.getId();
                        Event eventToCheck = documentSnapshot.toObject(Event.class);
                        Date dateNow = new Date();
                        final DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");
                        String newString = timeFormatterPrint.format(eventToCheck.getEventTimeFinish());
                        String newStringNow = timeFormatterPrint.format(dateNow);

                        int hourNow = Integer.parseInt(newStringNow.substring(0, 2));
                        int hour = Integer.parseInt(newString.substring(0, 2));
                        int minuteNow = Integer.parseInt(newStringNow.substring(3, 5));
                        int minute = Integer.parseInt(newString.substring(3, 5));

                        if (eventToCheck.getEventDateFinish().after(new Date())) {
                            yourCurrentEvents.put(eventId, documentSnapshot.toObject(Event.class));
                            yourCurrentEventsAdapter.notifyDataSetChanged();
                        } else if (eventToCheck.getEventDateFinish().equals(new Date())) {
                            if (hourNow == 0) {
                                if (hour == 0) {
                                    if (minute > minuteNow) {
                                        yourCurrentEvents.put(eventId, documentSnapshot.toObject(Event.class));
                                        yourCurrentEventsAdapter.notifyDataSetChanged();
                                    }
                                }
                            } else if (hour == hourNow && minute > minuteNow) {
                                yourCurrentEvents.put(eventId, documentSnapshot.toObject(Event.class));
                                yourCurrentEventsAdapter.notifyDataSetChanged();
                            } else if (hour > hourNow) {
                                yourCurrentEvents.put(eventId, documentSnapshot.toObject(Event.class));
                                yourCurrentEventsAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }

                yourCurrentEventsRecyclerView.setAdapter(yourCurrentEventsAdapter);
                yourCurrentEventsAdapter.setOnItemClickListener(new ConfirmedEventAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        String eventId = yourCurrentEventsAdapter.getEventId(position);

                        events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String path = documentSnapshot.getReference().getPath();
//                                    Toast.makeText(getContext(), "Position clicked: " + position, Toast.LENGTH_SHORT).show();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("path", path);
                                    navController.navigate(R.id.mainToDisplayEvent, bundle);
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
                new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                        new AlertDialog.Builder(context)
                                .setTitle("Usunięcie wydarzenia")
                                .setMessage("Czy na pewno chcesz usunąć wydarzenie?")
                                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        yourCurrentEventsAdapter.notifyDataSetChanged();
                                    }
                                })
                                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String eventToDeleteId = yourCurrentEventsAdapter.getEventId(viewHolder.getAdapterPosition());
                                        getAttendeesToDeleteIdList(eventToDeleteId);
                                        yourCurrentEventsAdapter.deleteItem(viewHolder.getAdapterPosition());
                                        Toast.makeText(context, "Usunięto wydarzenie", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setIcon(R.drawable.ic_decline)
                                .show();
                    }
                }).attachToRecyclerView(yourCurrentEventsRecyclerView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    public void getAttendeesToDeleteIdList(final String eventToDeleteId) {
        Toast.makeText(context, "Jestem w getAtToDel", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(context, "Jestem w upEvAt", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(context, "Jestem w upAtEv", Toast.LENGTH_SHORT).show();
        for (final String userId : attendeesToDeleteIdList) {
            attendeeEvents.document(userId).collection("Invited").whereEqualTo("Event", eventToDeleteId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                Toast.makeText(context, "Jestem w upAtEv w Invited onSucc!", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(context, "Jestem w upAtEv w Confirmed onSucc!", Toast.LENGTH_SHORT).show();
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
                                                        Toast.makeText(context, "Jestem w upAtEv w Declined onSucc!", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        confirmedEvents = new HashMap<>();
        yourCurrentEvents = new HashMap<>();

        allEventsAdapter = new ConfirmedEventAdapter(confirmedEvents);
        yourCurrentEventsAdapter = new ConfirmedEventAdapter(yourCurrentEvents);
        yourCurrentEventsRecyclerView = view.findViewById(R.id.yourCurrentEventsRecyclerView);
        allEventsRecyclerView = view.findViewById(R.id.allEventsRecyclerView);
        addEventButton = view.findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(addEventOnClickListener);

        allEventsRecyclerView.setHasFixedSize(false);
        allEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));//<----------

        yourCurrentEventsRecyclerView.setHasFixedSize(false);
        yourCurrentEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));//<----------

        setUpAllEventsRecyclerView(view);
        setUpYourCurrentEventsRecyclerView(view);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        yourEventsAdapter.startListening();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        yourEventsAdapter.stopListening();
//    }
}