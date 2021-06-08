package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class InvitationListFragment extends Fragment {

    NavController navController;
    ListView invitationListListView;
    TextView invitationListNoInvitationsTextView;
    ArrayList<String> invitationEventIdList;
    ArrayList<Event> invitationEventList;
    InvitationListAdapter adapter;
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    FirebaseAuthInstanceSingleton firebaseAuthInstanceSingleton = FirebaseAuthInstanceSingleton.getInstance();
    CollectionReference events = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Events");
    CollectionReference attendeeEvents = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("AttendeeEvents");
    Context context;
    String currentUserId = firebaseAuthInstanceSingleton.getFirebaseAuthRef().getCurrentUser().getUid();
    AlertDialog alertDialog;

    public InvitationListFragment() {
    }

    public static InvitationListFragment newInstance() {
        return new InvitationListFragment();
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
        return inflater.inflate(R.layout.fragment_invitation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        alertDialog = new SpotsDialog(context);

        invitationListListView = view.findViewById(R.id.invitationListListView);
        invitationListNoInvitationsTextView = view.findViewById(R.id.invitationListNoInvitationsTextView);

        invitationListNoInvitationsTextView.setVisibility(View.GONE);

        invitationEventList = new ArrayList<>();
        invitationEventIdList = new ArrayList<>();

        adapter = new InvitationListAdapter(context, invitationEventList, invitationEventIdList, navController);
        invitationListListView.setAdapter(adapter);
        alertDialog.show();
        loadEventsToListView();
    }

    public void loadEventsToListView() {
        attendeeEvents.document(currentUserId).collection("Invited").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.exists()) {
                            attendeeEvents.document(currentUserId).collection("Invited").document(documentSnapshot.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        final String eventId = Objects.requireNonNull(documentSnapshot.get("Event")).toString();
                                        invitationEventIdList.add(eventId);
                                        events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
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
                                                        invitationEventList.add(documentSnapshot.toObject(Event.class));
                                                        adapter.notifyDataSetChanged();
                                                    } else if (dateString.equals(dateNowString)) {
                                                        if (hourNow == 0) {
                                                            if (hour == 0) {
                                                                if (minute > minuteNow) {
                                                                    invitationEventList.add(documentSnapshot.toObject(Event.class));
                                                                    adapter.notifyDataSetChanged();
                                                                }
                                                            }
                                                        } else if (hour == hourNow && minute > minuteNow) {
                                                            invitationEventList.add(documentSnapshot.toObject(Event.class));
                                                            adapter.notifyDataSetChanged();
                                                        } else if (hour > hourNow) {
                                                            invitationEventList.add(documentSnapshot.toObject(Event.class));
                                                            adapter.notifyDataSetChanged();
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
                    adapter.notifyDataSetChanged();
                } else {
                    invitationListNoInvitationsTextView.setVisibility(View.VISIBLE);
                }
                if (invitationEventList.isEmpty()) {
                    invitationListNoInvitationsTextView.setVisibility(View.VISIBLE);
                }
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