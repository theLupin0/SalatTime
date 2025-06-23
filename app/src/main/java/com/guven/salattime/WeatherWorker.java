package com.guven.salattime;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherWorker extends Worker {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String API_KEY = "508c53815c3803d1e7e7959d07d23a83"; // Buraya kendi API anahtarını yaz
    private Context context;

    public WeatherWorker(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("WeatherWorker", "Hava durumu güncelleniyor...");

        // SharedPreferences'tan konumu al
        SharedPreferences prefs = context.getSharedPreferences("salat_prefs", Context.MODE_PRIVATE);
        float lat = prefs.getFloat("latitude", 0f);
        float lon = prefs.getFloat("longitude", 0f);

        if (lat == 0f || lon == 0f) {
            Log.e("WeatherWorker", "Konum bulunamadı.");
            return Result.failure();
        }

        // Geocoder ile şehir ismini al
        String cityName = getCityNameFromLocation(lat, lon);
        if (cityName == null) {
            Log.e("WeatherWorker", "Şehir adı alınamadı.");
            return Result.failure();
        }

        // Retrofit oluştur
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService apiService = retrofit.create(WeatherApiService.class);

        // Hava durumunu çek
        fetchWeather(apiService, cityName);

        return Result.success();
    }

    private String getCityNameFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                if (city == null) city = address.getAdminArea();
                String countryCode = address.getCountryCode(); // Örn: "TR"

                if (city != null && countryCode != null) {
                    return city + "," + countryCode; // Örn: "Istanbul,TR"
                }
            }
        } catch (IOException e) {
            Log.e("WeatherWorker", "Geocoder hatası: " + e.getMessage());
        }
        return null;
    }


    private void fetchWeather(WeatherApiService apiService, String city) {
        Call<WeatherResponse> call = apiService.getCurrentWeather(city, API_KEY, "metric");
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double temp = response.body().getMain().getTemp();
                    Log.d("WeatherWorker", city + " sıcaklığı: " + temp + "°C");

                    context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("current_city", city)
                            .putString("current_temp", String.format(Locale.getDefault(), "%.2f°C", temp))
                            .apply();
                } else {
                    Log.e("WeatherWorker", "Yanıt başarısız veya veri boş.");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("WeatherWorker", city + " için hava durumu alınamadı: " + t.getMessage());
            }
        });
    }
}
