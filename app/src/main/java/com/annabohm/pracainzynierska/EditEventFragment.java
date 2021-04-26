package com.annabohm.pracainzynierska;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class EditEventFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    NavController navController;
    Bundle bundle;
    Context context;
    EditText editEventNameEditText, editEventDateStartEditText, editEventTimeStartEditText, editEventDateFinishEditText, editEventTimeFinishEditText, editEventLocationEditText, editEventDescriptionEditText;
    Button editEventReadyButton, editEventCancelButton;
    Spinner editEventImageSpinner;
    Integer chosenImage = 0;
    Integer selectionCount = 0;
    DocumentReference documentReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private View.OnClickListener editEventReadyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //update fields in firestore if the fields aren't null
            DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat timeFormatter = new SimpleDateFormat("HH:mm");
            Date eventDateStart = null;
            Date eventDateFinish = null;
            Date eventTimeStart = null;
            Date eventTimeFinish = null;

            if (!editEventNameEditText.getText().toString().trim().isEmpty()) {
                documentReference.update("eventName", editEventNameEditText.getText().toString());
            }

            if (!editEventDateStartEditText.getText().toString().trim().isEmpty()) {
                try {
                    eventDateStart = dateFormatter.parse(editEventDateStartEditText.getText().toString());
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
                documentReference.update("eventDateStart", eventDateStart);
            }

            if (!editEventTimeStartEditText.getText().toString().trim().isEmpty()) {
                try {
                    eventTimeStart = timeFormatter.parse(editEventTimeStartEditText.getText().toString());
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
                documentReference.update("eventTimeStart", eventTimeStart);
            }

            if (!editEventDateFinishEditText.getText().toString().trim().isEmpty()) {
                try {
                    eventDateFinish = dateFormatter.parse(editEventDateFinishEditText.getText().toString());
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
                documentReference.update("eventDateFinish", eventDateFinish);
            }

            if (!editEventTimeFinishEditText.getText().toString().trim().isEmpty()) {
                try {
                    eventTimeFinish = timeFormatter.parse(editEventTimeFinishEditText.getText().toString());
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
                documentReference.update("eventTimeFinish", eventTimeFinish);
            }

            if (!editEventLocationEditText.getText().toString().trim().isEmpty()) {
                documentReference.update("eventLocation", editEventLocationEditText.getText().toString());
            }

            if (!editEventDescriptionEditText.getText().toString().trim().isEmpty()) {
                documentReference.update("eventDescription", editEventDescriptionEditText.getText().toString());
            }

            if (chosenImage != 0) {
                documentReference.update("eventImage", chosenImage);
            }

            if (editEventNameEditText.getText().toString().trim().isEmpty()
                    && editEventDateStartEditText.getText().toString().trim().isEmpty()
                    && editEventTimeStartEditText.getText().toString().isEmpty()
                    && editEventDateFinishEditText.getText().toString().isEmpty()
                    && editEventTimeFinishEditText.getText().toString().isEmpty()
                    && editEventLocationEditText.getText().toString().isEmpty()
                    && editEventDescriptionEditText.getText().toString().isEmpty()
                    && chosenImage == 0) {
                Toast.makeText(context, "You have not changed anything. To escape edit mode, click 'Cancel' button", Toast.LENGTH_SHORT).show();
                return;
            }

            navController.popBackStack();
        }
    };
    private View.OnClickListener editEventCancelOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };

    public EditEventFragment() {
        // Required empty public constructor
    }

    public static EditEventFragment newInstance(String param1, String param2) {
        EditEventFragment fragment = new EditEventFragment();
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
        return inflater.inflate(R.layout.fragment_edit_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        editEventNameEditText = view.findViewById(R.id.editEventNameEditText);
        editEventDateStartEditText = view.findViewById(R.id.editEventDateStartEditText);
        editEventTimeStartEditText = view.findViewById(R.id.editEventTimeStartEditText);
        editEventDateFinishEditText = view.findViewById(R.id.editEventDateFinishEditText);
        editEventTimeFinishEditText = view.findViewById(R.id.editEventTimeFinishEditText);
        editEventLocationEditText = view.findViewById(R.id.editEventLocationEditText);
        editEventDescriptionEditText = view.findViewById(R.id.editEventDescriptionEditText);
        editEventReadyButton = view.findViewById(R.id.editEventReadyButton);
        editEventCancelButton = view.findViewById(R.id.editEventCancelButton);
        editEventImageSpinner = view.findViewById(R.id.editEventImageSpinner);

        SimpleImageArrayAdapter adapter = new SimpleImageArrayAdapter(context,
                new Integer[]{R.drawable.rectangular_background_1, R.drawable.rectangular_background, R.drawable.rectangular_background_2, R.drawable.rectangular_background_3});
        editEventImageSpinner.setAdapter(adapter);
        editEventImageSpinner.setOnItemSelectedListener(this);

        editEventReadyButton.setOnClickListener(editEventReadyOnClickListener);
        editEventCancelButton.setOnClickListener(editEventCancelOnClickListener);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        documentReference = db.document(path);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Event event = new Event();
                if (documentSnapshot.exists()) {
                    event = documentSnapshot.toObject(Event.class);
                    DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
                    DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");

                    editEventNameEditText.setHint(event.getEventName());
                    editEventDateStartEditText.setHint(dateFormatterPrint.format(event.getEventDateStart()));
                    editEventTimeStartEditText.setHint(timeFormatterPrint.format(event.getEventTimeStart()));
                    editEventDateFinishEditText.setHint(dateFormatterPrint.format(event.getEventDateFinish()));
                    editEventTimeFinishEditText.setHint(timeFormatterPrint.format(event.getEventTimeFinish()));
                    editEventLocationEditText.setHint(event.getEventLocation());
                    editEventDescriptionEditText.setHint(event.getEventDescription());
                    //Map<String, Object> events = documentSnapshot.getData();
                } else {
                    Toast.makeText(context, "Document does not exist", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Reading data from Firestore failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (selectionCount == 0) {
            chosenImage = 0;
            selectionCount++;
        } else {
            chosenImage = (Integer) parent.getItemAtPosition(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}