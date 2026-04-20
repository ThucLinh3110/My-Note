package com.examplek49.mynote.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.examplek49.mynote.R;
import com.examplek49.mynote.models.Folder;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    public interface OnFolderClickListener {
        void onFolderClick(Folder folder);
    }

    private Context context;
    private List<Folder> folderList;
    private OnFolderClickListener listener;

    public FolderAdapter(Context context, List<Folder> folderList, OnFolderClickListener listener) {
        this.context = context;
        this.folderList = folderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folderList.get(position);
        holder.tvFolderName.setText(folder.getFolderName());

        try {
            holder.cardFolder.setCardBackgroundColor(Color.parseColor(folder.getColor()));
        } catch (Exception e) {
            holder.cardFolder.setCardBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> listener.onFolderClick(folder));
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderName;
        CardView cardFolder;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
            cardFolder = itemView.findViewById(R.id.cardFolder);
        }
    }
}