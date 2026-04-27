package com.example.mynote.adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.activities.AddNoteActivity;
import com.example.mynote.models.Folder;
import com.example.mynote.models.Note;
import com.example.mynote.firebase.FirebaseTrashHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final List<Note> noteList;
    private final String currentFolderId;
    private final String currentFolderName;

    private Map<String, Folder> folderMap = new HashMap<>();

    private int openedDeletePosition = -1;
    private float downX = 0f;
    private float downY = 0f;
    private float startTranslationX = 0f;
    private static final float MAX_SWIPE_DISTANCE = 120f;
    private static final float SWIPE_TRIGGER = 60f;

    public NoteAdapter(List<Note> noteList, String currentFolderId, String currentFolderName) {
        this.noteList = noteList;
        this.currentFolderId = currentFolderId;
        this.currentFolderName = currentFolderName;
    }

    public void setFolderMap(Map<String, Folder> folderMap) {
        this.folderMap = folderMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvDate.setText(note.getDate());

        if ("all".equals(currentFolderId)) {
            holder.cardFolderTag.setVisibility(View.VISIBLE);

            String noteFolderId = note.getFolderId();

            if (noteFolderId == null || noteFolderId.trim().isEmpty()
                    || "all".equals(noteFolderId) || "all_notes".equals(noteFolderId)) {
                holder.tvFolder.setText("Tất cả ghi chú");

                if (holder.imgFolderTag != null) {
                    holder.imgFolderTag.setColorFilter(Color.parseColor("#00B8FF"));
                }
            } else {
                Folder folder = folderMap.get(noteFolderId);

                if (folder != null) {
                    holder.tvFolder.setText(folder.getFolderName());

                    String color = folder.getColor();
                    if (color == null || color.trim().isEmpty()) {
                        color = "#BDBDBD";
                    }

                    if (holder.imgFolderTag != null) {
                        try {
                            holder.imgFolderTag.setColorFilter(Color.parseColor(color));
                        } catch (Exception e) {
                            holder.imgFolderTag.setColorFilter(Color.parseColor("#BDBDBD"));
                        }
                    }
                } else {
                    holder.tvFolder.setText("Thư mục");

                    if (holder.imgFolderTag != null) {
                        holder.imgFolderTag.setColorFilter(Color.parseColor("#BDBDBD"));
                    }
                }
            }
        } else {
            holder.cardFolderTag.setVisibility(View.GONE);
        }

        if (openedDeletePosition == position) {
            holder.layoutDeleteNote.setVisibility(View.VISIBLE);
            holder.cardNote.setTranslationX(-MAX_SWIPE_DISTANCE);
            holder.imgNoteArrow.setVisibility(View.GONE);
        } else {
            holder.layoutDeleteNote.setVisibility(View.GONE);
            holder.cardNote.setTranslationX(0f);
            holder.imgNoteArrow.setVisibility(View.VISIBLE);
        }

        holder.tvDeleteNote.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Note currentNote = noteList.get(currentPosition);
            showDeleteNoteDialog(holder, currentNote);
        });

        holder.cardNote.setOnTouchListener((v, event) -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return false;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    downY = event.getRawY();
                    startTranslationX = holder.cardNote.getTranslationX();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getRawX() - downX;
                    float deltaY = event.getRawY() - downY;

                    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 20) {
                        float newTranslationX = startTranslationX + deltaX;

                        if (newTranslationX < -MAX_SWIPE_DISTANCE) {
                            newTranslationX = -MAX_SWIPE_DISTANCE;
                        }

                        if (newTranslationX > 0) {
                            newTranslationX = 0;
                        }

                        holder.cardNote.setTranslationX(newTranslationX);

                        if (newTranslationX < 0) {
                            holder.layoutDeleteNote.setVisibility(View.VISIBLE);
                            holder.imgNoteArrow.setVisibility(View.GONE);
                        } else {
                            holder.layoutDeleteNote.setVisibility(View.GONE);
                            holder.imgNoteArrow.setVisibility(View.VISIBLE);
                        }

                        return true;
                    }
                    return false;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float totalDeltaX = event.getRawX() - downX;
                    float totalDeltaY = event.getRawY() - downY;

                    if (!(Math.abs(totalDeltaX) > Math.abs(totalDeltaY) && Math.abs(totalDeltaX) > 20)) {
                        handleNoteClick(holder);
                        return true;
                    }

                    float endTranslationX = holder.cardNote.getTranslationX();

                    if (endTranslationX < -SWIPE_TRIGGER) {
                        openedDeletePosition = currentPosition;
                    } else {
                        openedDeletePosition = -1;
                    }

                    notifyDataSetChanged();
                    return true;
            }

            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            // Nếu đang mở ô Xóa thì đóng lại
            if (openedDeletePosition != -1) {
                openedDeletePosition = -1;
                notifyDataSetChanged();
                return;
            }

            handleNoteClick(holder);
        });

        holder.cardNote.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            // Nếu đang mở ô Xóa thì đóng lại
            if (openedDeletePosition != -1) {
                openedDeletePosition = -1;
                notifyDataSetChanged();
                return;
            }

            handleNoteClick(holder);
        });
    }

    private void handleNoteClick(NoteViewHolder holder) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;

        Note note = noteList.get(currentPosition);

        if (openedDeletePosition != -1) {
            openedDeletePosition = -1;
            notifyDataSetChanged();
            return;
        }

        Intent intent = new Intent(holder.itemView.getContext(), AddNoteActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("noteId", note.getNoteId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("content", note.getContent());

        intent.putExtra("folderId", note.getFolderId());

        Folder folder = folderMap.get(note.getFolderId());
        if (folder != null) {
            intent.putExtra("folderName", folder.getFolderName());
            intent.putExtra("folderColor", folder.getColor());
        } else {
            intent.putExtra("folderName", currentFolderName);
            intent.putExtra("folderColor", "#007AFF");
        }

        holder.itemView.getContext().startActivity(intent);
    }

    private void showDeleteNoteDialog(NoteViewHolder holder, Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        View view = LayoutInflater.from(holder.itemView.getContext())
                .inflate(R.layout.dialog_delete_note, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancelDelete = view.findViewById(R.id.btnCancelDelete);
        TextView btnConfirmDelete = view.findViewById(R.id.btnConfirmDelete);

        btnCancelDelete.setOnClickListener(v -> dialog.dismiss());

        btnConfirmDelete.setOnClickListener(v -> {
            deleteNote(holder, note);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteNote(NoteViewHolder holder, Note note) {
        note.setFolderName(currentFolderName); // rất quan trọng

        FirebaseTrashHelper.moveNoteToTrash(note, new FirebaseTrashHelper.TrashCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(holder.itemView.getContext(), "Đã chuyển ghi chú vào thùng rác", Toast.LENGTH_SHORT).show();
                openedDeletePosition = -1;
                notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(holder.itemView.getContext(), "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList != null ? noteList.size() : 0;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvFolder, tvDeleteNote;
        CardView cardFolderTag, cardNote;
        View layoutDeleteNote;
        ImageView imgFolderTag, imgNoteArrow;

        NoteViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
            tvFolder = itemView.findViewById(R.id.tvNoteFolder);
            tvDeleteNote = itemView.findViewById(R.id.tvDeleteNote);

            cardFolderTag = itemView.findViewById(R.id.cardFolderTag);
            cardNote = itemView.findViewById(R.id.cardNote);

            layoutDeleteNote = itemView.findViewById(R.id.layoutDeleteNote);
            imgFolderTag = itemView.findViewById(R.id.imgNoteFolderIcon);
            imgNoteArrow = itemView.findViewById(R.id.imgNoteArrow);
        }
    }

    public void closeDeleteMenu() {
        if (openedDeletePosition != -1) {
            openedDeletePosition = -1;
            notifyDataSetChanged();
        }
    }

    public boolean isAnyDeleteMenuOpen() {
        return openedDeletePosition != -1;
    }
}
