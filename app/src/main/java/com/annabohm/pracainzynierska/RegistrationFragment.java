package com.annabohm.pracainzynierska;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrationFragment extends Fragment {

    private static final String TAG = "DEV'S TAG";
    NavController navController;
    Button createAccountButton, backToLoginButton;
    ImageView registerShowPasswordButton;
    EditText registerFirstNameEditText, registerLastNameEditText, registerEmailEditText, registerPhoneNumberEditText, registerPasswordEditText;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    ProgressBar registerProgressBar;
    Context context;
    String userID, registerFirstName, registerLastName, registerEmail, registerPassword, registerPhoneNumber;
    private View.OnClickListener backToLoginOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };
    private View.OnClickListener registerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            registerFirstName = registerFirstNameEditText.getText().toString();
            registerLastName = registerLastNameEditText.getText().toString();
            registerPhoneNumber = registerPhoneNumberEditText.getText().toString().trim();
            registerEmail = registerEmailEditText.getText().toString().trim();
            registerPassword = registerPasswordEditText.getText().toString();

            if (TextUtils.isEmpty(registerFirstName)) {
                registerFirstNameEditText.setError(getString(R.string.first_name_empty));
                return;
            }

            if (TextUtils.isEmpty(registerLastName)) {
                registerEmailEditText.setError(getString(R.string.last_name_empty));
                return;
            }

            registerFirstName = formatString(registerFirstName);
            registerLastName = formatString(registerLastName);

            if (TextUtils.isEmpty(registerPhoneNumber)) {
                registerEmailEditText.setError(getString(R.string.phone_number_empty));
                return;
            }

            if (TextUtils.isEmpty(registerEmail)) {
                registerEmailEditText.setError(getString(R.string.email1_empty));
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(registerEmailEditText.getText().toString()).matches()) {
                registerEmailEditText.setError(getString(R.string.email1_does_not_match_pattern));
                return;
            }

            if (TextUtils.isEmpty(registerPassword)) {
                registerEmailEditText.setError(getString(R.string.password1_empty));
                return;
            }

            if (registerPassword.length() < 6) {
                registerPasswordEditText.setError(getString(R.string.password1_too_short));
                return;
            }

            checkEmailExistsOrNot(registerEmailEditText, registerPasswordEditText, registerFirstNameEditText, registerLastNameEditText, registerPhoneNumberEditText);
        }
    };

    public RegistrationFragment() {
    }

    public static RegistrationFragment newInstance(String param1, String param2) {
        RegistrationFragment fragment = new RegistrationFragment();
        return fragment;
    }

    public String formatString(String text) {
        String[] textArray = text.split(" ");
        String formattedText = "";
        for (int i = 0; i < textArray.length; i++) {
            formattedText += textArray[i].substring(0, 1).toUpperCase() + textArray[i].substring(1).toLowerCase() + " ";
        }
        formattedText = formattedText.trim();
        return formattedText;
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
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        createAccountButton = view.findViewById(R.id.createAccountButton);
        backToLoginButton = view.findViewById(R.id.backToLoginButton);
        registerShowPasswordButton = view.findViewById(R.id.registerShowPasswordButton);
        registerFirstNameEditText = view.findViewById(R.id.registerFirstNameEditText);
        registerLastNameEditText = view.findViewById(R.id.registerLastNameEditText);
        registerEmailEditText = view.findViewById(R.id.registerEmailEditText);
        registerPhoneNumberEditText = view.findViewById(R.id.registerPhoneNumberEditText);
        registerPasswordEditText = view.findViewById(R.id.registerPasswordEditText);
        registerProgressBar = view.findViewById(R.id.registerProgressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        createAccountButton.setOnClickListener(registerOnClickListener);
        backToLoginButton.setOnClickListener(backToLoginOnClickListener);
    }

    private void checkEmailExistsOrNot(final EditText registerEmailEditText, final EditText registerPasswordEditText, final EditText registerFirstNameEditText, final EditText registerLastNameEditText, final EditText registerPhoneNumberEditText) {
        firebaseAuth.fetchSignInMethodsForEmail(registerEmailEditText.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                Log.d(TAG, "" + task.getResult().getSignInMethods().size());
                if (task.getResult().getSignInMethods().size() == 0) {
                    registerProgressBar.setVisibility(View.VISIBLE);
                    registerUser(registerEmailEditText, registerPasswordEditText, registerFirstNameEditText, registerLastNameEditText, registerPhoneNumberEditText);
                } else {
                    registerEmailEditText.setError(getString(R.string.email1_already_exists));
                    return;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void registerUser(EditText registerEmailEditText, EditText registerPasswordEditText, EditText registerFirstNameEditText, EditText registerLastNameEditText, EditText registerPhoneNumberEditText) {
        firebaseAuth.createUserWithEmailAndPassword(registerEmailEditText.getText().toString().trim(), registerPasswordEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    userID = firebaseAuth.getCurrentUser().getUid();
                    documentReference = firebaseFirestore.collection("Users").document(userID);
                    User newUser = new User(registerFirstName, registerLastName, registerEmail, registerPhoneNumber);
                    documentReference.set(newUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    });
                    Toast.makeText(context, R.string.register_success, Toast.LENGTH_SHORT).show();
                    navController.popBackStack();
                } else {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}