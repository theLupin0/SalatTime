package com.guven.salattime;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PrayerTimesFetcher {

    public interface PrayerTimesCallback {
        void onSuccess(String fajr, String dhuhr, String asr, String maghrib, String isha);
        void onFailure(String errorMessage);
    }

    private final OkHttpClient client = new OkHttpClient();

    public void fetchPrayerTimes(Context context, double latitude, double longitude, String language, PrayerTimesCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String country = prefs.getString("country", ""); // default boş

        if ("Turkey".equalsIgnoreCase(country)) {
            fetchFromDiyanetAPI(latitude, longitude, callback);
        } else {
            fetchFromAlAdhanAPI(latitude, longitude, language, callback);
        }
    }

    // AlAdhan API'den veri çekme (global kullanım)
    private void fetchFromAlAdhanAPI(double latitude, double longitude, String language, PrayerTimesCallback callback) {
        int method = 13; // Diyanet

        String url = "https://api.aladhan.com/v1/timings?latitude=" + latitude
                + "&longitude=" + longitude
                + "&method=" + method
                + "&language=" + language;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new android.os.Handler(Looper.getMainLooper()).post(() ->
                        callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new android.os.Handler(Looper.getMainLooper()).post(() ->
                            callback.onFailure("HTTP Error: " + response.code()));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONObject timings = json.getJSONObject("data").getJSONObject("timings");

                    String fajr = timings.getString("Fajr");
                    String dhuhr = timings.getString("Dhuhr");
                    String asr = timings.getString("Asr");
                    String maghrib = timings.getString("Maghrib");
                    String isha = timings.getString("Isha");

                    new android.os.Handler(Looper.getMainLooper()).post(() ->
                            callback.onSuccess(fajr, dhuhr, asr, maghrib, isha));

                } catch (JSONException e) {
                    new android.os.Handler(Looper.getMainLooper()).post(() ->
                            callback.onFailure("JSON parse error: " + e.getMessage()));
                }
            }
        });
    }

    // Diyanet API'den veri çekme (sadece Türkiye)
    private void fetchFromDiyanetAPI(double latitude, double longitude, PrayerTimesCallback callback) {/*
        // Örnek: Diyanet API ilçe bazlı çalışır, koordinattan il/ilçe tespiti gerekir.
        // Şu anlık örnek olması açısından Ankara verisi sabit girilmiştir:
        String url = "https://ezanvakti.herokuapp.com/vakitler?ilce=ankara";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new android.os.Handler(Looper.getMainLooper()).post(() ->
                        callback.onFailure(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new android.os.Handler(Looper.getMainLooper()).post(() ->
                            callback.onFailure("HTTP Error: " + response.code()));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.body().string());
                    JSONObject today = json.getJSONArray("vakitler").getJSONObject(0);

                    String fajr = today.getString("imsak");
                    String dhuhr = today.getString("ogle");
                    String asr = today.getString("ikindi");
                    String maghrib = today.getString("aksam");
                    String isha = today.getString("yatsi");

                    new android.os.Handler(Looper.getMainLooper()).post(() ->
                            callback.onSuccess(fajr, dhuhr, asr, maghrib, isha));

                } catch (JSONException e) {
                    new android.os.Handler(Looper.getMainLooper()).post(() ->
                            callback.onFailure("JSON parse error: " + e.getMessage()));
                }
            }
        });
    */}
}
