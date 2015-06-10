package com.brachialste.earthquakemonitor.db;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by brachialste on 9/06/15.
 */
public class DataBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7622242574847877565L;

    private int id;
    private double magnitude;
    private String place;
    private LatLng latLng;
    private long time;
    private double depth;

    private String date_entered;
    private int deleted;

    public DataBean(int id, double magnitude, String place, LatLng latLng, long time, double depth) {
        this.id = id;
        this.magnitude = magnitude;
        this.place = place;
        this.latLng = latLng;
        this.time = time;
        this.depth = depth;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public String getDate_entered() {
        return date_entered;
    }

    public void setDate_entered(String date_entered) {
        this.date_entered = date_entered;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }
}
