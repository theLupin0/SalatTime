package com.guven.salattime;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class InfoActivity extends AppCompatActivity {

    EditText editTextBirthday,nameEditText;
    AutoCompleteTextView countryEditText;
    Button submit;

    private TextView textView,textView2;
    private Handler handler = new Handler();
    private Handler handler1 = new Handler();
    private String fullText = "'وَلِكُلٍّ وِجْهَةٌ هُوَ مُوَلّ۪يهَا فَاسْتَبِقُوا الْخَيْرَاتِۜ اَيْنَ مَا تَكُونُوا يَأْتِ بِكُمُ اللّٰهُ جَم۪يعًاۜ اِنَّ اللّٰهَ عَلٰى كُلِّ شَيْءٍ قَد۪يرٌ'";
    private String fullText2 = "'For every nation is a direction to which it faces (in prayer). So race to [all that is] good. Wherever you may be, Allah will bring you forth [for judgment] all together. Indeed, Allah is over all things competent.'";
    private int index = 0,index1=0;
    private long typingDelay = 100; // Her harf gecikmesi

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editTextBirthday = findViewById(R.id.editTextBirthday);
        submit = findViewById(R.id.button3);
        nameEditText = findViewById(R.id.editText);
        countryEditText = findViewById(R.id.editText2);

        textView = findViewById(R.id.typewriterTextView);
        textView2 = findViewById(R.id.typewriterTextView2);
        startTypewriterEffectArabic();
        handler1.post(arabicRepeater);
        startTypewriterEffectEnglish();
        handler.post(englishRepeater);

        editTextBirthday.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                String input = s.toString().replace("/", "");
                StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < input.length() && i < 8; i++) {
                    formatted.append(input.charAt(i));
                    if ((i == 1 || i == 3) && i != input.length() - 1) {
                        formatted.append("/");
                    }
                }

                editTextBirthday.setText(formatted.toString());
                editTextBirthday.setSelection(formatted.length());

                isFormatting = false;
            }
        });

        //Country Secimi
        String [] countries = Locale.getISOCountries();
        List<String> countryList = new ArrayList<>();

        for (String countryCode : countries) {
            Locale locale = new Locale("", countryCode);
            String countryName = locale.getDisplayCountry(Locale.ENGLISH);
            if(!countryName.isEmpty()) {
                countryList.add(countryName);
            }
        }

        //Turkey ekleme
        if(!countryList.contains("Turkey")){
            countryList.add("Turkey");
        }

        Collections.sort(countryList); // Alfabetik Sıralama

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line,countryList
        );

        countryEditText.setAdapter(adapter);
        countryEditText.setOnClickListener(v -> countryEditText.showDropDown());


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEditText.getText().toString().trim().isEmpty() ||
                    countryEditText.getText().toString().trim().isEmpty() ||
                    editTextBirthday.getText().toString().trim().isEmpty()){
                    Toast.makeText(InfoActivity.this,"Please fill in the fields!" , Toast.LENGTH_LONG).show();
                }else{
                    String selectedCountry = countryEditText.getText().toString().trim();
                    editor.putString("name", nameEditText.getText().toString().trim());
                    editor.putString("country",selectedCountry);
                    editor.putString("birthday",editTextBirthday.getText().toString().trim());
                    editor.apply();
                    Intent i = new Intent(InfoActivity.this, MainActivity.class);
                    startActivity(i);
                }
            }
        });

    }

    private void startTypewriterEffectArabic() {
        index1 = 0;
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index1 < fullText.length()) {
                    textView.setText(fullText.substring(0, index1 + 1));
                    index1++;
                    handler1.postDelayed(this, typingDelay);
                }
            }
        }, 500); // başlangıç gecikmesi
    }

    private void startTypewriterEffectEnglish() {
        index = 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index < fullText2.length()) {
                    textView2.setText(fullText2.substring(0, index + 1));
                    index++;
                    handler.postDelayed(this, typingDelay);
                }
            }
        }, 500); // başlangıç gecikmesi
    }


    Runnable arabicRepeater = new Runnable() {
        @Override
        public void run() {
            startTypewriterEffectArabic();
            handler1.postDelayed(this, 20000);
        }
    };

    Runnable englishRepeater = new Runnable() {
        @Override
        public void run() {
            startTypewriterEffectEnglish();
            handler.postDelayed(this, 20000);
        }
    };

}