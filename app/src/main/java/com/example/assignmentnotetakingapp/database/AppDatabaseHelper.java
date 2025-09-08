package com.example.assignmentnotetakingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.assignmentnotetakingapp.models.Note;

import java.util.ArrayList;
import java.util.List;

public class AppDatabaseHelper extends SQLiteOpenHelper {

    // --- Database Info ---
    private static final String DATABASE_NAME = "app_database.db"; // Consolidated database file name
    private static final int DATABASE_VERSION = 1; // Start with version 1 for the combined schema


    // --- Table Names ---
    public static final String TABLE_USERS = "users";
    public static final String TABLE_NOTES = "notes";
    public static final String TABLE_PIN_LOCK = "pin_lock";


    // --- User Table Columns (from DBUser) ---
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PASSWORD_HINT = "password_hint";
    public static final String COLUMN_PROFILE_IMAGE_URI = "profile_image_uri";


    // --- Notes Table Columns (from DBHelper) ---
    public static final String COLUMN_NOTE_ID = "id";
    public static final String COLUMN_NOTE_TITLE = "title";
    public static final String COLUMN_NOTE_DETAILS = "details";
    public static final String COLUMN_NOTE_DATE = "date";
    public static final String COLUMN_NOTE_FAVORITE = "favorite";
    public static final String COLUMN_NOTE_URGENT = "urgent";



    // --- Pin Lock Table Columns (from DBPin) ---
    private static final String COLUMN_PIN_CODE = "pin_code";

    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // --- Create Users Table (using DBUser's schema) ---
        String createUsersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " + // Unique username is good practice
                COLUMN_EMAIL + " TEXT UNIQUE, " +   // Unique email is good practice
                COLUMN_PASSWORD + " TEXT NOT NULL, " + // Password should not be null
                COLUMN_PASSWORD_HINT + " TEXT," +
                COLUMN_PROFILE_IMAGE_URI + " TEXT" +
                ");";
        db.execSQL(createUsersTable);
        Log.d("AppDatabaseHelper", "Created " + TABLE_USERS + " table.");


