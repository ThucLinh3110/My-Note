    package com.example.mynote.activities;

    import android.content.Context;
    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.text.TextUtils;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.TextView;

    import android.widget.Toast;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;
    import android.graphics.Typeface;
    import android.text.Editable;
    import android.text.Spanned;
    import android.text.TextWatcher;
    import android.text.style.StyleSpan;
    import android.text.style.UnderlineSpan;
    import android.graphics.drawable.ColorDrawable;
    import android.widget.PopupWindow;
    import android.view.Gravity;
    import com.example.mynote.R;
    import com.example.mynote.models.Note;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;

    import com.bumptech.glide.Glide;

    import java.io.ByteArrayOutputStream;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.Locale;
    import android.graphics.Color;
    import android.widget.LinearLayout;
    import android.widget.GridLayout;
    import android.widget.PopupMenu;
    import android.view.KeyEvent;
    import android.view.inputmethod.EditorInfo;
    import com.bumptech.glide.Glide;
    import android.database.Cursor;
    import android.provider.OpenableColumns;

    import org.json.JSONObject;

    import java.io.IOException;

    import okhttp3.Call;
    import okhttp3.Callback;
    import okhttp3.MediaType;
    import okhttp3.MultipartBody;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.RequestBody;
    import okhttp3.Response;

    import android.view.Gravity;
    import android.view.View;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TableLayout;
    import android.widget.TableRow;

    import java.util.HashMap;
    import java.util.Map;

    public class AddNoteActivity extends AppCompatActivity {
        private EditText edtTitle, edtContent;
        private TextView btnSave, txtDateTime;
        private ImageView btnBack, imgGallery, imgCamera, imgUndo, imgPreview, btnEdit;
        private ImageView btnTable;
        private boolean isViewMode = false;
        private TextView btnTextStyle;
        //private ImageView btnBold, btnItalic, btnUnderline;
        private ImageView btnList;
        private boolean isBulletOn = false;
        private boolean isAutoBulletEditing = false;

        private boolean isBoldOn = false;
        private boolean isItalicOn = false;
        private boolean isUnderlineOn = false;

        private boolean isFormattingText = false;
        private int lastTextLength = 0;
        private DatabaseReference noteRef;
        private String mode, noteId, folderId, folderName;
        private long creationTime;
        private String selectedDate = "";

        private static final int REQUEST_CAMERA = 100;
        private static final int REQUEST_GALLERY = 200;

        // ĐÁNH BAY URI: Chỉ xài duy nhất 1 biến Byte này để trị dứt điểm máy ảo
        private byte[] imageBytes;
        private String currentImageUrl = "";
        private ArrayList<byte[]> imageBytesList = new ArrayList<>();
        private ArrayList<String> currentImageUrls = new ArrayList<>();

        private ArrayList<ArrayList<String>> tableData = new ArrayList<>();
        private int selectedRow = -1;
        private int selectedCol = -1;

        private LinearLayout layoutSelectFolder;
        private LinearLayout layoutTableContainer;

        private int tableRows = 3;
        private int tableCols = 2;
        private ArrayList<EditText> tableCells = new ArrayList<>();
        private boolean hasTable = false;

        private TextView tvFolderName;
        private ImageView imgFolderIcon;

        private static final int REQUEST_SELECT_FOLDER = 300;
        private String folderColor;
        private static final String CLOUD_NAME = "dpt47x8jy";
        private static final String UPLOAD_PRESET = "mynote_unsigned";

        private String selectedFolderColor = "#007AFF";
        private ImageView btnAttach;
        private static final int REQUEST_ATTACH_FILE = 500;
        private Uri attachedFileUri = null;
        private String attachedFileName = "";

        private String attachedFileUrl = "";
        private String attachedFileType = "";
        private interface OnImageUploadListener {
            void onSuccess(String imageUrl);
            void onFailure(String error);
        }
        private void uploadImageToCloudinary(byte[] imageBytes, OnImageUploadListener listener) {
            String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

            OkHttpClient client = new OkHttpClient();

            RequestBody fileBody = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "note.jpg", fileBody)
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> listener.onFailure(e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Upload failed";
                        runOnUiThread(() -> listener.onFailure(errorBody));
                        return;
                    }

                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        String imageUrl = jsonObject.getString("secure_url");
                        runOnUiThread(() -> listener.onSuccess(imageUrl));
                    } catch (Exception e) {
                        runOnUiThread(() -> listener.onFailure(e.getMessage()));
                    }
                }
            });
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_add_note);

            //storageReference = FirebaseStorage.getInstance().getReference("note_images");

            edtTitle = findViewById(R.id.edtTitle);
            edtContent = findViewById(R.id.edtContent);
            btnSave = findViewById(R.id.btnSave);
            btnBack = findViewById(R.id.btnBack);
            btnEdit = findViewById(R.id.btnEdit);
            txtDateTime = findViewById(R.id.txtDateTime);
            imgGallery = findViewById(R.id.imgGallery);
            imgPreview = findViewById(R.id.imgPreview);
            edtContent = findViewById(R.id.edtContent);
            /*btnBold = findViewById(R.id.btnBold);
            btnItalic = findViewById(R.id.btnItalic);
            btnUnderline = findViewById(R.id.btnUnderline);*/
            btnTextStyle = findViewById(R.id.btnTextStyle);
            btnTable = findViewById(R.id.btnTable);
            layoutTableContainer = findViewById(R.id.layoutTableContainer);

            btnTable = findViewById(R.id.btnTable);
            layoutTableContainer = findViewById(R.id.layoutTableContainer);

            if (btnTable != null) {
                btnTable.setOnClickListener(v -> {
                    showTable();
                });
            }

            btnList = findViewById(R.id.btnList);

            btnList.setOnClickListener(v -> {
                isBulletOn = !isBulletOn;
                updateBulletUI();

                if (isBulletOn) {
                    insertBulletAtCurrentLine();
                }
            });

            btnAttach = findViewById(R.id.btnAttach);

            if (btnAttach != null) {
                btnAttach.setOnClickListener(v -> openFilePicker());
            }
            setupAutoBulletWatcher();

            mode = getIntent().getStringExtra("mode");
            noteId = getIntent().getStringExtra("noteId");
            folderId = getIntent().getStringExtra("folderId");
            folderName = getIntent().getStringExtra("folderName");
            folderColor = getIntent().getStringExtra("folderColor");
            selectedDate = getIntent().getStringExtra("SELECTED_DATE");

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            noteRef = FirebaseDatabase.getInstance().getReference("notes").child(uid);

            layoutSelectFolder = findViewById(R.id.layoutSelectFolder);
            tvFolderName = findViewById(R.id.tvFolderName);
            imgFolderIcon = findViewById(R.id.imgFolderIcon);


            if (tvFolderName != null) {
                if (folderName != null && !folderName.trim().isEmpty()) {
                    tvFolderName.setText(folderName);
                } else {
                    tvFolderName.setText("Tất cả ghi chú");
                }
            }
            if (imgFolderIcon != null) {
                if (folderColor == null || folderColor.isEmpty()) {
                    folderColor = "#007AFF"; // fallback
                }

                try {
                    imgFolderIcon.setColorFilter(Color.parseColor(folderColor));
                } catch (Exception e) {
                    imgFolderIcon.setColorFilter(Color.parseColor("#007AFF"));
                }
            }
            setupFormattingButtons();
            setupContentFormattingWatcher();
            updateFormatButtonsUI();
            setupUI();
            setupFolderSelector();


        }

        /*private void setupFormattingButtons() {
            if (btnBold != null) {
                btnBold.setOnClickListener(v -> {
                    int start = edtContent.getSelectionStart();
                    int end = edtContent.getSelectionEnd();

                    if (start >= 0 && end > start) {
                        toggleBoldForSelection(start, end);
                    } else {
                        isBoldOn = !isBoldOn;
                        updateFormatButtonsUI();
                    }
                });
            }

            if (btnItalic != null) {
                btnItalic.setOnClickListener(v -> {
                    int start = edtContent.getSelectionStart();
                    int end = edtContent.getSelectionEnd();

                    if (start >= 0 && end > start) {
                        toggleItalicForSelection(start, end);
                    } else {
                        isItalicOn = !isItalicOn;
                        updateFormatButtonsUI();
                    }
                });
            }

            if (btnUnderline != null) {
                btnUnderline.setOnClickListener(v -> {
                    int start = edtContent.getSelectionStart();
                    int end = edtContent.getSelectionEnd();

                    if (start >= 0 && end > start) {
                        toggleUnderlineForSelection(start, end);
                    } else {
                        isUnderlineOn = !isUnderlineOn;
                        updateFormatButtonsUI();
                    }
                });
            }
        }*/

        private void showTable() {
            hasTable = true;

            if (layoutTableContainer == null) return;

            layoutTableContainer.setVisibility(View.VISIBLE);

            // Nếu bảng chưa có thì mới render lần đầu
            if (layoutTableContainer.getChildCount() == 0) {
                initTableData();
                renderTable();
            }
        }

        private void initTableData() {
            tableData.clear();

            tableRows = 3;
            tableCols = 3;

            for (int r = 0; r < tableRows; r++) {
                ArrayList<String> row = new ArrayList<>();

                for (int c = 0; c < tableCols; c++) {
                    row.add("");
                }

                tableData.add(row);
            }
        }
        private void setupFormattingButtons() {
            if (btnTextStyle != null) {
                btnTextStyle.setOnClickListener(v -> showTextStyleMenu(v));
            }
        }

        private void showTableMenu(View anchor) {
            PopupMenu popup = new PopupMenu(this, anchor);

            popup.getMenu().add("Thêm hàng phía trên");
            popup.getMenu().add("Thêm hàng phía dưới");
            popup.getMenu().add("Thêm cột bên trái");
            popup.getMenu().add("Thêm cột bên phải");
            popup.getMenu().add("Xóa hàng này");
            popup.getMenu().add("Xóa cột này");

            popup.setOnMenuItemClickListener(item -> {
                saveCurrentTableData();

                String title = item.getTitle().toString();

                if (selectedRow < 0) selectedRow = tableRows - 1;
                if (selectedCol < 0) selectedCol = tableCols - 1;

                if (title.equals("Thêm hàng phía trên")) {
                    insertRow(selectedRow);

                } else if (title.equals("Thêm hàng phía dưới")) {
                    insertRow(selectedRow + 1);

                } else if (title.equals("Thêm cột bên trái")) {
                    insertCol(selectedCol);

                } else if (title.equals("Thêm cột bên phải")) {
                    insertCol(selectedCol + 1);

                } else if (title.equals("Xóa hàng này") && tableRows > 1) {
                    tableData.remove(selectedRow);
                    tableRows--;

                } else if (title.equals("Xóa cột này") && tableCols > 1) {
                    for (int r = 0; r < tableData.size(); r++) {
                        tableData.get(r).remove(selectedCol);
                    }
                    tableCols--;
                }

                renderTable();
                return true;
            });

            popup.show();
        }

        private void saveCurrentTableData() {
            tableData.clear();

            for (int r = 0; r < tableRows; r++) {
                ArrayList<String> rowData = new ArrayList<>();

                for (int c = 0; c < tableCols; c++) {
                    int index = r * tableCols + c;

                    if (index < tableCells.size()) {
                        rowData.add(tableCells.get(index).getText().toString());
                    } else {
                        rowData.add("");
                    }
                }

                tableData.add(rowData);
            }
        }

        private void insertRow(int rowIndex) {
            ArrayList<String> newRow = new ArrayList<>();

            for (int c = 0; c < tableCols; c++) {
                newRow.add("");
            }

            tableData.add(rowIndex, newRow);
            tableRows++;
        }

        private void insertCol(int colIndex) {
            for (int r = 0; r < tableData.size(); r++) {
                tableData.get(r).add(colIndex, "");
            }

            tableCols++;
        }

        private void renderTable() {
            layoutTableContainer.removeAllViews();
            tableCells.clear();

            TextView menu = new TextView(this);
            menu.setText("•••");
            menu.setTextSize(20);
            menu.setGravity(Gravity.CENTER);
            menu.setTextColor(Color.parseColor("#6F7780"));
            menu.setOnClickListener(v -> showTableMenu(v));
            layoutTableContainer.addView(menu);

            GridLayout grid = new GridLayout(this);
            grid.setColumnCount(tableCols);
            grid.setRowCount(tableRows);

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int cellWidth = (screenWidth - dp(40)) / tableCols;

            for (int r = 0; r < tableRows; r++) {
                for (int c = 0; c < tableCols; c++) {

                    final int row = r;
                    final int col = c;

                    EditText cell = new EditText(this);

                    cell.setTextSize(14);
                    cell.setSingleLine(true);
                    cell.setMinHeight(dp(44));
                    cell.setBackgroundResource(R.drawable.bg_table_cell);
                    cell.setPadding(dp(8), 0, dp(8), 0);
                    cell.setTextColor(Color.parseColor("#333333"));
                    cell.setHint("");

                    // Hiện lại dữ liệu cũ sau khi render
                    if (row < tableData.size() && col < tableData.get(row).size()) {
                        cell.setText(tableData.get(row).get(col));
                    }

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = cellWidth;
                    params.height = dp(44);
                    cell.setLayoutParams(params);

                    final int index = tableCells.size();

                    // Lưu vị trí cell đang chọn
                    cell.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            selectedRow = row;
                            selectedCol = col;
                        }
                    });

                    cell.setOnClickListener(v -> {
                        selectedRow = row;
                        selectedCol = col;
                    });

                    // Enter: sang ô tiếp theo, nếu ô cuối thì thêm hàng mới
                    cell.setSingleLine(true);
                    cell.setImeOptions(EditorInfo.IME_ACTION_NEXT);

                    cell.setOnEditorActionListener((v, actionId, event) -> {
                        boolean isEnter =
                                actionId == EditorInfo.IME_ACTION_NEXT ||
                                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);

                        if (isEnter) {
                            saveCurrentTableData();

                            int nextIndex = index + 1;

                            if (nextIndex < tableCells.size()) {
                                tableCells.get(nextIndex).requestFocus();
                                tableCells.get(nextIndex)
                                        .setSelection(tableCells.get(nextIndex).getText().length());
                            } else {
                                insertRow(tableRows);
                                renderTable();

                                int newIndex = index + 1;
                                if (newIndex < tableCells.size()) {
                                    tableCells.get(newIndex).requestFocus();
                                }
                            }

                            return true;
                        }

                        return false;
                    });

                    tableCells.add(cell);
                    grid.addView(cell);
                }
            }

            layoutTableContainer.addView(grid);
        }

        private String getTableTextForSave() {
            StringBuilder builder = new StringBuilder();

            builder.append("[TABLE]\n");

            for (int r = 0; r < tableRows; r++) {
                for (int c = 0; c < tableCols; c++) {
                    int index = r * tableCols + c;

                    String cellText = "";
                    if (index < tableCells.size()) {
                        cellText = tableCells.get(index).getText().toString().trim();
                    }

                    builder.append(cellText);

                    if (c < tableCols - 1) {
                        builder.append(" | ");
                    }
                }

                builder.append("\n");
            }

            builder.append("[/TABLE]");

            return builder.toString();
        }

        private void moveToNextTableCell(int currentIndex) {
            int nextIndex = currentIndex + 1;

            if (nextIndex < tableCells.size()) {
                tableCells.get(nextIndex).requestFocus();
            }
        }
        private void setupAutoBulletWatcher() {
            if (edtContent == null) return;

            edtContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!isBulletOn || isAutoBulletEditing) return;

                    int cursor = edtContent.getSelectionStart();
                    if (cursor <= 0 || cursor > editable.length()) return;

                    if (editable.charAt(cursor - 1) == '\n') {
                        isAutoBulletEditing = true;
                        editable.insert(cursor, "• ");
                        edtContent.setSelection(cursor + 2);
                        isAutoBulletEditing = false;
                    }
                }
            });
        }

        private void updateBulletUI() {
            if (btnList == null) return;

            btnList.setColorFilter(Color.parseColor(
                    isBulletOn ? "#007AFF" : "#6F7780"
            ));

            btnList.setBackgroundResource(
                    isBulletOn ? R.drawable.bg_toolbar_active
                            : R.drawable.bg_toolbar_inactive
            );
        }
        private void insertBulletAtCurrentLine() {
            if (edtContent == null) return;

            Editable editable = edtContent.getText();
            int cursor = edtContent.getSelectionStart();

            if (cursor < 0) cursor = editable.length();

            int lineStart = cursor;
            while (lineStart > 0 && editable.charAt(lineStart - 1) != '\n') {
                lineStart--;
            }

            String line = editable.subSequence(lineStart, cursor).toString();

            if (!line.startsWith("• ")) {
                editable.insert(lineStart, "• ");
                edtContent.setSelection(cursor + 2);
            }
        }

        private int dp(int value) {
            return (int) (value * getResources().getDisplayMetrics().density);
        }
        private void showTextStyleMenu(View anchor) {
            PopupWindow popupWindow = new PopupWindow(this);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(10, 8, 10, 8);
            layout.setBackgroundColor(Color.WHITE);

            ImageView iconBold = createStyleIcon(R.drawable.ic_bold, isBoldOn);
            ImageView iconItalic = createStyleIcon(R.drawable.ic_italic, isItalicOn);
            ImageView iconUnderline = createStyleIcon(R.drawable.ic_underline, isUnderlineOn);

            iconBold.setOnClickListener(v -> {
                applyTextStyle("bold");
                popupWindow.dismiss();
            });

            iconItalic.setOnClickListener(v -> {
                applyTextStyle("italic");
                popupWindow.dismiss();
            });

            iconUnderline.setOnClickListener(v -> {
                applyTextStyle("underline");
                popupWindow.dismiss();
            });

            layout.addView(iconBold);
            layout.addView(iconItalic);
            layout.addView(iconUnderline);

            popupWindow.setContentView(layout);
            popupWindow.setWidth(dp(150));
            popupWindow.setHeight(dp(52));
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);
            popupWindow.setElevation(dp(4));

            popupWindow.showAsDropDown(anchor, -dp(55), -dp(2));
        }

        private ImageView createStyleIcon(int drawableRes, boolean active) {
            ImageView icon = new ImageView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(42), dp(42));
            params.setMargins(dp(4), 0, dp(4), 0);
            icon.setLayoutParams(params);

            icon.setPadding(dp(9), dp(9), dp(9), dp(9));
            icon.setImageResource(drawableRes);
            icon.setColorFilter(active ? Color.parseColor("#007AFF") : Color.parseColor("#6F7780"));
            icon.setBackgroundResource(active ? R.drawable.bg_toolbar_active : R.drawable.bg_toolbar_inactive);
            icon.setClickable(true);
            icon.setFocusable(true);

            return icon;
        }

        private void applyTextStyle(String type) {
            int start = edtContent.getSelectionStart();
            int end = edtContent.getSelectionEnd();

            if ("bold".equals(type)) {
                if (start >= 0 && end > start) {
                    toggleBoldForSelection(start, end);
                } else {
                    isBoldOn = !isBoldOn;
                }

            } else if ("italic".equals(type)) {
                if (start >= 0 && end > start) {
                    toggleItalicForSelection(start, end);
                } else {
                    isItalicOn = !isItalicOn;
                }

            } else if ("underline".equals(type)) {
                if (start >= 0 && end > start) {
                    toggleUnderlineForSelection(start, end);
                } else {
                    isUnderlineOn = !isUnderlineOn;
                }
            }

            updateFormatButtonsUI();
        }
        private void setupContentFormattingWatcher() {
            if (edtContent == null) return;

            edtContent.addTextChangedListener(new TextWatcher() {
                private int startChanged;
                private int countChanged;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (!isFormattingText) {
                        lastTextLength = s.length();
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    startChanged = start;
                    countChanged = count;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (isFormattingText) return;
                    if (countChanged <= 0) return;

                    int start = startChanged;
                    int end = startChanged + countChanged;

                    if (start < 0 || end > s.length() || start >= end) return;

                    isFormattingText = true;

                    if (isBoldOn) {
                        s.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    if (isItalicOn) {
                        s.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    if (isUnderlineOn) {
                        s.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    isFormattingText = false;
                }
            });
        }

        /*private void updateFormatButtonsUI() {
            if (btnBold != null) {
                btnBold.setBackgroundResource(isBoldOn
                        ? R.drawable.bg_toolbar_active
                        : R.drawable.bg_toolbar_inactive);
            }

            if (btnItalic != null) {
                btnItalic.setBackgroundResource(isItalicOn
                        ? R.drawable.bg_toolbar_active
                        : R.drawable.bg_toolbar_inactive);
            }

            if (btnUnderline != null) {
                btnUnderline.setBackgroundResource(isUnderlineOn
                        ? R.drawable.bg_toolbar_active
                        : R.drawable.bg_toolbar_inactive);
            }
        }*/
        private void updateFormatButtonsUI() {
            if (btnTextStyle != null) {
                if (isBoldOn || isItalicOn || isUnderlineOn) {
                    btnTextStyle.setTextColor(Color.parseColor("#007AFF"));
                    btnTextStyle.setBackgroundResource(R.drawable.bg_toolbar_active);
                } else {
                    btnTextStyle.setTextColor(Color.parseColor("#6F7780"));
                    btnTextStyle.setBackgroundResource(R.drawable.bg_toolbar_inactive);
                }
            }
        }

        private void toggleBoldForSelection(int start, int end) {
            Editable editable = edtContent.getText();
            StyleSpan[] spans = editable.getSpans(start, end, StyleSpan.class);

            boolean removed = false;

            for (StyleSpan span : spans) {
                if (span.getStyle() == Typeface.BOLD) {
                    int spanStart = editable.getSpanStart(span);
                    int spanEnd = editable.getSpanEnd(span);

                    if (spanStart <= start && spanEnd >= end) {
                        editable.removeSpan(span);
                        removed = true;
                    }
                }
            }

            if (!removed) {
                editable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        private void toggleItalicForSelection(int start, int end) {
            Editable editable = edtContent.getText();
            StyleSpan[] spans = editable.getSpans(start, end, StyleSpan.class);

            boolean removed = false;

            for (StyleSpan span : spans) {
                if (span.getStyle() == Typeface.ITALIC) {
                    int spanStart = editable.getSpanStart(span);
                    int spanEnd = editable.getSpanEnd(span);

                    if (spanStart <= start && spanEnd >= end) {
                        editable.removeSpan(span);
                        removed = true;
                    }
                }
            }

            if (!removed) {
                editable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        private void toggleUnderlineForSelection(int start, int end) {
            Editable editable = edtContent.getText();
            UnderlineSpan[] spans = editable.getSpans(start, end, UnderlineSpan.class);

            boolean removed = false;

            for (UnderlineSpan span : spans) {
                int spanStart = editable.getSpanStart(span);
                int spanEnd = editable.getSpanEnd(span);

                if (spanStart <= start && spanEnd >= end) {
                    editable.removeSpan(span);
                    removed = true;
                }
            }

            if (!removed) {
                editable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        private void setupUI() {
            if ("edit".equals(mode)) {
                if (edtTitle != null) edtTitle.setText(getIntent().getStringExtra("title"));
                if (edtContent != null) edtContent.setText(getIntent().getStringExtra("content"));

                if ("edit".equals(mode)) {
                    if (edtTitle != null) edtTitle.setText(getIntent().getStringExtra("title"));
                    if (edtContent != null) edtContent.setText(getIntent().getStringExtra("content"));

                    currentImageUrl = getIntent().getStringExtra("imageUrl");
                    if (currentImageUrl == null) currentImageUrl = "";

                    if (imgPreview != null && !currentImageUrl.isEmpty()) {
                        imgPreview.setVisibility(View.VISIBLE);
                        Glide.with(this).load(currentImageUrl).into(imgPreview);
                    }

                    creationTime = getIntent().getLongExtra("createdAt", System.currentTimeMillis());

                    String currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                    if (txtDateTime != null) txtDateTime.setText(currentDateTime);
                }
            } else {
                creationTime = System.currentTimeMillis();
                if (selectedDate == null || selectedDate.isEmpty()) {
                    selectedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                }
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                if (txtDateTime != null) txtDateTime.setText(selectedDate + "  " + currentTime);
            }

            if (btnBack != null) btnBack.setOnClickListener(v -> finish());
            if (btnSave != null) btnSave.setOnClickListener(v -> saveNote());

            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> setEditMode(true));
            }

            if (imgGallery != null) {
                imgGallery.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_GALLERY);
                    } catch (Exception e) {
                        Toast.makeText(this, "Không thể mở Thư viện ảnh!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if ("edit".equals(mode)) {
                setEditMode(false);
            } else {
                setEditMode(true);
            }
        }

        private void setupFolderSelector() {
            if (folderId == null || folderId.trim().isEmpty()) {
                folderId = "all";
            }

            if (tvFolderName != null) {
                if (folderName != null && !folderName.trim().isEmpty()) {
                    tvFolderName.setText(folderName);
                } else {
                    tvFolderName.setText("Tất cả ghi chú");
                }
            }

            if (layoutSelectFolder != null) {
                layoutSelectFolder.setOnClickListener(v -> {
                    Intent intent = new Intent(AddNoteActivity.this, SelectFolderActivity.class);
                    startActivityForResult(intent, REQUEST_SELECT_FOLDER);
                });
            }
        }

        private void updateSelectedFolderUI(String folderName, String folderColor) {
            tvFolderName.setText(folderName);

            if (folderColor == null || folderColor.isEmpty()) {
                folderColor = "#007AFF";
            }

            try {
                imgFolderIcon.setColorFilter(Color.parseColor(folderColor));
            } catch (Exception e) {
                imgFolderIcon.setColorFilter(Color.parseColor("#007AFF"));
            }
        }


        private void saveNote() {
            String title = (edtTitle != null) ? edtTitle.getText().toString().trim() : "";
            String content = (edtContent != null) ? edtContent.getText().toString().trim() : "";

            final String finalTitle = title;
            final String finalContent = content;

            if (TextUtils.isEmpty(finalTitle)
                    && TextUtils.isEmpty(finalContent)
                    && !hasTable
                    && attachedFileUri == null
                    && imageBytes == null
                    && (currentImageUrl == null || currentImageUrl.isEmpty())) {

                Toast.makeText(this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Đang lưu ghi chú...", Toast.LENGTH_SHORT).show();

            if (imageBytes != null) {
                uploadImageToCloudinary(imageBytes, new OnImageUploadListener() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        currentImageUrl = imageUrl;

                        uploadFileToCloudinary(attachedFileUri, new OnFileUploadListener() {
                            @Override
                            public void onSuccess(String fileUrl) {
                                saveNoteToRealtimeDB(finalTitle, finalContent, currentImageUrl, fileUrl);
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(AddNoteActivity.this,
                                        "Upload file thất bại: " + error,
                                        Toast.LENGTH_LONG).show();

                                saveNoteToRealtimeDB(finalTitle, finalContent, currentImageUrl, "");
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(AddNoteActivity.this,
                                "Upload ảnh thất bại: " + error,
                                Toast.LENGTH_LONG).show();

                        uploadFileToCloudinary(attachedFileUri, new OnFileUploadListener() {
                            @Override
                            public void onSuccess(String fileUrl) {
                                saveNoteToRealtimeDB(finalTitle, finalContent, currentImageUrl, fileUrl);
                            }

                            @Override
                            public void onFailure(String error) {
                                saveNoteToRealtimeDB(finalTitle, finalContent, currentImageUrl, "");
                            }
                        });
                    }
                });

            } else {
                uploadFileToCloudinary(attachedFileUri, new OnFileUploadListener() {
                    @Override
                    public void onSuccess(String fileUrl) {
                        saveNoteToRealtimeDB(finalTitle, finalContent, currentImageUrl, fileUrl);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(AddNoteActivity.this,
                                "Upload file thất bại: " + error,
                                Toast.LENGTH_LONG).show();

                        saveNoteToRealtimeDB(finalTitle, finalContent, currentImageUrl, "");
                    }
                });
            }
        }

        private void saveNoteToRealtimeDB(String title, String content, String imageUrl, String fileUrl) {
            long now = System.currentTimeMillis();
            String dateStr;

            if ("edit".equals(mode)) {
                dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(new Date(now));
            } else {
                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(new Date(now));
                dateStr = selectedDate + " " + currentTime;
            }

            boolean hasImage = imageUrl != null && !imageUrl.isEmpty();

            Map<String, Object> noteMap = new HashMap<>();

            String id;

            if ("edit".equals(mode)) {
                id = noteId;
            } else {
                id = noteRef.push().getKey();
            }

            if (id == null) return;

            noteMap.put("noteId", id);
            noteMap.put("folderId", folderId);
            noteMap.put("folderName", folderName != null ? folderName : "");
            noteMap.put("title", title);
            noteMap.put("content", content);
            noteMap.put("date", dateStr);
            noteMap.put("createdAt", "edit".equals(mode) ? creationTime : now);
            noteMap.put("updatedAt", now);
            noteMap.put("hasImage", hasImage);
            noteMap.put("imageUrl", imageUrl != null ? imageUrl : "");
            noteMap.put("deleted", false);
            noteMap.put("deletedAt", 0);

            // File đính kèm
            noteMap.put("attachedFileName", attachedFileName != null ? attachedFileName : "");
            noteMap.put("attachedFileUrl", fileUrl != null ? fileUrl : "");
            noteMap.put("attachedFileType", attachedFileType != null ? attachedFileType : "");

            // Bảng lưu dạng JSON/list
            noteMap.put("hasTable", hasTable);
            noteMap.put("tableRows", tableRows);
            noteMap.put("tableCols", tableCols);

            if (hasTable) {
                noteMap.put("tableData", getTableDataForFirebase());
            }

            noteRef.child(id).setValue(noteMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this,
                                "edit".equals(mode) ? "Đã cập nhật ghi chú!" : "Đã lưu ghi chú!",
                                Toast.LENGTH_SHORT).show();

                        mode = "edit";
                        noteId = id;
                        currentImageUrl = imageUrl != null ? imageUrl : "";
                        attachedFileUrl = fileUrl != null ? fileUrl : "";
                        imageBytes = null;

                        setEditMode(false);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                "Lưu ghi chú thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        }

        private void setEditMode(boolean enableEdit) {
            isViewMode = !enableEdit;

            if (edtTitle != null) edtTitle.setEnabled(enableEdit);
            if (edtContent != null) edtContent.setEnabled(enableEdit);

            if (layoutSelectFolder != null) layoutSelectFolder.setEnabled(enableEdit);

            // Luôn hiện đủ thanh công cụ
            if (imgGallery != null) imgGallery.setVisibility(View.VISIBLE);
            if (btnTextStyle != null) btnTextStyle.setVisibility(View.VISIBLE);
            if (btnList != null) btnList.setVisibility(View.VISIBLE);
            if (btnTable != null) btnTable.setVisibility(View.VISIBLE);
            if (btnAttach != null) btnAttach.setVisibility(View.VISIBLE);

            // Nút Lưu / Bút
            if (btnSave != null) btnSave.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
            if (btnEdit != null) btnEdit.setVisibility(enableEdit ? View.GONE : View.VISIBLE);

            if (edtTitle != null) {
                edtTitle.setFocusable(enableEdit);
                edtTitle.setFocusableInTouchMode(enableEdit);
                edtTitle.setCursorVisible(enableEdit);
            }

            if (edtContent != null) {
                edtContent.setFocusable(enableEdit);
                edtContent.setFocusableInTouchMode(enableEdit);
                edtContent.setCursorVisible(enableEdit);
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode != RESULT_OK || data == null) {
                return;
            }

            if (requestCode == REQUEST_ATTACH_FILE) {
                attachedFileUri = data.getData();

                if (attachedFileUri != null) {
                    attachedFileName = getFileName(attachedFileUri);
                    attachedFileType = getContentResolver().getType(attachedFileUri);

                    if (edtContent != null) {
                        edtContent.append("\n📎 " + attachedFileName + "\n");
                    }

                    Toast.makeText(this,
                            "Đã chọn file: " + attachedFileName,
                            Toast.LENGTH_SHORT).show();
                }

                return;
            }

            if (requestCode == REQUEST_SELECT_FOLDER) {
                folderId = data.getStringExtra("selectedFolderId");
                folderName = data.getStringExtra("selectedFolderName");
                String folderColor = data.getStringExtra("selectedFolderColor");

                if (folderId == null || folderId.trim().isEmpty()) {
                    folderId = "all_notes";
                }

                if (folderName == null || folderName.trim().isEmpty()) {
                    folderName = "Tất cả ghi chú";
                }

                updateSelectedFolderUI(folderName, folderColor);
                return;
            }

            try {
                if (requestCode == REQUEST_GALLERY && data.getData() != null) {
                    Uri uri = data.getData();

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    imageBytes = baos.toByteArray();

                    if (imgPreview != null) {
                        imgPreview.setImageBitmap(bitmap);
                        imgPreview.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(this, "Đã chọn ảnh! Hãy bấm Lưu.", Toast.LENGTH_SHORT).show();

                } else if (requestCode == REQUEST_CAMERA && data.getExtras() != null) {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    if (bitmap != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                        imageBytes = baos.toByteArray();

                        if (imgPreview != null) {
                            imgPreview.setImageBitmap(bitmap);
                            imgPreview.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(this, "Đã chụp ảnh! Hãy bấm Lưu.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Có lỗi xảy ra khi xử lý ảnh!", Toast.LENGTH_SHORT).show();
            }
        }

        private void insertTable() {
            if (edtContent == null) return;

            Editable editable = edtContent.getText();
            int cursor = edtContent.getSelectionStart();

            if (cursor < 0) {
                cursor = editable.length();
            }

            String table =
                    "\n┌──────────┬──────────┐\n" +
                            "│ Cột 1    │ Cột 2    │\n" +
                            "├──────────┼──────────┤\n" +
                            "│          │          │\n" +
                            "├──────────┼──────────┤\n" +
                            "│          │          │\n" +
                            "└──────────┴──────────┘\n";

            editable.insert(cursor, table);
            edtContent.setSelection(cursor + table.length());
        }

        private void toggleBulletLine() {
            if (edtContent == null) return;

            int cursor = edtContent.getSelectionStart();
            Editable editable = edtContent.getText();

            if (cursor < 0) cursor = editable.length();

            int lineStart = cursor;
            while (lineStart > 0 && editable.charAt(lineStart - 1) != '\n') {
                lineStart--;
            }

            int lineEnd = cursor;
            while (lineEnd < editable.length() && editable.charAt(lineEnd) != '\n') {
                lineEnd++;
            }

            String currentLine = editable.subSequence(lineStart, lineEnd).toString();

            if (currentLine.startsWith("• ")) {
                editable.delete(lineStart, lineStart + 2);
            } else {
                editable.insert(lineStart, "• ");
            }
        }

        private void openFilePicker() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Chọn file"), REQUEST_ATTACH_FILE);
        }

        private String getFileName(Uri uri) {
            String result = "file_đính_kèm";

            if (uri == null) return result;

            Cursor cursor = null;

            try {
                cursor = getContentResolver().query(uri, null, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                result = "file_đính_kèm";
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            return result;
        }

        private interface OnFileUploadListener {
            void onSuccess(String fileUrl);
            void onFailure(String error);
        }


        private ArrayList<ArrayList<String>> getTableDataForFirebase() {
            saveCurrentTableData();

            ArrayList<ArrayList<String>> result = new ArrayList<>();

            for (int r = 0; r < tableRows; r++) {
                ArrayList<String> row = new ArrayList<>();

                for (int c = 0; c < tableCols; c++) {
                    if (r < tableData.size() && c < tableData.get(r).size()) {
                        row.add(tableData.get(r).get(c));
                    } else {
                        row.add("");
                    }
                }

                result.add(row);
            }

            return result;
        }

        private void uploadFileToCloudinary(Uri fileUri, OnFileUploadListener listener) {
            if (fileUri == null) {
                listener.onSuccess("");
                return;
            }

            try {
                byte[] fileBytes = getBytesFromUri(fileUri);

                String url = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/raw/upload";

                OkHttpClient client = new OkHttpClient();

                String fileName = attachedFileName;
                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = "attached_file_" + System.currentTimeMillis();
                }

                RequestBody fileBody = RequestBody.create(
                        fileBytes,
                        MediaType.parse("application/octet-stream")
                );

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileName, fileBody)
                        .addFormDataPart("upload_preset", UPLOAD_PRESET)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> listener.onFailure(e.getMessage()));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            String errorBody = response.body() != null
                                    ? response.body().string()
                                    : "Upload file failed";

                            runOnUiThread(() -> listener.onFailure(errorBody));
                            return;
                        }

                        String responseBody = response.body() != null
                                ? response.body().string()
                                : "";

                        try {
                            JSONObject jsonObject = new JSONObject(responseBody);
                            String fileUrl = jsonObject.getString("secure_url");

                            runOnUiThread(() -> listener.onSuccess(fileUrl));

                        } catch (Exception e) {
                            runOnUiThread(() -> listener.onFailure(e.getMessage()));
                        }
                    }
                });

            } catch (Exception e) {
                listener.onFailure(e.getMessage());
            }
        }

        private byte[] getBytesFromUri(Uri uri) throws IOException {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            if (inputStream != null) {
                inputStream.close();
            }

            return byteBuffer.toByteArray();
        }
    }