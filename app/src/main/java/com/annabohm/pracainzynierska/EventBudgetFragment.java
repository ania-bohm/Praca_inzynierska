package com.annabohm.pracainzynierska;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EventBudgetFragment extends Fragment {


    public EventBudgetFragment() {
        // Required empty public constructor
    }

    public static EventBudgetFragment newInstance(String param1, String param2) {
        EventBudgetFragment fragment = new EventBudgetFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setDrawerLocked();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_budget, container, false);
    }
}