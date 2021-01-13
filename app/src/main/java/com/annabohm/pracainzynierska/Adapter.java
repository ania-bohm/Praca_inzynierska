package com.annabohm.pracainzynierska;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    List<Event> eventList;
    LayoutInflater inflater;
//    DatabaseReference mReference;
//    StorageReference photoRef;
    Context context;
    NavController navController;

    public Adapter(Context context, List<Event> eventList) {
        this.eventList = eventList;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.mini_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
//        Picasso.get().load(photos.get(holder.getAdapterPosition())).resize(400,400).centerCrop().into(holder.photo);
//        holder.deleteCheckBox.setChecked(false);
//        photoCount.setText(""+this.getItemCount()+" Photos");
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {
        LinearLayout miniEventLinearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            miniEventLinearLayout = itemView.findViewById(R.id.miniEventLinearLayout);
            miniEventLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    navController = Navigation.findNavController(view);
                    navController.navigate(R.id.mainToDisplayEvent, bundle);
                }
            });
        }
    }
}
