package com.example.mynote.models;

public class Folder {
    private String folderId;
    private String folderName;
    private String color;
    private long createdAt;

    public Folder() {
    }

    public Folder(String folderId, String folderName, String color, long createdAt) {
        this.folderId = folderId;
        this.folderName = folderName;
        this.color = color;
        this.createdAt = createdAt;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}