package com.ismt.applicationjournal;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Journal implements Serializable {
    @Exclude
    private String key;

    private String title;
    private String thought;
    private String pimage;
    private String lat, lng;
    private String date;


    public Journal() {
    }

    public Journal(String title, String thought, String pimage, String lat, String lng, String date) {
        this.title = title;
        this.thought = thought;
        this.pimage = pimage;
        this.lat = lat;
        this.lng = lng;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getPimage() {
        return pimage;
    }

    public void setPimage(String pimage) {
        this.pimage = pimage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
