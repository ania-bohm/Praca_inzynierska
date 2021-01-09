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

public class EditAccountSettingsFragment extends Fragment {

    NavController navController;
    Button confirmEditPersonalDataButton;
    Button rejectEditPersonalDataButton;

    public EditAccountSettingsFragment() {
        // Required empty public constructor
    }

    public static EditAccountSettingsFragment newInstance(String param1, String param2) {
        EditAccountSettingsFragment fragment = new EditAccountSettingsFragment();
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
        return inflater.inflate(R.layout.fragment_edit_account_settings, container, false);
    }

    private View.OnClickListener confirmEditOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };

    private View.OnClickListener rejectEditOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        confirmEditPersonalDataButton = view.findViewById(R.id.confirmEditPersonalDataButton);
        rejectEditPersonalDataButton = view.findViewById(R.id.rejectEditPersonalDataButton);
        confirmEditPersonalDataButton.setOnClickListener(confirmEditOnClickListener);
        rejectEditPersonalDataButton.setOnClickListener(rejectEditOnClickListener);
    }
}