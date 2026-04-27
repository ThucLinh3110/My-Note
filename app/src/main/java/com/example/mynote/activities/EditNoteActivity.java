package com.example.mynote.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mynote.R;
import com.example.mynote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {
    private EditText edtTitle, edtContent;
    private TextView txtDateTime, btnSave;
    private ImageView btnBack;

    private DatabaseReference noteRef;
    private String noteId, folderId;
    private long creationTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nhớ liên kết đúng với file giao diện Sửa của bạn
        setContentView(R.layout.activity_edit_note);

        // 1. Ánh xạ View
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        txtDateTime = findViewById(R.id.txtDateTime);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // 2. Nhận dữ liệu cũ từ NoteAdapter gửi sang
        noteId = getIntent().getStringExtra("noteId");
        folderId = getIntent().getStringExtra("folderId");
        String oldTitle = getIntent().getStringExtra("title");
        String oldContent = getIntent().getStringExtra("content");
        creationTime = getIntent().getLongExtra("createdAt", System.currentTimeMillis());

        // 3. Đổ dữ liệu cũ lên màn hình cho người dùng thấy
        edtTitle.setText(oldTitle);
        edtContent.setText(oldContent);

        // Cập nhật ngày giờ đang sửa (lấy giờ hiện tại)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        txtDateTime.setText(sdf.format(new Date(System.currentTimeMillis())));

        // 4. Kết nối Firebase
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);

        // 5. Bắt sự kiện các nút
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> updateNoteToFirebase());
    }

    private void updateNoteToFirebase() {
        String newTitle = edtTitle.getText().toString().trim();
        String newContent = edtContent.getText().toString().trim();

        if (TextUtils.isEmpty(newTitle)) {
            Toast.makeText(this, "Tiêu đề không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy thời gian lúc vừa bấm lưu
        long now = System.currentTimeMillis();
        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(now));

        // Đóng gói lại thành Object Note.
        // LƯU Ý: Dùng lại noteId cũ để Firebase hiểu là ghi đè (Cập nhật) chứ không phải tạo mới
        Note updatedNote = new Note(noteId, folderId, newTitle, newContent, dateStr, creationTime, now, false, false);

        noteRef.child(noteId).setValue(updatedNote).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã lưu thay đổi!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng trang Sửa, quay về danh sách
        });
    }
}