package com.example.mynote.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.activities.FolderDetailActivity;
import com.example.mynote.models.ColorOption;
import com.example.mynote.models.Folder;
import com.example.mynote.models.Note;
import com.example.mynote.firebase.FirebaseTrashHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private final Context context;
    private final List<Folder> folderList;
    private final String uid;

    private boolean isEditMode = false;
    private int openedDeletePosition = -1;

    private float downX = 0f;
    private float downY = 0f;
    private float startTranslationX = 0f;
    private static final float MAX_SWIPE_DISTANCE = 135f;
    private static final float SWIPE_TRIGGER = 60f;

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;

        if (!editMode) {
            openedDeletePosition = -1;
        }

        notifyDataSetChanged();
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

    public FolderAdapter(Context context, List<Folder> folderList) {
        this.context = context;
        this.folderList = folderList;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            this.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            throw new IllegalStateException("User chưa đăng nhập");
        }
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
        String folderColor = folder.getColor();

        if ("all".equals(folder.getFolderId())) {
            holder.imgFolder.setColorFilter(Color.parseColor("#007AFF"));
        } else if (folderColor != null && !folderColor.isEmpty()) {
            holder.imgFolder.setColorFilter(Color.parseColor(folderColor));
        } else {
            holder.imgFolder.setColorFilter(Color.parseColor("#007AFF"));
        }

        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);

        if ("all".equals(folder.getFolderId())) {
            notesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    holder.tvNoteCount.setText("(" + count + ")");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            notesRef.orderByChild("folderId").equalTo(folder.getFolderId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long count = snapshot.getChildrenCount();
                            holder.tvNoteCount.setText("(" + count + ")");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }

        boolean isAllFolder = "all".equals(folder.getFolderId());

        if (isEditMode && !isAllFolder) {
            holder.btnMinus.setVisibility(View.VISIBLE);
            holder.imgArrow.setVisibility(View.GONE);
        } else {
            holder.btnMinus.setVisibility(View.GONE);

            if (openedDeletePosition == position && !isAllFolder) {
                holder.imgArrow.setVisibility(View.GONE);
            } else {
                holder.imgArrow.setVisibility(View.VISIBLE);
            }
        }

        if (openedDeletePosition == position && !isAllFolder) {
            holder.layoutDelete.setVisibility(View.VISIBLE);
            holder.cardContent.setTranslationX(-MAX_SWIPE_DISTANCE);
        } else {
            holder.layoutDelete.setVisibility(View.GONE);
            holder.cardContent.setTranslationX(0f);
        }

        holder.btnMinus.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            if (openedDeletePosition == currentPosition) {
                openedDeletePosition = -1;
            } else {
                openedDeletePosition = currentPosition;
            }
            notifyDataSetChanged();
        });

        holder.tvDelete.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Folder currentFolder = folderList.get(currentPosition);
            showDeleteFolderDialog(currentFolder, currentPosition);
        });

        holder.cardContent.setOnTouchListener((v, event) -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return false;

            Folder currentFolder = folderList.get(currentPosition);

            if ("all".equals(currentFolder.getFolderId())) {
                return false;
            }

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    downY = event.getRawY();
                    startTranslationX = holder.cardContent.getTranslationX();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getRawX() - downX;
                    float deltaY = event.getRawY() - downY;

                    // Chỉ xử lý nếu vuốt ngang rõ ràng
                    if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 20) {
                        float newTranslationX = startTranslationX + deltaX;

                        if (newTranslationX < -MAX_SWIPE_DISTANCE) {
                            newTranslationX = -MAX_SWIPE_DISTANCE;
                        }

                        if (newTranslationX > 0) {
                            newTranslationX = 0;
                        }

                        holder.cardContent.setTranslationX(newTranslationX);

                        if (newTranslationX < 0) {
                            holder.layoutDelete.setVisibility(View.VISIBLE);
                            holder.imgArrow.setVisibility(View.GONE);
                        } else {
                            holder.layoutDelete.setVisibility(View.GONE);

                            if (isEditMode) {
                                holder.imgArrow.setVisibility(View.GONE);
                            } else {
                                holder.imgArrow.setVisibility(View.VISIBLE);
                            }
                        }

                        return true;
                    }
                    return false;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float totalDeltaX = event.getRawX() - downX;
                    float totalDeltaY = event.getRawY() - downY;

                    // Nếu không phải vuốt ngang thì coi như click bình thường
                    if (!(Math.abs(totalDeltaX) > Math.abs(totalDeltaY) && Math.abs(totalDeltaX) > 20)) {
                        handleFolderClick(holder);
                        return true;
                    }

                    float endTranslationX = holder.cardContent.getTranslationX();

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

        holder.itemView.setOnClickListener(v -> handleFolderClick(holder));
        holder.cardContent.setOnClickListener(v -> handleFolderClick(holder));

        holder.itemView.setOnLongClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return true;

            Folder currentFolder = folderList.get(currentPosition);

            if (!"all".equals(currentFolder.getFolderId())) {
                showEditFolderDialog(currentFolder);
            }
            return true;
        });
    }

    private void handleFolderClick(FolderViewHolder holder) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;

        Folder currentFolder = folderList.get(currentPosition);

        // Nếu đang mở ô Xóa thì đóng lại trước
        if (openedDeletePosition != -1) {
            openedDeletePosition = -1;
            notifyDataSetChanged();
            return;
        }

        // Nếu đang ở chế độ sửa thì bấm vào thư mục để đổi tên
        if (isEditMode) {
            if (!"all".equals(currentFolder.getFolderId())) {
                showEditFolderDialog(currentFolder);
            }
            return;
        }

        Intent intent = new Intent(context, FolderDetailActivity.class);
        intent.putExtra("folderId", currentFolder.getFolderId());
        intent.putExtra("folderName", currentFolder.getFolderName());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    private void showDeleteFolderDialog(Folder folder, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_folder, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView btnCancelDelete = view.findViewById(R.id.btnCancelDelete);
        TextView btnConfirmDelete = view.findViewById(R.id.btnConfirmDelete);

        btnCancelDelete.setOnClickListener(v -> dialog.dismiss());

        btnConfirmDelete.setOnClickListener(v -> {
            deleteFolder(folder, position);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void deleteFolder(Folder folder, int position) {
        if ("all".equals(folder.getFolderId())) {
            Toast.makeText(context, "Không thể xóa mục Tất cả ghi chú", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseTrashHelper.moveFolderToTrash(folder, new FirebaseTrashHelper.TrashCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Đã chuyển thư mục vào thùng rác", Toast.LENGTH_SHORT).show();
                openedDeletePosition = -1;
                notifyDataSetChanged();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(context, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditFolderDialog(Folder folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_folder, null);
        builder.setView(view);
        ImageView imgSelectedColorDot = view.findViewById(R.id.imgSelectedColorDot);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        EditText edtEditFolderName = view.findViewById(R.id.edtEditFolderName);
        TextView tvSelectedColor = view.findViewById(R.id.tvSelectedColor);
        LinearLayout layoutColor = view.findViewById(R.id.layoutColor);
        TextView btnCancelEdit = view.findViewById(R.id.btnCancelEdit);
        TextView btnSaveEdit = view.findViewById(R.id.btnSaveEdit);

        tvDialogTitle.setText("Tên thư mục");
        edtEditFolderName.setText(folder.getFolderName());
        edtEditFolderName.setSelection(folder.getFolderName().length());

        String currentColor = folder.getColor();
        if (currentColor == null || currentColor.isEmpty()) {
            currentColor = "#FFFFFF";
        }

        final String[] colorNames = {
                "Trắng", "Đỏ", "Cam", "Vàng", "Vàng nhạt", "Xanh nhạt",
                "Xanh bạc hà", "Xanh lá cây", "Xanh dương", "Xanh nước biển",
                "Xanh tím than", "Tím đậm", "Tím", "Tím nhạt", "Hồng nhạt", "Hồng"
        };

        final String[] colorHexes = {
                "#E5E5EA", "#FF3B30", "#FF9500", "#FFCC00", "#FFF59D", "#DCE775",
                "#80CBC4", "#34C759", "#00C7BE", "#007AFF",
                "#5856D6", "#AF52DE", "#D8B4E2", "#FFD1DC", "#FFB6C1", "#FF2D55"
        };

        List<ColorOption> colorOptions = new ArrayList<>();
        for (int i = 0; i < colorNames.length; i++) {
            colorOptions.add(new ColorOption(colorNames[i], colorHexes[i]));
        }

        final String[] selectedColorHex = { currentColor };

        int selectedIndex = 0;
        for (int i = 0; i < colorHexes.length; i++) {
            if (colorHexes[i].equalsIgnoreCase(currentColor)) {
                selectedIndex = i;
                break;
            }
        }
        tvSelectedColor.setText(colorNames[selectedIndex]);
        if (imgSelectedColorDot != null) {
            imgSelectedColorDot.setColorFilter(Color.parseColor(currentColor));
        }
        layoutColor.setOnClickListener(v -> {
            AlertDialog.Builder colorDialogBuilder =
                    new AlertDialog.Builder(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

            View colorView = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null);
            colorDialogBuilder.setView(colorView);

            AlertDialog colorDialog = colorDialogBuilder.create();

            if (colorDialog.getWindow() != null) {
                colorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            ImageView btnBackColorPicker = colorView.findViewById(R.id.btnBackColorPicker);
            androidx.recyclerview.widget.RecyclerView recyclerColorPicker =
                    colorView.findViewById(R.id.recyclerColorPicker);

            recyclerColorPicker.setLayoutManager(
                    new androidx.recyclerview.widget.LinearLayoutManager(context)
            );

            recyclerColorPicker.setAdapter(
                    new com.example.mynote.adapters.ColorPickerAdapter(
                            colorOptions,
                            selectedColorHex[0],
                            colorOption -> {
                                selectedColorHex[0] = colorOption.getHex();
                                tvSelectedColor.setText(colorOption.getName());

                                if (imgSelectedColorDot != null) {
                                    imgSelectedColorDot.setColorFilter(Color.parseColor(colorOption.getHex()));
                                }

                                folder.setColor(colorOption.getHex());
                                notifyDataSetChanged();

                                colorDialog.dismiss();
                            }
                    )
            );
            btnBackColorPicker.setOnClickListener(backView -> colorDialog.dismiss());

            colorDialog.show();
        });

        btnCancelEdit.setOnClickListener(v -> dialog.dismiss());

        btnSaveEdit.setOnClickListener(v -> {
            String newName = edtEditFolderName.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(context, "Tên thư mục không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference folderRef = FirebaseDatabase.getInstance()
                    .getReference("folders")
                    .child(uid)
                    .child(folder.getFolderId());

            folder.setFolderName(newName);
            folder.setColor(selectedColorHex[0]);

            folderRef.setValue(folder).addOnSuccessListener(unused -> {
                Toast.makeText(context, "Đã cập nhật thư mục", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
                dialog.dismiss();
            }).addOnFailureListener(e ->
                    Toast.makeText(context, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        dialog.show();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderName, tvNoteCount, tvDelete;
        ImageView btnMinus, imgArrow, imgFolder;
        LinearLayout layoutDelete;
        CardView cardContent;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderName = itemView.findViewById(R.id.tvFolderName);
            tvNoteCount = itemView.findViewById(R.id.tvNoteCount);
            tvDelete = itemView.findViewById(R.id.tvDelete);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            imgArrow = itemView.findViewById(R.id.imgArrow);
            layoutDelete = itemView.findViewById(R.id.layoutDelete);
            cardContent = itemView.findViewById(R.id.cardContent);
            imgFolder = itemView.findViewById(R.id.imgFolder);
        }
    }
}
