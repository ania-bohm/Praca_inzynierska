package com.annabohm.pracainzynierska;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class MainFragment extends Fragment {
    NavController navController;
    ImageView addEventButton;
    RecyclerView yourEventsRecyclerView;
    RecyclerView allEventsRecyclerView;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference attendeeEvents = db.collection("AttendeeEvents");
    EventAdapter yourEventsAdapter;
    ConfirmedEventAdapter allEventsAdapter;
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
        //Query query = collectionReference.orderBy("eventName", Query.Direction.ASCENDING);
        //FirestoreRecyclerOptions<Event> events = new FirestoreRecyclerOptions.Builder<Event>().setQuery(query, Event.class).build();
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        Query queryAttendeeEvents = attendeeEvents.document(currentUserId).collection("Confirmed");
        queryAttendeeEvents.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                final HashMap<String, Event> confirmedEvents = new HashMap<>();
                for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                    final String eventId = documentSnapshot.toObject(String.class);
                    events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            confirmedEvents.put(eventId, documentSnapshot.toObject(Event.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    });
                }
                allEventsAdapter = new ConfirmedEventAdapter(confirmedEvents);
                allEventsAdapter.setOnItemClickListener(new ConfirmedEventAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(final int position) {
                        String eventId = allEventsAdapter.getEventId(position);

                        events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String path = documentSnapshot.getReference().getPath();
                                    Toast.makeText(getContext(), "Position clicked: " + position, Toast.LENGTH_SHORT).show();
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
                allEventsRecyclerView.setHasFixedSize(false);
                allEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));//<----------
                allEventsRecyclerView.setAdapter(allEventsAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    private void setUpYourEventsRecyclerView(final View view) {
        //Query query = collectionReference.orderBy("eventName", Query.Direction.ASCENDING);
        //FirestoreRecyclerOptions<Event> events = new FirestoreRecyclerOptions.Builder<Event>().setQuery(query, Event.class).build();
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        Query query = events.orderBy("eventDateStart", Query.Direction.ASCENDING).whereEqualTo("eventAuthor", currentUserId);
        final FirestoreRecyclerOptions<Event> events = new FirestoreRecyclerOptions.Builder<Event>().setQuery(query, Event.class).build();
        yourEventsAdapter = new EventAdapter(events);
        yourEventsRecyclerView.setHasFixedSize(false);
        yourEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));//<----------
        yourEventsRecyclerView.setAdapter(yourEventsAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                yourEventsAdapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(yourEventsRecyclerView);

        yourEventsAdapter.setOnItemClickListener(new EventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Event event = documentSnapshot.toObject(Event.class);
                String path = documentSnapshot.getReference().getPath();
                String id = documentSnapshot.getId();
                DocumentReference documentReference = documentSnapshot.getReference();
                Toast.makeText(getContext(), "Position clicked: " + position, Toast.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString("path", path);
                navController.navigate(R.id.mainToDisplayEvent, bundle);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        yourEventsRecyclerView = view.findViewById(R.id.yourEventsRecyclerView);
        allEventsRecyclerView = view.findViewById(R.id.allEventsRecyclerView);
        addEventButton = view.findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(addEventOnClickListener);
        setUpAllEventsRecyclerView(view);
        setUpYourEventsRecyclerView(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        yourEventsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        yourEventsAdapter.stopListening();
    }

}