package com.example.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mynote.R;
import com.example.mynote.activities.OtpManager;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmailForgot;
    private Button btnSendCode;
    private TextView txtBackToLogin;

    private OtpManager otpManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        otpManager = new OtpManager(this);

        edtEmailForgot = findViewById(R.id.edtForgotEmail);
        btnSendCode = findViewById(R.id.btnSendCode);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);

        btnSendCode.setOnClickListener(v -> sendFakeOtp());

        txtBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void sendFakeOtp() {
        String email = edtEmailForgot.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmailForgot.setError("Vui lòng nhập email");
            edtEmailForgot.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmailForgot.setError("Email không hợp lệ");
            edtEmailForgot.requestFocus();
            return;
        }

        String otp = otpManager.generateOtp();
        otpManager.saveOtp(email, otp);

        Toast.makeText(
                this,
                "Mã OTP demo là: " + otp,
                Toast.LENGTH_LONG
        ).show();

        Intent intent = new Intent(ForgotPasswordActivity.this, OtpActivity.class);
        intent.putExtra("RESET_EMAIL", email);
        startActivity(intent);
    }
}
