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
import com.example.mynote.models.ColorOption;

import java.util.List;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder> {

    public interface OnColorSelectedListener {
        void onColorSelected(ColorOption colorOption);
    }

    private final List<ColorOption> colorList;
    private String selectedHex;
    private final OnColorSelectedListener listener;

    public ColorPickerAdapter(List<ColorOption> colorList, String selectedHex,
                              OnColorSelectedListener listener) {
        this.colorList = colorList;
        this.selectedHex = selectedHex;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color_picker, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        ColorOption colorOption = colorList.get(position);
        holder.tvColorName.setText(colorOption.getName());
        holder.imgColorFolder.setColorFilter(Color.parseColor(colorOption.getHex()));

        if (colorOption.getHex().equalsIgnoreCase(selectedHex)) {
            holder.tvCheckMark.setVisibility(View.VISIBLE);
        } else {
            holder.tvCheckMark.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedHex = colorOption.getHex();
            notifyDataSetChanged();
            listener.onColorSelected(colorOption);
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        ImageView imgColorFolder;
        TextView tvColorName, tvCheckMark;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            imgColorFolder = itemView.findViewById(R.id.imgColorFolder);
            tvColorName = itemView.findViewById(R.id.tvColorName);
            tvCheckMark = itemView.findViewById(R.id.tvCheckMark);
        }
    }
}
