package com.examplek49.mynote.models;

public class Note {
    private String noteId;
    private String folderId;
    private String title;
    private String content;
    private String date;
    private long createdAt;
    private long updatedAt;
    private boolean hasImage;
    private boolean deleted;

    public Note() {
    }

    public Note(String noteId, String folderId, String title, String content,
                String date, long createdAt, long updatedAt,
                boolean hasImage, boolean deleted) {
        this.noteId = noteId;
        this.folderId = folderId;
        this.title = title;
        this.content = content;
        this.date = date;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hasImage = hasImage;
        this.deleted = deleted;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}