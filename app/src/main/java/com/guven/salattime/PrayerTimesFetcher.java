package com.guven.salattime;

import android.location.Address;
import android.location.Geocoder;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Handler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PrayerTimesFetcher {
    public interface PrayerTimesCallback{
        void onSuccess(String fajr, String dhuhr, String asr, String maghrib, String isha);
        void onFailure(String errorMessage);
    }

    private final OkHttpClient client = new OkHttpClient();



    public void fetchPrayerTimes (double latitude, double longitude,String country,String language,PrayerTimesCallback callback){
        // Türkiye ise method=2, diğerleri için method=4
        int method = "Turkey".equalsIgnoreCase(country) ? 2 : 4;

        String url = "https://api.aladhan.com/v1/timings?latitude=" + latitude + "&longitude=" + longitude + "&method=" + method + "&language=" + language;

        Request request = new Request.Builder()
                .url(url)
                .build();

        //Async HTTP isteği gönderiyoruz
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Ana thread dışında olduğumuz için Handler ile ana thread'e dönüyoruz
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    callback.onFailure(e.getMessage());
                });

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(!response.isSuccessful()){
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                       callback.onFailure("HTTP Error: " + response.code());
                    });
                    return;
                }

                //Json
                String responseData = response.body().string();

                try {
                    JSONObject json = new JSONObject(responseData);
                    JSONObject data = json.getJSONObject("data");
                    JSONObject timings = data.getJSONObject("timings");

                    // Namaz vakitlerini çekiyoruz
                    String fajr = timings.getString("Fajr");
                    String dhuhr = timings.getString("Dhuhr");
                    String asr = timings.getString("Asr");
                    String maghrib = timings.getString("Maghrib");
                    String isha = timings.getString("Isha");

                    // Ana thread'e dönüp callback çağırıyoruz
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onSuccess(fajr, dhuhr, asr, maghrib, isha);
                    });

                } catch (JSONException e) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        callback.onFailure("JSON parse error: " + e.getMessage());
                    });
                }
            }
        });
    }
}
