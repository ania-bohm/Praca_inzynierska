package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import dmax.dialog.SpotsDialog;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class AccountSettingsFragment extends Fragment {

    NavController navController;
    ImageView editAccountPhotoImageView;
    ImageView editPersonalDataImageView;
    ImageView accountPhotoImageView;
    Button accountChangePasswordButton;
    Button accountPhotoDeleteButton;
    TextView accountFirstNameTextView, accountLastNameTextView, accountPhoneNumberTextView, accountEmailTextView;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    DocumentReference documentReference;
    StorageReference storageReference;
    String photoUri;
    Context context;
    AlertDialog alertDialog;

    private View.OnClickListener editAccountPhotoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            chooseFile();
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
    private View.OnClickListener accountPhotoDeleteOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            deleteOldPhoto();
            documentReference.update("userPhoto", "");
            reloadFragment();
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
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        alertDialog = new SpotsDialog(context);
        editAccountPhotoImageView = view.findViewById(R.id.editAccountPhotoImageView);
        editPersonalDataImageView = view.findViewById(R.id.editPersonalDataImageView);
        accountPhotoImageView = view.findViewById(R.id.accountPhotoImageView);
        accountChangePasswordButton = view.findViewById(R.id.accountChangePasswordButton);
        accountPhotoDeleteButton = view.findViewById(R.id.accountPhotoDeleteButton);
        accountFirstNameTextView = view.findViewById(R.id.accountFirstNameTextView);
        accountLastNameTextView = view.findViewById(R.id.accountLastNameTextView);
        accountPhoneNumberTextView = view.findViewById(R.id.accountPhoneNumberTextView);
        accountEmailTextView = view.findViewById(R.id.accountEmailTextView);
        storageReference = FirebaseStorage.getInstance().getReference("Images");
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        documentReference = firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid());
        editAccountPhotoImageView.setOnClickListener(editAccountPhotoOnClickListener);
        editPersonalDataImageView.setOnClickListener(editPersonalDataOnClickListener);
        accountChangePasswordButton.setOnClickListener(accountChangePasswordOnClickListener);
        accountPhotoDeleteButton.setOnClickListener(accountPhotoDeleteOnClickListener);
        alertDialog.show();
        displayPersonalData();
    }

    public void displayPersonalData() {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = new User();
                if (documentSnapshot.exists()) {
                    user = documentSnapshot.toObject(User.class);
                    photoUri = user.getUserPhoto();
                    if (photoUri != null && photoUri != "") {
                        Picasso.get()
                                .load(user.getUserPhoto().trim())
                                .transform(new CropCircleTransformation())
                                .into(accountPhotoImageView);
                    }
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
                    Toast.makeText(context, R.string.account_does_not_exist, Toast.LENGTH_SHORT).show();
                }
                alertDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, R.string.account_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    public void reloadFragment() {
        getFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
        displayPersonalData();
    }

    private String getExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                Uri imageUri = data.getData();
                final StorageReference mRef = storageReference.child(System.currentTimeMillis() + "." + getExtension(imageUri));
                mRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                documentReference.update("userPhoto", uri.toString());
                                deleteOldPhoto();
                                displayPersonalData();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });
            }
        }
    }

    public void deleteOldPhoto() {
        photoUri = getPhotoUri();
        if (photoUri != "" && photoUri != null) {
            final StorageReference oldPhotoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUri);
            if (oldPhotoRef != null) {
                oldPhotoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        reloadFragment();
                        displayPersonalData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG, exception.toString());
                    }
                });
            }
        }
    }

    public String getPhotoUri() {
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = new User();
                if (documentSnapshot.exists()) {
                    user = documentSnapshot.toObject(User.class);
                    photoUri = user.getUserPhoto();
                }
            }
        });
        return photoUri;
    }
}
