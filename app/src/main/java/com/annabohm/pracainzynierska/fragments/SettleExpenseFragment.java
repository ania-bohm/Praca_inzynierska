package com.annabohm.pracainzynierska.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

import com.annabohm.pracainzynierska.datamodels.CommonExpense;
import com.annabohm.pracainzynierska.datamodels.Event;
import com.annabohm.pracainzynierska.singletons.FirestoreInstanceSingleton;
import com.annabohm.pracainzynierska.activities.MainActivity;
import com.annabohm.pracainzynierska.R;
import com.annabohm.pracainzynierska.adapters.SettleExpenseAdapter;
import com.annabohm.pracainzynierska.adapters.SettleExpensePerPersonAdapter;
import com.annabohm.pracainzynierska.datamodels.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class SettleExpenseFragment extends Fragment {
    NavController navController;
    AlertDialog alertDialog;
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    CollectionReference commonExpenseLists = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("CommonExpenseLists");
    CollectionReference users = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Users");
    CollectionReference eventAttendees = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("EventAttendees");
    DocumentReference eventReference;
    Context context;
    Bundle bundle;
    String eventId, eventAuthor;
    ListView settleExpenseListView, settleExpensePerPersonListView;
    TextView settleExpenseEmptyTextView, settleExpensePerPersonTextView, settleExpenseTextView;
    HashMap<String, Double> expensePerPersonHashMap;
    HashMap<String, Double> expenseToSettleHashMap;
    SettleExpensePerPersonAdapter settleExpensePerPersonAdapter;
    SettleExpenseAdapter settleExpenseAdapter;
    double totalAmountToSettle = 0;

    public SettleExpenseFragment() {
    }

    public static SettleExpenseFragment newInstance() {
        return new SettleExpenseFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setDrawerLocked();
        if (((MainActivity) requireActivity()).getSupportActionBar() != null) {
            Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).hide();
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
        assert bundle != null;
        String path = bundle.getString("path");
        eventReference = firestoreInstanceSingleton.getFirebaseFirestoreRef().document(path);
        eventId = eventReference.getId();

        expensePerPersonHashMap = new HashMap<>();
        expenseToSettleHashMap = new HashMap<>();

        settleExpensePerPersonAdapter = new SettleExpensePerPersonAdapter(context, expensePerPersonHashMap);
        settleExpensePerPersonListView.setAdapter(settleExpensePerPersonAdapter);

        settleExpenseAdapter = new SettleExpenseAdapter(context, expenseToSettleHashMap);
        settleExpenseListView.setAdapter(settleExpenseAdapter);

        alertDialog.show();
        loadData();
    }

    public void loadData() {
        eventReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    eventAuthor = Objects.requireNonNull(documentSnapshot.toObject(Event.class)).getEventAuthor();
                }
                populateHashMapWithGuestId();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    public void populateHashMapWithGuestId() {
        eventAttendees.document(eventId).collection("Confirmed").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.exists()) {
                            String guestId = Objects.requireNonNull(documentSnapshot.get("User")).toString();
                            expensePerPersonHashMap.put(guestId, (double) 0);
                        }
                    }
                    expensePerPersonHashMap.put(eventAuthor, (double) 0);
                    calculateSettleExpensePerPersonId();
                } else {
                    settleExpenseEmptyTextView.setVisibility(View.VISIBLE);
                    alertDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    public void calculateSettleExpensePerPersonId() {
        commonExpenseLists.document(eventId).collection("CommonExpenseList").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.exists()) {
                            CommonExpense currentCommonExpense = documentSnapshot.toObject(CommonExpense.class);
                            assert currentCommonExpense != null;
                            if (currentCommonExpense.isCommonExpenseToSettle()) {
                                long commonExpenseValueLong = currentCommonExpense.getCommonExpenseValue();
                                double commonExpenseValueDouble = commonExpenseValueLong / 100;
                                totalAmountToSettle += commonExpenseValueDouble;
                                String payingUserId = currentCommonExpense.getCommonExpensePayingUserId();
                                expensePerPersonHashMap.put(payingUserId, expensePerPersonHashMap.get(payingUserId) + commonExpenseValueDouble);
                            }
                        }
                    }
                }
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
                            assert user != null;
                            idToDisplayNameList[index] = user.getUserFirstName() + " " + user.getUserLastName();
                        }
                    }
                }
                for (int i = 0; i < idToDisplayNameList.length; i++) {
                    if (idToDisplayNameList[i] != null) {
                        expensePerPersonHashMap.put(idToDisplayNameList[i], expensePerPersonHashMap.remove(idList.get(i)));
                    }
                }
                settleExpensePerPersonAdapter.setExpensePerPersonHashMap(expensePerPersonHashMap);
                settleExpensePerPersonAdapter.notifyDataSetChanged();
                calculateSettleExpense();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    public void calculateSettleExpense() {
        double averageAmountToPay = totalAmountToSettle/expensePerPersonHashMap.size();
        for (Map.Entry<String, Double> set : expensePerPersonHashMap.entrySet()) {
            String displayName = set.getKey();
            double valueToSettle = set.getValue() - averageAmountToPay;
            valueToSettle = round(valueToSettle, 2);
            expenseToSettleHashMap.put(displayName, valueToSettle);
        }
        settleExpenseAdapter.setExpenseHashMap(expenseToSettleHashMap);
        settleExpenseAdapter.notifyDataSetChanged();
        alertDialog.dismiss();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
