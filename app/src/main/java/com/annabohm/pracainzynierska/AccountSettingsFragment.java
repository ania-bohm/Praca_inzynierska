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

public class AccountSettingsFragment extends Fragment {

    NavController navController;
    ImageView editAccountPhotoImageView;
    ImageView editPersonalDataImageView;
    Button accountChangePasswordButton;

    public AccountSettingsFragment() {
        // Required empty public constructor
    }

    public static AccountSettingsFragment newInstance(String param1, String param2) {
        AccountSettingsFragment fragment = new AccountSettingsFragment();
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
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    private View.OnClickListener editAccountPhotoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    private View.OnClickListener editPersonalDataOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.accountSettingsToEditAccountSettings);
        }
    };

    private View.OnClickListener accountChangePasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.accountSettingsToChangePassword);
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        editAccountPhotoImageView = view.findViewById(R.id.editAccountPhotoImageView);
        editPersonalDataImageView = view.findViewById(R.id.editPersonalDataImageView);
        accountChangePasswordButton = view.findViewById(R.id.accountChangePasswordButton);
        editAccountPhotoImageView.setOnClickListener(editAccountPhotoOnClickListener);
        editPersonalDataImageView.setOnClickListener(editPersonalDataOnClickListener);
        accountChangePasswordButton.setOnClickListener(accountChangePasswordOnClickListener);
    }
}