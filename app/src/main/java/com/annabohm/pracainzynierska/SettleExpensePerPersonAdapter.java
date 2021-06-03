package com.annabohm.pracainzynierska;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettleExpensePerPersonAdapter extends BaseAdapter {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList list;
    private ArrayList<Double> expenseList;
    private ArrayList<String> userNameList;
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
        this.expensePerPersonHashMap = expensePerPersonHashMap;
        list = new ArrayList();
        list.addAll(expensePerPersonHashMap.entrySet());
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.expense_to_settle_item, parent, false);
        }

        TextView expenseToSettleItemNameTextView = convertView.findViewById(R.id.expenseToSettleItemNameTextView);
        TextView expenseToSettleItemValueTextView = convertView.findViewById(R.id.expenseToSettleItemValueTextView);

        Map.Entry<String, Double> item = getItem(position);

        expenseToSettleItemNameTextView.setText(item.getKey());
        expenseToSettleItemValueTextView.setText(String.valueOf(item.getValue()));
        return convertView;
    }

}
