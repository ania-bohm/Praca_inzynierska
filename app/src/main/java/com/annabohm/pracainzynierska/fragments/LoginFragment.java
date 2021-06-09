package com.annabohm.pracainzynierska.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.annabohm.pracainzynierska.singletons.FirebaseAuthInstanceSingleton;
import com.annabohm.pracainzynierska.activities.MainActivity;
import com.annabohm.pracainzynierska.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginFragment extends Fragment {
    public static final String myPreference = "myPref";
    public static final String Password = "Password";
    public static final String Email = "Email";
    NavController navController;
    Button registerButton;
    Button loginButton;
    EditText loginEmailEditText, loginPasswordEditText;
    ImageView loginShowPasswordButton;
    ProgressBar loginProgressBar;
   FirebaseAuthInstanceSingleton firebaseAuthInstanceSingleton = FirebaseAuthInstanceSingleton.getInstance();
    Context context;
    SharedPreferences sharedPreferences;
    InputMethodManager imm;
    boolean hiddenPassword = true;
    private final View.OnClickListener registerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            navController.navigate(R.id.loginToRegister);
        }
    };

    private final View.OnClickListener loginShowPasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (hiddenPassword) {
                loginPasswordEditText.setTransformationMethod(null);
            } else {
                loginPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
            }
            hiddenPassword = !hiddenPassword;
        }
    };
    private final View.OnClickListener loginOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String loginEmail = loginEmailEditText.getText().toString().trim();
            final String loginPassword = loginPasswordEditText.getText().toString();

            hideKeyboard();

            if(!loginCorrect(loginEmail, loginPassword)){
                return;
            }

            loginProgressBar.setVisibility(View.VISIBLE);

            login(loginEmail, loginPassword);
        }
    };

    public LoginFragment() {
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        sharedPreferences = this.requireActivity().getSharedPreferences(myPreference, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setDrawerLocked();
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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

        registerButton.setOnClickListener(registerOnClickListener);
        loginShowPasswordButton.setOnClickListener(loginShowPasswordOnClickListener);
        loginButton.setOnClickListener(loginOnClickListener);
    }

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(loginButton.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public boolean loginCorrect(String loginEmail, String loginPassword) {

        if (TextUtils.isEmpty(loginEmail)) {
            loginEmailEditText.setError(getString(R.string.login_email_error_empty));
            return false;
        }

        if (TextUtils.isEmpty(loginPassword)) {
            loginEmailEditText.setError(getString(R.string.login_password_error_empty));
            return false;
        }

        if (loginPassword.length() < 6) {
            loginPasswordEditText.setError(getString(R.string.login_password_error_too_short));
            return false;
        }
        return true;
    }

    public void login(final String loginEmail, final String loginPassword){
        firebaseAuthInstanceSingleton.getFirebaseAuthRef().signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Email, loginEmail);
                    editor.putString(Password, loginPassword);
                    editor.apply();
                    Toast.makeText(context, R.string.login_success, Toast.LENGTH_SHORT).show();
                    loginEmailEditText.setText("");
                    loginPasswordEditText.setText("");
                    loginEmailEditText.setHint("");
                    loginPasswordEditText.setHint("");
                    navController.navigate(R.id.loginToMain);
                } else {
                    Toast.makeText(context, R.string.login_fail, Toast.LENGTH_SHORT).show();
                    loginProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}