        // --- Create Notes Table (using DBHelper's schema) ---
        String createNotesTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NOTES + " (" +
                COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOTE_TITLE + " TEXT, " +
                COLUMN_NOTE_DETAILS + " TEXT, " + // Content of the note
                COLUMN_NOTE_DATE + " TEXT, " +
                COLUMN_NOTE_FAVORITE + " INTEGER DEFAULT 0, " + // 0 for false, 1 for true
                COLUMN_NOTE_URGENT + " INTEGER DEFAULT 0, " +   // 0 for false, 1 for true
                COLUMN_USER_ID + " INTEGER, " + // Foreign key column
                // Define the foreign key constraint referencing the users table in *this* database
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);";
        db.execSQL(createNotesTable);
        Log.d("AppDatabaseHelper", "Created " + TABLE_NOTES + " table.");


        // --- Create Pin Lock Table (from DBPin's schema, linked to Notes) ---
        String createPinLockTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PIN_LOCK + " (" +
                // note_id here is the primary key for pin_lock and references notes.id
                COLUMN_NOTE_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_PIN_CODE + " TEXT NOT NULL," +
                // Add foreign key constraint referencing the notes table in *this* database
                "FOREIGN KEY(" + COLUMN_NOTE_ID + ") REFERENCES " + TABLE_NOTES + "(" + COLUMN_NOTE_ID + ") ON DELETE CASCADE);";
        db.execSQL(createPinLockTable);
        Log.d("AppDatabaseHelper", "Created " + TABLE_PIN_LOCK + " table.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w("AppDatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion
                + ". This will destroy all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PIN_LOCK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        onCreate(db);
    }

    // --- User Methods (from DBUser) ---

    // Insert a new user
    public long insertUser(String username, String email, String password, String passwordHint, String profileImageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password); // Hashing recommended!
        values.put(COLUMN_PASSWORD_HINT, passwordHint);
        values.put(COLUMN_PROFILE_IMAGE_URI, profileImageUri); // Save the image URI

        long result = db.insert(TABLE_USERS, null, values);

        if (db != null && db.isOpen()) {
            db.close();
        }
        return result;
    }

    // Authenticate a user by email and password
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                    new String[]{email, password},
                    null, null, null);
            exists = cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error checking user: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return exists;
    }

    // Check if a user with the given email exists
    public boolean checkEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_EMAIL + "=?",
                    new String[]{email},
                    null, null, null);
            exists = cursor != null && cursor.moveToFirst();
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error checking email existence: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return exists;
    }

    // Get user details by email
    public Cursor getUserDetailsByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USERNAME,
                COLUMN_EMAIL,
                COLUMN_PASSWORD_HINT,
                COLUMN_PROFILE_IMAGE_URI
        };
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        return db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);
    }

    // Get User ID by email
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int userId = -1;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_EMAIL + "=?",
                    new String[]{email},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            }
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error getting user ID by email: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return userId;
    }

    // Update user details by email (Includes new password)
    public int updateUserByEmail(String email, String newUsername, String newEmail, String newPassword,
                                 String newPasswordHint, String newProfileImageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, newUsername);
        values.put(COLUMN_EMAIL, newEmail);
        values.put(COLUMN_PASSWORD, newPassword);
        values.put(COLUMN_PASSWORD_HINT, newPasswordHint);
        values.put(COLUMN_PROFILE_IMAGE_URI, newProfileImageUri);
        String whereClause = COLUMN_EMAIL + " = ?";
        String[] whereArgs = {email};

        int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
        if (db != null && db.isOpen()) {
            db.close();
        }
        return rowsAffected;
    }

    // Get password hint by email
    public String getPasswordHint(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hint = null;
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_PASSWORD_HINT},
                    COLUMN_EMAIL + "=?",
                    new String[]{email},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                hint = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HINT));
            }
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error getting password hint: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return hint;
    }

    // --- Note Methods (from DBHelper) ---

    // Insert a new note
    public long insertNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_TITLE, note.getTitle());
        values.put(COLUMN_NOTE_DETAILS, note.getContent());
        values.put(COLUMN_NOTE_DATE, note.getDate());
        values.put(COLUMN_NOTE_FAVORITE, note.isFavorite() ? 1 : 0);
        values.put(COLUMN_NOTE_URGENT, note.isUrgent() ? 1 : 0);
        values.put(COLUMN_USER_ID, note.getUserId());

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_NOTES, null, values);
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error inserting note: " + e.getMessage() + " - Values: " +
                    values.toString(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return result;
    }

    // Get a specific note by its ID
    public Note getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Note note = null;
        try {
            cursor = db.query(TABLE_NOTES,
                    null, COLUMN_NOTE_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                note = extractNoteFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error getting note by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return note;
    }

    // Delete a note by its ID
    public int deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;
        try {
            result = db.delete(TABLE_NOTES, COLUMN_NOTE_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error deleting note: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return result;
    }

    // Get all notes for a specific user
    public List<Note> getAllNotesForUser(int userId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NOTES,
                    null, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)},
                    null, null, COLUMN_NOTE_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Note note = extractNoteFromCursor(cursor);
                    notes.add(note);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error getting all notes for user: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return notes;
    }

    // Update an existing note
    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_TITLE, note.getTitle());
        values.put(COLUMN_NOTE_DETAILS, note.getContent());
        values.put(COLUMN_NOTE_DATE, note.getDate());
        values.put(COLUMN_NOTE_FAVORITE, note.isFavorite() ? 1 : 0);
        values.put(COLUMN_NOTE_URGENT, note.isUrgent() ? 1 : 0);
        values.put(COLUMN_USER_ID, note.getUserId());

        String whereClause = COLUMN_NOTE_ID + " = ?";
        String[] whereArgs = {String.valueOf(note.getId())};

        int rowsAffected = 0;
        try {
            rowsAffected = db.update(TABLE_NOTES, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error updating note: " + e.getMessage() + " - Note ID: " + note.getId(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return rowsAffected;
    }

    public List<Note> getFavoriteNotes(int userId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NOTES,
                    null, COLUMN_USER_ID + "=? AND " + COLUMN_NOTE_FAVORITE + "=1",
                    new String[]{String.valueOf(userId)}, // Where args
                    null, null, COLUMN_NOTE_DATE + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    notes.add(extractNoteFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error getting favorite notes for user: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return notes;
    }

    public List<Note> getUrgentNotes(int userId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NOTES,
                    null, COLUMN_USER_ID + "=? AND " + COLUMN_NOTE_URGENT + "=1",
                    new String[]{String.valueOf(userId)},
                    null, null, COLUMN_NOTE_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    notes.add(extractNoteFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error getting urgent notes for user: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return notes;
    }


    private Note extractNoteFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_DETAILS));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_DATE));
        boolean favorite = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_FAVORITE)) == 1;
        boolean urgent = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_URGENT)) == 1;
        int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));

        Note note = new Note(id, title, content);
        note.setDate(date);
        note.setFavorite(favorite);
        note.setUrgent(urgent);
        note.setUserId(userId);
        return note;
    }


    // --- Pin Lock Methods (from DBPin) ---

    public void setPinForNote(int noteId, String pin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_ID, noteId);
        values.put(COLUMN_PIN_CODE, pin);
        try {
            db.insertWithOnConflict(TABLE_PIN_LOCK, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error setting pin for note: " + e.getMessage() + " - Note ID: " + noteId, e);

        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    // Get the PIN for a specific note
    public String getPinForNote(int noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String pin = null;
        try {
            cursor = db.query(TABLE_PIN_LOCK,
                    new String[]{COLUMN_PIN_CODE},
                    COLUMN_NOTE_ID + " = ?",
                    new String[]{String.valueOf(noteId)},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                pin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIN_CODE));
            }
        } catch (Exception e) {
            Log.e("AppDatabaseHelper", "Error getting pin for note: " + e.getMessage() + " - Note ID: " + noteId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return pin;
    }


    @Override
    public synchronized void close() {

        super.close();
    }

}