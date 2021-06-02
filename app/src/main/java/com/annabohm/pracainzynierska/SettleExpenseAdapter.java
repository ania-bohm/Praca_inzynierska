package com.annabohm.pracainzynierska;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SettleExpenseAdapter extends ArrayAdapter<Double> implements View.OnClickListener {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Double> expenseList;
    private ArrayList<String> userNameList;
    private Context context;

    public SettleExpenseAdapter(@NonNull Context context, @NonNull List<Double> expenseList, List<String> userNameList) {
        super(context, R.layout.expense_to_settle_item, expenseList);
        this.expenseList = (ArrayList<Double>) expenseList;
        this.context = context;
        this.userNameList = (ArrayList) userNameList;
    }

    @Override
    public void onClick(View v) {

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.expense_to_settle_item, parent, false);
        }

        final TextView expenseToSettleItemNameTextView = convertView.findViewById(R.id.expenseToSettleItemNameTextView);
        final TextView expenseToSettleItemValueTextView = convertView.findViewById(R.id.expenseToSettleItemValueTextView);

        expenseToSettleItemNameTextView.setText(userNameList.get(position));
        expenseToSettleItemValueTextView.setText(String.valueOf(expenseList.get(position)));
        return convertView;
    }

    public ArrayList<Double> getExpenseList() {
        return expenseList;
    }

    public void setExpenseList(ArrayList<Double> expenseList) {
        this.expenseList = expenseList;
    }

    public ArrayList<String> getUserNameList() {
        return userNameList;
    }

    public void setUserNameList(ArrayList<String> userNameList) {
        this.userNameList = userNameList;
    }
}
