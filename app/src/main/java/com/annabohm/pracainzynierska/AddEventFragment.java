package com.annabohm.pracainzynierska;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class AddEventFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    NavController navController;
    Context context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference users = db.collection("Users");
    CollectionReference eventAttendees = db.collection("EventAttendees");
    CollectionReference attendeeEvents = db.collection("AttendeeEvents");
    Button eventReadyButton, eventCancelButton;
    EditText eventNameEditText, eventDateStartEditText, eventTimeStartEditText, eventDateFinishEditText, eventTimeFinishEditText, eventLocationEditText, eventDescriptionEditText;
    SearchView eventGuestListSearchView;
    ScrollView eventUserSearchScrollView;
    ListView eventUserSearchListView;
    ListView eventGuestListListView;
    Spinner eventImageSpinner;
    Integer chosenImage;
    ArrayList<User> invitedUsersList, foundUsersList;
    ArrayList<String> invitedUsersIdList, foundUsersIdList;
    UserSearchListAdapter invitedUsersAdapter, foundUsersAdapter;

    private View.OnClickListener eventReadyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String eventName = eventNameEditText.getText().toString();
            String eventDescription = eventDescriptionEditText.getText().toString();
            String eventLocation = eventLocationEditText.getText().toString();
            String dateStartValue = eventDateStartEditText.getText().toString();
            String dateFinishValue = eventDateFinishEditText.getText().toString();
            String timeStartValue = eventTimeStartEditText.getText().toString();
            String timeFinishValue = eventTimeFinishEditText.getText().toString();
            Integer eventImage = chosenImage;

            if (eventName.trim().isEmpty() || eventDescription.trim().isEmpty() || dateStartValue.trim().isEmpty() || dateFinishValue.trim().isEmpty() || timeStartValue.trim().isEmpty() || timeFinishValue.trim().isEmpty()) {
                Toast.makeText(context, "Please fill all the fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat timeFormatter = new SimpleDateFormat("HH:mm");
            Date eventDateStart = null;
            Date eventDateFinish = null;
            Date eventTimeStart = null;
            Date eventTimeFinish = null;

            try {
                eventDateStart = dateFormatter.parse(dateStartValue);
                eventDateFinish = dateFormatter.parse(dateFinishValue);
                eventTimeStart = timeFormatter.parse(timeStartValue);
                eventTimeFinish = timeFormatter.parse(timeFinishValue);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
                Log.i(TAG, e.toString());
            }

            final Event event = new Event(eventName, eventDateStart, eventTimeStart, eventDateFinish, eventTimeFinish, eventLocation, eventDescription, eventImage);
            events.add(event).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    //setGuestList(eventGuestList, documentReference);
                    Toast.makeText(context, "Event saved successfully", Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, "Id: " + documentReference.getId(), Toast.LENGTH_SHORT).show();
                    String eventId = documentReference.getId();
                    if (!invitedUsersIdList.isEmpty()) {
                        Map<String, ArrayList<String>> docDataUserId = new HashMap<>();
                        docDataUserId.put("Attendees", invitedUsersIdList);

                        eventAttendees.document(eventId).collection("Invited").add(docDataUserId).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(context, "EventAttendee added", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "EventAttendee adding failed!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, e.toString());
                            }
                        });

                        for (int i = 0; i < invitedUsersIdList.size(); i++) {
                            Map<String, String> docDataEventId = new HashMap<>();
                            docDataEventId.put("Event", eventId);
                            attendeeEvents.document(invitedUsersIdList.get(i)).collection("Invited").add(docDataEventId).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(context, "AttendeesEvent added", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "AttendeesEvent adding failed!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, e.toString());
                                }
                            });
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Event save failed!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                }
            });
            navController.popBackStack();
        }
    };

    private View.OnClickListener eventCancelOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            navController.popBackStack();
        }
    };

    public AddEventFragment() {
        // Required empty public constructor
    }

    public static AddEventFragment newInstance(String param1, String param2) {
        AddEventFragment fragment = new AddEventFragment();
        return fragment;
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
        ((MainActivity) getActivity()).setDrawerLocked();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        eventReadyButton = view.findViewById(R.id.eventReadyButton);
        eventCancelButton = view.findViewById(R.id.eventCancelButton);
        eventReadyButton.setOnClickListener(eventReadyOnClickListener);
        eventCancelButton.setOnClickListener(eventCancelOnClickListener);
        eventNameEditText = view.findViewById(R.id.eventNameEditText);
        eventDateStartEditText = view.findViewById(R.id.eventDateStartEditText);
        eventTimeStartEditText = view.findViewById(R.id.eventTimeStartEditText);
        eventDateFinishEditText = view.findViewById(R.id.eventDateFinishEditText);
        eventTimeFinishEditText = view.findViewById(R.id.eventTimeFinishEditText);
        eventLocationEditText = view.findViewById(R.id.eventLocationEditText);
        eventDescriptionEditText = view.findViewById(R.id.eventDescriptionEditText);
        eventGuestListSearchView = view.findViewById(R.id.eventGuestListSearchView);
        eventUserSearchScrollView = view.findViewById(R.id.eventUserSearchScrollView);
        eventUserSearchListView = view.findViewById(R.id.eventUserSearchListView);
        eventImageSpinner = view.findViewById(R.id.eventImageSpinner);
        eventGuestListListView = view.findViewById(R.id.eventGuestListListView);

        invitedUsersList = new ArrayList<>();
        foundUsersList = new ArrayList<>();

        invitedUsersIdList = new ArrayList<>();
        foundUsersIdList = new ArrayList<>();

        hideEventUserSearchListView();

        invitedUsersAdapter = new UserSearchListAdapter(context, invitedUsersList);
        foundUsersAdapter = new UserSearchListAdapter(context, foundUsersList);
        eventGuestListListView.setAdapter(invitedUsersAdapter);
        eventUserSearchListView.setAdapter(foundUsersAdapter);
        registerForContextMenu(eventGuestListListView);

        eventGuestListSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
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
                User user = foundUsersList.get(position);
                String userId = foundUsersIdList.get(position);
                if (!containsUserId(userId)) {
                    invitedUsersList.add(user);
                    invitedUsersIdList.add(userId);
                }
                invitedUsersAdapter.notifyDataSetChanged();
            }
        });

        eventGuestListListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        SimpleImageArrayAdapter adapter = new SimpleImageArrayAdapter(context,
                new Integer[]{R.drawable.rectangular_background_1, R.drawable.rectangular_background, R.drawable.rectangular_background_2, R.drawable.rectangular_background_3});
        eventImageSpinner.setAdapter(adapter);
        eventImageSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.floating_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteGuest:
                invitedUsersAdapter.remove(invitedUsersAdapter.getItem(info.position));
                Toast.makeText(context, "Position: " + info.position, Toast.LENGTH_SHORT).show();
                invitedUsersIdList.remove(info.position);
                return true;
            case R.id.cancelDeleteGuest:
                Toast.makeText(context, "Cancelled deleting", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void showEventUserSearchListView() {
        eventUserSearchScrollView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 180));
    }

    public void hideEventUserSearchListView() {
        eventUserSearchScrollView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

//    public boolean containsUser(User user) {
//        for (int i = 0; i < invitedUsersList.size(); i++) {
//            if (invitedUsersList.get(i).getUserEmail().equals(user.getUserEmail())) {
//                return true;
//            }
//        }
//        return false;
//    }

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
                        Toast.makeText(context, "User: " + user.toString(), Toast.LENGTH_SHORT).show();
                        userList.add(user);
                        userIdList.add(document.getId());
                    }
                }
                loadUsers(userList, userIdList);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "User search failed!", Toast.LENGTH_SHORT).show();
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