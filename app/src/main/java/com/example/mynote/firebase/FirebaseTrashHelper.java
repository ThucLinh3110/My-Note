package com.example.mynote.firebase;

import androidx.annotation.NonNull;

import com.example.mynote.models.Folder;
import com.example.mynote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseTrashHelper {

    public interface TrashCallback {
        void onSuccess();
        void onError(String message);
    }

    private static String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private static DatabaseReference getFoldersRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("folders").child(uid);
    }

    private static DatabaseReference getNotesRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("notes").child(uid);
    }

    private static DatabaseReference getTrashRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("trash").child(uid);
    }

    public static void moveNoteToTrash(Note note, @NonNull TrashCallback callback) {
        String uid = getUid();
        if (uid == null) {
            callback.onError("User chưa đăng nhập");
            return;
        }

        if (note == null || note.getNoteId() == null || note.getNoteId().trim().isEmpty()) {
            callback.onError("Ghi chú không hợp lệ");
            return;
        }

        long deletedAt = System.currentTimeMillis();
        note.setDeleted(true);
        note.setDeletedAt(deletedAt);

        Map<String, Object> updates = new HashMap<>();
        updates.put("/trash/" + uid + "/notes/" + note.getNoteId(), note);
        updates.put("/notes/" + uid + "/" + note.getNoteId(), null);

        FirebaseDatabase.getInstance().getReference()
                .updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static void moveFolderToTrash(Folder folder, @NonNull TrashCallback callback) {
        String uid = getUid();
        if (uid == null) {
            callback.onError("User chưa đăng nhập");
            return;
        }

        if (folder == null || folder.getFolderId() == null || folder.getFolderId().trim().isEmpty()) {
            callback.onError("Thư mục không hợp lệ");
            return;
        }

        String folderId = folder.getFolderId();
        long deletedAt = System.currentTimeMillis();

        folder.setDeleted(true);
        folder.setDeletedAt(deletedAt);

        getNotesRef(uid).orderByChild("folderId").equalTo(folderId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Object> updates = new HashMap<>();

                    updates.put("/trash/" + uid + "/folders/" + folderId, folder);
                    updates.put("/folders/" + uid + "/" + folderId, null);

                    for (DataSnapshot noteSnap : snapshot.getChildren()) {
                        Note note = noteSnap.getValue(Note.class);
                        if (note == null) continue;

                        note.setDeleted(true);
                        note.setDeletedAt(deletedAt);

                        if (note.getFolderName() == null || note.getFolderName().trim().isEmpty()) {
                            note.setFolderName(folder.getFolderName());
                        }

                        updates.put("/trash/" + uid + "/notes/" + note.getNoteId(), note);
                        updates.put("/notes/" + uid + "/" + note.getNoteId(), null);
                    }

                    FirebaseDatabase.getInstance().getReference()
                            .updateChildren(updates)
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static void restoreNote(Note note, @NonNull TrashCallback callback) {
        String uid = getUid();
        if (uid == null) {
            callback.onError("User chưa đăng nhập");
            return;
        }

        if (note == null || note.getNoteId() == null || note.getNoteId().trim().isEmpty()) {
            callback.onError("Ghi chú không hợp lệ");
            return;
        }

        note.setDeleted(false);
        note.setDeletedAt(0);

        Map<String, Object> updates = new HashMap<>();
        updates.put("/notes/" + uid + "/" + note.getNoteId(), note);
        updates.put("/trash/" + uid + "/notes/" + note.getNoteId(), null);

        FirebaseDatabase.getInstance().getReference()
                .updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static void restoreFolder(Folder folder, @NonNull TrashCallback callback) {
        String uid = getUid();
        if (uid == null) {
            callback.onError("User chưa đăng nhập");
            return;
        }

        if (folder == null || folder.getFolderId() == null || folder.getFolderId().trim().isEmpty()) {
            callback.onError("Thư mục không hợp lệ");
            return;
        }

        String folderId = folder.getFolderId();
        folder.setDeleted(false);
        folder.setDeletedAt(0);

        getTrashRef(uid).child("notes").orderByChild("folderId").equalTo(folderId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Object> updates = new HashMap<>();

                    updates.put("/folders/" + uid + "/" + folderId, folder);
                    updates.put("/trash/" + uid + "/folders/" + folderId, null);

                    for (DataSnapshot noteSnap : snapshot.getChildren()) {
                        Note note = noteSnap.getValue(Note.class);
                        if (note == null) continue;

                        note.setDeleted(false);
                        note.setDeletedAt(0);

                        updates.put("/notes/" + uid + "/" + note.getNoteId(), note);
                        updates.put("/trash/" + uid + "/notes/" + note.getNoteId(), null);
                    }

                    FirebaseDatabase.getInstance().getReference()
                            .updateChildren(updates)
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static void deleteNoteForever(String noteId, @NonNull TrashCallback callback) {
        String uid = getUid();
        if (uid == null) {
            callback.onError("User chưa đăng nhập");
            return;
        }

        if (noteId == null || noteId.trim().isEmpty()) {
            callback.onError("noteId không hợp lệ");
            return;
        }

        getTrashRef(uid).child("notes").child(noteId)
                .removeValue()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public static void deleteFolderForever(String folderId, @NonNull TrashCallback callback) {
        String uid = getUid();
        if (uid == null) {
            callback.onError("User chưa đăng nhập");
            return;
        }

        if (folderId == null || folderId.trim().isEmpty()) {
            callback.onError("folderId không hợp lệ");
            return;
        }

        getTrashRef(uid).child("notes").orderByChild("folderId").equalTo(folderId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("/trash/" + uid + "/folders/" + folderId, null);

                    for (DataSnapshot noteSnap : snapshot.getChildren()) {
                        Note note = noteSnap.getValue(Note.class);
                        if (note == null) continue;
                        updates.put("/trash/" + uid + "/notes/" + note.getNoteId(), null);
                    }

                    FirebaseDatabase.getInstance().getReference()
                            .updateChildren(updates)
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}