package com.example.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.adapters.NoteAdapter;
import com.example.mynote.models.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import com.example.mynote.models.Folder;

import java.util.HashMap;
import java.util.Map;
import android.widget.PopupMenu;
import android.widget.Toast;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import java.util.Calendar;
import java.util.Collections;
public class FolderDetailActivity extends AppCompatActivity {

    // Khai báo các thành phần giao diện
    private TextView tvFolderTitle;
    private RecyclerView rvNotesInFolder;
    private FloatingActionButton fabAddNote;
    private ImageView btnBackDetail, btnFilterDetail, btnSortDetail;

    // Biến để lưu trữ ID và Tên của thư mục hiện tại
    private String currentFolderId;
    private String currentFolderName; // ĐÃ BỔ SUNG: Biến lưu tên thư mục
    private String currentFolderColor; // ĐÃ BỔ SUNG: Biến lưu màu thư mục

    private NoteAdapter noteAdapter;
    private List<Note> noteList;
    private DatabaseReference noteRef, folderRef;
    private Map<String, Folder> folderMap = new HashMap<>();
    private String currentSortField = "Ngày ghi chú";
    private boolean isAscending = true;

    private List<Note> allNoteList = new ArrayList<>();
    private String currentFilter = "Tất cả";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_detail);

        // ÁNH XẠ CÁC VIEW TỪ XML ---
        initViews();

        // NHẬN DỮ LIỆU AN TOÀN ---
        if (getIntent() != null) {
            // ĐÃ SỬA: Lưu thẳng vào biến toàn cục currentFolderName
            currentFolderName = getIntent().getStringExtra("folderName");
            currentFolderId = getIntent().getStringExtra("folderId");
            currentFolderColor = getIntent().getStringExtra("folderColor");

            if (currentFolderName != null) {
                tvFolderTitle.setText(currentFolderName);
            } else {
                tvFolderTitle.setText("Thư mục không tên");
                currentFolderName = "Thư mục không tên"; // Đề phòng lỗi
            }
        }

        // Khi bấm vào nút cây bút (FAB), mở màn hình thêm ghi chú mới
        fabAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(FolderDetailActivity.this, AddNoteActivity.class);

            // ĐÃ BỔ SUNG: Gửi kèm cả ID và Tên sang trang AddNote
            intent.putExtra("folderId", currentFolderId);
            intent.putExtra("folderName", currentFolderName);
            intent.putExtra("folderColor", currentFolderColor);
            intent.putExtra("mode", "add");
            startActivity(intent);
        });

        rvNotesInFolder.setLayoutManager(new LinearLayoutManager(this));

        // Nút Back quay về Trang Chủ
        btnBackDetail.setOnClickListener(v -> finish());
        btnFilterDetail.setOnClickListener(v -> showFilterMenu());
        btnSortDetail.setOnClickListener(v -> showSortMenu());
        // Khởi tạo danh sách Ghi chú
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, currentFolderId, currentFolderName);
        rvNotesInFolder.setAdapter(noteAdapter);
        rvNotesInFolder.setOnTouchListener((v, event) -> {
            if (noteAdapter != null && noteAdapter.isAnyDeleteMenuOpen()) {
                noteAdapter.closeDeleteMenu();
            }
            return false;
        });

        // Kết nối Firebase lấy đúng các ghi chú thuộc Folder này
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);
        folderRef = FirebaseDatabase.getInstance().getReference("folders").child(uid);

        loadFoldersFromFirebase();
        loadNotesFromFirebase();
    }

    private void initViews() {
        tvFolderTitle = findViewById(R.id.tvFolderTitle);
        rvNotesInFolder = findViewById(R.id.rvNotesInFolder);
        fabAddNote = findViewById(R.id.fabAddNote);
        btnBackDetail = findViewById(R.id.btnBackDetail);
        btnFilterDetail = findViewById(R.id.btnFilterDetail);
        btnSortDetail = findViewById(R.id.btnSortDetail);
    }

    // =======================================
    // TẢI GHI CHÚ TỪ FIREBASE
    // =======================================
    private void loadNotesFromFirebase() {
        if (currentFolderId == null) return;

        if (currentFolderId.equals("all")) {
            // Nếu là "Tất cả ghi chú", lấy toàn bộ không phân biệt folder
            noteRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    allNoteList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Note note = ds.getValue(Note.class);
                        if (note != null) allNoteList.add(note);
                    }
                    applyFilterAndSort();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            // Nếu là Thư mục cụ thể (vd: LTW, Học tập), chỉ lấy note của thư mục đó
            noteRef.orderByChild("folderId").equalTo(currentFolderId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            allNoteList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Note note = ds.getValue(Note.class);
                                if (note != null) allNoteList.add(note);
                            }
                            applyFilterAndSort();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    private void loadFoldersFromFirebase() {
        folderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                folderMap.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Folder folder = ds.getValue(Folder.class);
                    if (folder != null && folder.getFolderId() != null) {
                        folderMap.put(folder.getFolderId(), folder);
                        
                        // Cập nhật màu nếu tìm thấy folder hiện tại
                        if (folder.getFolderId().equals(currentFolderId)) {
                            currentFolderColor = folder.getColor();
                        }
                    }
                }

                noteAdapter.setFolderMap(folderMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showFilterMenu() {
        PopupWindow popupWindow = new PopupWindow(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.WHITE);
        layout.setPadding(0, dp(8), 0, dp(8));

        TextView header = new TextView(this);
        header.setText("⌄  Lọc theo\n     " + currentFilter);
        header.setTextSize(14);
        header.setTextColor(Color.BLACK);
        header.setPadding(dp(12), dp(6), dp(12), dp(6));
        layout.addView(header);

        addLine(layout);

        addFilterItem(layout, "Tất cả", popupWindow);
        addLine(layout);

        addFilterItem(layout, "Hôm nay", popupWindow);
        addLine(layout);

        addFilterItem(layout, "Hôm qua", popupWindow);
        addLine(layout);

        addFilterItem(layout, "7 ngày gần nhất", popupWindow);
        addLine(layout);

        addFilterItem(layout, "30 ngày gần nhất", popupWindow);

        popupWindow.setContentView(layout);
        popupWindow.setWidth(dp(172));
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.showAsDropDown(btnFilterDetail, -dp(135), 0);
    }

    private void showSortMenu() {
        PopupWindow popupWindow = new PopupWindow(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.WHITE);
        layout.setPadding(0, 8, 0, 8);

        // Header
        TextView header = new TextView(this);
        header.setText("⌄  Sắp xếp theo\n     " + currentSortField);
        header.setTextSize(14);
        header.setTextColor(Color.BLACK);
        header.setPadding(dp(12), dp(6), dp(12), dp(6));
        layout.addView(header);

        addLine(layout);

        addSortItem(layout, "Ngày ghi chú", popupWindow);
        addLine(layout);

        addSortItem(layout, "Ngày chỉnh sửa", popupWindow);
        addLine(layout);

        addSortItem(layout, "Tiêu đề", popupWindow);
        addLine(layout);

        addSortItem(layout, "Tăng dần", popupWindow);
        addLine(layout);

        addSortItem(layout, "Giảm dần", popupWindow);

        popupWindow.setContentView(layout);
        popupWindow.setWidth(dp(172));
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        popupWindow.showAsDropDown(btnSortDetail, -dp(135), 0);
    }

    private void addSortItem(LinearLayout layout, String text, PopupWindow popupWindow) {
        TextView item = new TextView(this);

        boolean checked = false;

        if (text.equals("Ngày ghi chú") && currentSortField.equals("Ngày ghi chú")) {
            checked = true;
        } else if (text.equals("Ngày chỉnh sửa") && currentSortField.equals("Ngày chỉnh sửa")) {
            checked = true;
        } else if (text.equals("Tiêu đề") && currentSortField.equals("Tiêu đề")) {
            checked = true;
        } else if (text.equals("Tăng dần") && isAscending) {
            checked = true;
        } else if (text.equals("Giảm dần") && !isAscending) {
            checked = true;
        }

        item.setText(checked ? "✓  " + text : "     " + text);
        item.setTextSize(15);
        item.setTextColor(Color.BLACK);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(12), dp(6), dp(12), dp(6));

        item.setOnClickListener(v -> {
            if (text.equals("Tăng dần")) {
                isAscending = true;
            } else if (text.equals("Giảm dần")) {
                isAscending = false;
            } else {
                currentSortField = text;
            }

            applyFilterAndSort();
            popupWindow.dismiss();
        });

        layout.addView(item);
    }

    private void sortNotes() {
        Collections.sort(noteList, (n1, n2) -> {

            int result;

            if (currentSortField.equals("Tiêu đề")) {
                String t1 = n1.getTitle() == null ? "" : n1.getTitle();
                String t2 = n2.getTitle() == null ? "" : n2.getTitle();
                result = t1.compareToIgnoreCase(t2);

            } else if (currentSortField.equals("Ngày chỉnh sửa")) {
                result = Long.compare(n1.getUpdatedAt(), n2.getUpdatedAt());

            } else { // Ngày ghi chú
                result = Long.compare(n1.getCreatedAt(), n2.getCreatedAt());
            }

            return isAscending ? result : -result;
        });

        noteAdapter.notifyDataSetChanged();
    }

    private void addLine(LinearLayout layout) {
        View line = new View(this);
        line.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        line.setBackgroundColor(Color.parseColor("#D6D6D6"));
        layout.addView(line);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void addFilterItem(LinearLayout layout, String text, PopupWindow popupWindow) {
        TextView item = new TextView(this);

        boolean checked = text.equals(currentFilter);

        item.setText(checked ? "✓  " + text : "     " + text);
        item.setTextSize(15);
        item.setTextColor(Color.BLACK);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(12), dp(6), dp(12), dp(6));

        item.setOnClickListener(v -> {
            currentFilter = text;
            applyFilterAndSort();
            popupWindow.dismiss();
        });

        layout.addView(item);
    }
    private void applyFilterAndSort() {
        noteList.clear();

        long now = System.currentTimeMillis();

        for (Note note : allNoteList) {
            long createdAt = note.getCreatedAt();
            boolean match = true;

            if (currentFilter.equals("Hôm nay")) {
                match = isSameDay(createdAt, now);

            } else if (currentFilter.equals("Hôm qua")) {
                match = isYesterday(createdAt);

            } else if (currentFilter.equals("7 ngày gần nhất")) {
                match = now - createdAt <= 7L * 24 * 60 * 60 * 1000;

            } else if (currentFilter.equals("30 ngày gần nhất")) {
                match = now - createdAt <= 30L * 24 * 60 * 60 * 1000;
            }

            if (match) {
                noteList.add(note);
            }
        }

        Collections.sort(noteList, (n1, n2) -> {
            int result;

            if (currentSortField.equals("Tiêu đề")) {
                String t1 = n1.getTitle() == null ? "" : n1.getTitle();
                String t2 = n2.getTitle() == null ? "" : n2.getTitle();
                result = t1.compareToIgnoreCase(t2);

            } else if (currentSortField.equals("Ngày chỉnh sửa")) {
                result = Long.compare(n1.getUpdatedAt(), n2.getUpdatedAt());

            } else {
                result = Long.compare(n1.getCreatedAt(), n2.getCreatedAt());
            }

            return isAscending ? result : -result;
        });

        noteAdapter.notifyDataSetChanged();
    }

    private boolean isSameDay(long time1, long time2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTimeInMillis(time1);
        c2.setTimeInMillis(time2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isYesterday(long time) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        Calendar noteDay = Calendar.getInstance();
        noteDay.setTimeInMillis(time);

        return yesterday.get(Calendar.YEAR) == noteDay.get(Calendar.YEAR)
                && yesterday.get(Calendar.DAY_OF_YEAR) == noteDay.get(Calendar.DAY_OF_YEAR);
    }
}
