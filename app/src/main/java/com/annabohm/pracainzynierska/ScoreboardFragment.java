package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

public class ScoreboardFragment extends Fragment {
    ScoreboardFragment fragmentThis;
    NavController navController;
    AlertDialog alertDialog;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference users = db.collection("Users");
    CollectionReference eventAttendees = db.collection("EventAttendees");
    CollectionReference scoreLists = db.collection("ScoreLists");
    MaterialEditText scoreboardGuestNameMaterialEditText, scoreboardValueMaterialEditText;
    FloatingActionButton scoreFloatingActionButton;
    RecyclerView scoreRecyclerView;
    DocumentReference eventReference;
    Context context;
    Bundle bundle;
    String eventId, eventAuthorId, currentUserId;
    boolean isUpdate = false;
    String scoreItemToUpdateId = "";
    RecyclerView.LayoutManager layoutManager;
    ArrayList<Score> scoreList;
    ScoreboardAdapter scoreboardAdapter;

    public ScoreboardFragment() {

    }

    public static ScoreboardFragment newInstance(String param1, String param2) {
        ScoreboardFragment fragment = new ScoreboardFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scoreboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        currentUserId = firebaseAuth.getCurrentUser().getUid();

        scoreboardGuestNameMaterialEditText = view.findViewById(R.id.scoreboardGuestNameMaterialEditText);
        scoreboardValueMaterialEditText = view.findViewById(R.id.scoreboardValueMaterialEditText);
        scoreFloatingActionButton = view.findViewById(R.id.scoreFloatingActionButton);
        scoreRecyclerView = view.findViewById(R.id.scoreRecyclerView);

        scoreRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(context);
        scoreRecyclerView.setLayoutManager(layoutManager);

        scoreList = new ArrayList<>();
        alertDialog = new SpotsDialog(context);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        eventReference = db.document(path);
        eventId = eventReference.getId();

        eventReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                eventAuthorId = documentSnapshot.toObject(Event.class).getEventAuthor();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });

        scoreboardAdapter = new ScoreboardAdapter(fragmentThis, scoreList, eventId, currentUserId, eventAuthorId);
        scoreRecyclerView.setAdapter(scoreboardAdapter);

        loadData();

        scoreFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scoreboardGuestNameMaterialEditText.getText().toString().trim().isEmpty() || scoreboardValueMaterialEditText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(context, R.string.event_budget_empty_fields, Toast.LENGTH_SHORT).show();
                    return;
                }
                int scoreboardValue = Integer.valueOf(scoreboardValueMaterialEditText.getText().toString().trim());
                if (!isUpdate) {
                    setData(scoreboardGuestNameMaterialEditText.getText().toString().trim(), scoreboardValue);
                } else {
                    updateData(scoreboardGuestNameMaterialEditText.getText().toString().trim(), scoreboardValue);
                    isUpdate = !isUpdate;
                }
                scoreboardGuestNameMaterialEditText.setText("");
                scoreboardValueMaterialEditText.setText("");
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(getString(R.string.score_delete))) {
            if (currentUserId.equals(eventAuthorId)) {
                deleteItem(item.getOrder());
            } else {
                Toast.makeText(context, R.string.scoreboard_error, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onContextItemSelected(item);
    }

    private void deleteItem(int index) {
        scoreLists.document(eventId).collection("ScoreList").document(scoreList.get(index).getScoreId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private void updateData(String guestName, int value) {
        scoreLists.document(eventId)
                .collection("ScoreList")
                .document(scoreItemToUpdateId)
                .update("scoreGuestName", guestName, "scoreValue", value)
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
        scoreLists.document(eventId).collection("ScoreList").document(scoreItemToUpdateId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                loadData();
            }
        });
    }

    private void setData(String guestName, int value) {
        Map<String, Object> scoreObject = new HashMap<>();
        String id = UUID.randomUUID().toString();
        scoreObject.put("scoreId", id);
        scoreObject.put("scoreGuestName", guestName);
        scoreObject.put("scoreValue", value);
        scoreLists.document(eventId).collection("ScoreList").document(id).set(scoreObject).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        if (scoreList.size() > 0) {
            scoreList.clear();
        }
        alertDialog.show();
        scoreLists.document(eventId).collection("ScoreList").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    if (documentSnapshot.exists()) {
                        Score score = documentSnapshot.toObject(Score.class);
                        scoreList.add(score);
                        scoreboardAdapter.notifyDataSetChanged();
                    }
                }
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