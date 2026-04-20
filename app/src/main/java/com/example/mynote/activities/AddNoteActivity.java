package com.example.mynote.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.examplek49.mynote.R;
import com.examplek49.mynote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddNoteActivity extends AppCompatActivity {

    private EditText edtTitle, edtContent, edtDate;
    private Button btnSave, btnDelete;

    private DatabaseReference noteRef, trashRef;
    private String uid;
    private String folderId;
    private String mode;
    private String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        edtDate = findViewById(R.id.edtDate);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("notes");
        trashRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("trash");

        mode = getIntent().getStringExtra("mode");
        folderId = getIntent().getStringExtra("folderId");

        if ("edit".equals(mode)) {
            noteId = getIntent().getStringExtra("noteId");
            edtTitle.setText(getIntent().getStringExtra("title"));
            edtContent.setText(getIntent().getStringExtra("content"));
            edtDate.setText(getIntent().getStringExtra("date"));
        } else {
            btnDelete.setEnabled(false);
        }

        btnSave.setOnClickListener(v -> saveNote());
        btnDelete.setOnClickListener(v -> deleteNoteSoft());
    }

    private void saveNote() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();
        String date = edtDate.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            edtTitle.setError("Nhập tiêu đề");
            return;
        }
        if (TextUtils.isEmpty(content)) {
            edtContent.setError("Nhập nội dung");
            return;
        }
        if (TextUtils.isEmpty(date)) {
            edtDate.setError("Nhập ngày dạng yyyy-MM-dd");
            return;
        }

        long now = System.currentTimeMillis();

        if ("edit".equals(mode)) {
            Note updatedNote = new Note(noteId, folderId, title, content, date, now, now, false, false);

            noteRef.child(noteId).setValue(updatedNote)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã cập nhật ghi chú", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show());
        } else {
            String newNoteId = noteRef.push().getKey();
            Note note = new Note(newNoteId, folderId, title, content, date, now, now, false, false);

            noteRef.child(newNoteId).setValue(note)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã thêm ghi chú", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi thêm ghi chú", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteNoteSoft() {
        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        long now = System.currentTimeMillis();

        Note deletedNote = new Note(noteId, folderId, title, content, date, now, now, false, true);

        trashRef.child(noteId).setValue(deletedNote)
                .addOnSuccessListener(unused ->
                        noteRef.child(noteId).removeValue()
                                .addOnSuccessListener(unused1 -> {
                                    Toast.makeText(this, "Đã chuyển vào thùng rác", Toast.LENGTH_SHORT).show();
                                    finish();
                                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi xóa ghi chú", Toast.LENGTH_SHORT).show());
    }
}