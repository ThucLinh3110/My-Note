package com.example.mynote.models;

public class NoteImage {
    private String imageId;
    private String noteId;
    private String imageUrl;
    private long createdAt;

    public NoteImage() {
    }

    public NoteImage(String imageId, String noteId, String imageUrl, long createdAt) {
        this.imageId = imageId;
        this.noteId = noteId;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}