package com.example.assignmentnotetakingapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager; // Import FragmentManager

import com.example.assignmentnotetakingapp.R;
import com.example.assignmentnotetakingapp.database.AppDatabaseHelper;

public class MainActivity2 extends AppCompatActivity {

    private ImageButton btnLockNote;
    private int currentNoteId = -1;
    private AppDatabaseHelper dbHelper;

    public static final String EXTRA_NOTE_ID = "noteId";
    public static final String EXTRA_ACTION = "action";
    public static final String ACTION_ADD = "Add";
    public static final String ACTION_VIEW_EDIT = "ViewEdit";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        dbHelper = new AppDatabaseHelper(this);
        btnLockNote = findViewById(R.id.btnLock);

        Bundle extras = getIntent().getExtras();
        String action = null;
        if (extras != null) {
            action = extras.getString(EXTRA_ACTION);
            if (ACTION_VIEW_EDIT.equals(action)) {
                currentNoteId = extras.getInt(EXTRA_NOTE_ID, -1);
            }
        }

        if (ACTION_ADD.equals(action)) {
            if (btnLockNote != null) {
                btnLockNote.setVisibility(View.GONE);
            }
            showNoteDetailFragment(currentNoteId);

        } else if (ACTION_VIEW_EDIT.equals(action) && currentNoteId != -1) {
            String savedPin = dbHelper.getPinForNote(currentNoteId);
            if (savedPin != null && !savedPin.isEmpty()) {
                showUnlockDialog(currentNoteId);
                if (btnLockNote != null) {
                    btnLockNote.setVisibility(View.GONE);
                }
            } else {
                showNoteDetailFragment(currentNoteId);
                if (btnLockNote != null) {
                    btnLockNote.setVisibility(View.VISIBLE);
                }
            }

        } else {
            Log.e("MainActivity2", "Invalid action or missing note ID in intent.");
            Toast.makeText(this, "Cannot load note. Invalid request.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // --- Set listener for the Lock button ---
        if (btnLockNote != null) {
            btnLockNote.setOnClickListener(v -> {
                 if (currentNoteId != -1) {
                    String savedPin = dbHelper.getPinForNote(currentNoteId);
                    if (savedPin == null || savedPin.isEmpty()) {
                        showSetPinDialog(currentNoteId);
                    } else {
                       Toast.makeText(this, "Lock button clicked. Implement Change/Remove PIN or other logic.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w("MainActivity2", "Lock button clicked for a new note. This should not happen.");
                }
            });
        }
    }

    public void onNewNoteSaved(int newNoteId) {
        this.currentNoteId = newNoteId; // Update the activity's state with the new ID
        Log.d("MainActivity2", "New note saved, ID updated to: " + newNoteId);
        if (btnLockNote != null) {
            btnLockNote.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container_main);

        if (currentFragment instanceof NoteDetailFragment) {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }


    private void showSetPinDialog(int noteId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_set_pin, null);
        EditText etPin = view.findViewById(R.id.etPinInput);
        EditText confirmPin = view.findViewById(R.id.confirm_pin);
        Button btnSubmit = view.findViewById(R.id.btnSetPin);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            String pin = etPin.getText().toString();
            String confirm = confirmPin.getText().toString();

            if (pin.length() != 4) {
                etPin.setError("PIN must be 4 digits");
            } else if (!pin.equals(confirm)) {
                confirmPin.setError("PINs do not match");
            } else {
                if (noteId != -1) {
                    dbHelper.setPinForNote(noteId, pin);
                    dialog.dismiss();
                    Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cannot set PIN for unsaved note.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showUnlockDialog(int noteId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_pin_input, null);
        EditText etPin = view.findViewById(R.id.etPinInput);
        Button btnSubmit = view.findViewById(R.id.btnUnlockNote);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            String enteredPin = etPin.getText().toString();
            String savedPin = dbHelper.getPinForNote(noteId);

            if (enteredPin.equals(savedPin)) {
                dialog.dismiss();
                Toast.makeText(this, "Access Granted", Toast.LENGTH_SHORT).show();
                showNoteDetailFragment(noteId);
                if (btnLockNote != null) {
                    btnLockNote.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                etPin.setError("Incorrect PIN");
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showNoteDetailFragment(int noteId) {
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle args = new Bundle();
        args.putInt(NoteDetailFragment.NOTE_ID_KEY, noteId);
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_main, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}