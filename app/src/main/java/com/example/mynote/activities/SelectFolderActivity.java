package com.example.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.adapters.FolderSelectAdapter;
import com.example.mynote.models.Folder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectFolderActivity extends AppCompatActivity {

    private RecyclerView rvFolders;
    private ImageView btnClose, btnAddFolder;

    private final List<Folder> folderList = new ArrayList<>();
    private FolderSelectAdapter adapter;
    private DatabaseReference folderRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_folder);

        rvFolders = findViewById(R.id.rvFolders);
        btnClose = findViewById(R.id.btnClose);
        btnAddFolder = findViewById(R.id.btnAddFolder);

        rvFolders.setLayoutManager(new LinearLayoutManager(this));

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        folderRef = FirebaseDatabase.getInstance().getReference("folders").child(uid);

        adapter = new FolderSelectAdapter(folderList, folder -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedFolderId", folder.getFolderId());
            resultIntent.putExtra("selectedFolderName", folder.getFolderName());
            resultIntent.putExtra("selectedFolderColor", folder.getColor());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        rvFolders.setAdapter(adapter);

        btnClose.setOnClickListener(v -> finish());

        btnAddFolder.setOnClickListener(v -> {
            Toast.makeText(this, "Nút thêm thư mục bạn có thể nối sau", Toast.LENGTH_SHORT).show();
        });

        loadFolders();
    }

    private void loadFolders() {
        folderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                folderList.clear();

                Folder allFolder = new Folder("all_notes", "Tất cả ghi chú", 0, System.currentTimeMillis(), "#007AFF");
                folderList.add(allFolder);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Folder folder = dataSnapshot.getValue(Folder.class);
                    if (folder != null) {
                        folderList.add(folder);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SelectFolderActivity.this, "Lỗi tải thư mục!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}