package com.example.mynote;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Import các Fragment của bạn vào đây (Nhớ Alt + Enter nếu nó báo đỏ nhé)
import com.example.mynote.fragments.HomeFragment;
import com.example.mynote.fragments.CalendarFragment;
import com.example.mynote.fragments.ImageFragment;
import com.example.mynote.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Lệnh 1: Mặc định khi vừa mở app lên sẽ chiếu kênh "Ghi chú" (HomeFragment)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Lệnh 2: Bắt sự kiện khi bấm các nút dưới đáy
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // So sánh ID của nút được bấm (Nhớ check lại ID trong file bottom_nav_menu.xml của bạn)
            if (itemId == R.id.nav_note) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarFragment();
            } else if (itemId == R.id.nav_image) {
                selectedFragment = new ImageFragment();
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }

            // Tiến hành tráo Fragment
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}