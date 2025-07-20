package com.example.studymate.models;

public class NoteModel {
    private String id;
    private String title;
    private String date;
    private String imagePath; // Base64 encoded string of the image
    private double latitude;
    private double longitude;

    // Empty constructor is required for Firestore
    public NoteModel() {
    }

    public NoteModel(String id, String title, String date, String imagePath, double latitude, double longitude) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.imagePath = imagePath;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
