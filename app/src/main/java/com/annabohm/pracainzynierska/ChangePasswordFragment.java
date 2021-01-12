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

public class ChangePasswordFragment extends Fragment {

    NavController navController;
    Button confirmChangePasswordButton;
    Button rejectChangePasswordButton;
    ImageView changePasswordShowPasswordButton;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    public static ChangePasswordFragment newInstance(String param1, String param2) {
        ChangePasswordFragment fragment = new ChangePasswordFragment();
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
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    private View.OnClickListener confirmChangePasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };

    private View.OnClickListener rejectChangePasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };

    private View.OnClickListener changePasswordShowPasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        confirmChangePasswordButton = view.findViewById(R.id.confirmChangePasswordButton);
        rejectChangePasswordButton = view.findViewById(R.id.rejectChangePasswordButton);
        changePasswordShowPasswordButton = view.findViewById(R.id.changePasswordShowPasswordButton);
        confirmChangePasswordButton.setOnClickListener(confirmChangePasswordOnClickListener);
        rejectChangePasswordButton.setOnClickListener(rejectChangePasswordOnClickListener);
        changePasswordShowPasswordButton.setOnClickListener(changePasswordShowPasswordOnClickListener);
    }
}