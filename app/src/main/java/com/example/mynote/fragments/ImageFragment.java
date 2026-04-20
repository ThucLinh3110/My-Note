package com.example.mynote.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examplek49.mynote.R;
import com.examplek49.mynote.adapters.ImageAdapter;
import com.examplek49.mynote.models.NoteImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ImageFragment extends Fragment {

    private RecyclerView rvImages;
    private List<NoteImage> imageList;
    private ImageAdapter imageAdapter;
    private DatabaseReference imageRef;
    private String uid;

    public ImageFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        rvImages = view.findViewById(R.id.rvImages);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        imageRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("images");

        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(getContext(), imageList);

        rvImages.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvImages.setAdapter(imageAdapter);

        loadImages();

        return view;
    }

    private void loadImages() {
        imageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    NoteImage image = data.getValue(NoteImage.class);
                    if (image != null) {
                        imageList.add(image);
                    }
                }

                imageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi load ảnh", Toast.LENGTH_SHORT).show();
            }
        });
    }
}