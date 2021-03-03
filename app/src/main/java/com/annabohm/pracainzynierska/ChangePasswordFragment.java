package com.annabohm.pracainzynierska;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.content.ContentValues.TAG;

public class ChangePasswordFragment extends Fragment {

    public static final String myPreference = "myPref";
    public static final String Password = "Password";
    public static final String Email = "Email";
    NavController navController;
    Button confirmChangePasswordButton;
    Button rejectChangePasswordButton;
    ImageView changePasswordShowPasswordButton;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    AuthCredential authCredential;
    SharedPreferences sharedPreferences;
    EditText changePasswordOldEditText, changePasswordNewEditText, changePasswordRepeatEditText;

    private View.OnClickListener confirmChangePasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String oldPassword = changePasswordOldEditText.getText().toString();
            final String newPassword = changePasswordNewEditText.getText().toString();
            String repeatPassword = changePasswordRepeatEditText.getText().toString();

            if (TextUtils.isEmpty(oldPassword)) {
                changePasswordOldEditText.setError("Password is required");
                return;
            }

            if (TextUtils.isEmpty(newPassword)) {
                changePasswordNewEditText.setError("New password is required");
                return;
            }

            if (TextUtils.isEmpty(repeatPassword)) {
                changePasswordNewEditText.setError("Repeating password is required");
                return;
            }

            if(!checkOldPasswordCorrectness(oldPassword)) {
                changePasswordOldEditText.setError("Old password is incorrect");
                return;
            }

            if (newPassword.length() < 6) {
                changePasswordNewEditText.setError("Password must be at least 6 characters long");
                return;
            }

            if (repeatPassword.length() < 6) {
                changePasswordRepeatEditText.setError("Password must be at least 6 characters long");
                return;
            }

            if(!checkNewPasswordCorrectness(newPassword, repeatPassword)) {
                changePasswordRepeatEditText.setError("The passwords do not match");
                return;
            }

            // Get auth credentials from the user for re-authentication. The example below shows
            // email and password credentials but there are multiple possible providers,
            // such as GoogleAuthProvider or FacebookAuthProvider.
            authCredential = EmailAuthProvider.getCredential(sharedPreferences.getString(Email, null), sharedPreferences.getString(Password, null));
            // Prompt the user to re-provide their sign-in credentials
            firebaseUser.reauthenticate(authCredential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                firebaseUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString(Password, newPassword);
                                            editor.commit();
                                            Log.d(TAG, "Password updated");
                                        } else {
                                            Log.d(TAG, "Error password not updated");
                                        }
                                    }
                                });
                            } else {
                                Log.d(TAG, "Error auth failed");
                            }
                        }
                    });
            navController.popBackStack();
        }
    };

    private boolean checkOldPasswordCorrectness(String oldPassword) {
        if(sharedPreferences.getString(Password, null).equals(oldPassword)) {
            return true;
        }
        return false;
    }

    private boolean checkNewPasswordCorrectness(String newPassword, String repeatPassword) {
        if(newPassword.equals(repeatPassword)) {
            return true;
        }
        return false;
    }

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
        sharedPreferences = this.getActivity().getSharedPreferences(myPreference, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setDrawerLocked();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        confirmChangePasswordButton = view.findViewById(R.id.confirmChangePasswordButton);
        rejectChangePasswordButton = view.findViewById(R.id.rejectChangePasswordButton);
        changePasswordShowPasswordButton = view.findViewById(R.id.changePasswordShowPasswordButton);
        changePasswordOldEditText = view.findViewById(R.id.changePasswordOldEditText);
        changePasswordNewEditText = view.findViewById(R.id.changePasswordNewEditText);
        changePasswordRepeatEditText = view.findViewById(R.id.changePasswordRepeatEditText);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        confirmChangePasswordButton.setOnClickListener(confirmChangePasswordOnClickListener);
        rejectChangePasswordButton.setOnClickListener(rejectChangePasswordOnClickListener);
        changePasswordShowPasswordButton.setOnClickListener(changePasswordShowPasswordOnClickListener);
    }
}