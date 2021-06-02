package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;

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
    String eventId;
    String currentUserId = firebaseAuth.getCurrentUser().getUid();
    ListView settleExpenseListView, settleExpensePerPersonListView;
    TextView settleExpenseEmptyTextView;
    HashMap<String, Double> expensePerPersonHashMap;

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

        settleExpenseListView = view.findViewById(R.id.settleExpenseListView);
        settleExpensePerPersonListView = view.findViewById(R.id.settleExpensePerPersonListView);
        settleExpenseEmptyTextView = view.findViewById(R.id.settleExpenseEmptyTextView);

        alertDialog = new SpotsDialog(context);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        eventReference = db.document(path);
        eventId = eventReference.getId();

        expensePerPersonHashMap = new HashMap<>();
        calculateSettleExpensePerPersonId();
    }

    public void calculateSettleExpensePerPersonId() {
        commonExpenseLists.document(eventId).collection("CommonExpenseList").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.exists()) {
                            CommonExpense currentCommonExpense = documentSnapshot.toObject(CommonExpense.class);
                            if (currentCommonExpense.isCommonExpenseToSettle()) {
                                long commonExpenseValueLong = currentCommonExpense.getCommonExpenseValue();
                                double commonExpenseValueDouble = commonExpenseValueLong / 100;
                                String payingUserId = currentCommonExpense.getCommonExpensePayingUserId();
                                if (expensePerPersonHashMap.containsKey(payingUserId)) {
                                    expensePerPersonHashMap.put(payingUserId, expensePerPersonHashMap.get(payingUserId) + commonExpenseValueDouble);
                                } else {
                                    expensePerPersonHashMap.put(payingUserId, commonExpenseValueDouble);
                                }
                            }
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public HashMap<String, Double> convertPersonIdToDisplayName() {
        return null;
    }

}
