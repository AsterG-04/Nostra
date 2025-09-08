package com.example.assignmentnotetakingapp.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignmentnotetakingapp.R;
import com.example.assignmentnotetakingapp.database.AppDatabaseHelper;

public class PinActivity extends AppCompatActivity {

    private int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_set_pin);

        noteId = 1;

        Button btnSetPin = findViewById(R.id.btnSetPin);
        @SuppressLint("MissingInflatedId") Button btnUnlock = findViewById(R.id.btnUnlockNote);

        btnSetPin.setOnClickListener(v -> showSetPinDialog(noteId));
        btnUnlock.setOnClickListener(v -> showUnlockDialog(noteId));
    }

    private void showUnlockDialog(int noteId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_pin_input, null);
        EditText etPin = view.findViewById(R.id.etPinInput);
        Button btnSubmit = view.findViewById(R.id.btnUnlockNote);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        AppDatabaseHelper dbPin = new AppDatabaseHelper(this);
        String savedPin = dbPin.getPinForNote(noteId);

        btnSubmit.setOnClickListener(v -> {
            String entered = etPin.getText().toString();
            if (entered.equals(savedPin)) {
                dialog.dismiss();
                Toast.makeText(this, "Note Unlocked", Toast.LENGTH_SHORT).show();
            } else {
                etPin.setError("Incorrect PIN");
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    private void showSetPinDialog(int noteId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_set_pin, null);
        EditText etPin = view.findViewById(R.id.etPinInput);
        EditText confirmPin = view.findViewById(R.id.confirm_pin);
        Button btnSubmit = view.findViewById(R.id.btnSetPin);
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
                AppDatabaseHelper dbPin = new AppDatabaseHelper(this);
                dbPin.setPinForNote(noteId, pin);
                dialog.dismiss();
                Toast.makeText(this, "PIN set for note", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

}
