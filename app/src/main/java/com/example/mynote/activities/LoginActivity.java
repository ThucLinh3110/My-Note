package com.example.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.mynote.MainActivity;
import com.example.mynote.R;
import com.example.mynote.firebase.FirebaseAuthHelper;
import com.example.mynote.firebase.RecentAccountManager;
import com.example.mynote.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.mynote.firebase.FirebaseDatabaseHelper;
import com.example.mynote.models.User;
import com.google.firebase.auth.FirebaseUser;
import android.widget.ImageView;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

public class LoginActivity extends AppCompatActivity {

    private ImageView imgTogglePassword;
    private boolean isPasswordVisible = false;

    private static final String TAG = "LoginActivity";

    private EditText edtEmail, edtPassword;
    private AppCompatButton btnLogin;
    private LinearLayout btnGoogle;
    private TextView txtRegisterNow, txtForgotPassword;

    private FirebaseAuthHelper authHelper;
    private CredentialManager credentialManager;
    private String loginMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginMode = getIntent().getStringExtra("MODE");
        authHelper = new FirebaseAuthHelper();
        credentialManager = CredentialManager.create(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);

        imgTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Ẩn mật khẩu
                edtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                imgTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                // Hiện mật khẩu
                edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                imgTogglePassword.setImageResource(R.drawable.ic_eye_open);
            }

            isPasswordVisible = !isPasswordVisible;

            // giữ con trỏ ở cuối
            edtPassword.setSelection(edtPassword.getText().length());
        });;

        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtRegisterNow = findViewById(R.id.txtRegisterNow);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        String registeredEmail = getIntent().getStringExtra("REGISTERED_EMAIL");
        if (registeredEmail != null && !registeredEmail.isEmpty()) {
            edtEmail.setText(registeredEmail);
        }

        String resetEmail = getIntent().getStringExtra("RESET_EMAIL");
        if (resetEmail != null && !resetEmail.isEmpty()) {
            edtEmail.setText(resetEmail);
        }

        btnLogin.setOnClickListener(v -> loginUser());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        txtRegisterNow.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        txtForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!"ADD_ACCOUNT".equals(loginMode) && authHelper.getCurrentUser() != null) {
            goToMainActivity();
        }
    }
    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        authHelper.login(email, password)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);

                    if (task.isSuccessful()) {

                        FirebaseUser user = authHelper.getCurrentUser();

                        if (user != null && user.isEmailVerified()) {

                            User recentUser = new User(
                                    user.getUid(),
                                    user.getDisplayName() != null ? user.getDisplayName() : "Người dùng",
                                    user.getEmail() != null ? user.getEmail() : "",
                                    System.currentTimeMillis()
                            );

                            RecentAccountManager.saveRecentAccount(LoginActivity.this, recentUser);

                            Toast.makeText(LoginActivity.this,
                                    "Đăng nhập thành công!",
                                    Toast.LENGTH_SHORT).show();

                            goToMainActivity();

                        } else {

                            authHelper.signOut();

                            Toast.makeText(LoginActivity.this,
                                    "Email chưa được xác minh. Vui lòng kiểm tra Gmail.",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : "Đăng nhập thất bại";
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        btnGoogle.setEnabled(false);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        btnGoogle.setEnabled(true);
                        handleGoogleSignIn(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        btnGoogle.setEnabled(true);
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập Google thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Google Sign-In error", e);
                    }
                }
        );
    }

    private void handleGoogleSignIn(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;

            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(customCredential.getType())) {
                try {
                    GoogleIdTokenCredential googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(customCredential.getData());

                    String idToken = googleIdTokenCredential.getIdToken();
                    firebaseAuthWithGoogle(idToken);

                } catch (Exception e) {
                    Toast.makeText(this,
                            "Không đọc được thông tin tài khoản Google",
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Google token parse error", e);
                }
            } else {
                Toast.makeText(this,
                        "Credential Google không hợp lệ",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,
                    "Không nhận được tài khoản Google",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

        authHelper.getAuth().signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = authHelper.getCurrentUser();

                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            String fullName = firebaseUser.getDisplayName() != null
                                    ? firebaseUser.getDisplayName()
                                    : "";
                            String email = firebaseUser.getEmail() != null
                                    ? firebaseUser.getEmail()
                                    : "";

                            User user = new User(uid, fullName, email, System.currentTimeMillis());
                            new FirebaseDatabaseHelper().saveUser(user);
                            RecentAccountManager.saveRecentAccount(LoginActivity.this, user);
                        }

                        Toast.makeText(this,
                                "Đăng nhập Google thành công!",
                                Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        Toast.makeText(this,
                                "Firebase đăng nhập Google thất bại",
                                Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }

    private void goToMainActivity() {
        if ("ADD_ACCOUNT".equals(loginMode)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
