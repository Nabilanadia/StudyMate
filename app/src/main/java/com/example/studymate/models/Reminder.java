package com.example.studymate.models;

public class Reminder {
    private String title;
    private String dateTime;
    private String tag;

    public Reminder(String title, String dateTime, String tag) {
        this.title = title;
        this.dateTime = dateTime;
        this.tag = tag;
    }

    public String getTitle() {
        return title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getTag() {
        return tag;
    }
}
