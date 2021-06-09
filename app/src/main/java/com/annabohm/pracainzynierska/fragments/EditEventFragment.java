package com.annabohm.pracainzynierska.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.annabohm.pracainzynierska.others.DateHandler;
import com.annabohm.pracainzynierska.datamodels.Event;
import com.annabohm.pracainzynierska.singletons.FirestoreInstanceSingleton;
import com.annabohm.pracainzynierska.activities.MainActivity;
import com.annabohm.pracainzynierska.R;
import com.annabohm.pracainzynierska.adapters.SimpleImageArrayAdapter;
import com.annabohm.pracainzynierska.datamodels.User;
import com.annabohm.pracainzynierska.adapters.UserSearchListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class EditEventFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    NavController navController;
    private final View.OnClickListener editEventCancelOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };
    Bundle bundle;
    Context context;
    Event editedEvent;
    EditText editEventDateStartEditText, editEventTimeStartEditText, editEventDateFinishEditText, editEventTimeFinishEditText;
    MaterialEditText editEventNameMaterialEditText, editEventLocationMaterialEditText, editEventDescriptionMaterialEditText;
    Button editEventReadyButton, editEventCancelButton;
    Spinner editEventImageSpinner;
    SearchView editEventGuestListSearchView;
    ListView editEventUserSearchListView, editEventGuestListListView, editEventNewGuestListListView;
    Integer chosenImage = 0, selectionCount = 0, modificationCount = 0;
    String eventId;
    ArrayList<User> oldGuestList, newGuestList, foundUsersList;
    ArrayList<String> oldGuestIdList, newGuestIdList, foundUsersIdList;
    DocumentReference event;
    DateHandler dateHandler = new DateHandler("dd/MM/yyyy", "HH:mm");
    UserSearchListAdapter oldGuestListAdapter, newGuestListAdapter, foundUsersAdapter;
    SimpleImageArrayAdapter imageArrayAdapter;
    InputMethodManager imm;
    AlertDialog alertDialog;
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    CollectionReference users = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Users");
    CollectionReference eventAttendees = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("EventAttendees");
    CollectionReference attendeeEvents = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("AttendeeEvents");
    private final View.OnClickListener editEventReadyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String eventName = editEventNameMaterialEditText.getText().toString().trim();
            String eventDescription = editEventDescriptionMaterialEditText.getText().toString().trim();
            String eventLocation = editEventLocationMaterialEditText.getText().toString().trim();
            String dateStartValue = editEventDateStartEditText.getText().toString().trim();
            String dateFinishValue = editEventDateFinishEditText.getText().toString().trim();
            String timeStartValue = editEventTimeStartEditText.getText().toString().trim();
            String timeFinishValue = editEventTimeFinishEditText.getText().toString().trim();
            Integer eventImage = chosenImage;

            Date eventDateStart = null;
            Date eventDateFinish = null;
            Date eventTimeStart = null;
            Date eventTimeFinish = null;

            if (modificationCount == 0
                    && newGuestIdList.isEmpty()
                    && eventName.isEmpty()
                    && dateStartValue.isEmpty()
                    && timeStartValue.isEmpty()
                    && dateFinishValue.isEmpty()
                    && timeFinishValue.isEmpty()
                    && eventLocation.isEmpty()
                    && eventDescription.isEmpty()
                    && eventImage == 0) {
                Toast.makeText(context, R.string.edit_event_error_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!dateStartValue.isEmpty()) {
                if (dateHandler.isDateValid(dateStartValue)) {
                    eventDateStart = dateHandler.convertStringToDate(dateStartValue);
                } else {
                    Toast.makeText(context, R.string.event_dates_incorrect, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!timeStartValue.isEmpty()) {
                if (dateHandler.isTimeValid(timeStartValue)) {
                    eventTimeStart = dateHandler.convertStringToTime(timeStartValue);
                } else {
                    Toast.makeText(context, R.string.event_dates_incorrect, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!dateFinishValue.isEmpty()) {
                if (dateHandler.isDateValid(dateFinishValue)) {
                    eventDateFinish = dateHandler.convertStringToDate(dateFinishValue);
                } else {
                    Toast.makeText(context, R.string.event_dates_incorrect, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!timeFinishValue.isEmpty()) {
                if (dateHandler.isTimeValid(timeFinishValue)) {
                    eventTimeFinish = dateHandler.convertStringToTime(timeFinishValue);
                } else {
                    Toast.makeText(context, R.string.event_dates_incorrect, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (eventName.isEmpty()) {
                eventName = editedEvent.getEventName();
            }

            if (dateStartValue.isEmpty()) {
                eventDateStart = editedEvent.getEventDateStart();
            }

            if (timeStartValue.isEmpty()) {
                eventTimeStart = editedEvent.getEventTimeStart();
            }

            if (dateFinishValue.isEmpty()) {
                eventDateFinish = editedEvent.getEventDateFinish();
            }

            if (timeFinishValue.isEmpty()) {
                eventTimeFinish = editedEvent.getEventTimeFinish();
            }

            if (eventLocation.isEmpty()) {
                eventLocation = editedEvent.getEventLocation();
            }

            if (eventDescription.isEmpty()) {
                eventDescription = editedEvent.getEventDescription();
            }

            if (eventImage == 0) {
                eventImage = editedEvent.getEventImage();
            }

            if (dateHandler.datesCorrect(eventDateStart, eventDateFinish, eventTimeStart, eventTimeFinish)) {
                event.update("eventName", eventName);
                event.update("eventDateStart", eventDateStart);
                event.update("eventTimeStart", eventTimeStart);
                event.update("eventDateFinish", eventDateFinish);
                event.update("eventTimeFinish", eventTimeFinish);
                event.update("eventLocation", eventLocation);
                event.update("eventDescription", eventDescription);
                event.update("eventImage", eventImage);

                if (!newGuestIdList.isEmpty()) {
                    for (int i = 0; i < newGuestIdList.size(); i++) {
                        Map<String, String> docDataUserId = new HashMap<>();
                        docDataUserId.put("User", newGuestIdList.get(i));

                        eventAttendees.document(eventId).collection("Invited").add(docDataUserId).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        });
                    }

                    for (int i = 0; i < newGuestIdList.size(); i++) {
                        Map<String, String> docDataEventId = new HashMap<>();
                        docDataEventId.put("Event", eventId);
                        attendeeEvents.document(newGuestIdList.get(i)).collection("Invited").add(docDataEventId).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        });
                    }
                }

                navController.popBackStack();
            } else {
                Toast.makeText(context, R.string.event_dates_incorrect, Toast.LENGTH_SHORT).show();
            }
        }
    };


    public EditEventFragment() {
    }

    public static EditEventFragment newInstance() {
        return new EditEventFragment();
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
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inflater.inflate(R.layout.fragment_edit_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        alertDialog = new SpotsDialog(context);
        editEventNameMaterialEditText = view.findViewById(R.id.editEventNameMaterialEditText);
        editEventDateStartEditText = view.findViewById(R.id.editEventDateStartEditText);
        editEventTimeStartEditText = view.findViewById(R.id.editEventTimeStartEditText);
        editEventDateFinishEditText = view.findViewById(R.id.editEventDateFinishEditText);
        editEventTimeFinishEditText = view.findViewById(R.id.editEventTimeFinishEditText);
        editEventLocationMaterialEditText = view.findViewById(R.id.editEventLocationMaterialEditText);
        editEventDescriptionMaterialEditText = view.findViewById(R.id.editEventDescriptionMaterialEditText);
        editEventReadyButton = view.findViewById(R.id.editEventReadyButton);
        editEventCancelButton = view.findViewById(R.id.editEventCancelButton);
        editEventImageSpinner = view.findViewById(R.id.editEventImageSpinner);
        editEventGuestListSearchView = view.findViewById(R.id.editEventGuestListSearchView);
        editEventUserSearchListView = view.findViewById(R.id.editEventUserSearchListView);
        editEventGuestListListView = view.findViewById(R.id.editEventGuestListListView);
        editEventNewGuestListListView = view.findViewById(R.id.editEventNewGuestListListView);

        bundle = this.getArguments();
        assert bundle != null;
        String path = bundle.getString("path");
        event = firestoreInstanceSingleton.getFirebaseFirestoreRef().document(path);
        eventId = event.getId();

        imageArrayAdapter = new SimpleImageArrayAdapter(context,
                new Integer[]{
                        R.drawable.ic_empty_small,
                        R.drawable.ic_boardgame_small,
                        R.drawable.ic_card_small,
                        R.drawable.ic_dice_small,
                        R.drawable.ic_rpg_small,
                        R.drawable.ic_billiards_small,
                        R.drawable.ic_bowling_small,
                        R.drawable.ic_darts_small,
                        R.drawable.ic_pingpong_small,
                        R.drawable.ic_poker_small});
        editEventImageSpinner.setAdapter(imageArrayAdapter);
        editEventImageSpinner.setOnItemSelectedListener(this);

        oldGuestList = new ArrayList<>();
        newGuestList = new ArrayList<>();
        foundUsersList = new ArrayList<>();
        oldGuestIdList = new ArrayList<>();
        newGuestIdList = new ArrayList<>();
        foundUsersIdList = new ArrayList<>();

        foundUsersAdapter = new UserSearchListAdapter(context, foundUsersList);

        editEventReadyButton.setOnClickListener(editEventReadyOnClickListener);
        editEventCancelButton.setOnClickListener(editEventCancelOnClickListener);

        editEventGuestListListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        editEventGuestListSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                editEventGuestListSearchView.clearFocus();
                clearList();
                hideEventUserSearchListView();
                if (query.trim().length() > 2) {
                    searchUsers(query.trim());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                clearList();
                hideEventUserSearchListView();
                if (newText.trim().length() > 2) {
                    searchUsers(newText.trim());
                }
                return false;
            }
        });

        editEventUserSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();
                User user = foundUsersList.get(position);
                String userId = foundUsersIdList.get(position);
                if (!containsUserId(userId)) {
                    newGuestList.add(user);
                    newGuestIdList.add(userId);
                    newGuestListAdapter.notifyDataSetChanged();
                    extendEventNewGuestListListView();
                } else {
                    Toast.makeText(context, R.string.event_guest_search_list_error_duplicate, Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.show();
        loadData();
    }

    public void loadData() {
        event.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    editedEvent = documentSnapshot.toObject(Event.class);

                    oldGuestListAdapter = new UserSearchListAdapter(context, oldGuestList);
                    newGuestListAdapter = new UserSearchListAdapter(context, newGuestList);

                    editEventGuestListListView.setAdapter(oldGuestListAdapter);
                    editEventNewGuestListListView.setAdapter(newGuestListAdapter);
                    editEventUserSearchListView.setAdapter(foundUsersAdapter);

                    registerForContextMenu(editEventGuestListListView);
                    registerForContextMenu(editEventNewGuestListListView);

                    populateOldGuestList();

                    assert editedEvent != null;
                    editEventNameMaterialEditText.setHint(editedEvent.getEventName());
                    editEventDateStartEditText.setHint(dateHandler.convertDateToString(editedEvent.getEventDateStart()));
                    editEventTimeStartEditText.setHint(dateHandler.convertTimeToString(editedEvent.getEventTimeStart()));
                    editEventDateFinishEditText.setHint(dateHandler.convertDateToString(editedEvent.getEventDateFinish()));
                    editEventTimeFinishEditText.setHint(dateHandler.convertTimeToString(editedEvent.getEventTimeFinish()));
                    editEventLocationMaterialEditText.setHint(editedEvent.getEventLocation());
                    editEventDescriptionMaterialEditText.setHint(editedEvent.getEventDescription());
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

    public boolean containsUserId(String userId) {
        for (int i = 0; i < oldGuestIdList.size(); i++) {
            if (oldGuestIdList.get(i).equals(userId)) {
                return true;
            }
        }
        for (int i = 0; i < newGuestIdList.size(); i++) {
            if (newGuestIdList.get(i).equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public void setFoundUsersList(ArrayList<User> userList, ArrayList<String> userIdList) {
        foundUsersList.clear();
        foundUsersIdList.clear();
        foundUsersList.addAll(userList);
        foundUsersIdList.addAll(userIdList);
    }

    public void loadUsers(final ArrayList<User> userList, ArrayList<String> userIdList) {
        if (userList.size() == 0) {
            hideEventUserSearchListView();
        } else {
            showEventUserSearchListView();
            setFoundUsersList(userList, userIdList);
            foundUsersAdapter.notifyDataSetChanged();
        }
    }

    public void clearList() {
        foundUsersList.clear();
        foundUsersIdList.clear();
        foundUsersAdapter.notifyDataSetChanged();
    }

    public void searchUsers(String text) {
        String[] inputArray = text.trim().split(" ");

        for (int i = 0; i < inputArray.length; i++) {
            if (inputArray[i].length() > 0) {
                String s = inputArray[i];
                s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
                inputArray[i] = s;
            }
        }

        ArrayList<Task> taskList = new ArrayList<>();
        final ArrayList<User> userList = new ArrayList<>();
        final ArrayList<String> userIdList = new ArrayList<>();
        for (int i = 0; i < inputArray.length; i++) {
            StringBuilder firstPart = new StringBuilder();
            StringBuilder secondPart = new StringBuilder();
            for (int j = 0; j < i; j++) {
                firstPart.append(inputArray[j]);
                firstPart.append(" ");
            }
            firstPart = new StringBuilder(firstPart.toString().trim());

            for (int j = i; j < inputArray.length; j++) {
                secondPart.append(inputArray[j]);
                secondPart.append(" ");
            }
            secondPart = new StringBuilder(secondPart.toString().trim());

            Query firstQuery = users.whereEqualTo("userFirstName", firstPart.toString()).whereGreaterThanOrEqualTo("userLastName", secondPart.toString()).whereLessThanOrEqualTo("userLastName", secondPart.toString() + '\uf8ff');
            Query secondQuery = users.whereEqualTo("userLastName", firstPart.toString()).whereGreaterThanOrEqualTo("userFirstName", secondPart.toString()).whereLessThanOrEqualTo("userFirstName", secondPart.toString() + '\uf8ff');
            Task firstTask = firstQuery.get();
            Task secondTask = secondQuery.get();

            taskList.add(firstTask);
            taskList.add(secondTask);
        }

        if (inputArray.length == 1) {
            text = text.trim();
            text = text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
            Query firstQuery = users.whereGreaterThanOrEqualTo("userLastName", text).whereLessThanOrEqualTo("userLastName", text + '\uf8ff');
            Query secondQuery = users.whereGreaterThanOrEqualTo("userFirstName", text).whereLessThanOrEqualTo("userFirstName", text + '\uf8ff');
            Task firstTask = firstQuery.get();
            Task secondTask = secondQuery.get();

            taskList.add(firstTask);
            taskList.add(secondTask);
        }

        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(taskList.toArray(new Task[0]));
        allTasks.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                for (QuerySnapshot querySnapshot : querySnapshots) {
                    for (DocumentSnapshot document : (QuerySnapshot) querySnapshot) {
                        User user = document.toObject(User.class);
                        userList.add(user);
                        userIdList.add(document.getId());
                    }
                }
                loadUsers(userList, userIdList);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
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

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.editEventNewGuestListListView) {
            requireActivity().getMenuInflater().inflate(R.menu.floating_context_menu, menu);
        } else if (v.getId() == R.id.editEventGuestListListView) {
            requireActivity().getMenuInflater().inflate(R.menu.floating_context_menu_special, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteGuest:
                newGuestListAdapter.remove(newGuestListAdapter.getItem(info.position));
                newGuestIdList.remove(info.position);
                newGuestListAdapter.notifyDataSetChanged();
                extendEventNewGuestListListView();
                return true;
            case R.id.cancelDeleteGuest:
            case R.id.cancelDeleteGuestSpecial:
                return true;
            case R.id.deleteGuestSpecial:
                final String userId = oldGuestIdList.get(info.position);
                Query query1 = attendeeEvents.document(userId).collection("Invited").whereEqualTo("Event", eventId);
                query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                attendeeEvents.document(userId).collection("Invited").document(documentSnapshot.getId()).delete();
                                modificationCount++;
                            }
                        }
                    }
                });
                Query query2 = attendeeEvents.document(userId).collection("Confirmed").whereEqualTo("Event", eventId);
                query2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                attendeeEvents.document(userId).collection("Confirmed").document(documentSnapshot.getId()).delete();
                                modificationCount++;
                            }
                        }
                    }
                });
                Query query3 = attendeeEvents.document(userId).collection("Declined").whereEqualTo("Event", eventId);
                query3.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                attendeeEvents.document(userId).collection("Declined").document(documentSnapshot.getId()).delete();
                                modificationCount++;
                            }
                        }
                    }
                });
                Query query4 = eventAttendees.document(eventId).collection("Invited").whereEqualTo("User", userId);
                query4.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                eventAttendees.document(eventId).collection("Invited").document(documentSnapshot.getId()).delete();
                                modificationCount++;
                            }
                        }
                    }
                });
                Query query5 = eventAttendees.document(eventId).collection("Confirmed").whereEqualTo("User", userId);
                query5.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                eventAttendees.document(eventId).collection("Confirmed").document(documentSnapshot.getId()).delete();
                                modificationCount++;
                            }
                        }
                    }
                });
                Query query6 = eventAttendees.document(eventId).collection("Declined").whereEqualTo("User", userId);
                query6.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                eventAttendees.document(eventId).collection("Declined").document(documentSnapshot.getId()).delete();
                                modificationCount++;
                            }
                        }
                    }
                });
                oldGuestListAdapter.remove(oldGuestListAdapter.getItem(info.position));
                oldGuestIdList.remove(info.position);
                oldGuestListAdapter.notifyDataSetChanged();
                extendEventOldGuestListListView();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(editEventUserSearchListView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public void showEventUserSearchListView() {
        editEventUserSearchListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 350));
    }

    public void hideEventUserSearchListView() {
        editEventUserSearchListView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    public void extendEventOldGuestListListView() {
        int numberOfGuests = oldGuestList.size();
        editEventGuestListListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, numberOfGuests * 160));
    }

    public void extendEventNewGuestListListView() {
        int numberOfGuests = newGuestList.size();
        editEventNewGuestListListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, numberOfGuests * 160));
    }


    public void populateOldGuestList() {
        eventAttendees.document(eventId).collection("Invited").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        final String guestId = Objects.requireNonNull(documentSnapshot.get("User")).toString();
                        users.document(guestId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User guest = documentSnapshot.toObject(User.class);
                                oldGuestList.add(guest);
                                oldGuestIdList.add(guestId);
                                oldGuestListAdapter.notifyDataSetChanged();
                                extendEventOldGuestListListView();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });

        eventAttendees.document(eventId).collection("Confirmed").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        final String guestId = documentSnapshot.get("User").toString();
                        users.document(guestId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User guest = documentSnapshot.toObject(User.class);
                                oldGuestList.add(guest);
                                oldGuestIdList.add(guestId);
                                oldGuestListAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });

        eventAttendees.document(eventId).collection("Declined").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        final String guestId = documentSnapshot.get("User").toString();
                        users.document(guestId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User guest = documentSnapshot.toObject(User.class);
                                oldGuestList.add(guest);
                                oldGuestIdList.add(guestId);
                                oldGuestListAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
    }
}