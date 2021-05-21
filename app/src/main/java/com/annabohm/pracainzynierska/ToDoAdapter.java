package com.annabohm.pracainzynierska;

import android.content.Context;
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

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ToDoItemHolder> {

    ArrayList<ToDo> toDoList;
    String eventId;
    ToDoFragment toDoFragment;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference toDoLists = db.collection("ToDoLists");

    public ToDoAdapter(ToDoFragment toDoFragment, ArrayList<ToDo> toDoList, String eventId) {
        this.toDoFragment = toDoFragment;
        this.toDoList = toDoList;
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public ToDoItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.to_do_item, parent, false);
        return new ToDoItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ToDoItemHolder holder, final int position) {
        holder.toDoItemTitleTextView.setText(toDoList.get(position).getToDoTitle());
        holder.toDoItemDescriptionTextView.setText(toDoList.get(position).getToDoDescription());
        holder.toDoItemCheckedCheckBox.setChecked(toDoList.get(position).isToDoChecked());

        if (toDoList.get(position).isToDoChecked()) {
            holder.toDoItemLinearLayout.setBackgroundColor(Color.LTGRAY);
            holder.toDoItemTitleTextView.setPaintFlags(holder.toDoItemTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.toDoItemDescriptionTextView.setPaintFlags(holder.toDoItemDescriptionTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.toDoItemLinearLayout.setBackgroundColor(Color.WHITE);
            holder.toDoItemTitleTextView.setPaintFlags(holder.toDoItemTitleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.toDoItemDescriptionTextView.setPaintFlags(holder.toDoItemDescriptionTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                toDoFragment.toDoTitleMaterialEditText.setText(toDoList.get(position).getToDoTitle());
                toDoFragment.toDoDescriptionMaterialEditText.setText(toDoList.get(position).getToDoDescription());
                toDoFragment.isUpdate = true;
                toDoFragment.toDoItemToUpdateId = toDoList.get(position).getToDoId();
            }
        });

        holder.toDoItemCheckedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (holder.toDoItemCheckedCheckBox.isChecked()) {
                    toDoLists.document(eventId).collection("ToDoList").document(toDoList.get(position).getToDoId()).update("toDoChecked", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            toDoList.get(position).setToDoChecked(true);
                            String currentDoneCounterString = toDoFragment.displayToDoDoneTextView.getText().toString();
                            int currentDoneCounter = Integer.valueOf(currentDoneCounterString);
                            currentDoneCounter++;
                            toDoFragment.displayToDoDoneTextView.setText(String.valueOf(currentDoneCounter));
                            toDoFragment.displayToDoDoneTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            holder.toDoItemTitleTextView.setPaintFlags(holder.toDoItemTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.toDoItemDescriptionTextView.setPaintFlags(holder.toDoItemDescriptionTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.toDoItemLinearLayout.setBackgroundColor(Color.LTGRAY);
                            notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    });

                } else {
                    toDoLists.document(eventId).collection("ToDoList").document(toDoList.get(position).getToDoId()).update("toDoChecked", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            toDoList.get(position).setToDoChecked(false);
                            String currentDoneCounterString = toDoFragment.displayToDoDoneTextView.getText().toString();
                            int currentDoneCounter = Integer.valueOf(currentDoneCounterString);
                            currentDoneCounter--;
                            toDoFragment.displayToDoDoneTextView.setText(String.valueOf(currentDoneCounter));
                            toDoFragment.displayToDoDoneTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            holder.toDoItemLinearLayout.setBackgroundColor(Color.WHITE);
                            holder.toDoItemTitleTextView.setPaintFlags(holder.toDoItemTitleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            holder.toDoItemDescriptionTextView.setPaintFlags(holder.toDoItemDescriptionTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
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
        return toDoList.size();
    }

    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }

    public static class ToDoItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        ToDoAdapter.ItemClickListener itemClickListener;
        TextView toDoItemTitleTextView, toDoItemDescriptionTextView;
        CheckBox toDoItemCheckedCheckBox;
        LinearLayout toDoItemLinearLayout;

        public ToDoItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            toDoItemTitleTextView = itemView.findViewById(R.id.toDoItemTitleTextView);
            toDoItemDescriptionTextView = itemView.findViewById(R.id.toDoItemDescriptionTextView);
            toDoItemCheckedCheckBox = itemView.findViewById(R.id.toDoItemCheckedCheckBox);
            toDoItemLinearLayout = itemView.findViewById(R.id.toDoItemLinearLayout);
        }

        public void setItemClickListener(ToDoAdapter.ItemClickListener itemClickListener) {
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


