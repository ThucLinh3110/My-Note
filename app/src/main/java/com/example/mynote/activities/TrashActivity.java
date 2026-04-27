package com.example.mynote.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.adapters.TrashAdapter;
import com.example.mynote.models.Folder;
import com.example.mynote.models.Note;
import com.example.mynote.models.TrashItem;
import com.example.mynote.firebase.FirebaseTrashHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TrashActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvTrashNotes;
    private LinearLayout layoutEmpty;
    private ImageView ivRestoreAll, ivDeleteAll;

    private final List<TrashItem> trashList = new ArrayList<>();
    private TrashAdapter trashAdapter;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        initViews();
        setupFirebase();
        setupToolbar();
        setupRecyclerView();
        setupActions();
        loadTrashData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvTrashNotes = findViewById(R.id.rvTrashNotes);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        ivRestoreAll = findViewById(R.id.ivRestoreAll);
        ivDeleteAll = findViewById(R.id.ivDeleteAll);
    }

    private void setupFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("trash").child(uid);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        trashAdapter = new TrashAdapter(this, trashList, new TrashAdapter.OnTrashActionListener() {
            @Override
            public void onRestore(TrashItem item) {
                restoreTrashItem(item);
            }

            @Override
            public void onDeletePermanent(TrashItem item) {
                deleteTrashItemForever(item);
            }
        });

        rvTrashNotes.setLayoutManager(new LinearLayoutManager(this));
        rvTrashNotes.setAdapter(trashAdapter);
    }

    private void setupActions() {
        ivRestoreAll.setOnClickListener(v -> restoreAllItems());
        ivDeleteAll.setOnClickListener(v -> deleteAllItemsForever());
    }

    private void loadTrashData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trashList.clear();

                DataSnapshot folderSnapshot = snapshot.child("folders");
                for (DataSnapshot child : folderSnapshot.getChildren()) {
                    Folder folder = child.getValue(Folder.class);
                    if (folder != null) {
                        TrashItem item = new TrashItem();
                        item.setItemId(folder.getFolderId());
                        item.setItemType("folder");
                        item.setTitle(folder.getFolderName());
                        item.setContent("Thư mục đã xóa");
                        item.setColor(folder.getColor());
                        item.setNoteCount(folder.getNoteCount());
                        item.setCreatedAt(folder.getCreatedAt());
                        item.setDeletedAt(folder.getDeletedAt());
                        trashList.add(item);
                    }
                }

                DataSnapshot noteSnapshot = snapshot.child("notes");
                for (DataSnapshot child : noteSnapshot.getChildren()) {
                    Note note = child.getValue(Note.class);
                    if (note != null) {
                        TrashItem item = new TrashItem();
                        item.setItemId(note.getNoteId());
                        item.setItemType("note");
                        item.setFolderId(note.getFolderId());
                        item.setFolderName(note.getFolderName());
                        item.setTitle(note.getTitle());
                        item.setContent(note.getContent());
                        item.setDate(note.getDate());
                        item.setCreatedAt(note.getCreatedAt());
                        item.setUpdatedAt(note.getUpdatedAt());
                        item.setDeletedAt(note.getDeletedAt());
                        item.setHasImage(note.isHasImage());
                        item.setImageUrl(note.getImageUrl());
                        trashList.add(item);
                    }
                }

                trashAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrashActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (trashList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvTrashNotes.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvTrashNotes.setVisibility(View.VISIBLE);
        }
    }

    private void restoreTrashItem(TrashItem item) {
        if ("folder".equals(item.getItemType())) {
            Folder folder = new Folder();
            folder.setFolderId(item.getItemId());
            folder.setFolderName(item.getTitle());
            folder.setColor(item.getColor());
            folder.setNoteCount(item.getNoteCount());
            folder.setCreatedAt(item.getCreatedAt());
            folder.setDeleted(false);
            folder.setDeletedAt(0);

            FirebaseTrashHelper.restoreFolder(folder, new FirebaseTrashHelper.TrashCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(TrashActivity.this, "Khôi phục thư mục thành công", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(TrashActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Note note = new Note();
            note.setNoteId(item.getItemId());
            note.setFolderId(item.getFolderId());
            note.setFolderName(item.getFolderName());
            note.setTitle(item.getTitle());
            note.setContent(item.getContent());
            note.setDate(item.getDate());
            note.setCreatedAt(item.getCreatedAt());
            note.setUpdatedAt(item.getUpdatedAt());
            note.setDeleted(false);
            note.setDeletedAt(0);
            note.setHasImage(item.isHasImage());
            note.setImageUrl(item.getImageUrl());

            FirebaseTrashHelper.restoreNote(note, new FirebaseTrashHelper.TrashCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(TrashActivity.this, "Khôi phục ghi chú thành công", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(TrashActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteTrashItemForever(TrashItem item) {
        if ("folder".equals(item.getItemType())) {
            FirebaseTrashHelper.deleteFolderForever(item.getItemId(), new FirebaseTrashHelper.TrashCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(TrashActivity.this, "Đã xóa vĩnh viễn thư mục", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(TrashActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            FirebaseTrashHelper.deleteNoteForever(item.getItemId(), new FirebaseTrashHelper.TrashCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(TrashActivity.this, "Đã xóa vĩnh viễn ghi chú", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(TrashActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void restoreAllItems() {
        if (trashList.isEmpty()) {
            Toast.makeText(this, "Thùng rác trống", Toast.LENGTH_SHORT).show();
            return;
        }

        for (TrashItem item : new ArrayList<>(trashList)) {
            restoreTrashItem(item);
        }
    }

    private void deleteAllItemsForever() {
        if (trashList.isEmpty()) {
            Toast.makeText(this, "Thùng rác trống", Toast.LENGTH_SHORT).show();
            return;
        }

        for (TrashItem item : new ArrayList<>(trashList)) {
            deleteTrashItemForever(item);
        }
    }
}