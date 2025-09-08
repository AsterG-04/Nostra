package com.example.assignmentnotetakingapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.assignmentnotetakingapp.R;
import com.example.assignmentnotetakingapp.database.AppDatabaseHelper;


public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail, etHintofPassword, etPassword;
    Button btnOk, btnLogin;
    AppDatabaseHelper dbHelper;
    private ImageButton btnBack;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        dbHelper = new AppDatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etHintofPassword = findViewById(R.id.etHintofPassword);
        etPassword = findViewById(R.id.etPassword);
        btnOk = findViewById(R.id.btnOk);
        btnLogin = findViewById(R.id.btnLogin);


        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                String hint = dbHelper.getPasswordHint(email);

                if (hint != null) {
                    etHintofPassword.setText(hint);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
                    etHintofPassword.setText("");
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isValidUser = dbHelper.checkUser(email, password);

                if (isValidUser) {
                    Toast.makeText(ForgotPasswordActivity.this, "You have known the password, You may Now Login", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (dbHelper != null) {
             dbHelper.close();
         }
     }
}