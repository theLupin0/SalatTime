package com.guven.salattime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ImageButton buttonNext, buttonPrevious;

    private static final int LOCATION_PERMISSON_REQUEST_CODE = 100;
    LocationHelper locationHelper;

    TextView morninngTextView;

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

        //Animasyonları set Etme
        viewFlipper.setInAnimation(this,R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this,R.anim.slide_out_left);

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
        String morning = getString(R.string.morning);
        morninngTextView.setText(morning);

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

            //Dil
            String language = "tr";

            // Namaz vakitlerini çek
            PrayerTimesFetcher fetcher = new PrayerTimesFetcher();
            fetcher.fetchPrayerTimes(latitude, longitude,country ,language,new PrayerTimesFetcher.PrayerTimesCallback() {
                @Override
                public void onSuccess(String fajr, String dhuhr, String asr, String maghrib, String isha) {
                    // Gelen namaz vakitlerini ekrana yazdır (şimdilik Toast ile)
                    String message;
                    if(country.equals("Turkey")){
                        message = "Namaz Vakitleri:\n"
                                + "İmsak: " + fajr + "\n"
                                + "Öğle: " + dhuhr + "\n"
                                + "İkindi: " + asr + "\n"
                                + "Akşam: " + maghrib + "\n"
                                + "Yatsı: " + isha;
                    }else {
                        message = "Namaz Vakitleri:\n"
                                + "Fajr: " + fajr + "\n"
                                + "Dhuhr: " + dhuhr + "\n"
                                + "Asr: " + asr + "\n"
                                + "Maghrib: " + maghrib + "\n"
                                + "Isha: " + isha;
                    }
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(MainActivity.this, "Namaz vakitleri alınamadı: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
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