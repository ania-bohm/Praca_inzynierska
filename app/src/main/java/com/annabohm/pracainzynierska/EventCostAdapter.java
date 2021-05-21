package com.annabohm.pracainzynierska;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class EventCostAdapter extends RecyclerView.Adapter<EventCostAdapter.EventCostItemHolder> {

    EventBudgetFragment eventBudgetFragment;
    ArrayList<EventCost> eventCostList;
    String eventId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference eventCostLists = db.collection("EventCostLists");

    public EventCostAdapter(EventBudgetFragment eventBudgetFragment, ArrayList<EventCost> eventCostList, String eventId) {
        this.eventBudgetFragment = eventBudgetFragment;
        this.eventCostList = eventCostList;
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public EventCostItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_cost_item, parent, false);
        return new EventCostItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventCostItemHolder holder, final int position) {
        holder.eventCostItemTitleTextView.setText(eventCostList.get(position).getEventCostTitle());
        long eventCostValueLong = eventCostList.get(position).getEventCostValue();
        double eventCostValueDouble = eventCostValueLong / 100;
        holder.eventCostItemValueTextView.setText(String.valueOf(eventCostValueDouble));
        holder.eventCostItemPaidCheckBox.setChecked(eventCostList.get(position).isEventCostPaid());

        if (eventCostList.get(position).isEventCostPaid()) {
            holder.eventCostItemLinearLayout.setBackgroundColor(Color.LTGRAY);
            holder.eventCostItemTitleTextView.setPaintFlags(holder.eventCostItemTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.eventCostItemValueTextView.setPaintFlags(holder.eventCostItemValueTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.eventCostItemLinearLayout.setBackgroundColor(Color.WHITE);
            holder.eventCostItemTitleTextView.setPaintFlags(holder.eventCostItemTitleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.eventCostItemValueTextView.setPaintFlags(holder.eventCostItemValueTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                eventBudgetFragment.eventCostTitleMaterialEditText.setText(eventCostList.get(position).getEventCostTitle());
                eventBudgetFragment.eventCostValueMaterialEditText.setText(eventCostList.get(position).getEventCostValue());
                eventBudgetFragment.isUpdate = true;
                eventBudgetFragment.eventCostItemToUpdateId = eventCostList.get(position).getEventCostId();
            }
        });

        holder.eventCostItemPaidCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (holder.eventCostItemPaidCheckBox.isChecked()) {
                    eventCostLists.document(eventId).collection("EventCostList").document(eventCostList.get(position).getEventCostId()).update("eventCostPaid", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            eventCostList.get(position).setEventCostPaid(true);
                            double currentBudgetLeftDouble = Double.valueOf(eventBudgetFragment.displayEventBudgetLeftTextView.getText().toString());
                            long currentBudgetLeftLong = (long) (currentBudgetLeftDouble * 100);
                            long newPaidCostLong = eventCostList.get(position).getEventCostValue();
                            double newBudgetLeftDouble = (currentBudgetLeftLong - newPaidCostLong) / 100;
                            eventBudgetFragment.displayEventBudgetLeftTextView.setText(String.valueOf(newBudgetLeftDouble));
                            holder.eventCostItemTitleTextView.setPaintFlags(holder.eventCostItemTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.eventCostItemValueTextView.setPaintFlags(holder.eventCostItemValueTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.eventCostItemLinearLayout.setBackgroundColor(Color.LTGRAY);
                            notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    });

                } else {
                    eventCostLists.document(eventId).collection("EventCostList").document(eventCostList.get(position).getEventCostId()).update("eventCostPaid", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            eventCostList.get(position).setEventCostPaid(false);
                            double currentBudgetLeftDouble = Double.valueOf(eventBudgetFragment.displayEventBudgetLeftTextView.getText().toString());
                            long currentBudgetLeftLong = (long) (currentBudgetLeftDouble * 100);
                            long newUnPaidCostLong = eventCostList.get(position).getEventCostValue();
                            double newBudgetLeftDouble = (currentBudgetLeftLong + newUnPaidCostLong) / 100;
                            eventBudgetFragment.displayEventBudgetLeftTextView.setText(String.valueOf(newBudgetLeftDouble));
                            holder.eventCostItemLinearLayout.setBackgroundColor(Color.WHITE);
                            holder.eventCostItemTitleTextView.setPaintFlags(holder.eventCostItemTitleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            holder.eventCostItemValueTextView.setPaintFlags(holder.eventCostItemValueTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventCostList.size();
    }

    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }

    public static class EventCostItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        EventCostAdapter.ItemClickListener itemClickListener;
        TextView eventCostItemTitleTextView, eventCostItemValueTextView;
        CheckBox eventCostItemPaidCheckBox;
        LinearLayout eventCostItemLinearLayout;

        public EventCostItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            eventCostItemTitleTextView = itemView.findViewById(R.id.eventCostItemTitleTextView);
            eventCostItemValueTextView = itemView.findViewById(R.id.eventCostItemValueTextView);
            eventCostItemPaidCheckBox = itemView.findViewById(R.id.eventCostItemPaidCheckBox);
            eventCostItemLinearLayout = itemView.findViewById(R.id.eventCostItemLinearLayout);
        }

        public void setItemClickListener(EventCostAdapter.ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Wybierz akcję");
            menu.add(0, 0, getAdapterPosition(), "Usuń");
            menu.add(0, 0, getAdapterPosition(), "Anuluj");
        }
    }
}

