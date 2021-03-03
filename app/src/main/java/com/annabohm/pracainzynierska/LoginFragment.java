package com.annabohm.pracainzynierska;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    NavController navController;
    Button registerButton;
    Button loginButton;
    EditText loginEmailEditText, loginPasswordEditText;
    ImageView loginShowPasswordButton;
    ProgressBar loginProgressBar;
    FirebaseAuth firebaseAuth;
    Context context;
    private View.OnClickListener registerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            navController.navigate(R.id.loginToRegister);
        }
    };
    private View.OnClickListener loginOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String loginEmail = loginEmailEditText.getText().toString().trim();
            String loginPassword = loginPasswordEditText.getText().toString();


            if (TextUtils.isEmpty(loginEmail)) {
                loginEmailEditText.setError("Email is required");
                return;
            }

            if (TextUtils.isEmpty(loginPassword)) {
                loginEmailEditText.setError("Password is required");
                return;
            }

            if (loginPassword.length() < 6) {
                loginPasswordEditText.setError("Password must be at least 6 characters long");
                return;
            }

            loginProgressBar.setVisibility(View.VISIBLE);

            //authenticate the user
            firebaseAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.loginToMain);
                    } else {
                        Toast.makeText(context, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loginProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });

            //checkEmailExistsOrNot(loginEmailEditText, loginPasswordEditText);
            //navController.navigate(R.id.loginToMain);
        }
    };

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        registerButton = view.findViewById(R.id.registerButton);
        loginButton = view.findViewById(R.id.loginButton);
        loginShowPasswordButton = view.findViewById(R.id.loginShowPasswordButton);
        loginEmailEditText = view.findViewById(R.id.loginEmailEditText);
        loginPasswordEditText = view.findViewById(R.id.loginPasswordEditText);
        loginProgressBar = view.findViewById(R.id.loginProgressBar);
        firebaseAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(registerOnClickListener);
        loginButton.setOnClickListener(loginOnClickListener);
    }


}