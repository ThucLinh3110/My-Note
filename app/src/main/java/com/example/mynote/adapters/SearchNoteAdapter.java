package com.example.mynote.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.activities.AddNoteActivity;
import com.example.mynote.models.Folder;
import com.example.mynote.models.Note;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.text.TextUtils;

public class SearchNoteAdapter extends RecyclerView.Adapter<SearchNoteAdapter.SearchViewHolder> {

    private final List<Note> noteList;
    private final Map<String, Folder> folderMap;
    private String keyword = "";

    public SearchNoteAdapter(List<Note> noteList, Map<String, Folder> folderMap) {
        this.noteList = noteList;
        this.folderMap = folderMap;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword != null ? keyword : "";
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_note, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Note note = noteList.get(position);

        holder.tvTitle.setText(highlightKeyword(note.getTitle(), keyword));
        holder.tvDate.setText(note.getDate());

        String noteFolderId = note.getFolderId();
        Folder folder = folderMap.get(noteFolderId);

        if (folder != null) {
            holder.tvFolder.setText(folder.getFolderName());

            String color = folder.getColor();
            if (color == null || color.trim().isEmpty()) {
                color = "#00B8FF";
            }

            try {
                holder.imgFolderIcon.setColorFilter(Color.parseColor(color));
            } catch (Exception e) {
                holder.imgFolderIcon.setColorFilter(Color.parseColor("#00B8FF"));
            }
        } else {
            holder.tvFolder.setText("Tất cả ghi chú");
            holder.imgFolderIcon.setColorFilter(Color.parseColor("#00B8FF"));
        }

        // ===== XỬ LÝ ẢNH =====
        // Đổi "getImageUri()" thành đúng field ảnh mà Note của bạn đang có
        String imageUri = note.getImageUrl();

        if (imageUri != null && !imageUri.trim().isEmpty()) {
            holder.imgSearchThumb.setVisibility(View.VISIBLE);

            // Nếu bạn dùng ảnh local uri/string:
            try {
                holder.imgSearchThumb.setImageURI(android.net.Uri.parse(imageUri));
            } catch (Exception e) {
                holder.imgSearchThumb.setVisibility(View.GONE);
            }
        } else {
            holder.imgSearchThumb.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddNoteActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("noteId", note.getNoteId());
            intent.putExtra("title", note.getTitle());
            intent.putExtra("content", note.getContent());
            intent.putExtra("folderId", note.getFolderId());

            if (folder != null) {
                intent.putExtra("folderName", folder.getFolderName());
                intent.putExtra("folderColor", folder.getColor());
            }

            v.getContext().startActivity(intent);
        });
    }
    private CharSequence highlightKeyword(String text, String keyword) {
        if (text == null) return "";
        if (keyword == null || keyword.trim().isEmpty()) return text;

        String textLower = text.toLowerCase(Locale.ROOT);
        String keywordLower = keyword.toLowerCase(Locale.ROOT);

        SpannableString spannable = new SpannableString(text);
        int start = textLower.indexOf(keywordLower);

        while (start >= 0) {
            int end = start + keywordLower.length();
            spannable.setSpan(
                    new BackgroundColorSpan(Color.parseColor("#90CAF9")),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            start = textLower.indexOf(keywordLower, end);
        }

        return spannable;
    }

    @Override
    public int getItemCount() {
        return noteList != null ? noteList.size() : 0;
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvFolder;
        ImageView imgFolderIcon, imgSearchThumb;

        SearchViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSearchNoteTitle);
            tvDate = itemView.findViewById(R.id.tvSearchNoteDate);
            tvFolder = itemView.findViewById(R.id.tvSearchNoteFolder);
            imgFolderIcon = itemView.findViewById(R.id.imgSearchFolderIcon);
            imgSearchThumb = itemView.findViewById(R.id.imgSearchThumb);
        }
    }
}