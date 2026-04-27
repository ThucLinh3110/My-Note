package com.example.mynote.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mynote.R;
import com.example.mynote.activities.AddNoteActivity;
import com.example.mynote.models.Note;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context context;
    private List<Note> noteListWithImages;

    public ImageAdapter(Context context, List<Note> noteListWithImages) {
        this.context = context;
        this.noteListWithImages = noteListWithImages;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đã sửa thành file item_image của Linh
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Note note = noteListWithImages.get(position);

        // Load ảnh từ Firebase lên lưới
        Glide.with(context)
                .load(note.getImageUrl())
                .centerCrop()
                .into(holder.imgItemNote);

        // Bấm vào ảnh nhỏ -> Mở Dialog to
        holder.itemView.setOnClickListener(v -> showFullScreenImageDialog(note));
    }

    @Override
    public int getItemCount() {
        return noteListWithImages.size();
    }

    private void showFullScreenImageDialog(Note note) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_viewer);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#E6000000")));

        ImageView imgFullScreen = dialog.findViewById(R.id.imgFullScreen);
        ImageButton btnClose = dialog.findViewById(R.id.btnCloseDialog);
        ImageButton btnGoToNote = dialog.findViewById(R.id.btnGoToNote);

        Glide.with(context).load(note.getImageUrl()).into(imgFullScreen);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnGoToNote.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, AddNoteActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("noteId", note.getNoteId());
            intent.putExtra("title", note.getTitle());
            intent.putExtra("content", note.getContent());
            intent.putExtra("folderId", note.getFolderId());
            intent.putExtra("folderName", note.getFolderName());
            intent.putExtra("imageUrl", note.getImageUrl());
            intent.putExtra("createdAt", note.getCreatedAt());
            context.startActivity(intent);
        });

        dialog.show();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItemNote;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đã sửa ID thành imgItemNote theo đúng file XML của Linh
            imgItemNote = itemView.findViewById(R.id.imgItemNote);
        }
    }
}