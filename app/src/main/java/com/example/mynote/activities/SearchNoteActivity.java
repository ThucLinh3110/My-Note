package com.example.mynote.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.adapters.SearchNoteAdapter;
import com.example.mynote.models.Folder;
import com.example.mynote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchNoteActivity extends AppCompatActivity {

    private ImageView btnBackSearch;
    private EditText edtSearch;
    private TextView tvSearchResult;
    private RecyclerView rvSearchNotes;

    private SearchNoteAdapter adapter;
    private final List<Note> allNotes = new ArrayList<>();
    private final List<Note> filteredNotes = new ArrayList<>();
    private final Map<String, Folder> folderMap = new HashMap<>();

    private DatabaseReference noteRef;
    private DatabaseReference folderRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_note);

        btnBackSearch = findViewById(R.id.btnBackSearch);
        edtSearch = findViewById(R.id.edtSearch);
        tvSearchResult = findViewById(R.id.tvSearchResult);
        rvSearchNotes = findViewById(R.id.rvSearchNotes);

        btnBackSearch.setOnClickListener(v -> finish());

        rvSearchNotes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchNoteAdapter(filteredNotes, folderMap);
        rvSearchNotes.setAdapter(adapter);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);
        folderRef = FirebaseDatabase.getInstance().getReference("folders").child(uid);

        loadFolders();
        loadNotes();

        edtSearch.requestFocus();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadFolders() {
        folderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                folderMap.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Folder folder = ds.getValue(Folder.class);
                    if (folder != null && folder.getFolderId() != null) {
                        folderMap.put(folder.getFolderId(), folder);
                    }
                }

                Folder allFolder = new Folder();
                allFolder.setFolderId("all");
                allFolder.setFolderName("Tất cả ghi chú");
                allFolder.setColor("#00B8FF");
                folderMap.put("all", allFolder);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadNotes() {
        noteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allNotes.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Note note = ds.getValue(Note.class);
                    if (note != null) {
                        allNotes.add(0, note);
                    }
                }

                filterNotes(edtSearch.getText().toString().trim());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterNotes(String keyword) {
        filteredNotes.clear();
        adapter.setKeyword(keyword);

        if (keyword.isEmpty()) {
            tvSearchResult.setText("");
            adapter.notifyDataSetChanged();
            return;
        }

        String keywordLower = keyword.toLowerCase(Locale.ROOT);

        for (Note note : allNotes) {
            String title = note.getTitle() != null ? note.getTitle().toLowerCase(Locale.ROOT) : "";
            String content = note.getContent() != null ? note.getContent().toLowerCase(Locale.ROOT) : "";

            if (title.contains(keywordLower) || content.contains(keywordLower)) {
                filteredNotes.add(note);
            }
        }

        tvSearchResult.setText("Đã tìm thấy " + filteredNotes.size() + " kết quả.");
        adapter.notifyDataSetChanged();
    }
}