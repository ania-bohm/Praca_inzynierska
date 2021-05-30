package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class EditAccountSettingsFragment extends Fragment {
    public static final String myPreference = "myPref";
    public static final String Password = "Password";
    public static final String Email = "Email";
    NavController navController;
    Button confirmEditPersonalDataButton, rejectEditPersonalDataButton;
    EditText editAccountFirstNameEditText, editAccountLastNameEditText, editAccountEmailEditText, editAccountPhoneNumberEditText;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser;
    AuthCredential authCredential;
    DocumentReference documentReference;
    Context context;
    SharedPreferences sharedPreferences;
    AlertDialog alertDialog;

    private View.OnClickListener confirmEditOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!editAccountFirstNameEditText.getText().toString().trim().isEmpty()) {
                documentReference.update("userFirstName", editAccountFirstNameEditText.getText().toString());
            }

            if (!editAccountLastNameEditText.getText().toString().trim().isEmpty()) {
                documentReference.update("userLastName", editAccountLastNameEditText.getText().toString());
            }

            if (!editAccountEmailEditText.getText().toString().trim().isEmpty()) {
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                authCredential = EmailAuthProvider
                        .getCredential(sharedPreferences.getString(Email, null), sharedPreferences.getString(Password, null));
                firebaseUser.reauthenticate(authCredential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                user.updateEmail(editAccountEmailEditText.getText().toString().trim())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(context, R.string.change_email_success, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(context, R.string.change_email_fail, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });
                documentReference.update("userEmail", editAccountEmailEditText.getText().toString().trim());
            }

            if (!editAccountPhoneNumberEditText.getText().toString().trim().isEmpty()) {
                documentReference.update("userPhoneNumber", editAccountPhoneNumberEditText.getText().toString().trim());
            }

            if (editAccountFirstNameEditText.getText().toString().trim().isEmpty()
                    && editAccountLastNameEditText.getText().toString().trim().isEmpty()
                    && editAccountEmailEditText.getText().toString().isEmpty()
                    && editAccountPhoneNumberEditText.getText().toString().isEmpty()) {
                Toast.makeText(context, R.string.change_personal_data_error_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            navController.popBackStack();
        }
    };
    private View.OnClickListener rejectEditOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };

    public EditAccountSettingsFragment() {
    }

    public static EditAccountSettingsFragment newInstance(String param1, String param2) {
        EditAccountSettingsFragment fragment = new EditAccountSettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        sharedPreferences = this.getActivity().getSharedPreferences(myPreference, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setDrawerLocked();
        return inflater.inflate(R.layout.fragment_edit_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        alertDialog = new SpotsDialog(context);
        confirmEditPersonalDataButton = view.findViewById(R.id.confirmEditPersonalDataButton);
        rejectEditPersonalDataButton = view.findViewById(R.id.rejectEditPersonalDataButton);
        editAccountFirstNameEditText = view.findViewById(R.id.editAccountFirstNameEditText);
        editAccountLastNameEditText = view.findViewById(R.id.editAccountLastNameEditText);
        editAccountEmailEditText = view.findViewById(R.id.editAccountEmailEditText);
        editAccountPhoneNumberEditText = view.findViewById(R.id.editAccountPhoneNumberEditText);

        alertDialog.show();
        documentReference = db.collection("Users").document(firebaseAuth.getCurrentUser().getUid());
        displayPersonalData();
        confirmEditPersonalDataButton.setOnClickListener(confirmEditOnClickListener);
        rejectEditPersonalDataButton.setOnClickListener(rejectEditOnClickListener);
    }

    private void displayPersonalData() {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = new User();
                if (documentSnapshot.exists()) {
                    user = documentSnapshot.toObject(User.class);
                    String firstName = user.getUserFirstName().substring(0, 1).toUpperCase() + user.getUserFirstName().substring(1).toLowerCase();
                    String lastName = user.getUserLastName().substring(0, 1).toUpperCase() + user.getUserLastName().substring(1).toLowerCase();

                    editAccountFirstNameEditText.setHint(firstName);
                    editAccountLastNameEditText.setHint(lastName);
                    editAccountEmailEditText.setHint(user.getUserEmail());
                    editAccountPhoneNumberEditText.setHint(user.getUserPhoneNumber());

                    editAccountFirstNameEditText.setTypeface(Typeface.create(editAccountFirstNameEditText.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                    editAccountLastNameEditText.setTypeface(Typeface.create(editAccountLastNameEditText.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                    editAccountEmailEditText.setTypeface(Typeface.create(editAccountEmailEditText.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);
                    editAccountPhoneNumberEditText.setTypeface(Typeface.create(editAccountPhoneNumberEditText.getTypeface(), Typeface.NORMAL), Typeface.NORMAL);

                } else {
                    Toast.makeText(context, R.string.account_does_not_exist, Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }
}