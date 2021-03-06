package com.annabohm.pracainzynierska.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.annabohm.pracainzynierska.others.DateHandler;
import com.annabohm.pracainzynierska.datamodels.Event;
import com.annabohm.pracainzynierska.singletons.FirestoreInstanceSingleton;
import com.annabohm.pracainzynierska.R;
import com.annabohm.pracainzynierska.datamodels.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class ConfirmedEventAdapter extends RecyclerView.Adapter<ConfirmedEventAdapter.ViewHolder> {
    HashMap<String, Event> eventMap;
    ArrayList<Event> sortedEventsList;
    OnItemClickListener listener;
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    CollectionReference users = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Users");
    DocumentReference userAuthor;
    DateHandler dateHandler = new DateHandler("dd/MM/yyyy", "HH:mm");

    public ConfirmedEventAdapter(HashMap<String, Event> eventMap) {
        this.eventMap = eventMap;
        sortedEventsList = new ArrayList<>(eventMap.values());
        sortByDate();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.mini_event, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        sortedEventsList = new ArrayList<>(eventMap.values());
        sortByDate();
        if (sortedEventsList.size() > 0) {
            Date eventDateStart = sortedEventsList.get(position).getEventDateStart();
            Date eventTimeStart = sortedEventsList.get(position).getEventTimeStart();

            userAuthor = users.document(sortedEventsList.get(position).getEventAuthor());
            final Date finalEventDateStart = eventDateStart;
            final Date finalEventTimeStart = eventTimeStart;
            userAuthor.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        viewHolder.miniEventLinearLayout.setBackgroundResource(sortedEventsList.get(position).getEventImage());
                        viewHolder.miniEventNameTextView.setText(sortedEventsList.get(position).getEventName());
                        assert finalEventDateStart != null;
                        viewHolder.miniEventDateStartTextView.setText(dateHandler.convertDateToString(finalEventDateStart));
                        assert finalEventTimeStart != null;
                        viewHolder.miniEventTimeStartTextView.setText(dateHandler.convertTimeToString(finalEventTimeStart));
                        assert user != null;
                        viewHolder.miniEventAuthorTextView.setText(user.getUserFirstName() + " " + user.getUserLastName());
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                listener.onItemClick(position);
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, e.toString());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return eventMap.size();
    }

    public void sortByDate() {
        Collections.sort(sortedEventsList, new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return o1.getEventDateStart().compareTo(o2.getEventDateStart());
            }
        });
    }

    public String getEventId(int position) {
        for (Map.Entry<String, Event> set : eventMap.entrySet()) {
            if (sortedEventsList.get(position) == set.getValue()) {
                return set.getKey();
            }
        }
        return null;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout miniEventLinearLayout;
        CardView miniEventCardView;
        TextView miniEventNameTextView;
        TextView miniEventDateStartTextView;
        TextView miniEventTimeStartTextView;
        TextView miniEventAuthorTextView;

        public ViewHolder(View view) {
            super(view);
            miniEventCardView = view.findViewById(R.id.miniEventCardView);
            miniEventLinearLayout = view.findViewById(R.id.miniEventLinearLayout);
            miniEventNameTextView = view.findViewById(R.id.miniEventNameTextView);
            miniEventDateStartTextView = view.findViewById(R.id.miniEventDateStartTextView);
            miniEventTimeStartTextView = view.findViewById(R.id.miniEventTimeStartTextView);
            miniEventAuthorTextView = view.findViewById(R.id.miniEventAuthorTextView);
        }
    }
}
