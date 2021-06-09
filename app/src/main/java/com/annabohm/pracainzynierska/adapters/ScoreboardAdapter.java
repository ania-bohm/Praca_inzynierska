package com.annabohm.pracainzynierska.adapters;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.annabohm.pracainzynierska.singletons.FirestoreInstanceSingleton;
import com.annabohm.pracainzynierska.R;
import com.annabohm.pracainzynierska.datamodels.Score;
import com.annabohm.pracainzynierska.fragments.ScoreboardFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardAdapter.ScoreItemHolder> {
    ArrayList<Score> scoreList;
    String eventId, currentUserId, eventAuthorId;
    ScoreboardFragment scoreboardFragment;
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    CollectionReference scoreLists = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("ScoreLists");

    public ScoreboardAdapter(ScoreboardFragment scoreboardFragment, ArrayList<Score> scoreList, String eventId, String currentUserId, String eventAuthorId) {
        this.scoreboardFragment = scoreboardFragment;
        this.scoreList = scoreList;
        this.eventId = eventId;
        this.currentUserId = currentUserId;
        this.eventAuthorId = eventAuthorId;
    }

    @NonNull
    @Override
    public ScoreItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.score_list_item, parent, false);
        return new ScoreItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreItemHolder holder, final int position) {
        holder.scoreItemGuestNameTextView.setText(scoreList.get(position).getScoreGuestName());
        holder.scoreItemValueTextView.setText(String.valueOf(scoreList.get(position).getScoreValue()));

        holder.setItemClickListener(new ScoreboardAdapter.ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                scoreboardFragment.scoreboardGuestNameMaterialEditText.setText(scoreList.get(position).getScoreGuestName());
                scoreboardFragment.scoreboardValueMaterialEditText.setText(String.valueOf(scoreList.get(position).getScoreValue()));
                scoreboardFragment.isUpdate = true;
                scoreboardFragment.scoreItemToUpdateId = scoreList.get(position).getScoreId();
            }
        });

        holder.scoreDecrementValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scoreList.size() > 0) {
                    final int newValue = scoreList.get(position).getScoreValue() - 1;
                    if (newValue >= 0) {
                        scoreLists.document(eventId).collection("ScoreList").document(scoreList.get(position).getScoreId()).update("scoreValue", newValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                scoreList.get(position).setScoreValue(newValue);
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
            }
        });

        holder.scoreIncrementValueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scoreList.size() > 0) {
                    final int newValue = scoreList.get(position).getScoreValue() + 1;
                    scoreLists.document(eventId).collection("ScoreList").document(scoreList.get(position).getScoreId()).update("scoreValue", newValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            scoreList.get(position).setScoreValue(newValue);
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
        return scoreList.size();
    }


    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }


    public static class ScoreItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        ScoreboardAdapter.ItemClickListener itemClickListener;
        TextView scoreItemGuestNameTextView, scoreItemValueTextView;
        FloatingActionButton scoreDecrementValueButton, scoreIncrementValueButton;

        public ScoreItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            scoreItemGuestNameTextView = itemView.findViewById(R.id.scoreItemGuestNameTextView);
            scoreItemValueTextView = itemView.findViewById(R.id.scoreItemValueTextView);
            scoreDecrementValueButton = itemView.findViewById(R.id.scoreDecrementValueButton);
            scoreIncrementValueButton = itemView.findViewById(R.id.scoreIncrementValueButton);
        }

        public void setItemClickListener(ScoreboardAdapter.ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(R.string.score_select_action);
            menu.add(0, 0, getAdapterPosition(), R.string.score_delete);
            menu.add(0, 0, getAdapterPosition(), R.string.score_cancel);
        }
    }
}
