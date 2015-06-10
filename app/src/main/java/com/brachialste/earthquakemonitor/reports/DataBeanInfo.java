package com.brachialste.earthquakemonitor.reports;

/**
 * Created by brachialste on 23/02/15.
 */
public class DataBeanInfo {

    public String id;
    public String magnitude;
    public String place;
    public String location;
    public String date;
    public String depth;
    public int color;

    public DataBeanInfo(String id, String magnitude, String place, String location, String date,
                        String depth, int color) {
        this.id = id;
        this.magnitude = magnitude;
        this.place = place;
        this.location = location;
        this.date = date;
        this.depth = depth;
        this.color = color;
    }
}
