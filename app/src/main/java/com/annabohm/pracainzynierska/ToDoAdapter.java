package com.annabohm.pracainzynierska;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ToDoItemHolder> {

    ArrayList<ToDo> toDoList;

    public ToDoAdapter(ArrayList<ToDo> toDoList){
        this.toDoList = toDoList;
    }

    @NonNull
    @Override
    public ToDoItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.to_do_item, parent, false);
        return new ToDoItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoItemHolder holder, int position) {

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

        public ToDoItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            toDoItemTitleTextView = itemView.findViewById(R.id.toDoItemTitleTextView);
            toDoItemDescriptionTextView = itemView.findViewById(R.id.toDoItemDescriptionTextView);
            toDoItemCheckedCheckBox = itemView.findViewById(R.id.toDoItemCheckedCheckBox);
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


