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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class DisplayEventFragment extends Fragment {

    NavController navController;
    ImageView toDoListButton, eventBudgetButton, editEventButton;
    TextView showEventAuthorTextView, showEventNameTextView, showEventDateStartTextView, showEventTimeStartTextView, showEventDateFinishTextView, showEventTimeFinishTextView, showEventLocationTextView, showEventDescriptionTextView;
    ListView displayEventGuestListListView;
    ArrayList<User> userList;
    ArrayList<String> userStatusList;
    String eventId;
    GuestListAdapter adapter;
    Context context;
    Bundle bundle;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference users = db.collection("Users");
    CollectionReference eventAttendees = db.collection("EventAttendees");
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DocumentReference eventReference;

    private View.OnClickListener toDoListOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToToDoList, bundle);
        }
    };
    private View.OnClickListener eventBudgetOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToEventBudget, bundle);
        }
    };
    private View.OnClickListener editEventOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToEditEvent, bundle);
        }
    };

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
        ((MainActivity) getActivity()).setDrawerLocked();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        toDoListButton = view.findViewById(R.id.toDoListButton);
        eventBudgetButton = view.findViewById(R.id.eventBudgetButton);
        editEventButton = view.findViewById(R.id.editEventButton);
        showEventAuthorTextView = view.findViewById(R.id.showEventAuthorTextView);
        showEventNameTextView = view.findViewById(R.id.showEventNameTextView);
        showEventDateStartTextView = view.findViewById(R.id.showEventDateStartTextView);
        showEventTimeStartTextView = view.findViewById(R.id.showEventTimeStartTextView);
        showEventDateFinishTextView = view.findViewById(R.id.showEventDateFinishTextView);
        showEventTimeFinishTextView = view.findViewById(R.id.showEventTimeFinishTextView);
        showEventLocationTextView = view.findViewById(R.id.showEventLocationTextView);
        showEventDescriptionTextView = view.findViewById(R.id.showEventDescriptionTextView);
        displayEventGuestListListView = view.findViewById(R.id.displayEventGuestListListView);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        eventReference = db.document(path);
        eventId = eventReference.getId();

        userList = new ArrayList<>();
        userStatusList = new ArrayList<>();
        adapter = new GuestListAdapter(context, userList, userStatusList);
        displayEventGuestListListView.setAdapter(adapter);

        toDoListButton.setOnClickListener(toDoListOnClickListener);
        eventBudgetButton.setOnClickListener(eventBudgetOnClickListener);

        eventReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    final Event event = documentSnapshot.toObject(Event.class);
                    if (event.getEventAuthor().equals(firebaseAuth.getCurrentUser().getUid())) {
                        editEventButton.setVisibility(View.VISIBLE);
                        toDoListButton.setVisibility(View.VISIBLE);
                        eventBudgetButton.setVisibility(View.VISIBLE);
                        editEventButton.setOnClickListener(editEventOnClickListener);
                    } else {
                        editEventButton.setVisibility(View.GONE);
                        toDoListButton.setVisibility(View.GONE);
                        eventBudgetButton.setVisibility(View.GONE);
                    }
                    final DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
                    final DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");
                    users.document(event.getEventAuthor()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            User userAuthor = documentSnapshot.toObject(User.class);
                            showEventAuthorTextView.setText(userAuthor.getUserFirstName() + " " + userAuthor.getUserLastName());
                            showEventNameTextView.setText(event.getEventName());
                            showEventDateStartTextView.setText(dateFormatterPrint.format(event.getEventDateStart()));
                            showEventTimeStartTextView.setText(timeFormatterPrint.format(event.getEventTimeStart()));
                            showEventDateFinishTextView.setText(dateFormatterPrint.format(event.getEventDateFinish()));
                            showEventTimeFinishTextView.setText(timeFormatterPrint.format(event.getEventTimeFinish()));
                            showEventLocationTextView.setText(event.getEventLocation());
                            showEventDescriptionTextView.setText(event.getEventDescription());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    });

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
        initialiseGuestList();
    }

    public void initialiseGuestList() {
        eventAttendees.document(eventId).collection("Invited").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot queryDocumentSnapshots = task.getResult();
                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String guestId = documentSnapshot.get("User").toString();
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
                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String guestId = documentSnapshot.get("User").toString();
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
                QuerySnapshot queryDocumentSnapshots = task.getResult();if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String guestId = documentSnapshot.get("User").toString();
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