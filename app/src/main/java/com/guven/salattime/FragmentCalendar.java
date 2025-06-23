package com.guven.salattime;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class FragmentCalendar extends Fragment {
    private RecyclerView recyclerView;
    private List<SpecialDays> daysList = new ArrayList<>();
    private TextView kurban,ramazan, kandiller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar,container,false);

        ramazan = root.findViewById(R.id.textViewOzelGunler);
        loadSpecialDays();
        return root;
    }

    private void loadSpecialDays() {
        try {
            InputStream i = getContext().getAssets().open("bayramlar_kandiller.json");
            int size = i.available();
            byte[] buffer = new byte[size];
            i.read(buffer);
            i.close();

            String jsonStr = new String(buffer,"UTF-8");
            JSONObject json = new JSONObject(jsonStr);

            //Current Year
            String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

            if(!json.has(year)) return;

            JSONObject yearObject = json.getJSONObject(year);
            StringBuilder sb = new StringBuilder();


            if (yearObject.has("ramazan_baslangici")){
                sb.append("\n\n🕌 Ramazan Başlangıcı: ").append(yearObject.getString("ramazan_baslangici")).append("\n");
            }

            if (yearObject.has("bayramlar")){
                sb.append("\n\n🎉 Bayramlar:\n");
                JSONObject bayramlar = yearObject.getJSONObject("bayramlar");
                Iterator<String> keys = bayramlar.keys();
                while (keys.hasNext()) {
                    String ad = keys.next();
                    sb.append("• ").append(ad).append(": ").append(bayramlar.getString(ad)).append("\n");
                }
            }

            if (yearObject.has("kandiller")) {
                sb.append("\n\n✨ Kandiller:\n");
                JSONObject kandiller = yearObject.getJSONObject("kandiller");
                Iterator<String> keys = kandiller.keys();
                while (keys.hasNext()) {
                    String ad = keys.next();
                    sb.append("• ").append(ad).append(": ").append(kandiller.getString(ad)).append("\n");
                }
            }

            ramazan.setText(sb.toString());

            /*if(yearObject.has("ramazan_baslangici"))
                daysList.add(new SpecialDays("Ramazan Başlangıcı", yearObject.getString("ramazan_baslangici")));

            JSONObject bayramlar = yearObject.getJSONObject("bayramlar");
            Iterator<String> bayramKey = bayramlar.keys();
            while (bayramKey.hasNext()) {
                String ad = bayramKey.next();
                daysList.add(new SpecialDays(ad, bayramlar.getString(ad)));
            }

            JSONObject kandiller = yearObject.getJSONObject("kandiller");
            Iterator<String> kandilKey = kandiller.keys();
            while (kandilKey.hasNext()) {
                String ad = kandilKey.next();
                daysList.add(new SpecialDays(ad, kandiller.getString(ad)));
            }

            recyclerView.setAdapter(new SpecialDaysAdapter(daysList));*/

        }catch (Exception e){
            e.printStackTrace();
            ramazan.setText("Veri yüklenemedi: " + e.getMessage());
        }

    }
}
