package com.example.assignmentnotetakingapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignmentnotetakingapp.R;
import com.example.assignmentnotetakingapp.database.AppDatabaseHelper;
import com.google.android.material.imageview.ShapeableImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    public static final String EXTRA_USER_EMAIL = "com.example.assignmentnotetakingapp.USER_EMAIL";

    private ShapeableImageView ivProfileImage;
    private ImageButton btnAddImage;
    private ImageButton btnBack;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPassword;

    private EditText etPasswordHint;
    private Button btnSave;

    private Uri imageUri;
    private AppDatabaseHelper dbHelper;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ivProfileImage = findViewById(R.id.profile_image);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnBack = findViewById(R.id.btnBack);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordHint = findViewById(R.id.etPasswordHint);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = new AppDatabaseHelper(this);

        currentUserEmail = getIntent().getStringExtra(EXTRA_USER_EMAIL);

        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Error: User email not provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProfileData(currentUserEmail);

        if (btnAddImage != null) {
            btnAddImage.setOnClickListener(v -> openImageChooser());
        } else { Log.e("EditProfileActivity", "btnAddImage not found!"); }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        } else { Log.e("EditProfileActivity", "btnSave not found!"); }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        } else { Log.e("EditProfileActivity", "btnBack not found!"); }
    }

    private void loadProfileData(String email) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getUserDetailsByEmail(email);

            if (cursor != null && cursor.moveToFirst()) {
                int usernameColIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_USERNAME);
                int emailColIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_EMAIL);
                int passwordHintColIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PASSWORD_HINT);
                int imageUriColIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PROFILE_IMAGE_URI);

                String username = cursor.getString(usernameColIndex);
                String loadedEmail = cursor.getString(emailColIndex);
                String passwordHint = cursor.getString(passwordHintColIndex);
                String loadedImageUriString = cursor.getString(imageUriColIndex);

                if (etUsername != null) etUsername.setText(username);
                if (etEmail != null) etEmail.setText(loadedEmail);

                if (etPasswordHint != null) etPasswordHint.setText(passwordHint);

                if (ivProfileImage != null) {
                    if (loadedImageUriString != null && !loadedImageUriString.isEmpty()) {
                        try {
                            imageUri = Uri.parse(loadedImageUriString);
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                            ivProfileImage.setImageURI(imageUri);
                            Log.d("EditProfileActivity", "Successfully loaded image URI: " + imageUri.toString());
                        } catch (SecurityException e) {
                            Log.e("EditProfileActivity", "SecurityException loading image URI: " + (imageUri != null ? imageUri.toString() : "null") + " - " + e.getMessage());
                            Toast.makeText(this, "Cannot load image. Please select again.", Toast.LENGTH_SHORT).show();
                            imageUri = null;
                            ivProfileImage.setImageResource(R.drawable.default_profile);
                        } catch (Exception e) {
                            Log.e("EditProfileActivity", "Error loading image: " + (imageUri != null ? imageUri.toString() : "null") + " - " + e.getMessage());
                            Toast.makeText(this, "Error loading image.", Toast.LENGTH_SHORT).show();
                            imageUri = null;
                            ivProfileImage.setImageResource(R.drawable.default_profile);
                        }
                    } else {
                        ivProfileImage.setImageResource(R.drawable.default_profile);
                    }
                }

            } else {
                Log.e("EditProfileActivity", "Profile data not found in DB for email: " + email);
                Toast.makeText(this, "Profile data not found.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e("EditProfileActivity", "Error loading profile data from DB: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading profile data.", Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (ivProfileImage != null) {
                ivProfileImage.setImageURI(imageUri);
            }

            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            try {
                getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                Log.d("EditProfileActivity", "Successfully took persistent URI permission for: " + imageUri.toString());
                Toast.makeText(this, "Image selected and permission granted", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                Log.e("EditProfileActivity", "Failed to take persistent URI permission: " + e.getMessage(), e);
                Toast.makeText(this, "Image selected but permission could not be granted persistently. It may not load later.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("EditProfileActivity", "Error taking URI permission: " + e.getMessage(), e);
                Toast.makeText(this, "Error processing image URI.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfile() {
        if (etUsername == null || etEmail == null || etPassword == null || etPasswordHint == null) {
            Log.e("EditProfileActivity", "EditText fields not found in layout!");
            Toast.makeText(this, "Error saving profile: UI not ready.", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordHint = etPasswordHint.getText().toString().trim();
        String imageUriString = (imageUri != null) ? imageUri.toString() : null;

        // Basic validation
        if (username.isEmpty()) {
            etUsername.setError("Username cannot be empty");
            Toast.makeText(this, "Username is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email cannot be empty");
            Toast.makeText(this, "Email is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password cannot be empty");
            Toast.makeText(this, "Password is required.", Toast.LENGTH_SHORT).show();
            return;
        }


        // --- Email Uniqueness Check (if email was changed) ---
        if (!email.equalsIgnoreCase(currentUserEmail)) {
            int existingUserId = dbHelper.getUserIdByEmail(email);
            int currentUserId = dbHelper.getUserIdByEmail(currentUserEmail);

            if (existingUserId != -1 && existingUserId != currentUserId) {
                etEmail.setError("Email already in use by another account.");
                Toast.makeText(this, "Email already in use.", Toast.LENGTH_SHORT).show();
                Log.w("EditProfileActivity", "Attempted to save profile with duplicate email: " + email);
                return;
            }
        }

        // --- Update data in Database ---
        int rowsAffected = dbHelper.updateUserByEmail(currentUserEmail, username, email, password, passwordHint, imageUriString);

        if (rowsAffected > 0) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            currentUserEmail = email; // Update currentUserEmail if email was changed and saved
            finish();
        } else {
            Log.e("EditProfileActivity", "Profile update failed or no changes made for email: " + currentUserEmail);
            Toast.makeText(this, "Profile update failed or no changes made.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}