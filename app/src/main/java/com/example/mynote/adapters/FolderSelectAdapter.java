package com.example.mynote.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.models.Folder;

import java.util.List;

public class FolderSelectAdapter extends RecyclerView.Adapter<FolderSelectAdapter.FolderViewHolder> {

    public interface OnFolderClickListener {
        void onFolderClick(Folder folder);
    }

    private final List<Folder> folderList;
    private final OnFolderClickListener listener;

    public FolderSelectAdapter(List<Folder> folderList, OnFolderClickListener listener) {
        this.folderList = folderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folderList.get(position);

        holder.txtFolderName.setText(folder.getFolderName());
        holder.txtNoteCount.setText("(" + folder.getNoteCount() + ")");

        String color = folder.getColor();
        if (color == null || color.trim().isEmpty()) {
            color = "#BDBDBD";
        }

        try {
            holder.imgFolder.setColorFilter(Color.parseColor(color));
        } catch (Exception e) {
            holder.imgFolder.setColorFilter(Color.parseColor("#BDBDBD"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFolderClick(folder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderList == null ? 0 : folderList.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFolder;
        TextView txtFolderName, txtNoteCount;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFolder = itemView.findViewById(R.id.imgFolder);
            txtFolderName = itemView.findViewById(R.id.txtFolderName);
            txtNoteCount = itemView.findViewById(R.id.txtNoteCount);
        }
    }
}