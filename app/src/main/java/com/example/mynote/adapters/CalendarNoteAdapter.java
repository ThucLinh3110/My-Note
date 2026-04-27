package com.example.mynote.adapters;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mynote.R;
import com.example.mynote.models.Note;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalendarNoteAdapter extends RecyclerView.Adapter<CalendarNoteAdapter.CalendarViewHolder> {
    private ArrayList<String> daysOfMonth;
    private String monthYear;
    private OnItemListener onItemListener;
    private List<Note> allNotes = new ArrayList<>();
    private HashMap<String, String> folderColors = new HashMap<>();
    private int selectedPosition = -1;

    public interface OnItemListener {
        void onItemClick(String date);
    }

    public CalendarNoteAdapter(ArrayList<String> daysOfMonth, String monthYear, OnItemListener onItemListener) {
        this.daysOfMonth = daysOfMonth;
        this.monthYear = monthYear;
        this.onItemListener = onItemListener;
    }

    public void setNotes(List<Note> allNotes) {
        this.allNotes = allNotes;
        notifyDataSetChanged();
    }

    public void setFolderColors(HashMap<String, String> folderColors) {
        this.folderColors = folderColors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        String day = daysOfMonth.get(position);
        holder.tvDay.setText(day);

        // QUAN TRỌNG: Dọn sạch hộp chứa cũ đi để tránh bị nhân bản khi lướt lên lướt xuống
        holder.layoutBanners.removeAllViews();

        if (!day.isEmpty()) {
            if (selectedPosition == position) {
                holder.tvDay.setBackgroundResource(R.drawable.border_day);
                holder.tvDay.setTextColor(Color.parseColor("#007AFF"));
            } else {
                holder.tvDay.setBackgroundResource(0);
                holder.tvDay.setTextColor(Color.parseColor("#333333"));
            }

            String formattedDate = (day.length() == 1 ? "0" + day : day) + "/" + monthYear;
            int noteCount = 0; // Biến đếm số lượng ghi chú trong ngày

            for (Note note : allNotes) {
                if (note.getDate() != null && note.getDate().startsWith(formattedDate) && !note.isDeleted()) {

                    // Giới hạn tối đa 3 thanh màu để không làm tràn ô lịch
                    if (noteCount >= 3) {
                        break;
                    }

                    // 1. TẠO MỘT THANH TEXTVIEW MỚI BẰNG CODE
                    TextView tvBanner = new TextView(holder.itemView.getContext());
                    tvBanner.setText(note.getTitle());
                    tvBanner.setTextColor(Color.WHITE);
                    tvBanner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9); // Chữ nhỏ xíu cho vừa vặn
                    tvBanner.setMaxLines(1);
                    tvBanner.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    tvBanner.setPadding(6, 0, 6, 0);

                    // 2. LẤY MÀU TỪ THƯ MỤC
                    String folderId = note.getFolderId();
                    if (folderId != null && folderColors.containsKey(folderId)) {
                        try {
                            tvBanner.setBackgroundColor(Color.parseColor(folderColors.get(folderId)));
                        } catch (Exception e) {
                            tvBanner.setBackgroundColor(Color.parseColor("#03A9F4"));
                        }
                    } else {
                        tvBanner.setBackgroundColor(Color.parseColor("#03A9F4"));
                    }

                    // 3. TẠO KHOẢNG CÁCH GIỮA CÁC THANH MÀU (Margin)
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 2, 0, 2); // Khoảng cách trên dưới là 2
                    tvBanner.setLayoutParams(params);

                    // 4. NHÉT THANH MÀU VÀO TRONG HỘP CHỨA
                    holder.layoutBanners.addView(tvBanner);
                    noteCount++;
                }
            }
        } else {
            holder.tvDay.setBackgroundResource(0);
        }
    }

    @Override
    public int getItemCount() {
        return daysOfMonth != null ? daysOfMonth.size() : 0;
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvDay;
        LinearLayout layoutBanners; // Hộp chứa mới
        OnItemListener onItemListener;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            layoutBanners = itemView.findViewById(R.id.layoutBanners); // Ánh xạ hộp chứa
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (!tvDay.getText().toString().isEmpty()) {
                selectedPosition = getAdapterPosition();
                notifyDataSetChanged();

                String d = tvDay.getText().toString();
                String formattedDate = (d.length() == 1 ? "0" + d : d) + "/" + monthYear;
                onItemListener.onItemClick(formattedDate);
            }
        }
    }
}