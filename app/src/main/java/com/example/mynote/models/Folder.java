package com.example.mynote.models;

public class Folder {
    private String folderId;
    private String folderName;
    private int noteCount;
    private long createdAt;
    private String color;

    private boolean deleted;
    private long deletedAt;

    public Folder() {
    }

    public Folder(String folderName) {
        this.folderName = folderName;
    }

    public Folder(String folderId, String folderName, int noteCount) {
        this.folderId = folderId;
        this.folderName = folderName;
        this.noteCount = noteCount;
    }

    // Hàm tạo 4 tham số dùng để thêm mới
    public Folder(String folderId, String folderName, int noteCount, long createdAt) {
        this.folderId = folderId;
        this.folderName = folderName;
        this.noteCount = noteCount;
        this.createdAt = createdAt;
    }

    // Thêm: Hàm tạo 5 tham số có bao gồm màu sắc dùng khi người dùng chọn màu
    public Folder(String folderId, String folderName, int noteCount, long createdAt, String color) {
        this.folderId = folderId;
        this.folderName = folderName;
        this.noteCount = noteCount;
        this.createdAt = createdAt;
        this.color = color;
    }

    // --- CÁC HÀM GET/SET ---
    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public int getNoteCount() { return noteCount; }
    public void setNoteCount(int noteCount) { this.noteCount = noteCount; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Thêm: Getter/Setter cho color
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(long deletedAt) { this.deletedAt = deletedAt; }
}