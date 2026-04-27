package com.example.mynote.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.activities.AddNoteActivity;
import com.example.mynote.adapters.FolderAdapter;
import com.example.mynote.models.Folder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.widget.ImageView;
import com.example.mynote.activities.SearchNoteActivity;
public class HomeFragment extends Fragment {
    private RecyclerView recyclerFolders;
    private FolderAdapter adapter;
    private List<Folder> folderList = new ArrayList<>();
    private DatabaseReference folderRef;
    private String uid;

    private boolean isEditing = false;
    private TextView btnEditMode;

    // === BỘ BIẾN XỬ LÝ MÀU SẮC THƯ MỤC ===
    private final String[] colorNames = {"Trắng", "Đỏ", "Cam", "Vàng", "Vàng nhạt", "Xanh nhạt", "Xanh bạc hà", "Xanh lá cây", "Xanh dương", "Xanh nước biển", "Xanh tím than", "Tím đậm", "Tím", "Tím nhạt", "Hồng nhạt", "Hồng"};
    private final String[] colorHexes = {"#E5E5EA", "#FF3B30", "#FF9500", "#FFCC00", "#FFF59D", "#DCE775", "#80CBC4", "#34C759", "#00C7BE", "#007AFF", "#5856D6", "#AF52DE", "#D8B4E2", "#FFD1DC", "#FF2D55", "#FF2D55"};
    private int selectedColorIndex = 0;
    private String selectedColorHex = colorHexes[0];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ImageView btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchNoteActivity.class);
            startActivity(intent);
        });
        recyclerFolders = view.findViewById(R.id.recyclerFolders);
        recyclerFolders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FolderAdapter(getContext(), folderList);
        recyclerFolders.setAdapter(adapter);


        FloatingActionButton fab = view.findViewById(R.id.fabAddFolder);
        fab.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), AddNoteActivity.class);
            i.putExtra("mode", "add");
            startActivity(i);
        });

        ImageView btnAddFolderTop = view.findViewById(R.id.btnAddFolderTop);
        btnAddFolderTop.setOnClickListener(v -> showAddFolderDialog());

        btnEditMode = view.findViewById(R.id.btnSua);
        if (btnEditMode != null) {
            btnEditMode.setOnClickListener(v -> {
                isEditing = !isEditing;

                if (isEditing) {
                    btnEditMode.setText("Hoàn thành");
                    btnEditMode.setTextColor(android.graphics.Color.parseColor("#007AFF"));
                    adapter.setEditMode(true);
                } else {
                    btnEditMode.setText("Sửa");
                    btnEditMode.setTextColor(android.graphics.Color.parseColor("#007AFF"));
                    adapter.setEditMode(false);
                }
            });
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        folderRef = FirebaseDatabase.getInstance().getReference("folders").child(uid);

        loadFolders();

        recyclerFolders.setOnTouchListener((v, event) -> {
            if (isEditing && adapter != null && adapter.isAnyDeleteMenuOpen()) {
                adapter.closeDeleteMenu();
            }
            return false;
        });

        return view;
    }

    private void loadFolders() {
        folderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                folderList.clear();

                folderList.add(new Folder("all", "Tất cả ghi chú", 0, System.currentTimeMillis()));

                for (DataSnapshot data : snapshot.getChildren()) {
                    Folder folder = data.getValue(Folder.class);
                    if (folder != null) {
                        folderList.add(folder);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_folder, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            );
        }

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        EditText edtEditFolderName = view.findViewById(R.id.edtEditFolderName);
        TextView tvSelectedColor = view.findViewById(R.id.tvSelectedColor);
        ImageView imgSelectedColorDot = view.findViewById(R.id.imgSelectedColorDot);
        LinearLayout layoutColor = view.findViewById(R.id.layoutColor);
        TextView btnCancelEdit = view.findViewById(R.id.btnCancelEdit);
        TextView btnSaveEdit = view.findViewById(R.id.btnSaveEdit);

        tvDialogTitle.setText("Thư mục mới");
        edtEditFolderName.setText("");
        edtEditFolderName.setHint("Tên thư mục");

        selectedColorIndex = 0;
        selectedColorHex = colorHexes[0];

        tvSelectedColor.setText(colorNames[selectedColorIndex]);

        if (imgSelectedColorDot != null) {
            imgSelectedColorDot.setColorFilter(android.graphics.Color.parseColor(selectedColorHex));
        }

        List<com.example.mynote.models.ColorOption> colorOptions = new ArrayList<>();
        for (int i = 0; i < colorNames.length; i++) {
            colorOptions.add(new com.example.mynote.models.ColorOption(colorNames[i], colorHexes[i]));
        }

        layoutColor.setOnClickListener(v -> {
            AlertDialog.Builder colorDialogBuilder =
                    new AlertDialog.Builder(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);

            View colorView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_color_picker, null);
            colorDialogBuilder.setView(colorView);

            AlertDialog colorDialog = colorDialogBuilder.create();

            if (colorDialog.getWindow() != null) {
                colorDialog.getWindow().setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
                );
            }

            ImageView btnBackColorPicker = colorView.findViewById(R.id.btnBackColorPicker);
            RecyclerView recyclerColorPicker = colorView.findViewById(R.id.recyclerColorPicker);

            recyclerColorPicker.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerColorPicker.setAdapter(
                    new com.example.mynote.adapters.ColorPickerAdapter(
                            colorOptions,
                            selectedColorHex,
                            colorOption -> {
                                selectedColorHex = colorOption.getHex();
                                selectedColorIndex = getColorIndexByHex(selectedColorHex);
                                tvSelectedColor.setText(colorOption.getName());

                                if (imgSelectedColorDot != null) {
                                    imgSelectedColorDot.setColorFilter(
                                            android.graphics.Color.parseColor(colorOption.getHex())
                                    );
                                }

                                colorDialog.dismiss();
                            }
                    )
            );

            btnBackColorPicker.setOnClickListener(backView -> colorDialog.dismiss());

            colorDialog.show();
        });

        btnCancelEdit.setOnClickListener(v -> dialog.dismiss());

        btnSaveEdit.setText("Tạo");
        btnSaveEdit.setOnClickListener(v -> {
            String folderName = edtEditFolderName.getText().toString().trim();

            if (folderName.isEmpty()) {
                Toast.makeText(getContext(), "Tên thư mục không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            createNewFolder(folderName);
            dialog.dismiss();
        });

        dialog.show();
    }

    private int getColorIndexByHex(String hex) {
        for (int i = 0; i < colorHexes.length; i++) {
            if (colorHexes[i].equalsIgnoreCase(hex)) {
                return i;
            }
        }
        return 0;
    }

    // === HÀM VẼ GIAO DIỆN CHỌN MÀU ===
    private void showColorPickerDialog(TextView tvSelectedColor, ImageView imgColorDot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Màu");

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, colorNames) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setPadding(50, 40, 50, 40);
                layout.setGravity(android.view.Gravity.CENTER_VERTICAL);

                // Dùng icon thư mục chuẩn của bạn
                ImageView icon = new ImageView(getContext());
                icon.setImageResource(R.drawable.ic_folder);
                icon.setColorFilter(android.graphics.Color.parseColor(colorHexes[position]));
                icon.setLayoutParams(new LinearLayout.LayoutParams(60, 60));

                TextView text = new TextView(getContext());
                text.setText(colorNames[position]);
                text.setTextSize(16);
                text.setTextColor(android.graphics.Color.BLACK);
                text.setPadding(40, 0, 0, 0);

                layout.addView(icon);
                layout.addView(text);
                return layout;
            }
        };

        builder.setAdapter(adapter, (dialog, which) -> {
            selectedColorIndex = which;
            selectedColorHex = colorHexes[which];

            // Đổ màu cập nhật lại UI
            if (tvSelectedColor != null) tvSelectedColor.setText(colorNames[which]);
            if (imgColorDot != null) imgColorDot.setColorFilter(android.graphics.Color.parseColor(selectedColorHex));
        });

        builder.show();
    }

    private void createNewFolder(String folderName) {
        String folderId = folderRef.push().getKey();
        // Vẫn dùng Model cũ của bạn để khởi tạo
        Folder newFolder = new Folder(folderId, folderName, 0, System.currentTimeMillis());

        folderRef.child(folderId).setValue(newFolder).addOnSuccessListener(aVoid -> {
            // === LƯU THÊM MÃ MÀU LÊN FIREBASE (Không làm hỏng cấu trúc cũ) ===
            folderRef.child(folderId).child("color").setValue(selectedColorHex);
            Toast.makeText(getContext(), "Đã tạo thư mục: " + folderName, Toast.LENGTH_SHORT).show();
        });
    }
}