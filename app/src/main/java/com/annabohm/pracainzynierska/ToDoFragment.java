package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ToDoFragment extends Fragment {

    ToDoFragment fragmentThis;
    ArrayList<ToDo> toDoList;
    RecyclerView toDoRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton toDoFloatingActionButton;
    MaterialEditText toDoTitleMaterialEditText, toDoDescriptionMaterialEditText;
    TextView displayToDoDoneTextView, displayToDoSlashTextView, displayToDoAllTextView;
    ToDoAdapter toDoAdapter;
    int taskCounter = 0;
    int taskDoneCounter = 0;
    boolean isUpdate = false;
    String eventId, currentUserId, eventAuthor;
    String toDoItemToUpdateId = "";
    AlertDialog alertDialog;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference toDoLists = db.collection("ToDoLists");
    DocumentReference eventReference;
    Context context;
    Bundle bundle;

    public ToDoFragment() {
    }

    public static ToDoFragment newInstance(String param1, String param2) {
        ToDoFragment fragment = new ToDoFragment();
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
        return inflater.inflate(R.layout.fragment_to_do, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        toDoTitleMaterialEditText = view.findViewById(R.id.toDoTitleMaterialEditText);
        toDoDescriptionMaterialEditText = view.findViewById(R.id.toDoDescriptionMaterialEditText);
        displayToDoAllTextView = view.findViewById(R.id.displayToDoAllTextView);
        displayToDoSlashTextView = view.findViewById(R.id.displayToDoSlashTextView);
        displayToDoDoneTextView = view.findViewById(R.id.displayToDoDoneTextView);
        toDoFloatingActionButton = view.findViewById(R.id.toDoFloatingActionButton);
        toDoRecyclerView = view.findViewById(R.id.toDoRecyclerView);

        toDoRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        toDoRecyclerView.setLayoutManager(layoutManager);

        toDoList = new ArrayList<>();
        alertDialog = new SpotsDialog(context);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        eventReference = db.document(path);
        eventId = eventReference.getId();

        eventReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eventAuthor = documentSnapshot.toObject(Event.class).getEventAuthor();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });

        loadData();

        toDoFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toDoTitleMaterialEditText.getText().toString().trim().isEmpty() || toDoDescriptionMaterialEditText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, R.string.event_budget_empty_fields, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isUpdate) {
                    setData(toDoTitleMaterialEditText.getText().toString().trim(), toDoDescriptionMaterialEditText.getText().toString().trim());
                } else {
                    updateData(toDoTitleMaterialEditText.getText().toString().trim(), toDoDescriptionMaterialEditText.getText().toString().trim());
                    isUpdate = !isUpdate;
                }
                toDoTitleMaterialEditText.setText("");
                toDoDescriptionMaterialEditText.setText("");
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(getString(R.string.to_do_delete))) {
            if (currentUserId.equals(eventAuthor) || currentUserId.equals(toDoList.get(item.getOrder()).getToDoCreatorId())) {
                deleteItem(item.getOrder());
            }
            else {
                Toast.makeText(context, R.string.to_do_error, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onContextItemSelected(item);
    }

    private void deleteItem(int index) {
        toDoLists.document(eventId).collection("ToDoList").document(toDoList.get(index).getToDoId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private void updateData(String title, String description) {
        toDoLists.document(eventId)
                .collection("ToDoList")
                .document(toDoItemToUpdateId)
                .update("toDoTitle", title, "toDoDescription", description)
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
        toDoLists.document(eventId).collection("ToDoList").document(toDoItemToUpdateId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                loadData();
            }
        });
    }

    private void setData(String title, String description) {
        Map<String, Object> toDoObject = new HashMap<>();
        String id = UUID.randomUUID().toString();
        toDoObject.put("toDoId", id);
        toDoObject.put("toDoTitle", title);
        toDoObject.put("toDoDescription", description);
        toDoObject.put("toDoCreatorId", currentUserId);
        toDoObject.put("toDoChecked", false);
        toDoLists.document(eventId).collection("ToDoList").document(id).set(toDoObject).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        if (toDoList.size() > 0) {
            toDoList.clear();
        }
        alertDialog.show();
        toDoLists.document(eventId).collection("ToDoList").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.exists()) {
                        ToDo toDoItem = documentSnapshot.toObject(ToDo.class);
                        if (toDoItem.isToDoChecked()) {
                            taskDoneCounter++;
                        }
                        taskCounter++;
                        toDoList.add(toDoItem);
                    }
                }
                displayToDoDoneTextView.setText(String.valueOf(taskDoneCounter));
                displayToDoSlashTextView.setText("/");
                displayToDoAllTextView.setText(String.valueOf(taskCounter));
                taskDoneCounter = 0;
                taskCounter = 0;
                toDoAdapter = new ToDoAdapter(fragmentThis, toDoList, eventId, currentUserId);
                toDoRecyclerView.setAdapter(toDoAdapter);
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