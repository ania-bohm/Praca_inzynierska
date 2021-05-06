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
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class InvitationListFragment extends Fragment {

    NavController navController;
    ListView invitationListListView;
    ArrayList<String> invitationEventIdList;
    ArrayList<Event> invitationEventList;
    InvitationListAdapter adapter;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference attendeeEvents = db.collection("AttendeeEvents");
    CollectionReference eventAttendees = db.collection("EventAttendees");
    Context context;
    String currentUserId;

    public InvitationListFragment() {
        // Required empty public constructor
    }

    public static InvitationListFragment newInstance(String param1, String param2) {
        InvitationListFragment fragment = new InvitationListFragment();
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
        return inflater.inflate(R.layout.fragment_invitation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        invitationListListView = view.findViewById(R.id.invitationListListView);
        invitationEventList = new ArrayList<>();
        invitationEventIdList = new ArrayList<>();
        adapter = new InvitationListAdapter(context, invitationEventList, invitationEventIdList, navController);
        invitationListListView.setAdapter(adapter);
        loadEventsToListView();

//        eventUserSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                User user = foundUsersList.get(position);
//                String userId = foundUsersIdList.get(position);
//                if (!containsUserId(userId)) {
//                    invitedUsersList.add(user);
//                    invitedUsersIdList.add(userId);
//                }
//                invitedUsersAdapter.notifyDataSetChanged();
//            }
//        });
    }

    public void loadEventsToListView() {
        Log.d(TAG, "Jestem w loadEventsToListView----------------------------------");
        attendeeEvents.document(currentUserId).collection("Invited").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        attendeeEvents.document(currentUserId).collection("Invited").document(documentSnapshot.getId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String eventId = documentSnapshot.get("Event").toString();
                                invitationEventIdList.add(eventId);
                                Log.d(TAG, "----------------------------------" + eventId);
                                events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        invitationEventList.add(documentSnapshot.toObject(Event.class));
                                        adapter.notifyDataSetChanged();
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
                                Log.d(TAG, "----------------------------------");
                                Log.d(TAG, e.toString());
                            }
                        });
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "----------------------------------");
                Log.d(TAG, e.toString());
            }
        });
    }
}