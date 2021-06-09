package com.annabohm.pracainzynierska.fragments;

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
import com.annabohm.pracainzynierska.singletons.FirebaseAuthInstanceSingleton;
import com.annabohm.pracainzynierska.singletons.FirestoreInstanceSingleton;
import com.annabohm.pracainzynierska.activities.MainActivity;
import com.annabohm.pracainzynierska.R;
import com.annabohm.pracainzynierska.adapters.SimpleImageArrayAdapter;
import com.annabohm.pracainzynierska.datamodels.User;
import com.annabohm.pracainzynierska.adapters.UserSearchListAdapter;
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

import static android.content.ContentValues.TAG;

public class AddEventFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    NavController navController;
    private final View.OnClickListener eventCancelOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };
    Context context;
    UserSearchListAdapter invitedUsersAdapter, foundUsersAdapter;
    SimpleImageArrayAdapter imageArrayAdapter;
    InputMethodManager imm;
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    FirebaseAuthInstanceSingleton firebaseAuthInstanceSingleton = FirebaseAuthInstanceSingleton.getInstance();
    CollectionReference events = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Events");
    CollectionReference users = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Users");
    CollectionReference eventAttendees = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("EventAttendees");
    CollectionReference attendeeEvents = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("AttendeeEvents");
    Button eventReadyButton, eventCancelButton;
    EditText eventDateStartEditText, eventTimeStartEditText, eventDateFinishEditText, eventTimeFinishEditText;
    MaterialEditText eventNameMaterialEditText, eventLocationMaterialEditText, eventDescriptionMaterialEditText, test;
    SearchView eventGuestListSearchView;
    ListView eventUserSearchListView, eventGuestListListView;
    Spinner eventImageSpinner;
    Integer chosenImage;
    String eventAuthor = Objects.requireNonNull(firebaseAuthInstanceSingleton.getFirebaseAuthRef().getCurrentUser()).getUid();
    ArrayList<User> invitedUsersList, foundUsersList;
    ArrayList<String> invitedUsersIdList, foundUsersIdList;
    DateHandler dateHandler;
    private final View.OnClickListener eventReadyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String eventName = eventNameMaterialEditText.getText().toString().trim();
            String eventDescription = eventDescriptionMaterialEditText.getText().toString().trim();
            String eventLocation = eventLocationMaterialEditText.getText().toString().trim();
            String dateStartValue = eventDateStartEditText.getText().toString().trim();
            String dateFinishValue = eventDateFinishEditText.getText().toString().trim();
            String timeStartValue = eventTimeStartEditText.getText().toString().trim();
            String timeFinishValue = eventTimeFinishEditText.getText().toString().trim();
            Integer eventImage = chosenImage;

            if (eventName.isEmpty() || dateStartValue.isEmpty() || dateFinishValue.isEmpty() || timeStartValue.isEmpty() || timeFinishValue.isEmpty() || eventLocation.isEmpty()) {
                Toast.makeText(context, R.string.event_empty_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            Date eventDateStart;
            Date eventDateFinish;
            Date eventTimeStart;
            Date eventTimeFinish;

            if (dateHandler.isDateValid(dateStartValue)
                    && dateHandler.isTimeValid(dateStartValue)
                    && dateHandler.isDateValid(dateFinishValue)
                    && dateHandler.isTimeValid(timeFinishValue)) {
                eventDateStart = dateHandler.convertStringToDate(dateStartValue);
                eventDateFinish = dateHandler.convertStringToDate(dateFinishValue);
                eventTimeStart = dateHandler.convertStringToTime(timeStartValue);
                eventTimeFinish = dateHandler.convertStringToTime(timeFinishValue);
                if (!dateHandler.datesCorrect(eventDateStart, eventTimeStart, eventDateFinish, eventTimeFinish)) {
                    Toast.makeText(context, R.string.event_dates_incorrect, Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(context, R.string.event_dates_incorrect, Toast.LENGTH_SHORT).show();
                return;
            }

            final Event event = new Event(eventName, eventDateStart, eventTimeStart, eventDateFinish, eventTimeFinish, eventLocation, eventDescription, eventImage, eventAuthor);
            events.add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(context, R.string.event_add_success, Toast.LENGTH_SHORT).show();
                    String eventId = documentReference.getId();
                    if (!invitedUsersIdList.isEmpty()) {
                        for (int i = 0; i < invitedUsersIdList.size(); i++) {
                            Map<String, String> docDataUserId = new HashMap<>();
                            docDataUserId.put("User", invitedUsersIdList.get(i));

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

                        for (int i = 0; i < invitedUsersIdList.size(); i++) {
                            Map<String, String> docDataEventId = new HashMap<>();
                            docDataEventId.put("Event", eventId);
                            attendeeEvents.document(invitedUsersIdList.get(i)).collection("Invited").add(docDataEventId).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, R.string.event_add_fail, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                }
            });
            navController.popBackStack();
        }
    };

    public AddEventFragment() {
    }

    public static AddEventFragment newInstance() {
        return new AddEventFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setDrawerLocked();
        imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        return inflater.inflate(R.layout.fragment_add_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        eventReadyButton = view.findViewById(R.id.eventReadyButton);
        eventCancelButton = view.findViewById(R.id.eventCancelButton);
        eventGuestListSearchView = view.findViewById(R.id.eventGuestListSearchView);
        eventNameMaterialEditText = view.findViewById(R.id.eventNameMaterialEditText);
        eventDateStartEditText = view.findViewById(R.id.eventDateStartEditText);
        eventTimeStartEditText = view.findViewById(R.id.eventTimeStartEditText);
        eventDateFinishEditText = view.findViewById(R.id.eventDateFinishEditText);
        eventTimeFinishEditText = view.findViewById(R.id.eventTimeFinishEditText);
        eventLocationMaterialEditText = view.findViewById(R.id.eventLocationMaterialEditText);
        eventDescriptionMaterialEditText = view.findViewById(R.id.eventDescriptionMaterialEditText);
//        test = view.findViewById(R.id.test);
        eventUserSearchListView = view.findViewById(R.id.eventUserSearchListView);
        eventImageSpinner = view.findViewById(R.id.eventImageSpinner);
        eventGuestListListView = view.findViewById(R.id.eventGuestListListView);

        dateHandler = new DateHandler("dd/MM/yyyy", "HH:mm");

        invitedUsersList = new ArrayList<>();
        foundUsersList = new ArrayList<>();
        invitedUsersIdList = new ArrayList<>();
        foundUsersIdList = new ArrayList<>();

        invitedUsersAdapter = new UserSearchListAdapter(context, invitedUsersList);
        foundUsersAdapter = new UserSearchListAdapter(context, foundUsersList);
        eventGuestListListView.setAdapter(invitedUsersAdapter);
        eventUserSearchListView.setAdapter(foundUsersAdapter);

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
        eventImageSpinner.setAdapter(imageArrayAdapter);
        eventImageSpinner.setOnItemSelectedListener(this);

        registerForContextMenu(eventGuestListListView);

        hideEventUserSearchListView();

        eventReadyButton.setOnClickListener(eventReadyOnClickListener);
        eventCancelButton.setOnClickListener(eventCancelOnClickListener);

        eventGuestListSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                eventGuestListSearchView.clearFocus();
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

        eventUserSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard();
                User user = foundUsersList.get(position);
                String userId = foundUsersIdList.get(position);
                if (!containsUserId(userId)) {
                    invitedUsersList.add(user);
                    invitedUsersIdList.add(userId);
                }
                invitedUsersAdapter.notifyDataSetChanged();
                extendEventGuestListListView();
            }
        });

        eventGuestListListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        requireActivity().getMenuInflater().inflate(R.menu.floating_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteGuest:
                invitedUsersAdapter.remove(invitedUsersAdapter.getItem(info.position));
                invitedUsersIdList.remove(info.position);
                invitedUsersAdapter.notifyDataSetChanged();
                extendEventGuestListListView();
                return true;
            case R.id.cancelDeleteGuest:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(eventUserSearchListView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public void showEventUserSearchListView() {
        eventUserSearchListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 350));
    }

    public void hideEventUserSearchListView() {
        eventUserSearchListView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    public void extendEventGuestListListView() {
        int numberOfGuests = invitedUsersIdList.size();
        eventGuestListListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, numberOfGuests * 160));
    }

    public boolean containsUserId(String userId) {
        for (int i = 0; i < invitedUsersIdList.size(); i++) {
            if (invitedUsersIdList.get(i).equals(userId)) {
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
                    for (DocumentSnapshot document : querySnapshot) {
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
        chosenImage = (Integer) parent.getItemAtPosition(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        chosenImage = (Integer) parent.getItemAtPosition(0);
    }

}