package com.annabohm.pracainzynierska;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettleExpenseAdapter extends BaseAdapter {
    ArrayList list;
    private Context context;
    private HashMap<String, Double> expenseHashMap;

    public SettleExpenseAdapter(Context context, HashMap<String, Double> expenseHashMap) {
        this.context = context;
        this.expenseHashMap = expenseHashMap;
        list = new ArrayList();
        list.addAll(expenseHashMap.entrySet());
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Map.Entry<String, Double> getItem(int position) {
        return (Map.Entry) list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setExpenseHashMap(HashMap<String, Double> newExpenseHashMap) {
        this.expenseHashMap = newExpenseHashMap;
        list = new ArrayList();
        list.addAll(expenseHashMap.entrySet());
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expense_to_settle_item, parent, false);
        }

        TextView expenseToSettleItemNameTextView = convertView.findViewById(R.id.expenseToSettleItemNameTextView);
        TextView expenseToSettleItemGiveOrReceiveTextView = convertView.findViewById(R.id.expenseToSettleItemGiveOrReceiveTextView);
        TextView expenseToSettleItemValueTextView = convertView.findViewById(R.id.expenseToSettleItemValueTextView);

        Map.Entry<String, Double> item = getItem(position);

        expenseToSettleItemNameTextView.setText(item.getKey());
        if (item.getValue() < 0) {
            double value = -1 * item.getValue();
            expenseToSettleItemGiveOrReceiveTextView.setText(R.string.settle_expense_gives);
            expenseToSettleItemValueTextView.setText(String.valueOf(value));
        } else {
            expenseToSettleItemGiveOrReceiveTextView.setText(R.string.settle_expense_receives);
            expenseToSettleItemValueTextView.setText(String.valueOf(item.getValue()));
        }

        return convertView;
    }

}
