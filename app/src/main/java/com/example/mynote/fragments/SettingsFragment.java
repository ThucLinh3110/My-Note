package com.example.mynote.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynote.R;
import com.example.mynote.activities.LoginActivity;
import com.example.mynote.adapters.SettingsAdapter;
import com.example.mynote.models.SettingItem;
import com.example.mynote.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import com.example.mynote.firebase.RecentAccountManager;
import com.example.mynote.activities.TrashActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    private RecyclerView recyclerSettings;
    private SettingsAdapter settingsAdapter;
    private final List<SettingItem> settingItems = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private User recentAccount1;
    private User recentAccount2;
    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        recyclerSettings = view.findViewById(R.id.recyclerSettings);
        recyclerSettings.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadCurrentUser();

        return view;
    }
    private void loadCurrentUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(getContext(), "Chưa có tài khoản đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = firebaseUser.getUid();
        String authEmail = firebaseUser.getEmail();
        String authDisplayName = firebaseUser.getDisplayName();

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String finalName;
                String finalEmail;

                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);

                    String dbName = user != null ? user.getFullName() : "";
                    String dbEmail = user != null ? user.getEmail() : "";

                    if (!TextUtils.isEmpty(authDisplayName)) {
                        finalName = authDisplayName;
                    } else if (!TextUtils.isEmpty(dbName)) {
                        finalName = dbName;
                    } else {
                        finalName = "Người dùng";
                    }

                    if (!TextUtils.isEmpty(authEmail)) {
                        finalEmail = authEmail;
                    } else if (!TextUtils.isEmpty(dbEmail)) {
                        finalEmail = dbEmail;
                    } else {
                        finalEmail = "";
                    }
                } else {
                    finalName = !TextUtils.isEmpty(authDisplayName) ? authDisplayName : "Người dùng";
                    finalEmail = !TextUtils.isEmpty(authEmail) ? authEmail : "";
                }

                setupSettingsData(finalName, finalEmail);
                setupAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Không tải được dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSettingsData(String fullName, String email) {
        settingItems.clear();

        List<User> recentAccounts = RecentAccountManager.getRecentAccounts(requireContext());

        recentAccount1 = null;
        recentAccount2 = null;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "";

        List<User> filteredAccounts = new ArrayList<>();
        for (User user : recentAccounts) {
            if (user != null && user.getUid() != null && !user.getUid().equals(currentUid)) {
                filteredAccounts.add(user);
            }
        }

        if (filteredAccounts.size() > 0) {
            recentAccount1 = filteredAccounts.get(0);
        }

        if (filteredAccounts.size() > 1) {
            recentAccount2 = filteredAccounts.get(1);
        }

        settingItems.add(new SettingItem(SettingItem.TYPE_PROFILE_CARD, fullName, email));
        settingItems.add(new SettingItem(SettingItem.TYPE_MENU_CARD, "", ""));
        settingItems.add(new SettingItem(SettingItem.TYPE_SECTION_TITLE, "CHUYỂN ĐỔI TÀI KHOẢN", ""));
        settingItems.add(new SettingItem(SettingItem.TYPE_ACCOUNT_CARD, recentAccount1, recentAccount2));
        settingItems.add(new SettingItem(SettingItem.TYPE_ADD_BUTTON, "", ""));
    }

    private void setupAdapter() {
        settingsAdapter = new SettingsAdapter(settingItems, new SettingsAdapter.OnSettingClickListener() {
            @Override
            public void onTrashClick() {
                Intent intent = new Intent(getActivity(), TrashActivity.class);
                startActivity(intent);
            }

            @Override
            public void onLogoutClick() {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                if (getActivity() != null) {
                    getActivity().finish();
                }
            }

            @Override
            public void onAccount1Click() {
                Toast.makeText(getContext(), "Tài khoản phụ 1", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAccount2Click() {
                Toast.makeText(getContext(), "Tài khoản phụ 2", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAddAccountClick() {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Thêm tài khoản")
                        .setMessage("Mở màn hình đăng nhập để thêm tài khoản khác.")
                        .setNegativeButton("Hủy", null)
                        .setPositiveButton("Tiếp tục", (dialog, which) -> {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.putExtra("MODE", "ADD_ACCOUNT");
                            startActivity(intent);
                        })
                        .show();
            }
            @Override
            public void onProfileClick() {
                Toast.makeText(getContext(), "Thông tin tài khoản", Toast.LENGTH_SHORT).show();
            }

                });

        recyclerSettings.setAdapter(settingsAdapter);
    }
}