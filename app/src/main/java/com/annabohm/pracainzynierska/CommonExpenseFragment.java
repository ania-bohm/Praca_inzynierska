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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class CommonExpenseFragment extends Fragment {
    NavController navController;
    CommonExpenseFragment fragmentThis;
    ArrayList<CommonExpense> commonExpenseList;
    RecyclerView.LayoutManager layoutManager;
    CommonExpenseAdapter commonExpenseAdapter;
    TextView displayCommonExpenseSumTextView, displayCommonExpenseToSettleTextView;
    MaterialEditText commonExpenseTitleMaterialEditText, commonExpenseValueMaterialEditText;
    FloatingActionButton commonExpenseFloatingActionButton, commonExpenseSumUpFloatingActionButton;
    RecyclerView commonExpenseRecyclerView;
    AlertDialog alertDialog;
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    FirebaseAuthInstanceSingleton firebaseAuthInstanceSingleton = FirebaseAuthInstanceSingleton.getInstance();
    CollectionReference commonExpenseLists = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("CommonExpenseLists");
    DocumentReference eventReference;
    Context context;
    Bundle bundle;
    boolean isUpdate = false;
    String eventId, eventAuthor;
    String commonExpenseItemToUpdateId = "";
    String currentUserId = Objects.requireNonNull(firebaseAuthInstanceSingleton.getFirebaseAuthRef().getCurrentUser()).getUid();

    public CommonExpenseFragment() {
    }

    public static CommonExpenseFragment newInstance() {
        return new CommonExpenseFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        fragmentThis = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setDrawerLocked();
        if (((MainActivity) requireActivity()).getSupportActionBar() != null) {
            Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).hide();
        }
        return inflater.inflate(R.layout.fragment_common_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        displayCommonExpenseSumTextView = view.findViewById(R.id.displayCommonExpenseSumTextView);
        displayCommonExpenseToSettleTextView = view.findViewById(R.id.displayCommonExpenseToSettleTextView);
        commonExpenseTitleMaterialEditText = view.findViewById(R.id.commonExpenseTitleMaterialEditText);
        commonExpenseValueMaterialEditText = view.findViewById(R.id.commonExpenseValueMaterialEditText);
        commonExpenseFloatingActionButton = view.findViewById(R.id.commonExpenseFloatingActionButton);
        commonExpenseSumUpFloatingActionButton = view.findViewById(R.id.commonExpenseSumUpFloatingActionButton);
        commonExpenseRecyclerView = view.findViewById(R.id.commonExpenseRecyclerView);

        bundle = this.getArguments();
        assert bundle != null;
        String path = bundle.getString("path");
        eventReference = firestoreInstanceSingleton.getFirebaseFirestoreRef().document(path);
        eventId = eventReference.getId();
        eventReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eventAuthor = Objects.requireNonNull(documentSnapshot.toObject(Event.class)).getEventAuthor();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
        commonExpenseList = new ArrayList<>();
        alertDialog = new SpotsDialog(context);

        commonExpenseRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        commonExpenseRecyclerView.setLayoutManager(layoutManager);
        commonExpenseAdapter = new CommonExpenseAdapter(fragmentThis, commonExpenseList, eventId, currentUserId);
        commonExpenseRecyclerView.setAdapter(commonExpenseAdapter);

        commonExpenseValueMaterialEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
                String str = Objects.requireNonNull(commonExpenseValueMaterialEditText.getText()).toString();
                if (str.isEmpty()) return;
                String str2 = perfectDecimal(str, 6, 2);

                if (!str2.equals(str)) {
                    commonExpenseValueMaterialEditText.setText(str2);
                    commonExpenseValueMaterialEditText.setSelection(str2.length());
                }
            }
        });

        loadData();

        commonExpenseFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commonExpenseValueMaterialEditText.getText().toString().trim().isEmpty() || commonExpenseTitleMaterialEditText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, R.string.event_budget_empty_fields, Toast.LENGTH_SHORT).show();
                    return;
                }
                double commonExpenseValueDouble = Double.parseDouble(commonExpenseValueMaterialEditText.getText().toString());
                long commonExpenseValueLong = (long) (commonExpenseValueDouble * 100);
                if (!isUpdate) {
                    setData(commonExpenseTitleMaterialEditText.getText().toString().trim(), commonExpenseValueLong);
                } else {
                    updateData(commonExpenseTitleMaterialEditText.getText().toString().trim(), commonExpenseValueLong);
                    isUpdate = !isUpdate;
                }
                commonExpenseTitleMaterialEditText.setText("");
                commonExpenseValueMaterialEditText.setText("");
            }
        });

        commonExpenseSumUpFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.commonExpenseToSettleExpense, bundle);
            }
        });
    }

    public String perfectDecimal(String str, int MAX_BEFORE_POINT, int MAX_DECIMAL) {
        if (str.charAt(0) == '.') str = "0" + str;
        int max = str.length();

        StringBuilder rFinal = new StringBuilder();
        boolean after = false;
        int i = 0, up = 0, decimal = 0;
        char t;
        while (i < max) {
            t = str.charAt(i);
            if (t != '.' && !after) {
                up++;
                if (up > MAX_BEFORE_POINT) return rFinal.toString();
            } else if (t == '.') {
                after = true;
            } else {
                decimal++;
                if (decimal > MAX_DECIMAL)
                    return rFinal.toString();
            }
            rFinal.append(t);
            i++;
        }
        return rFinal.toString();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(getString(R.string.event_cost_delete))) {
            if (currentUserId.equals(eventAuthor) || currentUserId.equals(commonExpenseList.get(item.getOrder()).getCommonExpensePayingUserId())) {
                deleteItem(item.getOrder());
            } else {
                Toast.makeText(context, R.string.common_expense_error, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onContextItemSelected(item);
    }

    private void deleteItem(int index) {
        commonExpenseLists.document(eventId).collection("CommonExpenseList").document(commonExpenseList.get(index).getCommonExpenseId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                loadData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    private void updateData(String title, long value) {
        commonExpenseLists.document(eventId)
                .collection("CommonExpenseList")
                .document(commonExpenseItemToUpdateId)
                .update("commonExpenseTitle", title, "commonExpenseValue", value)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
        commonExpenseLists.document(eventId).collection("CommonExpenseList").document(commonExpenseItemToUpdateId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                loadData();
            }
        });
    }

    private void setData(String title, long value) {
        Map<String, Object> commonExpenseObject = new HashMap<>();
        String id = UUID.randomUUID().toString();
        commonExpenseObject.put("commonExpenseId", id);
        commonExpenseObject.put("commonExpenseTitle", title);
        commonExpenseObject.put("commonExpensePayingUserId", currentUserId);
        commonExpenseObject.put("commonExpenseValue", value);
        commonExpenseObject.put("commonExpenseToSettle", false);
        commonExpenseLists.document(eventId).collection("CommonExpenseList").document(id).set(commonExpenseObject).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                loadData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }

    public void loadData() {
        if (commonExpenseList.size() > 0) {
            commonExpenseList.clear();
        }
        alertDialog.show();
        commonExpenseLists.document(eventId).collection("CommonExpenseList").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                    if (documentSnapshot.exists()) {
                        CommonExpense commonExpense = documentSnapshot.toObject(CommonExpense.class);
                        commonExpenseList.add(commonExpense);
                    }
                }
                long commonExpenseSumLong = 0;
                long commonExpenseToSettleLong = 0;
                for (CommonExpense commonExpense : commonExpenseList) {
                    if (commonExpense.isCommonExpenseToSettle()) {
                        commonExpenseToSettleLong += commonExpense.getCommonExpenseValue();
                    }
                    commonExpenseSumLong += commonExpense.getCommonExpenseValue();
                }
                double commonExpenseSumDouble = commonExpenseSumLong / 100;
                double commonExpenseToSettleDouble = commonExpenseToSettleLong / 100;
                displayCommonExpenseSumTextView.setText(String.valueOf(commonExpenseSumDouble));
                displayCommonExpenseToSettleTextView.setText(String.valueOf(commonExpenseToSettleDouble));
                commonExpenseAdapter.notifyDataSetChanged();
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