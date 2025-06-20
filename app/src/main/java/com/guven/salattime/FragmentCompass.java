package com.guven.salattime;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class FragmentCompass extends Fragment implements SensorEventListener {

    private ImageView compassImageView;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] gravity;
    private float[] geomagnetic;

    private double userLatitude = 0;
    private double userLongitude = 0;

    // Uyarı kontrol flagleri
    private boolean isHorizontal = false;     // Başlangıçta cihaz yatay mı?
    private boolean tiltWarningShown = false; // Dikey uyarısı gösterildi mi?

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compass, container, false);
        compassImageView = view.findViewById(R.id.compassImageView);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // SharedPreferences'dan konumu al
        SharedPreferences prefs = requireActivity().getSharedPreferences("salat_prefs", Context.MODE_PRIVATE);
        userLatitude = prefs.getFloat("latitude", 0f);
        userLongitude = prefs.getFloat("longitude", 0f);

        // Eğer konum 0 ise kullanıcıdan al
        if (userLatitude == 0f && userLongitude == 0f) {
            getUserLocation();
        }

        return view;
    }

    private void getUserLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Konum izni gerekli", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLatitude = location.getLatitude();
                userLongitude = location.getLongitude();

                // SharedPreferences'a kaydet
                SharedPreferences prefs = requireActivity().getSharedPreferences("salat_prefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putFloat("latitude", (float) userLatitude)
                        .putFloat("longitude", (float) userLongitude)
                        .apply();
            } else {
                Toast.makeText(getContext(), "Konum alınamadı", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Basit low-pass filtre ile ivme verilerini yumuşatma (isteğe bağlı ama titreşimi azaltır)
    // Low-pass filtresi
    private float[] lowPass(float[] input, float[] output) {
        if (output == null) return input.clone();
        final float ALPHA = 0.1f; // ALPHA küçüldükçe daha yumuşak
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    // Pusula döndürmesini yumuşatma için:
// Şimdiki açı ile hedef açı arasında en kısa açı farkını hesapla ve yavaşça döndür
    private float smoothRotation(float currentRotation, float targetRotation) {
        float diff = targetRotation - currentRotation;

        // Açıyı -180 ile 180 aralığına getir (en kısa yönü hesapla)
        while (diff < -180) diff += 360;
        while (diff > 180) diff -= 360;

        // Dönüş hızını belirle (örnek: %15 yumuşatma)
        return currentRotation + diff * 0.15f;
    }


    // Global değişken pusula dönüşü için
    private float currentCompassRotation = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = lowPass(event.values.clone(), gravity);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = lowPass(event.values.clone(), geomagnetic);
        }

        if (gravity != null && geomagnetic != null) {
            // Cihaz eğim açısını hesapla
            float gx = gravity[0];
            float gy = gravity[1];
            float gz = gravity[2];

            double gravityMagnitude = Math.sqrt(gx * gx + gy * gy + gz * gz);
            double tiltRadians = Math.acos(gz / gravityMagnitude);
            double tiltDegrees = Math.toDegrees(tiltRadians);

            // Başlangıçta cihaz yatay değilse uyar
            if (!isHorizontal && tiltDegrees > 70) {
                showToast("Lütfen cihazı yatay tutun");
                isHorizontal = true;
                tiltWarningShown = false;
            }

            // Cihaz dikey konuma yaklaştığında uyar
            if (isHorizontal && !tiltWarningShown && tiltDegrees < 30) {
                showToast("Cihaz çok dikey, lütfen yatay tutun");
                tiltWarningShown = true;
            }

            // Kompas yönünü hesapla ve görüntüyü döndür
            float[] R = new float[9];
            float[] orientation = new float[3];
            boolean success = SensorManager.getRotationMatrix(R, null, gravity, geomagnetic);
            if (success) {
                SensorManager.getOrientation(R, orientation);
                float azimuth = (float) Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360) % 360;

                float qiblaAngle = QiblaUtils.calculateQiblaDirection(userLatitude, userLongitude);
                float targetRotation = (qiblaAngle - azimuth + 360) % 360;

                // Yumuşak dönüş
                currentCompassRotation = smoothRotation(currentCompassRotation, targetRotation);
                compassImageView.setRotation(currentCompassRotation);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
