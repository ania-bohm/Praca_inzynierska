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

public class SettleExpensePerPersonAdapter extends BaseAdapter {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList list;
    private Context context;
    private HashMap<String, Double> expensePerPersonHashMap;

    public SettleExpensePerPersonAdapter(Context context, HashMap<String, Double> expensePerPersonHashMap) {
        this.context = context;
        this.expensePerPersonHashMap = expensePerPersonHashMap;
        list = new ArrayList();
        list.addAll(expensePerPersonHashMap.entrySet());
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

    public void setExpensePerPersonHashMap(HashMap<String, Double> newExpensePerPersonHashMap){
        this.expensePerPersonHashMap = newExpensePerPersonHashMap;
        list = new ArrayList();
        list.addAll(expensePerPersonHashMap.entrySet());
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expense_to_settle_per_person_item, parent, false);
        }

        TextView expenseToSettlePerPersonItemNameTextView = convertView.findViewById(R.id.expenseToSettlePerPersonItemNameTextView);
        TextView expenseToSettleItemValueTextView = convertView.findViewById(R.id.expenseToSettlePerPersonItemValueTextView);

        Map.Entry<String, Double> item = getItem(position);

        expenseToSettlePerPersonItemNameTextView.setText(item.getKey());
        expenseToSettleItemValueTextView.setText(String.valueOf(item.getValue()));
        return convertView;
    }

}
