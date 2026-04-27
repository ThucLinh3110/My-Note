package com.example.mynote.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.adapters.ImageAdapter;
import com.example.mynote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageFragment extends Fragment {

    private RecyclerView recyclerImages;
    private ImageButton btnSortImages;
    private ImageAdapter imageAdapter;
    private List<Note> notesWithImages;
    private DatabaseReference noteRef;

    // Mặc định: Xếp ảnh mới nhất (creationTime lớn nhất) lên đầu
    private boolean isDescending = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        // Ánh xạ các ID từ file fragment_image.xml
        recyclerImages = view.findViewById(R.id.recyclerImages);
        btnSortImages = view.findViewById(R.id.btnSortImages);

        // Cài đặt lưới 3 cột như yêu cầu của Linh
        recyclerImages.setLayoutManager(new GridLayoutManager(getContext(), 3));
        notesWithImages = new ArrayList<>();
        imageAdapter = new ImageAdapter(getContext(), notesWithImages);
        recyclerImages.setAdapter(imageAdapter);

        // Kết nối tới đúng "kho" ghi chú của Linh trên Firebase
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);

        // Tải ảnh về
        loadImagesFromFirebase();

        // Xử lý khi Linh bấm vào nút sắp xếp (2 mũi tên xanh)
        if (btnSortImages != null) {
            btnSortImages.setOnClickListener(v -> {
                isDescending = !isDescending; // Đảo ngược kiểu sắp xếp
                sortImageList(); // Sắp xếp lại danh sách
            });
        }

        return view;
    }

    private void loadImagesFromFirebase() {
        // Lắng nghe sự thay đổi trên Firebase, cứ có Note mới là ảnh tự hiện ra
        noteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notesWithImages.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Note note = data.getValue(Note.class);
                    // Chỉ lấy những ghi chú KHÔNG bị xóa (vào thùng rác) VÀ PHẢI CÓ ẢNH
                    if (note != null && !note.isDeleted() &&
                            note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
                        notesWithImages.add(note);
                    }
                }
                sortImageList(); // Tải xong thì sắp xếp luôn
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sortImageList() {
        Collections.sort(notesWithImages, (n1, n2) -> {
            if (isDescending) {
                // Mới nhất hiện trước
                return Long.compare(n2.getCreatedAt(), n1.getCreatedAt());
            } else {
                // Cũ nhất hiện trước
                return Long.compare(n1.getCreatedAt(), n2.getCreatedAt());
            }
        });
        // Báo cho Adapter biết là "đội hình" đã thay đổi để vẽ lại màn hình
        if (imageAdapter != null) {
            imageAdapter.notifyDataSetChanged();
        }
    }
}