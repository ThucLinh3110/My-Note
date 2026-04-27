package com.example.mynote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;

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
import com.example.mynote.firebase.FirebaseDatabaseHelper;
import com.example.mynote.models.User;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.mynote.firebase.RecentAccountManager;

public class RegisterActivity extends AppCompatActivity {

    private ImageView imgTogglePassword, imgToggleConfirmPassword;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private static final String TAG = "RegisterActivity";

    private EditText edtFullName, edtRegisterEmail, edtRegisterPassword, edtConfirmPassword;
    private AppCompatButton btnCreateAccount;
    private TextView txtBackToLogin;
    private LinearLayout btnRegisterGoogle;

    private FirebaseAuthHelper authHelper;
    private FirebaseDatabaseHelper databaseHelper;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        imgToggleConfirmPassword = findViewById(R.id.imgToggleConfirmPassword);

        authHelper = new FirebaseAuthHelper();
        databaseHelper = new FirebaseDatabaseHelper();
        credentialManager = CredentialManager.create(this);

        edtFullName = findViewById(R.id.edtFullName);
        edtRegisterEmail = findViewById(R.id.edtRegisterEmail);
        edtRegisterPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);
        btnRegisterGoogle = findViewById(R.id.btnRegisterGoogle);

        imgTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtRegisterPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );
                imgTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                edtRegisterPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );
                imgTogglePassword.setImageResource(R.drawable.ic_eye_open);
            }

            isPasswordVisible = !isPasswordVisible;
            edtRegisterPassword.setSelection(edtRegisterPassword.getText().length());
        });

        imgToggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                edtConfirmPassword.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );
                imgToggleConfirmPassword.setImageResource(R.drawable.ic_eye_closed);
            } else {
                edtConfirmPassword.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );
                imgToggleConfirmPassword.setImageResource(R.drawable.ic_eye_open);
            }

            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            edtConfirmPassword.setSelection(edtConfirmPassword.getText().length());
        });

        btnCreateAccount.setOnClickListener(v -> registerUser());

        txtBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        btnRegisterGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void registerUser() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtRegisterEmail.getText().toString().trim();
        String password = edtRegisterPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            edtFullName.setError("Vui lòng nhập họ tên");
            edtFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edtRegisterEmail.setError("Vui lòng nhập email");
            edtRegisterEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtRegisterEmail.setError("Email không hợp lệ");
            edtRegisterEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtRegisterPassword.setError("Vui lòng nhập mật khẩu");
            edtRegisterPassword.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            edtRegisterPassword.setError(
                    "Mật khẩu ≥ 8 ký tự, gồm chữ hoa, số và ký tự đặc biệt"
            );
            edtRegisterPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            edtConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            edtConfirmPassword.requestFocus();
            return;
        }

        btnCreateAccount.setEnabled(false);

        authHelper.register(fullName, email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        btnCreateAccount.setEnabled(true);

                        String errorMessage;
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = "Email này đã được đăng ký rồi. Vui lòng đăng nhập.";
                        } else {
                            errorMessage = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Đăng ký thất bại";
                        }

                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser firebaseUser = authHelper.getCurrentUser();
                    if (firebaseUser == null) {
                        btnCreateAccount.setEnabled(true);
                        Toast.makeText(
                                RegisterActivity.this,
                                "Tạo tài khoản thành công nhưng không lấy được thông tin người dùng.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    String uid = firebaseUser.getUid();
                    User user = new User(uid, fullName, email, System.currentTimeMillis());

                    Task<Void> saveTask = databaseHelper.saveUser(user);

                    if (saveTask == null) {
                        btnCreateAccount.setEnabled(true);
                        Toast.makeText(
                                RegisterActivity.this,
                                "Không thể lưu dữ liệu người dùng.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    saveTask
                            .addOnSuccessListener(unused -> {
                                RecentAccountManager.saveRecentAccount(RegisterActivity.this, user);
                                firebaseUser.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                    btnCreateAccount.setEnabled(true);
                                    authHelper.signOut();

                                    if (verifyTask.isSuccessful()) {
                                        Toast.makeText(
                                                RegisterActivity.this,
                                                "Đã gửi email xác minh. Vui lòng kiểm tra Gmail trước khi đăng nhập.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    } else {
                                        Toast.makeText(
                                                RegisterActivity.this,
                                                "Tạo tài khoản thành công nhưng gửi email xác minh thất bại.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }

                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.putExtra("REGISTERED_EMAIL", email);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                            })
                            .addOnFailureListener(e -> {
                                btnCreateAccount.setEnabled(true);
                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Tạo tài khoản thành công nhưng lưu dữ liệu người dùng thất bại: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                });
    }

    private void signInWithGoogle() {
        btnRegisterGoogle.setEnabled(false);

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
                        btnRegisterGoogle.setEnabled(true);
                        handleGoogleSignIn(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        btnRegisterGoogle.setEnabled(true);
                        Toast.makeText(
                                RegisterActivity.this,
                                "Đăng nhập Google thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
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
                    Toast.makeText(
                            this,
                            "Không đọc được thông tin tài khoản Google",
                            Toast.LENGTH_SHORT
                    ).show();
                    Log.e(TAG, "Google token parse error", e);
                }
            } else {
                Toast.makeText(
                        this,
                        "Credential Google không hợp lệ",
                        Toast.LENGTH_SHORT
                ).show();
            }
        } else {
            Toast.makeText(
                    this,
                    "Không nhận được tài khoản Google",
                    Toast.LENGTH_SHORT
            ).show();
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
                            databaseHelper.saveUser(user);
                            RecentAccountManager.saveRecentAccount(RegisterActivity.this, user);
                        }

                        Toast.makeText(
                                this,
                                "Đăng nhập Google thành công!",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        Toast.makeText(
                                this,
                                "Firebase đăng nhập Google thất bại",
                                Toast.LENGTH_SHORT
                        ).show();
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }

    private boolean isValidPassword(String password) {
        String pattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";
        return password.matches(pattern);
    }
}