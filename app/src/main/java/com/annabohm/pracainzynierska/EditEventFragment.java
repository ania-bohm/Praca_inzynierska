package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

import static android.content.ContentValues.TAG;

public class EditEventFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    NavController navController;
    Bundle bundle;
    Context context;
    EditText editEventNameEditText, editEventDateStartEditText, editEventTimeStartEditText, editEventDateFinishEditText, editEventTimeFinishEditText, editEventLocationEditText, editEventDescriptionEditText, editEventBudgetEditText;
    Button editEventReadyButton, editEventCancelButton;
    Spinner editEventImageSpinner;
    SearchView editEventGuestListSearchView;
    ListView editEventUserSearchListView, editEventGuestListListView, editEventNewGuestListListView;
    Integer chosenImage = 0, selectionCount = 0, modificationCount = 0;
    String eventId;
    ArrayList<User> oldGuestList, newGuestList, foundUsersList;
    ArrayList<String> oldGuestIdList, newGuestIdList, foundUsersIdList;
    DocumentReference event;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference users = db.collection("Users");
    CollectionReference eventAttendees = db.collection("EventAttendees");
    CollectionReference attendeeEvents = db.collection("AttendeeEvents");
    UserSearchListAdapter oldGuestListAdapter, newGuestListAdapter, foundUsersAdapter;
    SimpleImageArrayAdapter imageArrayAdapter;
    InputMethodManager imm;
    AlertDialog alertDialog;

    private View.OnClickListener editEventReadyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat timeFormatter = new SimpleDateFormat("HH:mm");
            Date eventDateStart = null;
            Date eventDateFinish = null;
            Date eventTimeStart = null;
            Date eventTimeFinish = null;

            if (!editEventNameEditText.getText().toString().trim().isEmpty()) {
                event.update("eventName", editEventNameEditText.getText().toString());
            }

            if (!editEventDateStartEditText.getText().toString().trim().isEmpty()) {
                try {
                    eventDateStart = dateFormatter.parse(editEventDateStartEditText.getText().toString());
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
                event.update("eventDateStart", eventDateStart);
            }

            if (!editEventTimeStartEditText.getText().toString().trim().isEmpty()) {
                try {
                    eventTimeStart = timeFormatter.parse(editEventTimeStartEditText.getText().toString());
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
                event.update("eventTimeStart", eventTimeStart);
            }

            if (!editEventDateFinishEditText.getText().toString().trim().isEmpty()) {
                try {
                    eventDateFinish = dateFormatter.parse(editEventDateFinishEditText.getText().toString());
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
                event.update("eventDateFinish", eventDateFinish);
            }

            if (!editEventTimeFinishEditText.getText().toString().trim().isEmpty()) {
                try {
                    eventTimeFinish = timeFormatter.parse(editEventTimeFinishEditText.getText().toString());
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
                event.update("eventTimeFinish", eventTimeFinish);
            }

            if (!editEventLocationEditText.getText().toString().trim().isEmpty()) {
                event.update("eventLocation", editEventLocationEditText.getText().toString());
            }

            if (!editEventDescriptionEditText.getText().toString().trim().isEmpty()) {
                event.update("eventDescription", editEventDescriptionEditText.getText().toString());
            }

            if (!editEventBudgetEditText.getText().toString().trim().isEmpty()) {
                double eventBudgetDouble = Double.valueOf(editEventBudgetEditText.getText().toString());
                long eventBudgetLong = (long) (eventBudgetDouble * 100);
                event.update("eventBudget", eventBudgetLong);
            }

            if (chosenImage != 0) {
                event.update("eventImage", chosenImage);
            }

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

            if (modificationCount == 0
                    && newGuestIdList.isEmpty()
                    && editEventNameEditText.getText().toString().trim().isEmpty()
                    && editEventDateStartEditText.getText().toString().trim().isEmpty()
                    && editEventTimeStartEditText.getText().toString().isEmpty()
                    && editEventDateFinishEditText.getText().toString().isEmpty()
                    && editEventTimeFinishEditText.getText().toString().isEmpty()
                    && editEventLocationEditText.getText().toString().isEmpty()
                    && editEventDescriptionEditText.getText().toString().isEmpty()
                    & editEventBudgetEditText.getText().toString().isEmpty()
                    && chosenImage == 0) {
                Toast.makeText(context, R.string.edit_event_error_empty, Toast.LENGTH_SHORT).show();
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
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inflater.inflate(R.layout.fragment_edit_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        alertDialog = new SpotsDialog(context);
        editEventNameEditText = view.findViewById(R.id.editEventNameEditText);
        editEventDateStartEditText = view.findViewById(R.id.editEventDateStartEditText);
        editEventTimeStartEditText = view.findViewById(R.id.editEventTimeStartEditText);
        editEventDateFinishEditText = view.findViewById(R.id.editEventDateFinishEditText);
        editEventTimeFinishEditText = view.findViewById(R.id.editEventTimeFinishEditText);
        editEventLocationEditText = view.findViewById(R.id.editEventLocationEditText);
        editEventDescriptionEditText = view.findViewById(R.id.editEventDescriptionEditText);
        editEventBudgetEditText = view.findViewById(R.id.editEventBudgetEditText);
        editEventReadyButton = view.findViewById(R.id.editEventReadyButton);
        editEventCancelButton = view.findViewById(R.id.editEventCancelButton);
        editEventImageSpinner = view.findViewById(R.id.editEventImageSpinner);
        editEventGuestListSearchView = view.findViewById(R.id.editEventGuestListSearchView);
        editEventUserSearchListView = view.findViewById(R.id.editEventUserSearchListView);
        editEventGuestListListView = view.findViewById(R.id.editEventGuestListListView);
        editEventNewGuestListListView = view.findViewById(R.id.editEventNewGuestListListView);

        bundle = this.getArguments();
        String path = bundle.getString("path");
        event = db.document(path);
        eventId = event.getId();

        imageArrayAdapter = new SimpleImageArrayAdapter(context,
                new Integer[]{R.drawable.rectangular_background_1, R.drawable.rectangular_background, R.drawable.rectangular_background_2, R.drawable.rectangular_background_3});
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

        editEventBudgetEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable arg0) {
                String str = editEventBudgetEditText.getText().toString();
                if (str.isEmpty()) {
                    return;
                }
                String str2 = PerfectDecimal(str, 6, 2);
                if (!str2.equals(str)) {
                    editEventBudgetEditText.setText(str2);
                    editEventBudgetEditText.setSelection(str2.length());
                }
            }
        });

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
                Event event = new Event();
                if (documentSnapshot.exists()) {
                    event = documentSnapshot.toObject(Event.class);
                    DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
                    DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");

                    oldGuestListAdapter = new UserSearchListAdapter(context, oldGuestList);
                    newGuestListAdapter = new UserSearchListAdapter(context, newGuestList);

                    editEventGuestListListView.setAdapter(oldGuestListAdapter);
                    editEventNewGuestListListView.setAdapter(newGuestListAdapter);
                    editEventUserSearchListView.setAdapter(foundUsersAdapter);

                    registerForContextMenu(editEventGuestListListView);
                    registerForContextMenu(editEventNewGuestListListView);

                    populateOldGuestList();

                    editEventNameEditText.setHint(event.getEventName());
                    editEventDateStartEditText.setHint(dateFormatterPrint.format(event.getEventDateStart()));
                    editEventTimeStartEditText.setHint(timeFormatterPrint.format(event.getEventTimeStart()));
                    editEventDateFinishEditText.setHint(dateFormatterPrint.format(event.getEventDateFinish()));
                    editEventTimeFinishEditText.setHint(timeFormatterPrint.format(event.getEventTimeFinish()));
                    editEventLocationEditText.setHint(event.getEventLocation());
                    editEventDescriptionEditText.setHint(event.getEventDescription());
                    long eventBudgetLong = event.getEventBudget();
                    double eventBudgetDouble = eventBudgetLong / 100;
                    editEventBudgetEditText.setHint(String.valueOf(eventBudgetDouble));
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
        for (int i = 0; i < userList.size(); i++) {
            foundUsersList.add(userList.get(i));
        }

        for (int i = 0; i < userIdList.size(); i++) {
            foundUsersIdList.add(userIdList.get(i));
        }
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
            String firstPart = "", secondPart = "";
            for (int j = 0; j < i; j++) {
                firstPart += inputArray[j];
                firstPart += " ";
            }
            firstPart = firstPart.trim();

            for (int j = i; j < inputArray.length; j++) {
                secondPart += inputArray[j];
                secondPart += " ";
            }
            secondPart = secondPart.trim();

            Query firstQuery = users.whereEqualTo("userFirstName", firstPart).whereGreaterThanOrEqualTo("userLastName", secondPart).whereLessThanOrEqualTo("userLastName", secondPart + '\uf8ff');
            Query secondQuery = users.whereEqualTo("userLastName", firstPart).whereGreaterThanOrEqualTo("userFirstName", secondPart).whereLessThanOrEqualTo("userFirstName", secondPart + '\uf8ff');
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

        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(taskList.toArray(new Task[taskList.size()]));
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
            getActivity().getMenuInflater().inflate(R.menu.floating_context_menu, menu);
        } else if (v.getId() == R.id.editEventGuestListListView) {
            getActivity().getMenuInflater().inflate(R.menu.floating_context_menu_special, menu);
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
                return true;
            case R.id.deleteGuestSpecial:
                final String userId = oldGuestIdList.get(info.position);
                Query query1 = attendeeEvents.document(userId).collection("Invited").whereEqualTo("Event", eventId);
                query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
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
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
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
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
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
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
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
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
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
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
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
            case R.id.cancelDeleteGuestSpecial:
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
                        final String guestId = documentSnapshot.get("User").toString();
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