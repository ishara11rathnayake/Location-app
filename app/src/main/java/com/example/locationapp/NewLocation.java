package com.example.locationapp;

import android.location.Location;

import java.util.Date;

public class NewLocation {
    private double lat;
    private double lng;
    private double alt;
    private Date timestap;
    private Location location;

    public NewLocation(double lat, double lng, double alt, Date timestap, Location location) {
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.timestap = timestap;
        this.location = location;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public Date getTimestap() {
        return timestap;
    }

    public void setTimestap(Date timestap) {
        this.timestap = timestap;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
