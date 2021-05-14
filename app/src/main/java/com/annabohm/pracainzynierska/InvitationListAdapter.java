package com.annabohm.pracainzynierska;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.content.ContentValues.TAG;

public class InvitationListAdapter extends ArrayAdapter<Event> implements View.OnClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference attendeeEvents = db.collection("AttendeeEvents");
    CollectionReference eventAttendees = db.collection("EventAttendees");
    ArrayList<String> eventAttendeesList;
    NavController navController;
    private ArrayList<Event> eventList;
    private ArrayList<String> eventIdList;
    private Context context;

    public InvitationListAdapter(@NonNull Context context, @NonNull List<Event> eventList, List<String> eventIdList, NavController navController) {
        super(context, R.layout.invitation, eventList);
        this.eventList = (ArrayList) eventList;
        this.context = context;
        this.eventIdList = (ArrayList) eventIdList;
        this.navController = navController;
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        this.eventAttendeesList = new ArrayList<>();
        final Event event = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.invitation, parent, false);
        }
        TextView invitationEventNameTextView = convertView.findViewById(R.id.invitationEventNameTextView);
        final TextView invitationEventAuthorTextView = convertView.findViewById(R.id.invitationEventAuthorTextView);
        Button invitationDisplayEventButton = convertView.findViewById(R.id.invitationDisplayEventButton);
        Button invitationConfirmEventButton = convertView.findViewById(R.id.invitationConfirmEventButton);
        Button invitationDeclineEventButton = convertView.findViewById(R.id.invitationDeclineEventButton);

        db.collection("Users").document(event.getEventAuthor()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                invitationEventAuthorTextView.setText("Autor: " + user.getUserFirstName() + " " + user.getUserLastName());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });

        invitationEventNameTextView.setText(event.getEventName());


        invitationDisplayEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventId = eventIdList.get(position);
                events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String path = documentSnapshot.getReference().getPath();
                            Toast.makeText(getContext(), "Position clicked: " + position, Toast.LENGTH_SHORT).show();
                            Bundle bundle = new Bundle();
                            bundle.putString("path", path);
                            navController.navigate(R.id.invitationListToDisplayEvent, bundle);
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

        invitationConfirmEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventId = eventIdList.get(position);
                final String currentUser = firebaseAuth.getCurrentUser().getUid();

                eventAttendees.document(eventId).collection("Invited").whereEqualTo("User", currentUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                documentSnapshot.getReference().delete();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

                Map<String, String> docDataUserId = new HashMap<>();
                docDataUserId.put("User", currentUser);
                eventAttendees.document(eventId).collection("Confirmed").add(docDataUserId).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
//                        Toast.makeText(context, "Invitation confirmed in eventAttendees!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

                attendeeEvents.document(currentUser).collection("Invited").whereEqualTo("Event", eventId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                documentSnapshot.getReference().delete();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });
                Map<String, String> docDataEventId = new HashMap<>();
                docDataEventId.put("Event", eventId);
                attendeeEvents.document(currentUser).collection("Confirmed").add(docDataEventId).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(context, "Invitation confirmed in attendeeEvents!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });
                eventList.remove(position);
                eventIdList.remove(position);
                notifyDataSetChanged();
            }
        });

        invitationDeclineEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventId = eventIdList.get(position);
                final String currentUser = firebaseAuth.getCurrentUser().getUid();

                eventAttendees.document(eventId).collection("Invited").whereEqualTo("User", currentUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                documentSnapshot.getReference().delete();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

                Map<String, String> docDataUserId = new HashMap<>();
                docDataUserId.put("User", currentUser);
                eventAttendees.document(eventId).collection("Declined").add(docDataUserId).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(context, "Invitation declined in eventAttendees!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

                attendeeEvents.document(currentUser).collection("Invited").whereEqualTo("Event", eventId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                documentSnapshot.getReference().delete();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

                Map<String, String> docDataEventId = new HashMap<>();
                docDataUserId.put("Event", eventId);
                attendeeEvents.document(currentUser).collection("Declined").add(docDataEventId).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(context, "Invitation declined in attendeeEvents!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

                eventList.remove(position);
                eventIdList.remove(position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public void setEventList(ArrayList<Event> eventList) {
        this.eventList.clear();
        for (Event event : eventList) {
            this.eventList.add(event);
        }
    }

    public void setEventIdList(ArrayList<String> eventIdList) {
        this.eventIdList.clear();
        for (String eventId : eventIdList) {
            this.eventIdList.add(eventId);
        }
    }

    @Override
    public void remove(@Nullable Event object) {
        super.remove(object);
    }
}