package com.guven.salattime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationHelper {
    private final Context context;

    // Konum servislerine erişmek için kullanılan Google Play hizmetleri sınıfı
    private final FusedLocationProviderClient locationClient;

    public interface LocationCallBack{
        // Latitude (enlem) ve Longitude (boylam) koordinatlarını geri verir
        void onLocationReceived(double latitude, double longitude);
    }

    public LocationHelper(Context context){
        this.context=context;
        locationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation(LocationCallBack callBack){
        locationClient.getLastLocation()
                .addOnSuccessListener((OnSuccessListener<Location>) location->{
                    if(location != null){
                        callBack.onLocationReceived(location.getLatitude(),location.getLongitude());
                    }
                });
    }
}
