package com.example.mynote.firebase;

import androidx.annotation.NonNull;

import com.example.mynote.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthHelper {

    private final FirebaseAuth mAuth;
    private final FirebaseDatabaseHelper databaseHelper;

    public FirebaseAuthHelper() {
        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new FirebaseDatabaseHelper();
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void signOut() {
        mAuth.signOut();
    }

    public Task<AuthResult> login(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> register(String fullName, String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                    if (firebaseUser != null) {
                        User user = new User(
                                firebaseUser.getUid(),
                                fullName,
                                email,
                                System.currentTimeMillis()
                        );
                        databaseHelper.saveUser(user);
                    }
                });
    }

    public Task<Void> sendResetPasswordEmail(@NonNull String email) {
        return mAuth.sendPasswordResetEmail(email);
    }
}