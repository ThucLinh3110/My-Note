package com.example.mynote.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.activities.AddNoteActivity;
import com.example.mynote.adapters.CalendarNoteAdapter;
import com.example.mynote.adapters.NoteAdapter;
import com.example.mynote.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private ViewGroup rootLayoutCalendar;
    private RecyclerView calendarRecyclerView, rvCalendarNotes;
    private View btnEditFab;
    private ImageView btnPrevMonth, btnNextMonth;
    private TextView tvMonthYear, tvSelectedDateLabel, btnCollapse;
    private LinearLayout layoutSelectedDate;

    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private CalendarNoteAdapter calendarAdapter;

    private DatabaseReference noteRef, folderRef; // Đã thêm folderRef
    private String currentSelectedDate = "";
    private Calendar currentCalendar;

    // Lưu trữ bảng màu: Key là FolderID, Value là Mã Màu HEX
    private HashMap<String, String> folderColorMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        rootLayoutCalendar = view.findViewById(R.id.rootLayoutCalendar);
        calendarRecyclerView = view.findViewById(R.id.recyclerCalendar);
        rvCalendarNotes = view.findViewById(R.id.recyclerNotesByDate);
        btnEditFab = view.findViewById(R.id.fabAddNoteCalendar);
        tvSelectedDateLabel = view.findViewById(R.id.tvSelectedDateLabel);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        layoutSelectedDate = view.findViewById(R.id.layoutSelectedDate);
        btnCollapse = view.findViewById(R.id.btnCollapse);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        noteRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);
        folderRef = FirebaseDatabase.getInstance().getReference("folders").child(uid); // Gọi folder

        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, "all", "Tất cả ghi chú");
        rvCalendarNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCalendarNotes.setAdapter(noteAdapter);

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        currentCalendar = Calendar.getInstance();
        currentSelectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentCalendar.getTime());

        updateCalendar();

        loadFoldersForCalendar(); // Lấy màu trước
        loadAllNotesForCalendar(); // Rồi vẽ lên lịch

        if (tvMonthYear != null) {
            tvMonthYear.setOnClickListener(v -> {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(),
                        (view1, year, month, dayOfMonth) -> {
                            currentCalendar.set(Calendar.YEAR, year);
                            currentCalendar.set(Calendar.MONTH, month);
                            currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            updateCalendar();
                        },
                        currentCalendar.get(Calendar.YEAR),
                        currentCalendar.get(Calendar.MONTH),
                        currentCalendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            });
        }

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, -1);
                updateCalendar();
            });
        }
        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                currentCalendar.add(Calendar.MONTH, 1);
                updateCalendar();
            });
        }

        if (btnCollapse != null) {
            btnCollapse.setOnClickListener(v -> {
                TransitionManager.beginDelayedTransition(rootLayoutCalendar);
                calendarRecyclerView.setVisibility(View.VISIBLE);
                layoutSelectedDate.setVisibility(View.GONE);
                rvCalendarNotes.setVisibility(View.GONE);
            });
        }

        if (btnEditFab != null) {
            btnEditFab.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddNoteActivity.class);
                intent.putExtra("mode", "add");
                intent.putExtra("SELECTED_DATE", currentSelectedDate);
                startActivity(intent);
            });
        }

        return view;
    }

    private void updateCalendar() {
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int year = currentCalendar.get(Calendar.YEAR);
        String monthYearStr = (month < 10 ? "0" + month : String.valueOf(month)) + "/" + year;

        if (tvMonthYear != null) {
            tvMonthYear.setText("THG " + month + " " + year);
        }

        ArrayList<String> daysInMonthArray = new ArrayList<>();
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 2;
        if (firstDayOfWeek < 0) firstDayOfWeek = 6;

        int daysInMonthCount = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= 42; i++) {
            if (i <= firstDayOfWeek || i > daysInMonthCount + firstDayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - firstDayOfWeek));
            }
        }

        calendarAdapter = new CalendarNoteAdapter(daysInMonthArray, monthYearStr, date -> {
            currentSelectedDate = date;
            TransitionManager.beginDelayedTransition(rootLayoutCalendar);
            calendarRecyclerView.setVisibility(View.GONE);
            layoutSelectedDate.setVisibility(View.VISIBLE);
            rvCalendarNotes.setVisibility(View.VISIBLE);
            if (tvSelectedDateLabel != null) tvSelectedDateLabel.setText("Ghi chú ngày " + date);

            loadNotesFromFirebase(date);
        });

        // Nhớ đưa bảng màu qua cho Adapter vẽ
        calendarAdapter.setFolderColors(folderColorMap);
        calendarRecyclerView.setAdapter(calendarAdapter);

        loadFoldersForCalendar();
        loadAllNotesForCalendar();

        TransitionManager.beginDelayedTransition(rootLayoutCalendar);
        calendarRecyclerView.setVisibility(View.VISIBLE);
        layoutSelectedDate.setVisibility(View.GONE);
        rvCalendarNotes.setVisibility(View.GONE);
    }

    // --- HÀM TẢI BẢNG MÀU THƯ MỤC ---
    private void loadFoldersForCalendar() {
        folderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                folderColorMap.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String folderId = data.getKey();
                    String color = data.child("color").getValue(String.class); // Lấy mã màu (ví dụ: #FF3B30)
                    if (folderId != null && color != null) {
                        folderColorMap.put(folderId, color);
                    }
                }
                if (calendarAdapter != null) {
                    calendarAdapter.setFolderColors(folderColorMap);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadAllNotesForCalendar() {
        noteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Note> allNotes = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Note note = data.getValue(Note.class);
                    if (note != null && !note.isDeleted()) {
                        allNotes.add(note);
                    }
                }
                if (calendarAdapter != null) {
                    calendarAdapter.setNotes(allNotes);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadNotesFromFirebase(String targetDate) {
        noteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Note note = data.getValue(Note.class);
                    if (note != null && !note.isDeleted() && note.getDate() != null && note.getDate().startsWith(targetDate)) {
                        noteList.add(note);
                    }
                }
                if (noteAdapter != null) noteAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFoldersForCalendar();
        loadAllNotesForCalendar();
        if (!currentSelectedDate.isEmpty()) loadNotesFromFirebase(currentSelectedDate);
    }
}
