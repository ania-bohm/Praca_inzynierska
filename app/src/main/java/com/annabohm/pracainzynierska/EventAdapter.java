package com.annabohm.pracainzynierska;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class EventAdapter extends FirestoreRecyclerAdapter<Event, EventAdapter.EventHolder> {
    OnItemClickListener listener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference users = db.collection("Users");
    DocumentReference userAuthor;

    public EventAdapter(@NonNull FirestoreRecyclerOptions<Event> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final EventHolder holder, int position, @NonNull final Event model) {
        Date eventDateStart = model.getEventDateStart();
        Date eventTimeStart = model.getEventTimeStart();
        DateFormat dateFormatterRead = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
        final DateFormat dateFormatterPrint = new SimpleDateFormat("dd/MM/yyyy");
        final DateFormat timeFormatterPrint = new SimpleDateFormat("HH:mm");

        try {
            eventDateStart = dateFormatterRead.parse(eventDateStart.toString());
            eventTimeStart = dateFormatterRead.parse(eventTimeStart.toString());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            Log.i(TAG, e.toString());
        }

        userAuthor = users.document(model.getEventAuthor());
        final Date finalEventDateStart = eventDateStart;
        final Date finalEventTimeStart = eventTimeStart;
        userAuthor.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    holder.miniEventLinearLayout.setBackgroundResource(model.getEventImage());
                    holder.miniEventNameTextView.setText(model.getEventName());
                    holder.miniEventAuthorTextView.setText(user.getUserFirstName() + " " + user.getUserLastName());
                    holder.miniEventDateStartTextView.setText(dateFormatterPrint.format(finalEventDateStart));
                    holder.miniEventTimeStartTextView.setText(timeFormatterPrint.format(finalEventTimeStart));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, e.toString());
            }
        });
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mini_event, parent, false);
        return new EventHolder(view);
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    class EventHolder extends RecyclerView.ViewHolder {
        LinearLayout miniEventLinearLayout;
        TextView miniEventNameTextView;
        TextView miniEventAuthorTextView;
        TextView miniEventDateStartTextView;
        TextView miniEventTimeStartTextView;

        public EventHolder(@NonNull View itemView) {
            super(itemView);
            miniEventLinearLayout = itemView.findViewById(R.id.miniEventLinearLayout);
            miniEventNameTextView = itemView.findViewById(R.id.miniEventNameTextView);
            miniEventAuthorTextView = itemView.findViewById(R.id.miniEventAuthorTextView);
            miniEventDateStartTextView = itemView.findViewById(R.id.miniEventDateStartTextView);
            miniEventTimeStartTextView = itemView.findViewById(R.id.miniEventTimeStartTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }
}
