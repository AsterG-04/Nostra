package com.example.assignmentnotetakingapp.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignmentnotetakingapp.database.AppDatabaseHelper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.assignmentnotetakingapp.R;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.imageview.ShapeableImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ShapeableImageView profileImageHeader;
    private TextView usernameHeader;

    FloatingActionButton fabAddNote;
    private SharedPreferences sharedPreferences;
    private AppDatabaseHelper dbHelper;

    private static final Class<?> LOGIN_ACTIVITY_CLASS = LoginActivity.class;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_LOGGED_IN_USER_EMAIL = "loggedInUserEmail";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        drawerLayout = findViewById(R.id.draw_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        } else {
            Log.e("MainActivity", "NavigationView not found in layout!");
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        if (drawerLayout != null) {
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        } else {
            Log.e("MainActivity", "DrawerLayout not found in layout!");
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dbHelper = new AppDatabaseHelper(this);

        // --- Get the logged-in user's ID from SharedPreferences ---
        int loggedInUserId = sharedPreferences.getInt(KEY_USER_ID, -1);

        if (loggedInUserId == -1) {
            Log.e("MainActivity", "Logged-in User ID not found in SharedPreferences! Redirecting to Login.");
            Toast.makeText(this, "Session invalid. Please log in.", Toast.LENGTH_LONG).show();
            logoutUser();
            return;
        }

        // --- Initialize Navigation Drawer Header Views ---
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                profileImageHeader = headerView.findViewById(R.id.profile_image);
                usernameHeader = headerView.findViewById(R.id.profile_name);

                ImageView editProfileIcon = headerView.findViewById(R.id.edit_profile_icon);
                if (editProfileIcon != null) {
                    editProfileIcon.setOnClickListener(v -> launchEditProfile());
                } else {
                    Log.e("MainActivity", "edit_profile_icon not found in nav_header.xml!");
                }
            } else {
                Log.e("MainActivity", "Navigation header view not found!");
            }
        }

        loadProfileHeader();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, NotesFragment.newInstance("All", loggedInUserId))
                    .commit();
            if (navigationView != null) {
                navigationView.setCheckedItem(R.id.home);
            }
        }

        fabAddNote = findViewById(R.id.fabAddNote);
        if (fabAddNote != null) {
            fabAddNote.setOnClickListener(v -> {

                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra(MainActivity2.EXTRA_ACTION, MainActivity2.ACTION_ADD);

                startActivity(intent);
            });
        } else {
            Log.e("MainActivity", "fabAddNote not found in layout!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileHeader();
    }


    private void loadProfileHeader() {
        String loggedInUserEmail = sharedPreferences.getString(KEY_LOGGED_IN_USER_EMAIL, null);

        if (loggedInUserEmail != null && !loggedInUserEmail.isEmpty()) {
            Cursor cursor = null;
            try {
                cursor = dbHelper.getUserDetailsByEmail(loggedInUserEmail);

                if (cursor != null && cursor.moveToFirst()) {
                    int usernameColIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_USERNAME);
                    int imageUriColIndex = cursor.getColumnIndexOrThrow(AppDatabaseHelper.COLUMN_PROFILE_IMAGE_URI);

                    String username = cursor.getString(usernameColIndex);
                    String imageUriString = cursor.getString(imageUriColIndex);

                    if (usernameHeader != null) usernameHeader.setText(username);

                    if (profileImageHeader != null) {
                        if (imageUriString != null && !imageUriString.isEmpty()) {
                            try {
                                Uri uri = Uri.parse(imageUriString);
                                profileImageHeader.setImageURI(uri);
                            } catch (SecurityException e) {
                                Log.e("MainActivity", "SecurityException loading header image: " + e.getMessage());
                                profileImageHeader.setImageResource(R.drawable.default_profile);
                            } catch (Exception e) {
                                Log.e("MainActivity", "Error loading header image: " + e.getMessage(), e);
                                profileImageHeader.setImageResource(R.drawable.default_profile);
                            }
                        } else {
                            profileImageHeader.setImageResource(R.drawable.default_profile);
                        }
                    }
                } else {
                    Log.e("MainActivity", "Logged in user data not found in DB for email: " + loggedInUserEmail + ". Consider forcing re-login.");
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error loading profile header data: " + e.getMessage(), e);
                Toast.makeText(this, "Error loading profile data.", Toast.LENGTH_SHORT).show();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Log.w("MainActivity", "No loggedInUserEmail found in SharedPreferences. User is not logged in.");
            if (usernameHeader != null) usernameHeader.setText("Guest");
            if (profileImageHeader != null) profileImageHeader.setImageResource(R.drawable.default_profile);

        }
    }

    private void launchEditProfile() {
        String loggedInUserEmail = sharedPreferences.getString(KEY_LOGGED_IN_USER_EMAIL, null);

        if (loggedInUserEmail != null && !loggedInUserEmail.isEmpty()) {
            Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
            intent.putExtra(EditProfileActivity.EXTRA_USER_EMAIL, loggedInUserEmail); // Assuming EditProfileActivity uses this extra key
            startActivity(intent);
        } else {
            Log.w("MainActivity", "Cannot launch Edit Profile: LoggedInUserEmail is null/empty.");
            Toast.makeText(this, "Cannot edit profile: user not identified.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int id = item.getItemId();

        int loggedInUserId = sharedPreferences.getInt(KEY_USER_ID, -1);
        if (loggedInUserId == -1) {
            Log.e("MainActivity", "Logged-in User ID not found during navigation item selection!");
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            if (drawerLayout != null) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return false;
        }


        if (id == R.id.home) {
            selectedFragment = NotesFragment.newInstance("All", loggedInUserId);
        } else if (id == R.id.favourtie) {
            selectedFragment = NotesFragment.newInstance("Favorite", loggedInUserId);
        } else if (id == R.id.urgent) {
            selectedFragment = NotesFragment.newInstance("Urgent", loggedInUserId);
        } else if (id == R.id.nav_logout || id == R.id.nav_exit) {
            logoutUser();
            if (drawerLayout != null) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            return true;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return selectedFragment != null || id == R.id.nav_logout || id == R.id.nav_exit;
    }

    // Method to handle user logout
    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_LOGGED_IN_USER_EMAIL);
        editor.apply();


        Intent intent = new Intent(this, LOGIN_ACTIVITY_CLASS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof NotesFragment && !((NotesFragment) currentFragment).getFilterType().equals("All")) {
                int loggedInUserId = sharedPreferences.getInt(KEY_USER_ID, -1);
                if (loggedInUserId != -1) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, NotesFragment.newInstance("All", loggedInUserId))
                            .commit();
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    if (navigationView != null) {
                        navigationView.setCheckedItem(R.id.home);
                    }
                } else {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
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