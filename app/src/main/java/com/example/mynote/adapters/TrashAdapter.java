package com.example.mynote.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.models.TrashItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.TrashViewHolder> {

    public interface OnTrashActionListener {
        void onRestore(TrashItem item);
        void onDeletePermanent(TrashItem item);
    }

    private final Context context;
    private final List<TrashItem> trashList;
    private final OnTrashActionListener listener;

    public TrashAdapter(Context context, List<TrashItem> trashList, OnTrashActionListener listener) {
        this.context = context;
        this.trashList = trashList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TrashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trash_note, parent, false);
        return new TrashViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrashViewHolder holder, int position) {
        TrashItem item = trashList.get(position);

        holder.tvTitle.setText(item.getTitle() == null ? "" : item.getTitle());

        if ("folder".equals(item.getItemType())) {
            holder.tvContent.setText("Thư mục đã xóa");
        } else {
            String content = item.getContent();
            if (content == null || content.trim().isEmpty()) {
                holder.tvContent.setText("Ghi chú đã xóa");
            } else {
                holder.tvContent.setText(content);
            }
        }

        holder.tvDate.setText(formatDate(item.getDeletedAt()));

        holder.ivRestore.setOnClickListener(v -> {
            if (listener != null) listener.onRestore(item);
        });

        holder.ivDeletePermanent.setOnClickListener(v -> {
            if (listener != null) listener.onDeletePermanent(item);
        });
    }

    @Override
    public int getItemCount() {
        return trashList == null ? 0 : trashList.size();
    }

    static class TrashViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        ImageView ivRestore, ivDeletePermanent;

        public TrashViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivRestore = itemView.findViewById(R.id.ivRestore);
            ivDeletePermanent = itemView.findViewById(R.id.ivDeletePermanent);
        }
    }

    private String formatDate(long time) {
        if (time <= 0) return "";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date(time));
    }
}