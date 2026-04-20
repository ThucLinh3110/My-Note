package com.examplek49.mynote.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.examplek49.mynote.R;
import com.examplek49.mynote.models.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    private Context context;
    private List<Note> noteList;
    private OnNoteClickListener listener;

    public NoteAdapter(Context context, List<Note> noteList, OnNoteClickListener listener) {
        this.context = context;
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);

        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());
        holder.tvDate.setText(note.getDate());

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;
        CardView cardNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
            cardNote = itemView.findViewById(R.id.cardNote);
        }
    }
}