package com.example.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mynote.R;

import java.util.regex.Pattern;
import android.widget.ImageView;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;


public class ResetPasswordActivity extends AppCompatActivity {

    private ImageView imgToggleNewPassword, imgToggleConfirmPassword;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;    private EditText edtNewPassword, edtConfirmNewPassword;
    private Button btnResetPassword;

    private OtpManager otpManager;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        imgToggleNewPassword = findViewById(R.id.imgToggleNewPassword);
        imgToggleConfirmPassword = findViewById(R.id.imgToggleConfirmPassword);

        otpManager = new OtpManager(this);

        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        email = getIntent().getStringExtra("RESET_EMAIL");

        btnResetPassword.setOnClickListener(v -> resetPasswordDemo());

        imgToggleNewPassword.setOnClickListener(v -> {
            if (isNewPasswordVisible) {
                edtNewPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );
                imgToggleNewPassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                edtNewPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );
                imgToggleNewPassword.setImageResource(R.drawable.ic_eye_open);
            }

            isNewPasswordVisible = !isNewPasswordVisible;
            edtNewPassword.setSelection(edtNewPassword.getText().length());
        });

        imgToggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                edtConfirmNewPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );
                imgToggleConfirmPassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                edtConfirmNewPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );
                imgToggleConfirmPassword.setImageResource(R.drawable.ic_eye_open);
            }

            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            edtConfirmNewPassword.setSelection(edtConfirmNewPassword.getText().length());
        });
    }

    // 🔥 THÊM DUY NHẤT HÀM NÀY
    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";
        return Pattern.matches(pattern, password);
    }

    private void resetPasswordDemo() {
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmNewPassword.getText().toString().trim();

        if (!otpManager.isVerified(email)) {
            Toast.makeText(this, "Bạn chưa xác nhận OTP", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
            edtNewPassword.requestFocus();
            return;
        }

        // 🔥 CHỈ SỬA ĐOẠN NÀY
        if (!isValidPassword(newPassword)) {
            edtNewPassword.setError("Mật khẩu ≥ 8 ký tự, gồm chữ hoa, số và ký tự đặc biệt");
            edtNewPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmNewPassword.setError("Vui lòng xác nhận mật khẩu");
            edtConfirmNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            edtConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
            edtConfirmNewPassword.requestFocus();
            return;
        }

        otpManager.clearOtp();

        Toast.makeText(this, "Đã đổi mật khẩu thành công", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.putExtra("RESET_EMAIL", email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
