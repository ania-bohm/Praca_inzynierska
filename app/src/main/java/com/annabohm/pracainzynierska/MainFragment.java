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
import android.widget.ImageView;


public class MainFragment extends Fragment {

    NavController navController;
    ImageView accountSettingsButton;
    ImageView menuButton;
    ImageView addEventButton;

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
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    private View.OnClickListener accountSettingsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.mainToAccountSettings);
        }
    };

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
        accountSettingsButton = view.findViewById(R.id.accountSettingsButton);
        menuButton = view.findViewById(R.id.menuButton);
        addEventButton = view.findViewById(R.id.addEventButton);
        accountSettingsButton.setOnClickListener(accountSettingsOnClickListener);
        addEventButton.setOnClickListener(addEventOnClickListener);
    }
}