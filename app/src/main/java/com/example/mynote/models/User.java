package com.example.mynote.models;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private long createdAt;

    public User() {
        // Bắt buộc cho Firebase
    }

    public User(String uid, String fullName, String email, long createdAt) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
