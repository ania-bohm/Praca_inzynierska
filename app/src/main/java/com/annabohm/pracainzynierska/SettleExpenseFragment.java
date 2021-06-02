package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class SettleExpenseFragment extends Fragment {
    NavController navController;
    AlertDialog alertDialog;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference commonExpenseLists = db.collection("CommonExpenseLists");
    CollectionReference events = db.collection("Events");
    CollectionReference users = db.collection("Users");
    DocumentReference eventReference;
    Context context;
    Bundle bundle;
    String eventId;
    String currentUserId = firebaseAuth.getCurrentUser().getUid();
    ListView settleExpenseListView, settleExpensePerPersonListView;
    TextView settleExpenseEmptyTextView, settleExpensePerPersonTextView, settleExpenseTextView;
    HashMap<String, Double> expensePerPersonHashMap;
    ArrayList<String> userNameList;
    ArrayList<Double> expenseList;
    SettleExpenseAdapter settleExpenseAdapter;

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
        settleExpensePerPersonTextView = view.findViewById(R.id.settleExpensePerPersonTextView);
        settleExpenseTextView = view.findViewById(R.id.settleExpenseTextView);

        settleExpenseEmptyTextView.setVisibility(View.GONE);
        settleExpensePerPersonTextView.setVisibility(View.GONE);
        settleExpenseTextView.setVisibility(View.GONE);

        alertDialog = new SpotsDialog(context);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        eventReference = db.document(path);
        eventId = eventReference.getId();

        expensePerPersonHashMap = new HashMap<>();
        userNameList = new ArrayList<>();
        expenseList = new ArrayList<>();

        settleExpenseAdapter = new SettleExpenseAdapter(context, expenseList, userNameList);
        settleExpensePerPersonListView.setAdapter(settleExpenseAdapter);
        alertDialog.show();
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
                Toast.makeText(context, "hashmapsize: " + expensePerPersonHashMap.size(), Toast.LENGTH_SHORT).show();
                if (expensePerPersonHashMap.isEmpty()) {
                    settleExpenseEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    settleExpensePerPersonTextView.setVisibility(View.VISIBLE);
                    if (expensePerPersonHashMap.size() > 1) {
                        settleExpenseTextView.setVisibility(View.VISIBLE);
                    }
                }
                convertPersonIdToDisplayName();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    public void convertPersonIdToDisplayName() {
        final ArrayList<String> idList = new ArrayList<String>(expensePerPersonHashMap.keySet());
        final String[] idToDisplayNameList = new String[idList.size()];

        users.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (idList.contains(documentSnapshot.getId())) {
                            int index = idList.indexOf(documentSnapshot.getId());
                            User user = documentSnapshot.toObject(User.class);
                            idToDisplayNameList[index] = user.getUserFirstName() + " " + user.getUserLastName();
                        }
                    }
                }
                for (int i = 0; i < idToDisplayNameList.length; i++) {
                    if (idToDisplayNameList[i] != null) {
                        expensePerPersonHashMap.put(idToDisplayNameList[i], expensePerPersonHashMap.remove(idList.get(i)));
                    }
                }
                expenseList = new ArrayList<>(expensePerPersonHashMap.values());
                userNameList = new ArrayList<>(expensePerPersonHashMap.keySet());
                settleExpenseAdapter.setExpenseList(expenseList);
                settleExpenseAdapter.setUserNameList(userNameList);
                settleExpenseAdapter.notifyDataSetChanged();
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
