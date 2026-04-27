package com.example.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.adapters.ColorPickerAdapter;
import com.example.mynote.models.ColorOption;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_HEX = "selected_hex";
    public static final String EXTRA_SELECTED_NAME = "selected_name";

    private RecyclerView recyclerColors;
    private ImageView btnBackColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        recyclerColors = findViewById(R.id.recyclerColors);
        btnBackColor = findViewById(R.id.btnBackColor);

        btnBackColor.setOnClickListener(v -> finish());

        String selectedHex = getIntent().getStringExtra(EXTRA_SELECTED_HEX);
        if (selectedHex == null || selectedHex.isEmpty()) {
            selectedHex = "#E5E5EA";
        }

        List<ColorOption> colorList = new ArrayList<>();
        colorList.add(new ColorOption("Trắng", "#E5E5EA"));
        colorList.add(new ColorOption("Đỏ", "#FF3B30"));
        colorList.add(new ColorOption("Cam", "#FF9500"));
        colorList.add(new ColorOption("Vàng", "#FFCC00"));
        colorList.add(new ColorOption("Vàng nhạt", "#FFF59D"));
        colorList.add(new ColorOption("Xanh nhạt", "#DCE775"));
        colorList.add(new ColorOption("Xanh bạc hà", "#80CBC4"));
        colorList.add(new ColorOption("Xanh lá cây", "#34C759"));
        colorList.add(new ColorOption("Xanh dương", "#00C7BE"));
        colorList.add(new ColorOption("Xanh nước biển", "#007AFF"));
        colorList.add(new ColorOption("Xanh tím than", "#1E3A8A"));
        colorList.add(new ColorOption("Tím đậm", "#6D28D9"));
        colorList.add(new ColorOption("Tím", "#8E6BD1"));
        colorList.add(new ColorOption("Tím nhạt", "#D8B4E2"));
        colorList.add(new ColorOption("Hồng nhạt", "#F9C2E3"));
        colorList.add(new ColorOption("Hồng", "#FF6FAE"));

        recyclerColors.setLayoutManager(new LinearLayoutManager(this));
        recyclerColors.setAdapter(new ColorPickerAdapter(colorList, selectedHex, colorOption -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_SELECTED_HEX, colorOption.getHex());
            resultIntent.putExtra(EXTRA_SELECTED_NAME, colorOption.getName());
            setResult(RESULT_OK, resultIntent);
            finish();
        }));
    }
}