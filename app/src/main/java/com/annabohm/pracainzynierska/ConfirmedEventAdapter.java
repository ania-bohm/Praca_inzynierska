package com.annabohm.pracainzynierska;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference events = db.collection("Events");
    CollectionReference users = db.collection("Users");
    DocumentReference userAuthor;

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
            DateFormat dateFormatterRead = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            final DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
            final DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");

            try {
                eventDateStart = dateFormatterRead.parse(eventDateStart.toString());
                eventTimeStart = dateFormatterRead.parse(eventTimeStart.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                Log.i(TAG, e.toString());
            }

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
                        viewHolder.miniEventDateStartTextView.setText(dateFormatterPrint.format(finalEventDateStart));
                        viewHolder.miniEventTimeStartTextView.setText(timeFormatterPrint.format(finalEventTimeStart));
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

    public void deleteItem(int position) {
        events.document(getEventId(position)).delete();
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
