package com.annabohm.pracainzynierska;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

public class DisplayEventFragment extends Fragment {

    NavController navController;
    ImageView toDoListButton;
    ImageView shoppingListButton;
    ImageView editEventButton;


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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity)getActivity()).setDrawerLocked();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_display_event, container, false);
    }

    private View.OnClickListener toDoListOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToToDoList);
        }
    };

    private View.OnClickListener shoppingListOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToShoppingList);
        }
    };

    private View.OnClickListener editEventOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.displayEventToEditEvent);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        toDoListButton = view.findViewById(R.id.toDoListButton);
        shoppingListButton = view.findViewById(R.id.shoppingListButton);
        editEventButton = view.findViewById(R.id.editEventButton);
        toDoListButton.setOnClickListener(toDoListOnClickListener);
        shoppingListButton.setOnClickListener(shoppingListOnClickListener);
        editEventButton.setOnClickListener(editEventOnClickListener);
    }
}