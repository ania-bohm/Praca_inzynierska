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

public class CommonExpenseAdapter extends RecyclerView.Adapter<CommonExpenseAdapter.CommonExpenseItemHolder> {

    CommonExpenseFragment commonExpenseFragment;
    ArrayList<CommonExpense> commonExpenseList;
    String eventId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference commonExpenseLists = db.collection("CommonExpenseLists");

    public CommonExpenseAdapter(CommonExpenseFragment commonExpenseFragment, ArrayList<CommonExpense> commonExpenseList, String eventId) {
        this.commonExpenseFragment = commonExpenseFragment;
        this.commonExpenseList = commonExpenseList;
        this.eventId = eventId;
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
                commonExpenseFragment.eventCostTitleMaterialEditText.setText(commonExpenseList.get(position).getEventCostTitle());
                commonExpenseFragment.eventCostValueMaterialEditText.setText(commonExpenseList.get(position).getEventCostValue());
                commonExpenseFragment.isUpdate = true;
                commonExpenseFragment.eventCostItemToUpdateId = commonExpenseList.get(position).getEventCostId();
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
                            double currentBudgetLeftDouble = Double.valueOf(commonExpenseFragment.displa.getText().toString());
                            long currentBudgetLeftLong = (long) (currentBudgetLeftDouble * 100);
                            long newPaidCostLong = commonExpenseList.get(position).getEventCostValue();
                            double newBudgetLeftDouble = (currentBudgetLeftLong - newPaidCostLong) / 100;
                            if (currentBudgetLeftLong - newPaidCostLong < 0) {
                                commonExpenseFragment.displayEventBudgetLeftTextView.setTextColor(Color.RED);
                            } else {
                                commonExpenseFragment.displayEventBudgetLeftTextView.setTextColor(Color.WHITE);
                            }
                            commonExpenseFragment.displayEventBudgetLeftTextView.setText(String.valueOf(newBudgetLeftDouble));
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
                    commonExpenseLists.document(eventId).collection("EventCostList").document(commonExpenseList.get(position).getEventCostId()).update("eventCostPaid", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            commonExpenseList.get(position).setEventCostPaid(false);
                            double currentBudgetLeftDouble = Double.valueOf(eventBudgetFragment.displayEventBudgetLeftTextView.getText().toString());
                            long currentBudgetLeftLong = (long) (currentBudgetLeftDouble * 100);
                            long newUnPaidCostLong = commonExpenseList.get(position).getEventCostValue();
                            double newBudgetLeftDouble = (currentBudgetLeftLong + newUnPaidCostLong) / 100;
                            if (currentBudgetLeftLong + newUnPaidCostLong < 0) {
                                eventBudgetFragment.displayEventBudgetLeftTextView.setTextColor(Color.RED);
                            } else {
                                eventBudgetFragment.displayEventBudgetLeftTextView.setTextColor(Color.WHITE);
                            }
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

