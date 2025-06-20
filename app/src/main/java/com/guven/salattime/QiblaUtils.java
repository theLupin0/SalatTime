package com.guven.salattime;

public class QiblaUtils {
    public static float calculateQiblaDirection(double userLat, double userLon) {
        double kaabaLat = 21.42252;
        double kaabaLon = 39.82621;

        double userLatRad = Math.toRadians(userLat);
        double userLonRad = Math.toRadians(userLon);
        double kaabaLatRad = Math.toRadians(kaabaLat);
        double kaabaLonRad = Math.toRadians(kaabaLon);

        double deltaLon = kaabaLonRad - userLonRad;
        double x = Math.sin(deltaLon);
        double y = Math.cos(userLatRad) * Math.tan(kaabaLatRad) - Math.sin(userLatRad) * Math.cos(deltaLon);

        double qiblaDirection = Math.toDegrees(Math.atan2(x, y));
        return (float) ((qiblaDirection + 360) % 360);
    }
}

