package com.example.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mynote.R;

public class OtpActivity extends AppCompatActivity {

    private TextView txtOtpDemo;
    private EditText edtOtp;
    private Button btnConfirmOtp;
    private TextView txtChangeEmail, txtResendOtp, txtBackToLogin;

    private OtpManager otpManager;
    private String email;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        otpManager = new OtpManager(this);

        edtOtp = findViewById(R.id.edtOtp);
        btnConfirmOtp = findViewById(R.id.btnConfirmOtp);
        txtChangeEmail = findViewById(R.id.txtChangeEmail);
        txtResendOtp = findViewById(R.id.txtResendOtp);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
        txtOtpDemo = findViewById(R.id.txtOtpDemo);

        email = getIntent().getStringExtra("RESET_EMAIL");

        // Hiện OTP ngay khi vừa vào màn hình
        String firstOtp = otpManager.sendOtp(email);
        txtOtpDemo.setText("OTP demo: " + firstOtp);

        btnConfirmOtp.setOnClickListener(v -> verifyOtp());

        txtChangeEmail.setOnClickListener(v -> finish());

        txtResendOtp.setOnClickListener(v -> {
            if (txtResendOtp.isEnabled()) {
                String newOtp = otpManager.sendOtp(email);
                txtOtpDemo.setText("OTP demo: " + newOtp);
                startCountdown();
            }
        });

        txtBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(OtpActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        startCountdown();
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        txtResendOtp.setEnabled(false);
        txtResendOtp.setText("Gửi lại (59s)");

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                txtResendOtp.setText("Gửi lại (" + seconds + "s)");
            }

            @Override
            public void onFinish() {
                txtResendOtp.setText("Gửi lại");
                txtResendOtp.setEnabled(true);
            }
        }.start();
    }

    private void verifyOtp() {
        String otpInput = edtOtp.getText().toString().trim();

        if (TextUtils.isEmpty(otpInput)) {
            edtOtp.setError("Vui lòng nhập mã OTP");
            edtOtp.requestFocus();
            return;
        }

        if (otpInput.length() != 6) {
            edtOtp.setError("OTP phải gồm 6 số");
            edtOtp.requestFocus();
            return;
        }

        boolean isValid = otpManager.verifyOtp(email, otpInput);

        if (isValid) {
            Toast.makeText(this, "Xác nhận OTP thành công", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(OtpActivity.this, ResetPasswordActivity.class);
            intent.putExtra("RESET_EMAIL", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "OTP không đúng", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
