package com.annabohm.pracainzynierska;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class AppSettingsFragment extends Fragment {
    Spinner changeLanguageSpinner;
    Button changeLanguageButton;
    String[] languageOptions;
    ArrayAdapter<String> languageOptionsAdapter;
    Context context;
    AlertDialog alertDialog;

    public AppSettingsFragment() {
    }

    public static AppSettingsFragment newInstance(String param1, String param2) {
        return new AppSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setDrawerLocked();
        return inflater.inflate(R.layout.fragment_app_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        alertDialog = new SpotsDialog(context);
        changeLanguageSpinner = view.findViewById(R.id.changeLanguageSpinner);
        changeLanguageButton = view.findViewById(R.id.changeLanguageButton);
        alertDialog.show();
        languageOptions = new String[]{getString(R.string.language_polish), getString(R.string.language_english)};
        languageOptionsAdapter = new ArrayAdapter<>(context, R.layout.custom_spinner_list_item, languageOptions);
        languageOptionsAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        changeLanguageSpinner.setAdapter(languageOptionsAdapter);
        alertDialog.dismiss();
        changeLanguageButton.setOnClickListener(changeLanguageButtonOnClickListener);
    }

    public View.OnClickListener changeLanguageButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = changeLanguageSpinner.getSelectedItemPosition();
            switch(position) {
                case 0:
                    updateResourcesLegacy(context, "pl");
                    MainActivity.mThis.recreate();
                    break;
                case 1:
                    updateResourcesLegacy(context, "en");
                    MainActivity.mThis.recreate();
                    break;
            }
        }
    };

    @SuppressWarnings("deprecation")
    private static void updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}