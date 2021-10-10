package com.annabohm.pracainzynierska.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.annabohm.pracainzynierska.others.DateHandler;
import com.annabohm.pracainzynierska.datamodels.Event;
import com.annabohm.pracainzynierska.singletons.FirebaseAuthInstanceSingleton;
import com.annabohm.pracainzynierska.singletons.FirestoreInstanceSingleton;
import com.annabohm.pracainzynierska.adapters.GuestListAdapter;
import com.annabohm.pracainzynierska.activities.MainActivity;
import com.annabohm.pracainzynierska.R;
import com.annabohm.pracainzynierska.datamodels.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class DisplayEventFragment extends Fragment {

    NavController navController;
    ImageView toDoListButton, scoreboardButton, editEventButton, commonExpenseButton, chatButton;
    TextView showEventAuthorTextView, showEventNameTextView, showEventDateStartTextView, showEventTimeStartTextView, showEventDateFinishTextView, showEventTimeFinishTextView, showEventLocationTextView, showEventDescriptionTextView;
    ListView displayEventGuestListListView;
    ArrayList<User> userList;
    ArrayList<String> userStatusList;
    String eventId;
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    FirebaseAuthInstanceSingleton firebaseAuthInstanceSingleton = FirebaseAuthInstanceSingleton.getInstance();
    CollectionReference users = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Users");
    CollectionReference eventAttendees = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("EventAttendees");
    String currentUserId = firebaseAuthInstanceSingleton.getFirebaseAuthRef().getCurrentUser().getUid();
    String eventAuthor;
    boolean isCurrentUserGuestOrAuthor = false;
    DocumentReference eventReference;
    AlertDialog alertDialog;
    DateHandler dateHandler = new DateHandler("dd/MM/yyyy", "HH:mm");
    GuestListAdapter adapter;
    Context context;
    Bundle bundle;
    private final View.OnClickListener toDoListOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToToDoList, bundle);
        }
    };
    private final View.OnClickListener scoreboardOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToScoreboard, bundle);
        }
    };
    private final View.OnClickListener editEventOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToEditEvent, bundle);
        }
    };
    private final View.OnClickListener commonExpenseOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToCommonExpense, bundle);
        }
    };
    private final View.OnClickListener chatOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToChat, bundle);
        }
    };


    public DisplayEventFragment() {
    }

    public static DisplayEventFragment newInstance() {
        return new DisplayEventFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setDrawerLocked();
        return inflater.inflate(R.layout.fragment_display_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        toDoListButton = view.findViewById(R.id.toDoListButton);
        scoreboardButton = view.findViewById(R.id.scoreboardButton);
        editEventButton = view.findViewById(R.id.editEventButton);
        commonExpenseButton = view.findViewById(R.id.commonExpenseButton);
        chatButton = view.findViewById(R.id.chatButton);
        showEventAuthorTextView = view.findViewById(R.id.showEventAuthorTextView);
        showEventNameTextView = view.findViewById(R.id.showEventNameTextView);
        showEventDateStartTextView = view.findViewById(R.id.showEventDateStartTextView);
        showEventTimeStartTextView = view.findViewById(R.id.showEventTimeStartTextView);
        showEventDateFinishTextView = view.findViewById(R.id.showEventDateFinishTextView);
        showEventTimeFinishTextView = view.findViewById(R.id.showEventTimeFinishTextView);
        showEventLocationTextView = view.findViewById(R.id.showEventLocationTextView);
        showEventDescriptionTextView = view.findViewById(R.id.showEventDescriptionTextView);
        displayEventGuestListListView = view.findViewById(R.id.displayEventGuestListListView);
        alertDialog = new SpotsDialog(context);

        displayEventGuestListListView.setFocusable(false);

        editEventButton.setVisibility(View.GONE);
        toDoListButton.setVisibility(View.GONE);
        scoreboardButton.setVisibility(View.GONE);
        commonExpenseButton.setVisibility(View.GONE);
        chatButton.setVisibility(View.GONE);

        bundle = this.getArguments();
        assert bundle != null;
        String path = bundle.getString("path");
        eventReference = firestoreInstanceSingleton.getFirebaseFirestoreRef().document(path);
        eventId = eventReference.getId();

        userList = new ArrayList<>();
        userStatusList = new ArrayList<>();
        adapter = new GuestListAdapter(context, userList, userStatusList);
        displayEventGuestListListView.setAdapter(adapter);

        toDoListButton.setOnClickListener(toDoListOnClickListener);
        scoreboardButton.setOnClickListener(scoreboardOnClickListener);
        commonExpenseButton.setOnClickListener(commonExpenseOnClickListener);
        chatButton.setOnClickListener(chatOnClickListener);

        eventAttendees.document(eventId).collection("Confirmed").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.get("User").equals(currentUserId)) {
                                isCurrentUserGuestOrAuthor = true;
                            }
                        }
                    }
                }
                loadData();
                initialiseGuestList();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    public void loadData() {
        alertDialog.show();
        eventReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                assert documentSnapshot != null;
                if (documentSnapshot.exists()) {
                    final Event event = documentSnapshot.toObject(Event.class);
                    assert event != null;
                    if (event.getEventAuthor().equals(currentUserId)) {
                        isCurrentUserGuestOrAuthor = true;
                        editEventButton.setVisibility(View.VISIBLE);
                        editEventButton.setOnClickListener(editEventOnClickListener);
                    } else {
                        editEventButton.setVisibility(View.GONE);
                    }
                    if (isCurrentUserGuestOrAuthor) {
                        toDoListButton.setVisibility(View.VISIBLE);
                        scoreboardButton.setVisibility(View.VISIBLE);
                        commonExpenseButton.setVisibility(View.VISIBLE);
                        chatButton.setVisibility(View.VISIBLE);
                    }

                    users.document(event.getEventAuthor()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User userAuthor = documentSnapshot.toObject(User.class);
                            assert userAuthor != null;
                            showEventAuthorTextView.setText(userAuthor.getUserFirstName() + " " + userAuthor.getUserLastName());
                            showEventNameTextView.setText(event.getEventName());
                            showEventDateStartTextView.setText(dateHandler.convertDateToString(event.getEventDateStart()));
                            showEventTimeStartTextView.setText(dateHandler.convertTimeToString(event.getEventTimeStart()));
                            showEventDateFinishTextView.setText(dateHandler.convertDateToString(event.getEventDateFinish()));
                            showEventTimeFinishTextView.setText(dateHandler.convertTimeToString(event.getEventTimeFinish()));
                            showEventLocationTextView.setText(event.getEventLocation());
                            showEventDescriptionTextView.setText(event.getEventDescription());
                            alertDialog.dismiss();
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

    public void initialiseGuestList() {
        eventAttendees.document(eventId).collection("Invited").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot queryDocumentSnapshots = task.getResult();
                assert queryDocumentSnapshots != null;
                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String guestId = Objects.requireNonNull(documentSnapshot.get("User")).toString();
                        users.document(guestId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User guest = documentSnapshot.toObject(User.class);
                                userList.add(guest);
                                userStatusList.add("Invited");
                                adapter.notifyDataSetChanged();
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });

        eventAttendees.document(eventId).collection("Confirmed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot queryDocumentSnapshots = task.getResult();
                assert queryDocumentSnapshots != null;
                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String guestId = Objects.requireNonNull(documentSnapshot.get("User")).toString();
                        users.document(guestId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User guest = documentSnapshot.toObject(User.class);
                                userList.add(guest);
                                userStatusList.add("Confirmed");
                                adapter.notifyDataSetChanged();
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });

        eventAttendees.document(eventId).collection("Declined").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot queryDocumentSnapshots = task.getResult();
                assert queryDocumentSnapshots != null;
                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String guestId = Objects.requireNonNull(documentSnapshot.get("User")).toString();
                        users.document(guestId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User guest = documentSnapshot.toObject(User.class);
                                userList.add(guest);
                                userStatusList.add("Declined");
                                adapter.notifyDataSetChanged();
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }
}