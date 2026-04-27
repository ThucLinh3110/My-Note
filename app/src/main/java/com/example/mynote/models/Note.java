package com.example.mynote.models;

import java.util.ArrayList;

public class Note {
    private String noteId;
    private String folderId;
    private String folderName; // thêm
    private String title;
    private String content;
    private String date;
    private long createdAt;
    private long updatedAt;
    private long deletedAt; // thêm

    private boolean deleted;
    private boolean hasImage;
    private String imageUrl;
    private ArrayList<String> imageUrls;

    public ArrayList<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Note() { }

    public Note(String noteId, String folderId, String title, String content, String date,
                long createdAt, long updatedAt, boolean hasImage, boolean deleted) {
        this.noteId = noteId;
        this.folderId = folderId;
        this.title = title;
        this.content = content;
        this.date = date;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hasImage = hasImage;
        this.deleted = deleted;
        this.imageUrl = "";
        this.folderName = "";
        this.deletedAt = 0;
    }

    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }

    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(long deletedAt) { this.deletedAt = deletedAt; }

    public boolean isHasImage() { return hasImage; }
    public void setHasImage(boolean hasImage) { this.hasImage = hasImage; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}