package com.annabohm.pracainzynierska;

import android.content.Context;
import android.graphics.Typeface;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.ContentValues.TAG;

public class AccountSettingsFragment extends Fragment {

    NavController navController;
    ImageView editAccountPhotoImageView;
    ImageView editPersonalDataImageView;
    Button accountChangePasswordButton;
    TextView accountFirstNameTextView, accountLastNameTextView, accountPhoneNumberTextView, accountEmailTextView;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    DocumentReference documentReference;
    Context context;

    private View.OnClickListener editAccountPhotoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
    private View.OnClickListener editPersonalDataOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.accountSettingsToEditAccountSettings);
            Log.i(TAG, "Jestem w kliknieciu i navcontrollerze");
        }
    };
    private View.OnClickListener accountChangePasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.navigate(R.id.accountSettingsToChangePassword);
        }
    };

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
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setDrawerLocked();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        editAccountPhotoImageView = view.findViewById(R.id.editAccountPhotoImageView);
        editPersonalDataImageView = view.findViewById(R.id.editPersonalDataImageView);
        accountChangePasswordButton = view.findViewById(R.id.accountChangePasswordButton);
        accountFirstNameTextView = view.findViewById(R.id.accountFirstNameTextView);
        accountLastNameTextView = view.findViewById(R.id.accountLastNameTextView);
        accountPhoneNumberTextView = view.findViewById(R.id.accountPhoneNumberTextView);
        accountEmailTextView = view.findViewById(R.id.accountEmailTextView);
        editAccountPhotoImageView.setOnClickListener(editAccountPhotoOnClickListener);
        editPersonalDataImageView.setOnClickListener(editPersonalDataOnClickListener);
        accountChangePasswordButton.setOnClickListener(accountChangePasswordOnClickListener);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid());

        displayPersonalData();
    }

    public void displayPersonalData() {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = new User();
                if (documentSnapshot.exists()) {
                    user = documentSnapshot.toObject(User.class);
                    String firstName = user.getUserFirstName().substring(0, 1).toUpperCase() + user.getUserFirstName().substring(1).toLowerCase();
                    String lastName = user.getUserLastName().substring(0, 1).toUpperCase() + user.getUserLastName().substring(1).toLowerCase();
                    accountFirstNameTextView.setText(firstName);
                    accountLastNameTextView.setText(lastName);
                    accountEmailTextView.setText(user.getUserEmail());
                    accountPhoneNumberTextView.setText(user.getUserPhoneNumber());

                    accountFirstNameTextView.setTypeface(Typeface.create(accountFirstNameTextView.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                    accountLastNameTextView.setTypeface(Typeface.create(accountLastNameTextView.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                    accountEmailTextView.setTypeface(Typeface.create(accountEmailTextView.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                    accountPhoneNumberTextView.setTypeface(Typeface.create(accountPhoneNumberTextView.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);

                } else {
                    Toast.makeText(context, "User does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Reading data from FireStore failed", Toast.LENGTH_SHORT).show();
            }
        });

    }
}