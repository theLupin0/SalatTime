package com.guven.salattime;

import static android.content.Context.MODE_PRIVATE;
import static android.text.TextUtils.substring;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FragmentSettings extends Fragment {
    public FragmentSettings(){}

    AutoCompleteTextView country;
    Button submit;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings,container,false);

        SharedPreferences preferences = getContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        country = view.findViewById(R.id.countryeditText);
        submit = view.findViewById(R.id.button3);

        String [] countries = Locale.getISOCountries();
        List<String> countryList = new ArrayList<>();

        if (!countryList.contains("Turkey")){
            countryList.add("Turkey");
            countryList.add("England");
            countryList.add("Other");
        }

        Collections.sort(countryList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), // Fragment için
                android.R.layout.simple_dropdown_item_1line,countryList
        );

        country.setAdapter(adapter);
        country.setOnClickListener(v -> country.showDropDown());

        submit.setOnClickListener(new View.OnClickListener() {
            int count =0;
            @Override
            public void onClick(View v) {
                if(!country.getText().toString().trim().isEmpty()){
                    editor.putString("country",country.getText().toString().trim());
                    Toast.makeText(getContext(),"Bilgileriniz Güncellendi",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(),"Bilgi girilmedi",Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }
}
