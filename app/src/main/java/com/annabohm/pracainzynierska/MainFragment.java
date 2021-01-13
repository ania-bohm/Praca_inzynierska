package com.annabohm.pracainzynierska;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;


public class MainFragment extends Fragment {

    NavController navController;
    Adapter yourEventsAdapter;
    Adapter allEventsAdapter;
    ImageView addEventButton;
    RecyclerView yourEventsRecyclerView;
    RecyclerView allEventsRecyclerView;
    List<Event> yourEventsList;
    List<Event> allEventsList;

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        addEventButton = view.findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(addEventOnClickListener);

        yourEventsRecyclerView = view.findViewById(R.id.yourEventsRecyclerView);
        allEventsRecyclerView = view.findViewById(R.id.allEventsRecyclerView);
        yourEventsList = new ArrayList<>();
        allEventsList = new ArrayList<>();
//        mReference = FirebaseDatabase.getInstance().getReference();
//
//        mReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                photos.clear();
//                for (DataSnapshot child : snapshot.getChildren()) {
//                    photos.add(child.getValue().toString());
//                }
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
        yourEventsAdapter = new Adapter(getContext(), yourEventsList);
        allEventsAdapter = new Adapter(getContext(), allEventsList);
        yourEventsAdapter.notifyDataSetChanged();
        allEventsAdapter.notifyDataSetChanged();
        GridLayoutManager yourEventsLayoutManager = new GridLayoutManager(getContext(),3, GridLayoutManager.HORIZONTAL, false);
        GridLayoutManager allEventsLayoutManager = new GridLayoutManager(getContext(),3, GridLayoutManager.HORIZONTAL, false);
        yourEventsRecyclerView.setLayoutManager(yourEventsLayoutManager);
        allEventsRecyclerView.setLayoutManager(allEventsLayoutManager);
        yourEventsRecyclerView.setAdapter(yourEventsAdapter);
        allEventsRecyclerView.setAdapter(allEventsAdapter);
    }
}