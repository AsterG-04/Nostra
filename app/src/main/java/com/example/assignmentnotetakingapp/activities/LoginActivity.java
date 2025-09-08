package com.example.assignmentnotetakingapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.assignmentnotetakingapp.R;

import com.example.assignmentnotetakingapp.database.AppDatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnRegister;
    TextView tvForgotPassword;
    ImageView ivTogglePassword;

    AppDatabaseHelper dbHelper;
    boolean isPasswordVisible = false;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_LOGGED_IN_USER_EMAIL = "loggedInUserEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        dbHelper = new AppDatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isValidUser = dbHelper.checkUser(email, password);

                if (isValidUser) {
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                    int userId = -1;
                    String userEmail = email;
                    Cursor userCursor = null;

                    try {
                        userCursor = dbHelper.getUserDetailsByEmail(email);
                        if (userCursor != null && userCursor.moveToFirst()) {
                            int userIdColIndex = userCursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_USER_ID);
                            userId = userCursor.getInt(userIdColIndex);
                        } else {
                            Log.e("LoginActivity", "User authenticated by checkUser but details not found by email: " + email);
                            Toast.makeText(LoginActivity.this, "Login error: Could not retrieve complete user data.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Error retrieving user details after login: " + e.getMessage(), e);
                        Toast.makeText(LoginActivity.this, "Login error: Database issue during data retrieval.", Toast.LENGTH_SHORT).show();
                        return;
                    } finally {
                        if (userCursor != null) {
                            userCursor.close();
                        }
                    }

                    if (userId == -1) {
                        Log.e("LoginActivity", "User ID is -1 after retrieval for email: " + email);
                        Toast.makeText(LoginActivity.this, "Login error: Invalid user ID obtained after authentication.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putBoolean(KEY_IS_LOGGED_IN, true);
                    editor.putInt(KEY_USER_ID, userId);
                    editor.putString(KEY_LOGGED_IN_USER_EMAIL, userEmail);
                    editor.apply();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
