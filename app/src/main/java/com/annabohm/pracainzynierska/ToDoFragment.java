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
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class ToDoFragment extends Fragment {

    ArrayList<ToDo> toDoList;
    RecyclerView toDoRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton toDoFloatingActionButton;
    MaterialEditText toDoTitleMaterialEditText, toDoDescriptionMaterialEditText;
    ToDoAdapter toDoAdapter;
    boolean isUpdate = false;
    String eventId;
    String toDoItemToUpdateId = "";
    AlertDialog alertDialog;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference toDoLists = db.collection("ToDoLists");
    DocumentReference eventReference;
    Context context;
    Bundle bundle;

    public ToDoFragment() {
        // Required empty public constructor
    }

    public static ToDoFragment newInstance(String param1, String param2) {
        ToDoFragment fragment = new ToDoFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_to_do, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toDoList = new ArrayList<>();
        alertDialog = new SpotsDialog(context);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        eventReference = db.document(path);
        eventId = eventReference.getId();

        toDoTitleMaterialEditText = view.findViewById(R.id.toDoTitleMaterialEditText);
        toDoDescriptionMaterialEditText = view.findViewById(R.id.toDoDescriptionMaterialEditText);
        toDoFloatingActionButton = view.findViewById(R.id.toDoFloatingActionButton);
        toDoFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isUpdate) {
                    setData(toDoTitleMaterialEditText.getText().toString().trim(), toDoDescriptionMaterialEditText.getText().toString().trim());
                }
            }
        });
        toDoRecyclerView = view.findViewById(R.id.toDoRecyclerView);
        toDoRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        toDoRecyclerView.setLayoutManager(layoutManager);

        loadData();
    }

    private void setData(String title, String description) {
        Map<String, Object> toDoObject = new HashMap<>();
        toDoObject.put("toDoTitle", title);
        toDoObject.put("toDoDescription", description);
        toDoLists.document(eventId).collection("ToDoList").add(toDoObject).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
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
                        toDoList.add(toDoItem);
                    }
                }
                toDoAdapter = new ToDoAdapter(toDoList);
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