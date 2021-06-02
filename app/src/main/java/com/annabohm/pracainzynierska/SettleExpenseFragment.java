package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class SettleExpenseFragment extends Fragment {
    NavController navController;
    AlertDialog alertDialog;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference commonExpenseLists = db.collection("CommonExpenseLists");
    CollectionReference events = db.collection("Events");
    DocumentReference eventReference;
    Context context;
    Bundle bundle;
    boolean isUpdate = false;
    String eventId;
    String currentUserId = firebaseAuth.getCurrentUser().getUid();

    public SettleExpenseFragment() {
    }

    public static SettleExpenseFragment newInstance(String param1, String param2) {
        SettleExpenseFragment fragment = new SettleExpenseFragment();
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
        if (((MainActivity) getActivity()).getSupportActionBar() != null) {
            ((MainActivity) getActivity()).getSupportActionBar().hide();
        }
        return inflater.inflate(R.layout.fragment_settle_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        alertDialog = new SpotsDialog(context);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        eventReference = db.document(path);
        eventId = eventReference.getId();
    }
}
