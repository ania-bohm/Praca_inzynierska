package com.annabohm.pracainzynierska.adapters;

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

import com.annabohm.pracainzynierska.datamodels.CommonExpense;
import com.annabohm.pracainzynierska.datamodels.Event;
import com.annabohm.pracainzynierska.singletons.FirestoreInstanceSingleton;
import com.annabohm.pracainzynierska.R;
import com.annabohm.pracainzynierska.datamodels.User;
import com.annabohm.pracainzynierska.fragments.CommonExpenseFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class CommonExpenseAdapter extends RecyclerView.Adapter<CommonExpenseAdapter.CommonExpenseItemHolder> {

    CommonExpenseFragment commonExpenseFragment;
    ArrayList<CommonExpense> commonExpenseList;
    String eventId, currentUserId;
    FirestoreInstanceSingleton firestoreInstanceSingleton = FirestoreInstanceSingleton.getInstance();
    CollectionReference commonExpenseLists = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("CommonExpenseLists");
    CollectionReference users = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Users");
    CollectionReference events = firestoreInstanceSingleton.getFirebaseFirestoreRef().collection("Events");
    String eventAuthorId;

    public CommonExpenseAdapter(CommonExpenseFragment commonExpenseFragment, ArrayList<CommonExpense> commonExpenseList, String eventId, String currentUserId) {
        this.commonExpenseFragment = commonExpenseFragment;
        this.commonExpenseList = commonExpenseList;
        this.eventId = eventId;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public CommonExpenseItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.common_expense_item, parent, false);
        return new CommonExpenseItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommonExpenseItemHolder holder, final int position) {
        users.document(commonExpenseList.get(position).getCommonExpensePayingUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                assert user != null;
                String displayName = user.getUserFirstName() + " " + user.getUserLastName();
                holder.commonExpenseItemPayingUserDisplayTextView.setText(displayName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
        holder.commonExpenseItemTitleTextView.setText(commonExpenseList.get(position).getCommonExpenseTitle());
        long commonExpenseValueLong = commonExpenseList.get(position).getCommonExpenseValue();
        double commonExpenseValueDouble = commonExpenseValueLong / 100;
        holder.commonExpenseItemValueTextView.setText(String.valueOf(commonExpenseValueDouble));
        holder.commonExpenseItemToSettleCheckBox.setChecked(commonExpenseList.get(position).isCommonExpenseToSettle());

        if (commonExpenseList.get(position).isCommonExpenseToSettle()) {
            holder.commonExpenseItemLinearLayout.setBackgroundColor(Color.LTGRAY);
            holder.commonExpenseItemTitleTextView.setPaintFlags(holder.commonExpenseItemTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.commonExpenseItemValueTextView.setPaintFlags(holder.commonExpenseItemValueTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.commonExpenseItemLinearLayout.setBackgroundColor(Color.WHITE);
            holder.commonExpenseItemTitleTextView.setPaintFlags(holder.commonExpenseItemTitleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.commonExpenseItemValueTextView.setPaintFlags(holder.commonExpenseItemValueTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if (currentUserId.equals(commonExpenseList.get(position).getCommonExpensePayingUserId())) {
                    commonExpenseFragment.commonExpenseTitleMaterialEditText.setText(commonExpenseList.get(position).getCommonExpenseTitle());
                    commonExpenseFragment.commonExpenseValueMaterialEditText.setText(String.valueOf((double) (commonExpenseList.get(position).getCommonExpenseValue() / 100)));
                    commonExpenseFragment.isUpdate = true;
                    commonExpenseFragment.commonExpenseItemToUpdateId = commonExpenseList.get(position).getCommonExpenseId();
                }
            }
        });
        if (!currentUserId.equals(commonExpenseList.get(position).getCommonExpensePayingUserId())) {
            holder.commonExpenseItemToSettleCheckBox.setEnabled(false);
        }
        events.document(eventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Event event = documentSnapshot.toObject(Event.class);
                    assert event != null;
                    eventAuthorId = event.getEventAuthor();
                    if (currentUserId.equals(eventAuthorId)) {
                        holder.commonExpenseItemToSettleCheckBox.setEnabled(true);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.toString());
            }
        });
        holder.commonExpenseItemToSettleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (holder.commonExpenseItemToSettleCheckBox.isChecked()) {
                    commonExpenseLists.document(eventId).collection("CommonExpenseList").document(commonExpenseList.get(position).getCommonExpenseId()).update("commonExpenseToSettle", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            commonExpenseList.get(position).setCommonExpenseToSettle(true);
                            double currentCommonExpenseToSettleDouble = Double.parseDouble(commonExpenseFragment.displayCommonExpenseToSettleTextView.getText().toString());
                            long currentCommonExpenseToSettleLong = (long) (currentCommonExpenseToSettleDouble * 100);
                            long newCommonExpenseToSettleLong = commonExpenseList.get(position).getCommonExpenseValue();
                            double newBudgetLeftDouble = (currentCommonExpenseToSettleLong + newCommonExpenseToSettleLong) / 100;

                            commonExpenseFragment.displayCommonExpenseToSettleTextView.setText(String.valueOf(newBudgetLeftDouble));
                            holder.commonExpenseItemTitleTextView.setPaintFlags(holder.commonExpenseItemTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.commonExpenseItemValueTextView.setPaintFlags(holder.commonExpenseItemValueTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            holder.commonExpenseItemLinearLayout.setBackgroundColor(Color.LTGRAY);
                            notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    });

                } else {
                    commonExpenseLists.document(eventId).collection("CommonExpenseList").document(commonExpenseList.get(position).getCommonExpenseId()).update("commonExpenseToSettle", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            commonExpenseList.get(position).setCommonExpenseToSettle(false);
                            double currentCommonExpenseToSettleDouble = Double.parseDouble(commonExpenseFragment.displayCommonExpenseToSettleTextView.getText().toString());
                            long currentCommonExpenseToSettleLong = (long) (currentCommonExpenseToSettleDouble * 100);
                            long newCommonExpenseToSettleLong = commonExpenseList.get(position).getCommonExpenseValue();
                            double newBudgetLeftDouble = (currentCommonExpenseToSettleLong - newCommonExpenseToSettleLong) / 100;

                            commonExpenseFragment.displayCommonExpenseToSettleTextView.setText(String.valueOf(newBudgetLeftDouble));
                            holder.commonExpenseItemLinearLayout.setBackgroundColor(Color.WHITE);
                            holder.commonExpenseItemTitleTextView.setPaintFlags(holder.commonExpenseItemTitleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            holder.commonExpenseItemValueTextView.setPaintFlags(holder.commonExpenseItemValueTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
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
        return commonExpenseList.size();
    }

    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }

    public static class CommonExpenseItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        CommonExpenseAdapter.ItemClickListener itemClickListener;
        TextView commonExpenseItemTitleTextView, commonExpenseItemValueTextView, commonExpenseItemPayingUserDisplayTextView;
        CheckBox commonExpenseItemToSettleCheckBox;
        LinearLayout commonExpenseItemLinearLayout;

        public CommonExpenseItemHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
            commonExpenseItemTitleTextView = itemView.findViewById(R.id.commonExpenseItemTitleTextView);
            commonExpenseItemValueTextView = itemView.findViewById(R.id.commonExpenseItemValueTextView);
            commonExpenseItemPayingUserDisplayTextView = itemView.findViewById(R.id.commonExpenseItemPayingUserDisplayTextView);
            commonExpenseItemToSettleCheckBox = itemView.findViewById(R.id.commonExpenseItemToSettleCheckBox);
            commonExpenseItemLinearLayout = itemView.findViewById(R.id.commonExpenseItemLinearLayout);
        }

        public void setItemClickListener(CommonExpenseAdapter.ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition(), false);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(R.string.event_cost_select_action);
            menu.add(0, 0, getAdapterPosition(), R.string.event_cost_delete);
            menu.add(0, 0, getAdapterPosition(), R.string.event_cost_cancel);
        }
    }
}

