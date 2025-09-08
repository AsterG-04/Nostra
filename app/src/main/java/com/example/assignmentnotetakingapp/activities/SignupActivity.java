package com.example.assignmentnotetakingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.assignmentnotetakingapp.R;
import com.example.assignmentnotetakingapp.database.AppDatabaseHelper;

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    EditText etUsername, etEmail, etPassword, etConfirmPassword, etHint;
    Button btnSignup;
    TextView tvLogin1;
    AppDatabaseHelper dbHelper;

    // Email regex pattern
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        dbHelper = new AppDatabaseHelper(this);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etHint = findViewById(R.id.etHint);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin1 = findViewById(R.id.tvLogin1);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                String hint = etHint.getText().toString().trim();

                // --- Validation ---
                if (username.isEmpty()) {
                    etUsername.setError("Username is required");
                    etUsername.requestFocus();
                    return;
                }

                if (email.isEmpty()) {
                    etEmail.setError("Email is required");
                    etEmail.requestFocus();
                    return;
                }

                if (!isValidEmail(email)) {
                    etEmail.setError("Enter a valid email address");
                    etEmail.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    etPassword.setError("Password is required");
                    etPassword.requestFocus();
                    return;
                }

                if (password.length() < MIN_PASSWORD_LENGTH) {
                    etPassword.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
                    etPassword.requestFocus();
                    return;
                }

                if (confirmPassword.isEmpty()) {
                    etConfirmPassword.setError("Confirm Password is required");
                    etConfirmPassword.requestFocus();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    etConfirmPassword.setError("Passwords do not match");
                    etConfirmPassword.requestFocus();
                    return;
                }

                if (hint.isEmpty()) {
                    etHint.setError("Password Hint is required");
                    etHint.requestFocus();
                    return;
                }

                if (dbHelper.checkEmail(email)) {
                    etEmail.setError("Email already exists");
                    etEmail.requestFocus();
                    return;
                }

                // --- Add to DB ---
                long result = dbHelper.insertUser(username, email, password, confirmPassword, hint);

                if (result != -1) {
                    Toast.makeText(SignupActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvLogin1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });

    }

    // Helper method for email validation
    private boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }
}
