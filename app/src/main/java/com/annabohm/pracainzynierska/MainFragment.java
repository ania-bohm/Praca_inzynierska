package com.annabohm.pracainzynierska;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class MainFragment extends Fragment {
    NavController navController;
    FloatingActionButton addEventFloatingActionButton;
    RecyclerView yourCurrentEventsRecyclerView, allEventsRecyclerView;
    TextView yourEventsEmptyTextView, allEventsEmptyTextView, welcomeUserFirstNameTextView;
    FirebaseAuthInstanceSingleton firebaseAuthInstanceSingleton = FirebaseAuthInstanceSingleton.getInstance();
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    CollectionReference events = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Events");
    CollectionReference users = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Users");
    CollectionReference attendeeEvents = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("AttendeeEvents");
    CollectionReference eventAttendees = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("EventAttendees");
    ArrayList<String> attendeesToDeleteIdList = new ArrayList<>();
    ConfirmedEventAdapter allEventsAdapter;
    ConfirmedEventAdapter yourCurrentEventsAdapter;
    HashMap<String, Event> confirmedEvents;
    HashMap<String, Event> yourCurrentEvents;
    String currentUserId = Objects.requireNonNull(firebaseAuthInstanceSingleton.getFirebaseAuthRef().getCurrentUser()).getUid();
    Context context;
    SpotsDialog alertDialog;

    private final View.OnClickListener addEventOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.mainToAddEvent);
        }
    };

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setDrawerUnlocked();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    private void setUpAllEventsRecyclerView() {
        confirmedEvents.clear();
        attendeeEvents.document(currentUserId).collection("Confirmed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> documentSnapshots = Objects.requireNonNull(task.getResult()).getDocuments();
                if (!documentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                        if (documentSnapshot.exists()) {
                            final String eventId = Objects.requireNonNull(documentSnapshot.get("Event")).toString();
                            events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Event eventToCheck = documentSnapshot.toObject(Event.class);
                                    Date dateNow = new Date();
                                    final DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");
                                    final DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
                                    assert eventToCheck != null;
                                    String newString = timeFormatterPrint.format(eventToCheck.getEventTimeFinish());
                                    String newStringNow = timeFormatterPrint.format(dateNow);

                                    int hourNow = Integer.parseInt(newStringNow.substring(0, 2));
                                    int hour = Integer.parseInt(newString.substring(0, 2));
                                    int minuteNow = Integer.parseInt(newStringNow.substring(3, 5));
                                    int minute = Integer.parseInt(newString.substring(3, 5));
                                    String dateNowString = dateFormatterPrint.format(dateNow);
                                    String dateString = dateFormatterPrint.format(eventToCheck.getEventDateFinish());

                                    if (eventToCheck.getEventDateFinish().after(new Date())) {
                                        confirmedEvents.put(eventId, documentSnapshot.toObject(Event.class));
                                        allEventsAdapter.notifyDataSetChanged();
                                    } else if (dateString.equals(dateNowString)) {
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
                } else {
                    allEventsEmptyTextView.setVisibility(View.VISIBLE);
                }

                allEventsAdapter.setOnItemClickListener(new ConfirmedEventAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(final int position) {
                        String eventId = allEventsAdapter.getEventId(position);

                        events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String path = documentSnapshot.getReference().getPath();
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

    private void setUpYourCurrentEventsRecyclerView() {
        yourCurrentEvents.clear();
        events.orderBy("eventDateStart", Query.Direction.ASCENDING).whereEqualTo("eventAuthor", currentUserId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> documentSnapshots = Objects.requireNonNull(task.getResult()).getDocuments();
                if (!documentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                        if (documentSnapshot.exists()) {
                            final String eventId = documentSnapshot.getId();
                            Event eventToCheck = documentSnapshot.toObject(Event.class);
                            Date dateNow = new Date();
                            final DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");
                            final DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
                            assert eventToCheck != null;
                            String newString = timeFormatterPrint.format(eventToCheck.getEventTimeFinish());
                            String newStringNow = timeFormatterPrint.format(dateNow);

                            int hourNow = Integer.parseInt(newStringNow.substring(0, 2));
                            int hour = Integer.parseInt(newString.substring(0, 2));
                            int minuteNow = Integer.parseInt(newStringNow.substring(3, 5));
                            int minute = Integer.parseInt(newString.substring(3, 5));
                            String dateNowString = dateFormatterPrint.format(dateNow);
                            String dateString = dateFormatterPrint.format(eventToCheck.getEventDateFinish());

                            if (eventToCheck.getEventDateFinish().after(new Date())) {
                                yourCurrentEvents.put(eventId, documentSnapshot.toObject(Event.class));
                                yourCurrentEventsAdapter.notifyDataSetChanged();
                            } else if (dateString.equals(dateNowString)) {
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
                } else {
                    yourEventsEmptyTextView.setVisibility(View.VISIBLE);
                }
                if (yourCurrentEvents.isEmpty()) {
                    yourEventsEmptyTextView.setVisibility(View.VISIBLE);
                }
                alertDialog.dismiss();
                yourCurrentEventsAdapter.setOnItemClickListener(new ConfirmedEventAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        String eventId = yourCurrentEventsAdapter.getEventId(position);

                        events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String path = documentSnapshot.getReference().getPath();
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
                                .setTitle(R.string.event_delete_title)
                                .setMessage(R.string.event_delete_message)
                                .setNegativeButton(R.string.event_delete_no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        yourCurrentEventsAdapter.notifyDataSetChanged();
                                        setUpYourCurrentEventsRecyclerView();
                                    }
                                })
                                .setPositiveButton(R.string.event_delete_yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String eventToDeleteId = yourCurrentEventsAdapter.getEventId(viewHolder.getAdapterPosition());
                                        getAttendeesToDeleteIdList(eventToDeleteId);
                                        events.document(yourCurrentEventsAdapter.getEventId(viewHolder.getAdapterPosition())).delete();
                                        yourCurrentEventsAdapter.notifyDataSetChanged();
                                        setUpYourCurrentEventsRecyclerView();
                                        Toast.makeText(context, R.string.event_delete_success, Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setIcon(R.drawable.ic_delete)
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        yourCurrentEventsRecyclerView = view.findViewById(R.id.yourCurrentEventsRecyclerView);
        allEventsRecyclerView = view.findViewById(R.id.allEventsRecyclerView);
        yourEventsEmptyTextView = view.findViewById(R.id.yourEventsEmptyTextView);
        allEventsEmptyTextView = view.findViewById(R.id.allEventsEmptyTextView);
        welcomeUserFirstNameTextView = view.findViewById(R.id.welcomeUserFirstNameTextView);

        users.document(currentUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    welcomeUserFirstNameTextView.setText(getString(R.string.welcome_user) + " " + currentUser.getUserFirstName() + "!");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });

        confirmedEvents = new HashMap<>();
        yourCurrentEvents = new HashMap<>();

        allEventsAdapter = new ConfirmedEventAdapter(confirmedEvents);
        yourCurrentEventsAdapter = new ConfirmedEventAdapter(yourCurrentEvents);

        yourEventsEmptyTextView.setVisibility(View.GONE);
        allEventsEmptyTextView.setVisibility(View.GONE);

        addEventFloatingActionButton = view.findViewById(R.id.addEventFloatingActionButton);
        addEventFloatingActionButton.setOnClickListener(addEventOnClickListener);

        allEventsRecyclerView.setHasFixedSize(false);
        allEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        yourCurrentEventsRecyclerView.setHasFixedSize(false);
        yourCurrentEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        yourCurrentEventsRecyclerView.setAdapter(yourCurrentEventsAdapter);
        allEventsRecyclerView.setAdapter(allEventsAdapter);

        alertDialog = new SpotsDialog(context);
        alertDialog.show();
        setUpAllEventsRecyclerView();
        setUpYourCurrentEventsRecyclerView();
    }
}