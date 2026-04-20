package com.examplek49.mynote.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examplek49.mynote.R;
import com.examplek49.mynote.activities.FolderDetailActivity;
import com.examplek49.mynote.adapters.FolderAdapter;
import com.examplek49.mynote.models.Folder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvFolders;
    private ImageButton btnAddFolder;
    private FolderAdapter folderAdapter;
    private List<Folder> folderList;

    private DatabaseReference folderRef;
    private String uid;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvFolders = view.findViewById(R.id.rvFolders);
        btnAddFolder = view.findViewById(R.id.btnAddFolder);

        folderList = new ArrayList<>();
        folderAdapter = new FolderAdapter(getContext(), folderList, folder -> {
            Intent intent = new Intent(getActivity(), FolderDetailActivity.class);
            intent.putExtra("folderId", folder.getFolderId());
            intent.putExtra("folderName", folder.getFolderName());
            startActivity(intent);
        });

        rvFolders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFolders.setAdapter(folderAdapter);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        folderRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("folders");

        loadFolders();

        btnAddFolder.setOnClickListener(v -> showAddFolderDialog());

        return view;
    }

    private void loadFolders() {
        folderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                folderList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Folder folder = data.getValue(Folder.class);
                    if (folder != null) {
                        folderList.add(folder);
                    }
                }
                folderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi load folder", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddFolderDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_folder, null);
        EditText edtFolderName = dialogView.findViewById(R.id.edtFolderName);

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm thư mục")
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String folderName = edtFolderName.getText().toString().trim();

                    if (TextUtils.isEmpty(folderName)) {
                        Toast.makeText(getContext(), "Nhập tên thư mục", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String folderId = folderRef.push().getKey();
                    Folder folder = new Folder(folderId, folderName, "#7ED957", System.currentTimeMillis());

                    folderRef.child(folderId).setValue(folder)
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(getContext(), "Đã thêm thư mục", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Lỗi thêm thư mục", Toast.LENGTH_SHORT).show());
                })
                .show();
    }
}