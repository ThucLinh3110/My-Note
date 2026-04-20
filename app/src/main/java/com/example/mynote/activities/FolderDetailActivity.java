package com.example.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examplek49.mynote.R;
import com.examplek49.mynote.adapters.NoteAdapter;
import com.examplek49.mynote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FolderDetailActivity extends AppCompatActivity {

    private TextView tvFolderTitle;
    private RecyclerView rvNotes;
    private ImageButton btnAddNote;

    private List<Note> noteList;
    private NoteAdapter noteAdapter;

    private DatabaseReference noteRef;
    private String uid;
    private String folderId;
    private String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_detail);

        tvFolderTitle = findViewById(R.id.tvFolderTitle);
        rvNotes = findViewById(R.id.rvNotes);
        btnAddNote = findViewById(R.id.btnAddNote);

        folderId = getIntent().getStringExtra("folderId");
        folderName = getIntent().getStringExtra("folderName");

        tvFolderTitle.setText(folderName);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("notes");

        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(this, noteList, note -> {
            Intent intent = new Intent(FolderDetailActivity.this, AddNoteActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("folderId", folderId);
            intent.putExtra("noteId", note.getNoteId());
            intent.putExtra("title", note.getTitle());
            intent.putExtra("content", note.getContent());
            intent.putExtra("date", note.getDate());
            startActivity(intent);
        });

        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setAdapter(noteAdapter);

        loadNotes();

        btnAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(FolderDetailActivity.this, AddNoteActivity.class);
            intent.putExtra("mode", "add");
            intent.putExtra("folderId", folderId);
            startActivity(intent);
        });
    }

    private void loadNotes() {
        noteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Note note = data.getValue(Note.class);
                    if (note != null && folderId.equals(note.getFolderId()) && !note.isDeleted()) {
                        noteList.add(note);
                    }
                }
                noteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FolderDetailActivity.this, "Lỗi load note", Toast.LENGTH_SHORT).show();
            }
        });
    }
}