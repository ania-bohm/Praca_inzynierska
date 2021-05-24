package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class EventBudgetFragment extends Fragment {
    EventBudgetFragment fragmentThis;
    ArrayList<EventCost> eventCostList;
    RecyclerView.LayoutManager layoutManager;
    EventCostAdapter eventCostAdapter;
    TextView displayEventBudgetTextView, displayEventBudgetLeftTextView;
    MaterialEditText eventCostTitleMaterialEditText, eventCostValueMaterialEditText;
    FloatingActionButton eventCostFloatingActionButton;
    RecyclerView eventCostRecyclerView;
    boolean isUpdate = false;
    String eventId;
    String eventCostItemToUpdateId = "";
    AlertDialog alertDialog;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference eventCostLists = db.collection("EventCostLists");
    CollectionReference events = db.collection("Events");
    DocumentReference eventReference;
    Context context;
    Bundle bundle;

    public EventBudgetFragment() {
    }

    public static EventBudgetFragment newInstance(String param1, String param2) {
        EventBudgetFragment fragment = new EventBudgetFragment();
        return fragment;
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
        ((MainActivity) getActivity()).setDrawerLocked();
        if (((MainActivity) getActivity()).getSupportActionBar() != null) {
            ((MainActivity) getActivity()).getSupportActionBar().hide();
        }
        return inflater.inflate(R.layout.fragment_event_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayEventBudgetTextView = view.findViewById(R.id.displayEventBudgetTextView);
        displayEventBudgetLeftTextView = view.findViewById(R.id.displayEventBudgetLeftTextView);
        eventCostTitleMaterialEditText = view.findViewById(R.id.eventCostTitleMaterialEditText);
        eventCostValueMaterialEditText = view.findViewById(R.id.eventCostValueMaterialEditText);
        eventCostFloatingActionButton = view.findViewById(R.id.eventCostFloatingActionButton);
        eventCostRecyclerView = view.findViewById(R.id.eventCostRecyclerView);

        eventCostRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        eventCostRecyclerView.setLayoutManager(layoutManager);

        eventCostList = new ArrayList<>();
        alertDialog = new SpotsDialog(context);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        eventReference = db.document(path);
        eventId = eventReference.getId();

        eventCostValueMaterialEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
                String str = eventCostValueMaterialEditText.getText().toString();
                if (str.isEmpty()) return;
                String str2 = PerfectDecimal(str, 6, 2);

                if (!str2.equals(str)) {
                    eventCostValueMaterialEditText.setText(str2);
                    eventCostValueMaterialEditText.setSelection(str2.length());
                }
            }
        });

        eventCostFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventCostValueMaterialEditText.getText().toString().trim().isEmpty() || eventCostTitleMaterialEditText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, R.string.event_budget_empty_fields, Toast.LENGTH_SHORT).show();
                    return;
                }
                double eventCostValueDouble = Double.parseDouble(eventCostValueMaterialEditText.getText().toString());
                long eventCostValueLong = (long) (eventCostValueDouble * 100);
                if (!isUpdate) {
                    setData(eventCostTitleMaterialEditText.getText().toString().trim(), eventCostValueLong);
                } else {
                    updateData(eventCostTitleMaterialEditText.getText().toString().trim(), eventCostValueLong);
                    isUpdate = !isUpdate;
                }
            }
        });

        loadData();
    }

    public String PerfectDecimal(String str, int MAX_BEFORE_POINT, int MAX_DECIMAL) {
        if (str.charAt(0) == '.') str = "0" + str;
        int max = str.length();

        String rFinal = "";
        boolean after = false;
        int i = 0, up = 0, decimal = 0;
        char t;
        while (i < max) {
            t = str.charAt(i);
            if (t != '.' && after == false) {
                up++;
                if (up > MAX_BEFORE_POINT) return rFinal;
            } else if (t == '.') {
                after = true;
            } else {
                decimal++;
                if (decimal > MAX_DECIMAL)
                    return rFinal;
            }
            rFinal = rFinal + t;
            i++;
        }
        return rFinal;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(getString(R.string.event_cost_delete))) {
            deleteItem(item.getOrder());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteItem(int index) {
        eventCostLists.document(eventId).collection("EventCostList").document(eventCostList.get(index).getEventCostId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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
        eventCostLists.document(eventId)
                .collection("EventCostList")
                .document(eventCostItemToUpdateId)
                .update("eventCostTitle", title, "eventCostValue", value)
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
        eventCostLists.document(eventId).collection("EventCostList").document(eventCostItemToUpdateId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                loadData();
            }
        });
    }

    private void setData(String title, long value) {
        Map<String, Object> eventCostObject = new HashMap<>();
        String id = UUID.randomUUID().toString();
        eventCostObject.put("eventCostId", id);
        eventCostObject.put("eventCostTitle", title);
        eventCostObject.put("eventCostValue", value);
        eventCostObject.put("eventCostPaid", false);
        eventCostLists.document(eventId).collection("EventCostList").document(id).set(eventCostObject).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        if (eventCostList.size() > 0) {
            eventCostList.clear();
        }
        alertDialog.show();
        eventCostLists.document(eventId).collection("EventCostList").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.exists()) {
                        EventCost eventCost = documentSnapshot.toObject(EventCost.class);
                        eventCostList.add(eventCost);
                    }
                }
                events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Event event = documentSnapshot.toObject(Event.class);
                        long eventBudgetLong = event.getEventBudget();
                        double eventBudgetDouble = eventBudgetLong / 100;
                        displayEventBudgetTextView.setText(String.valueOf(eventBudgetDouble));
                        long paidCostsLong = 0;
                        for (EventCost eventCost : eventCostList) {
                            if (eventCost.isEventCostPaid()) {
                                paidCostsLong += eventCost.getEventCostValue();
                            }
                        }
                        double paidCostsDouble = paidCostsLong / 100;
                        if (eventBudgetDouble - paidCostsDouble < 0) {
                            displayEventBudgetLeftTextView.setTextColor(Color.RED);
                        } else {
                            displayEventBudgetLeftTextView.setTextColor(Color.WHITE);
                        }
                        displayEventBudgetLeftTextView.setText(String.valueOf(eventBudgetDouble - paidCostsDouble));
                        eventCostAdapter = new EventCostAdapter(fragmentThis, eventCostList, eventId);
                        eventCostRecyclerView.setAdapter(eventCostAdapter);
                        alertDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
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