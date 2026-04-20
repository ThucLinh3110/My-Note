package com.example.mynote.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.examplek49.mynote.R;
import com.examplek49.mynote.adapters.NoteAdapter;
import com.examplek49.mynote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {

    private EditText edtSelectedDate;
    private Button btnLoadByDate;
    private RecyclerView rvCalendarNotes;

    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private DatabaseReference noteRef;
    private String uid;

    public CalendarFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        edtSelectedDate = view.findViewById(R.id.edtSelectedDate);
        btnLoadByDate = view.findViewById(R.id.btnLoadByDate);
        rvCalendarNotes = view.findViewById(R.id.rvCalendarNotes);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("notes");

        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(getContext(), noteList, note ->
                Toast.makeText(getContext(), note.getTitle(), Toast.LENGTH_SHORT).show());

        rvCalendarNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCalendarNotes.setAdapter(noteAdapter);

        btnLoadByDate.setOnClickListener(v -> loadNotesByDate());

        return view;
    }

    private void loadNotesByDate() {
        String selectedDate = edtSelectedDate.getText().toString().trim();

        if (TextUtils.isEmpty(selectedDate)) {
            edtSelectedDate.setError("Nhập ngày yyyy-MM-dd");
            return;
        }

        noteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Note note = data.getValue(Note.class);
                    if (note != null && !note.isDeleted() && selectedDate.equals(note.getDate())) {
                        noteList.add(note);
                    }
                }

                noteAdapter.notifyDataSetChanged();

                if (noteList.isEmpty()) {
                    Toast.makeText(getContext(), "Không có ghi chú ngày này", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}