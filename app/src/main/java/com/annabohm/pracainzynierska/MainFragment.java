package com.annabohm.pracainzynierska;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;


public class MainFragment extends Fragment {

    NavController navController;
    ImageView addEventButton;
    RecyclerView yourEventsRecyclerView;
    RecyclerView allEventsRecyclerView;
    Button button;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference = db.collection("Events").document("My first event!");
    CollectionReference collectionReference = db.collection("Events");
    EventAdapter eventAdapter;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((MainActivity)getActivity()).setDrawerUnlocked();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    private View.OnClickListener addEventOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.mainToAddEvent);
        }
    };

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.mainToDisplayEvent);
        }
    };

    private void setUpAllEventsRecyclerView(View view) {
        //Query query = collectionReference.orderBy("eventName", Query.Direction.ASCENDING);
        //FirestoreRecyclerOptions<Event> events = new FirestoreRecyclerOptions.Builder<Event>().setQuery(query, Event.class).build();
        Query query = collectionReference.orderBy("eventName", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Event> events = new FirestoreRecyclerOptions.Builder<Event>().setQuery(query, Event.class).build();
        eventAdapter = new EventAdapter(events);
        allEventsRecyclerView = view.findViewById(R.id.allEventsRecyclerView);
        allEventsRecyclerView.setHasFixedSize(true);
        allEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));//<----------
        allEventsRecyclerView.setAdapter(eventAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        addEventButton = view.findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(addEventOnClickListener);
        button = view.findViewById(R.id.button);
        button.setOnClickListener(buttonOnClickListener);

        setUpAllEventsRecyclerView(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        eventAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventAdapter.stopListening();
    }
}