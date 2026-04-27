package com.example.mynote.firebase;

import com.example.mynote.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseHelper {

    private final DatabaseReference databaseReference;

    public FirebaseDatabaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseReference getRootRef() {
        return databaseReference;
    }

    public DatabaseReference getUsersRef() {
        return databaseReference.child("users");
    }

    public Task<Void> saveUser(User user) {
        if (user != null && user.getUid() != null && !user.getUid().isEmpty()) {
            return getUsersRef().child(user.getUid()).setValue(user);
        }
        return null;
    }
}