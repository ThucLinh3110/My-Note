package com.examplek49.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.examplek49.mynote.R;
import com.examplek49.mynote.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginNow;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginNow = findViewById(R.id.tvLoginNow);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLoginNow.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edtName.setError("Nhập họ tên");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Nhập mật khẩu");
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            return;
        }
        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();

                    User user = new User(uid, name, email);

                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .child("profile")
                            .setValue(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lưu user lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Đăng ký lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}