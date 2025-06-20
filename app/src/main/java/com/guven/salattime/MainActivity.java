package com.guven.salattime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ImageButton buttonNext, buttonPrevious;

    private static final int LOCATION_PERMISSON_REQUEST_CODE = 100;
    LocationHelper locationHelper;

    TextView morninngTextView,morninngSaatView,noonSaatView,noonTextView,eveningSaatView,afternoonSaatView,aftersubberSaatView,afternoonTextView,eveningTextView,aftersubberTextView,sunrise,sunset;
    LinearLayout settingsBtn, homeBtn, calendarBtn, compassBtn;


    FrameLayout frameLayout;

    private Handler handler = new Handler();
    private int delay = 10000;
    private Runnable autoFlipRunnable;


    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences preferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String countryName = preferences.getString("country","");
        //Dil
        if(countryName.equalsIgnoreCase("Turkey")){
            setLocale("tr");
        }else{
            setLocale("en");
        }

        viewFlipper = findViewById(R.id.viewFlipper);
        buttonNext = findViewById(R.id.imageNext);
        buttonPrevious = findViewById(R.id.imagePervious);
        settingsBtn = findViewById(R.id.settingsBtn);
        homeBtn = findViewById(R.id.homeBtn);
        calendarBtn = findViewById(R.id.calendarBtn);
        compassBtn = findViewById(R.id.compassBtn);

        sunrise = findViewById(R.id.textView13);
        sunset = findViewById(R.id.textView16);

        frameLayout = findViewById(R.id.frame);

        settingsBtn.setOnClickListener(v -> loadFragment(new FragmentSettings(),true));
        homeBtn.setOnClickListener(v -> loadFragment(new FragmentHome(),true));
        compassBtn.setOnClickListener(v -> loadFragment(new FragmentCompass(),true));


        //Animasyonları set Etme
        viewFlipper.setInAnimation(this,R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this,R.anim.slide_out_left);

        loadDuasFromAss();

        buttonNext.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this,R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this,R.anim.slide_out_left);
            viewFlipper.showNext();
            resetAutoFlip();
        });

        buttonPrevious.setOnClickListener(v -> {
            viewFlipper.setInAnimation(this,R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this,R.anim.slide_out_right);
            viewFlipper.showPrevious();
            resetAutoFlip();
        });

        autoFlipRunnable = new Runnable() {
            @Override
            public void run() {
                viewFlipper.setInAnimation(MainActivity.this,R.anim.slide_in_right);
                viewFlipper.setOutAnimation(MainActivity.this,R.anim.slide_out_left);
                viewFlipper.showNext();
                handler.postDelayed(this,delay);
            }
        };
        handler.postDelayed(autoFlipRunnable,delay);

        morninngTextView = findViewById(R.id.morning);
        noonTextView = findViewById(R.id.noon);
        afternoonTextView = findViewById(R.id.afternoon);
        eveningTextView = findViewById(R.id.evening);
        aftersubberTextView = findViewById(R.id.aftersubber);

        morninngSaatView = findViewById(R.id.morningSaat);
        noonSaatView = findViewById(R.id.noonSaat);
        afternoonSaatView = findViewById(R.id.afternoonSaat);
        eveningSaatView = findViewById(R.id.eveningSaat);
        aftersubberSaatView = findViewById(R.id.aftersubberSaat);



        String morning = "Sabah";
        String noon = "Öğle";
        String afternoon = "İkindi";
        String evening = "Akşam";
        String aftersubber = "Yatsı";

        morninngTextView.setText(morning);
        noonTextView.setText(noon);
        afternoonTextView.setText(afternoon);
        eveningTextView.setText(evening);
        aftersubberTextView.setText(aftersubber);

        //İzin kontrolü
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSON_REQUEST_CODE);
        }else{
            startLocation();
        }


    }

    private void loadDuasFromAss() {
        try {
            InputStream i = getAssets().open("ayetler.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(i));
            ArrayList<String> lines = new ArrayList<>();

            // Tüm satırları oku ve listeye al
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            i.close();

            if (lines.size() == 0) return;

            Random random = new Random();
            int startIndex = random.nextInt(lines.size());

            // ViewFlipper içeriğini temizle
            viewFlipper.removeAllViews();

            // Döngüyü startIndex'ten başlat, sonuna gelince başa dön (dairesel)
            for (int k = 0; k < lines.size(); k++) {
                int idx = (startIndex + k) % lines.size();
                String currentLine = lines.get(idx);

                String[] parts = currentLine.split("\\|");
                if (parts.length == 3) {
                    String id = parts[0].trim();
                    String verse = parts[1].trim();
                    String no = parts[2].trim();

                    LinearLayout layout = new LinearLayout(this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(8, 8, 8, 8);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    layout.setLayoutParams(layoutParams);

                    TextView idText = new TextView(this);
                    idText.setText(id);
                    idText.setTextColor(Color.parseColor("#EAFBFAFA"));
                    idText.setTextSize(16);
                    idText.setGravity(Gravity.CENTER);
                    layout.addView(idText);

                    TextView verseText = new TextView(this);
                    verseText.setText(verse);
                    verseText.setTextColor(Color.parseColor("#FDFBFB"));
                    verseText.setTextSize(12);
                    verseText.setGravity(Gravity.CENTER);
                    verseText.setPadding(0, 8, 0, 0);
                    verseText.setSingleLine(false);
                    layout.addView(verseText);

                    TextView noText = new TextView(this);
                    noText.setText(no);
                    noText.setTextColor(Color.parseColor("#FDFBFBAE"));
                    noText.setTextSize(9);
                    noText.setGravity(Gravity.CENTER);
                    layout.addView(noText);

                    viewFlipper.addView(layout);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Hata", Toast.LENGTH_LONG).show();
        }
    }


    private void calculateSunTimes(double latitude, double longitude) {
        com.luckycatlabs.sunrisesunset.dto.Location location = new com.luckycatlabs.sunrisesunset.dto.Location(
                String.valueOf(latitude),
                String.valueOf(longitude)
        );

        SunriseSunsetCalculator calculator =
                new SunriseSunsetCalculator(location,TimeZone.getDefault());

        String sunriseTime = calculator.getOfficialSunriseForDate(Calendar.getInstance());
        String sunsetTime = calculator.getOfficialSunsetForDate(Calendar.getInstance());

        sunrise.setText(sunriseTime);
        sunset.setText(sunsetTime);
    }

    private void loadFragment(Fragment fragment, boolean b) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(b){
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );
        }

        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoFlipRunnable);
    }




    private void resetAutoFlip() {
        handler.removeCallbacks(autoFlipRunnable);
        handler.postDelayed(autoFlipRunnable,delay);
    }

    private void setLocale(String lan) {
        Locale locale = new Locale(lan);
        Locale.setDefault(locale);
        getResources().getConfiguration().setLocale(locale); // O anki uygulama konfigürasyonunu, yeni Locale ile günceller
        getResources().updateConfiguration(getResources().getConfiguration(),getResources().getDisplayMetrics()); // Yeni yapılandırmayı ve ekran ayarlarını uygulamanın kaynaklarına bildirir.
    }

    private void startLocation() {
        locationHelper = new LocationHelper(this);
        locationHelper.getLastLocation((latitude, longitude) -> {
            Log.d("SALATTIME", "Konum alındı: Latitude = " + latitude + ", Longitude = " + longitude);

            //Ülke Adı Al

            String country = getCountryName(latitude,longitude);
            Log.d("SalatTime" , "Ulke: " + country);

            SharedPreferences prefs = getSharedPreferences("salat_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putFloat("latitude", (float) latitude)
                    .putFloat("longitude", (float) longitude)
                    .apply();

            String language;
            if (country.equals("Turkey")){
                language = "tr";
            }else {
                language = "en";
            }
            // Namaz vakitlerini çek
            PrayerTimesFetcher fetcher = new PrayerTimesFetcher();
            fetcher.fetchPrayerTimes(MainActivity.this,latitude, longitude, language, new PrayerTimesFetcher.PrayerTimesCallback() {
                @Override
                public void onSuccess(String fajr, String dhuhr, String asr, String maghrib, String isha) {
                    morninngSaatView.setText(fajr);
                    noonSaatView.setText(dhuhr);
                    afternoonSaatView.setText(asr);
                    eveningSaatView.setText(maghrib);
                    aftersubberSaatView.setText(isha);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(MainActivity.this, "Namaz vakitleri alınamadı: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
            calculateSunTimes(latitude,longitude);
        });
    }

    public String getCountryName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //İzin sonucu callback'i
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

        if(requestCode == LOCATION_PERMISSON_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //İzin verildi, konum başlat
                startLocation();
            }else{
                Toast.makeText(this, "Konum izni reddedildi!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}