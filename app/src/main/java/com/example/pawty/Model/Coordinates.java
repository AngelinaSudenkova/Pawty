package com.example.pawty.Model;

import androidx.annotation.NonNull;

public class Coordinates {

    private double latitude;
    private double longitude;

    public Coordinates() {

    }

    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }


    public double getLongitude() {
        return longitude;
    }


    @NonNull
    @Override
    public String toString() {
        return "lat: " + getLatitude() + "long: " + getLongitude();
    }
}
