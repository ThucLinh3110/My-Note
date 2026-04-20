package com.examplek49.mynote.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.examplek49.mynote.R;
import com.examplek49.mynote.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {

    private TextView tvEmail, tvLogout;

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        tvEmail = view.findViewById(R.id.tvEmail);
        tvLogout = view.findViewById(R.id.tvLogout);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            tvEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }

        tvLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}