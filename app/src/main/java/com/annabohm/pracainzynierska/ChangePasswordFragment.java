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
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
    boolean hiddenPassword = true;
    Context context;

    private View.OnClickListener confirmChangePasswordOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String oldPassword = changePasswordOldEditText.getText().toString();
            final String newPassword = changePasswordNewEditText.getText().toString();
            String repeatPassword = changePasswordRepeatEditText.getText().toString();

            if (!changePasswordCorrect(oldPassword, newPassword, repeatPassword)) {
                return;
            }

            changePassword(newPassword);
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
            if (hiddenPassword) {
                changePasswordOldEditText.setTransformationMethod(null);
            } else {
                changePasswordOldEditText.setTransformationMethod(new PasswordTransformationMethod());
            }
            hiddenPassword = !hiddenPassword;
        }
    };

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    public static ChangePasswordFragment newInstance(String param1, String param2) {
        ChangePasswordFragment fragment = new ChangePasswordFragment();
        return fragment;
    }

    private boolean checkOldPasswordCorrectness(String oldPassword) {
        if (sharedPreferences.getString(Password, null).equals(oldPassword)) {
            return true;
        }
        return false;
    }

    private boolean checkNewPasswordCorrectness(String newPassword, String repeatPassword) {
        if (newPassword.equals(repeatPassword)) {
            return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getActivity().getSharedPreferences(myPreference, Context.MODE_PRIVATE);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setDrawerLocked();
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

    public boolean changePasswordCorrect(String oldPassword, String newPassword, String repeatPassword) {
        if (TextUtils.isEmpty(oldPassword)) {
            changePasswordOldEditText.setError(getString(R.string.change_password_error_empty));
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            changePasswordNewEditText.setError(getString(R.string.change_password_error_new_empty));
            return false;
        }

        if (TextUtils.isEmpty(repeatPassword)) {
            changePasswordNewEditText.setError(getString(R.string.change_password_error_repeat_empty));
            return false;
        }

        if (!checkOldPasswordCorrectness(oldPassword)) {
            changePasswordOldEditText.setError(getString(R.string.change_password_error_old_incorrect));
            return false;
        }

        if (newPassword.length() < 6) {
            changePasswordNewEditText.setError(getString(R.string.change_password_error_too_short));
            return false;
        }

        if (repeatPassword.length() < 6) {
            changePasswordRepeatEditText.setError(getString(R.string.change_password_error_too_short));
            return false;
        }

        if (!checkNewPasswordCorrectness(newPassword, repeatPassword)) {
            changePasswordRepeatEditText.setError(getString(R.string.change_password_error_mismatch));
            return false;
        }
        return true;
    }

    public void changePassword(final String newPassword) {
        authCredential = EmailAuthProvider.getCredential(sharedPreferences.getString(Email, null), sharedPreferences.getString(Password, null));
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
                                        Toast.makeText(context, R.string.change_password_success, Toast.LENGTH_SHORT).show();
                                        navController.popBackStack();
                                    } else {
                                        Toast.makeText(context, R.string.change_password_fail, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }
}