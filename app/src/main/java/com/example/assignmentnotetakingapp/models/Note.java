package com.example.assignmentnotetakingapp.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Note {
    private int id;
    private String title;
    private String content;
    private String date;
    private boolean isFavorite;
    private boolean isUrgent;
    private String pinCode;
    private int userId;

    // Constructor for creating a new note
    public Note(String title, String content, int userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        this.isFavorite = false;
        this.isUrgent = false;
        this.pinCode = "";
    }

    // Constructor for loading from the database
    public Note(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

}
