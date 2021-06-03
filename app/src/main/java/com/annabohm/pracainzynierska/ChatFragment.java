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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class ChatFragment extends Fragment {
    NavController navController;
    AlertDialog alertDialog;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference messageLists = db.collection("MessageLists");
    CollectionReference events = db.collection("Events");
    CollectionReference users = db.collection("Users");
    DocumentReference eventReference;
    Context context;
    Bundle bundle;
    String eventId, eventAuthor;
    String currentUserId = firebaseAuth.getCurrentUser().getUid();
    String currentUserDisplayName = "";
    EditText sendMessageEditText;
    Button sendMessageButton;
    RecyclerView messageRecyclerView;
    ChatAdapter chatAdapter;
    ArrayList<Message> messageList;
    LinearLayoutManager linearLayoutManager;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        sendMessageEditText = view.findViewById(R.id.sendMessageEditText);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);
        messageRecyclerView = view.findViewById(R.id.messageRecyclerView);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        eventReference = db.document(path);
        eventId = eventReference.getId();

        users.document(currentUserId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    currentUserDisplayName = currentUser.getUserFirstName() + " " + currentUser.getUserLastName();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });

        messageRecyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        messageList = new ArrayList<>();

        chatAdapter = new ChatAdapter(context, messageList, currentUserId);

        alertDialog = new SpotsDialog(context);

        sendMessageButton.setOnClickListener(sendMessageOnClickListener);
    }

    View.OnClickListener sendMessageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //sendMessage();
        }
    };

    public void sendMessage(String messageContent, Date createdAt, String senderName, String senderId){

    }
}