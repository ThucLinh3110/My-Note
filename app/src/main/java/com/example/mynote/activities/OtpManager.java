package com.example.mynote.activities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;
import java.util.Random;

public class OtpManager {

    private static final String PREF_NAME = "otp_demo_prefs";
    private static final String KEY_OTP = "otp";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_VERIFIED = "verified";

    private final SharedPreferences prefs;

    public OtpManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String generateOtp() {
        int number = 100000 + new Random().nextInt(900000);
        return String.format(Locale.getDefault(), "%06d", number);
    }

    public void saveOtp(String email, String otp) {
        prefs.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_OTP, otp)
                .putBoolean(KEY_VERIFIED, false)
                .apply();
    }

    public String sendOtp(String email) {
        String otp = generateOtp();
        saveOtp(email, otp);
        return otp;
    }

    public boolean verifyOtp(String email, String otpInput) {
        String savedEmail = prefs.getString(KEY_EMAIL, "");
        String savedOtp = prefs.getString(KEY_OTP, "");

        boolean success = savedEmail.equals(email) && savedOtp.equals(otpInput);

        if (success) {
            prefs.edit().putBoolean(KEY_VERIFIED, true).apply();
        }

        return success;
    }

    public boolean isVerified(String email) {
        String savedEmail = prefs.getString(KEY_EMAIL, "");
        boolean verified = prefs.getBoolean(KEY_VERIFIED, false);
        return savedEmail.equals(email) && verified;
    }

    public void clearOtp() {
        prefs.edit().clear().apply();
    }
}