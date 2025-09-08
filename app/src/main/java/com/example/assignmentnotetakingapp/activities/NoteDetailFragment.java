package com.example.assignmentnotetakingapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import com.example.assignmentnotetakingapp.R;
import com.example.assignmentnotetakingapp.models.Note;
import com.example.assignmentnotetakingapp.database.AppDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

public class NoteDetailFragment extends Fragment {

    public static final String NOTE_ID_KEY = "NOTE_ID_KEY";
    private int currentNoteId = -1;

    private EditText etNoteTitle, noteEditText;
    private Button btnEdit1;
    private ImageButton btnFavorite;
    private ImageButton btnUrgent;
    private ImageButton btnZoomIn;
    private ImageButton btnZoomOut;
    private ImageButton btnUndo;
    private ImageButton btnRedo;
    private ImageButton btnBackground;
    private ImageButton btnMic;
    private ImageButton btnEditTextContent;
    private ImageButton btnDelete;
    private ImageButton btnBack;
    private ImageButton btnSaveNoteSidebar;


    private float textSize = 16f;
    private final Stack<String> undoStack = new Stack<>();
    private final Stack<String> redoStack = new Stack<>();
    private int bgStyleIndex = 0;
    private final int[] bgStyles = {
            R.color.soft_beige,
            R.color.blush_pink,
            R.color.pastel_lavender,
            R.color.mint_cream,
            R.color.sky_mist,
            R.color.light_peach,
            R.color.buttercream
    };
    private boolean isFavorite = false;
    private boolean isUrgent = false;
    private boolean isUndoOrRedo = false;

    private static final int SPEECH_REQUEST_CODE = 1;
    private View rightSidebar;
    private ImageButton btnToggleSidebar;
    private LinearLayout sectionA, sectionB, sectionC, sectionD;

    private SharedPreferences sharedPreferences;
    private AppDatabaseHelper dbHelper;

    public NoteDetailFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentNoteId = getArguments().getInt(NOTE_ID_KEY, -1);
        }
        dbHelper = new AppDatabaseHelper(getContext());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_detail, container, false);

        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        // --- Find UI elements ---
        etNoteTitle = view.findViewById(R.id.etNoteTitle);
        noteEditText = view.findViewById(R.id.note_text);

        btnEdit1 = view.findViewById(R.id.btnEdit1);
        btnSaveNoteSidebar = view.findViewById(R.id.btn_save_note);
        btnToggleSidebar = view.findViewById(R.id.btn_toggle_sidebar);  
        btnFavorite = view.findViewById(R.id.note_favorite_icon);  
        btnUrgent = view.findViewById(R.id.note_urgent_icon);  
        btnZoomIn = view.findViewById(R.id.btn_zoom_in);  
        btnZoomOut = view.findViewById(R.id.btn_zoom_out);  
        btnUndo = view.findViewById(R.id.btn_undo);  
        btnRedo = view.findViewById(R.id.btn_redo);  
        btnBackground = view.findViewById(R.id.btn_change_background);  
        btnMic = view.findViewById(R.id.btn_mic);  
        btnEditTextContent = view.findViewById(R.id.btn_edit_text_content);  
        btnDelete = view.findViewById(R.id.btn_delete);  
        btnBack = view.findViewById(R.id.btnBack);  


        // Floating bar sections (LinearLayouts)
        sectionA = view.findViewById(R.id.sectionA);
        sectionB = view.findViewById(R.id.sectionB);
        sectionC = view.findViewById(R.id.sectionC);
        sectionD = view.findViewById(R.id.sectionD);

        // Sidebar view (LinearLayout included by ID)
        rightSidebar = view.findViewById(R.id.included_sidebar);


        if (currentNoteId != -1) {
            loadNoteFromDB(currentNoteId);
            if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);
        } else {
            isFavorite = false;
            isUrgent = false;
            bgStyleIndex = 0;
            if (noteEditText != null) {
                noteEditText.setBackgroundResource(bgStyles[bgStyleIndex]);
            }
            if (btnDelete != null) btnDelete.setVisibility(View.GONE);
        }

        updateStatusIcons();

        if (btnEdit1 != null) {
            if (currentNoteId == -1) {
                btnEdit1.setText("ADD NOTE");
            } else {
                btnEdit1.setText("EDIT NOTE");
            }
            btnEdit1.setOnClickListener(v -> {
                saveNote();
            });
        } else {
            Log.e("NoteFragment", "Main Save Button (btnEdit1) not found in layout!");
        }


        if (noteEditText != null) {
            noteEditText.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int offset = noteEditText.getOffsetForPosition(event.getX(), event.getY());

                    if (offset >= 0 && offset < noteEditText.length()) {
                        char tappedChar = noteEditText.getText().charAt(offset);
                        Editable editable = noteEditText.getText();

                        if (tappedChar == '\u2610') {
                            editable.replace(offset, offset + 1, "\u2611");
                            return true;
                        } else if (tappedChar == '\u2611') {
                            editable.replace(offset, offset + 1, "\u2610");
                            return true;
                        }
                    }
                }

                return false;
            });
        }

        if (noteEditText != null) {
            noteEditText.addTextChangedListener(new TextWatcher() {
                private String previousText = "";
                private boolean isEditing = false;

                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                    previousText = charSequence.toString();
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    if (!isUndoOrRedo && !previousText.equals(charSequence.toString())) {
                        undoStack.push(previousText);
                        redoStack.clear();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (isEditing || noteEditText == null) return;

                    int cursorPos = noteEditText.getSelectionStart();
                    if (cursorPos < 1 || editable.length() < cursorPos) return;

                    if (editable.charAt(cursorPos - 1) == '\n') {
                        isEditing = true;

                        int prevLineEnd = cursorPos - 2;
                        int prevLineStart = getLineStart(editable, prevLineEnd);

                        if (prevLineStart >= 0) {
                            String prevLine = editable.subSequence(prevLineStart, prevLineEnd + 1).toString().trim();

                            // --- Auto-checkboxing ---
                            if (prevLine.startsWith("\u2610 ") || prevLine.startsWith("\u2611 ")) {
                                String insertText = "\u2610 ";
                                editable.insert(cursorPos, insertText);
                                noteEditText.setSelection(cursorPos + insertText.length());
                            }
                            // --- Auto-numbering ---
                            else if (prevLine.matches("^\\d+\\.\\s?.*")) {
                                String numberStr = prevLine.split("\\.")[0].trim();
                                try {
                                    int number = Integer.parseInt(numberStr);
                                    number++;
                                    String insertText = number + ". ";
                                    editable.insert(cursorPos, insertText);
                                    noteEditText.setSelection(cursorPos + insertText.length());
                                } catch (NumberFormatException e) {
                                    Log.e("NoteDetailFragment", "NumberFormatException during auto-numbering: " + e.getMessage());
                                }
                            }
                        }
                        isEditing = false;
                    }
                }
            });
        }


        if (btnToggleSidebar != null && rightSidebar != null) {
            btnToggleSidebar.setOnClickListener(v -> {
                rightSidebar.setVisibility(rightSidebar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });
        } else { Log.e("NoteFragment", "Sidebar toggle button or sidebar view not found!"); }


        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                isFavorite = !isFavorite;
                updateStatusIcons();
                Toast.makeText(getContext(), isFavorite ? "Marked as Favorite" : "Unmarked as Favorite", Toast.LENGTH_SHORT).show();
            });
        } else { Log.e("NoteFragment", "Favorite button (note_favorite_icon) not found"); }


        if (btnUrgent != null) {
            btnUrgent.setOnClickListener(v -> {
                isUrgent = !isUrgent;
                updateStatusIcons();
                Toast.makeText(getContext(), isUrgent ? "Marked as Urgent" : "Unmarked as Urgent", Toast.LENGTH_SHORT).show();
            });
        } else { Log.e("NoteFragment", "Urgent button (note_urgent_icon) not found"); }


        if (btnZoomIn != null && noteEditText != null) {
            btnZoomIn.setOnClickListener(v -> {
                textSize += 2f;
                noteEditText.setTextSize(textSize);
            });
        } else { Log.e("NoteFragment", "ZoomIn button (btn_zoom_in) or noteEditText not found"); }

        if (btnZoomOut != null && noteEditText != null) {
            btnZoomOut.setOnClickListener(v -> {
                textSize = Math.max(10f, textSize - 2f);
                noteEditText.setTextSize(textSize);
            });
        } else { Log.e("NoteFragment", "ZoomOut button (btn_zoom_out) or noteEditText not found"); }


        if (btnUndo != null && noteEditText != null) {
            btnUndo.setOnClickListener(v -> {
                if (!undoStack.isEmpty()) {
                    isUndoOrRedo = true;
                    redoStack.push(noteEditText.getText().toString());
                    String previous = undoStack.pop();
                    noteEditText.setText(previous);
                    noteEditText.setSelection(previous.length());
                    isUndoOrRedo = false;
                } else {
                    Toast.makeText(getContext(), "Nothing to undo", Toast.LENGTH_SHORT).show();
                }
            });
        } else { Log.e("NoteFragment", "Undo button (btn_undo) or noteEditText not found"); }


        if (btnRedo != null && noteEditText != null) {
            btnRedo.setOnClickListener(v -> {
                if (!redoStack.isEmpty()) {
                    isUndoOrRedo = true;
                    undoStack.push(noteEditText.getText().toString());
                    String next = redoStack.pop();
                    noteEditText.setText(next);
                    noteEditText.setSelection(next.length());
                    isUndoOrRedo = false;
                } else {
                    Toast.makeText(getContext(), "Nothing to redo", Toast.LENGTH_SHORT).show();
                }
            });
        } else { Log.e("NoteFragment", "Redo button (btn_redo) or noteEditText not found"); }


        if (btnBackground != null && noteEditText != null) {
            btnBackground.setOnClickListener(v -> {
                bgStyleIndex = (bgStyleIndex + 1) % bgStyles.length;
                noteEditText.setBackgroundResource(bgStyles[bgStyleIndex]);

            });
        } else { Log.e("NoteFragment", "Background button (btn_change_background) or noteEditText not found"); }


        if (btnMic != null) {
            btnMic.setOnClickListener(v -> {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                // work with speech recognition, if it's available on the device
                if (getContext() != null && intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivityForResult(intent, SPEECH_REQUEST_CODE);
                } else {
                    Toast.makeText(getContext(), "Speech input not supported on your device", Toast.LENGTH_SHORT).show();
                }
            });
        } else { Log.e("NoteFragment", "Mic button (btn_mic) not found"); }


        if (btnEditTextContent != null && noteEditText != null) { // Null check for noteEditText
            btnEditTextContent.setOnClickListener(v -> {
                boolean editable = noteEditText.isEnabled();
                noteEditText.setEnabled(!editable);
                Toast.makeText(getContext(), editable ? "Editing Disabled" : "Editing Enabled", Toast.LENGTH_SHORT).show();
            });
        } else { Log.e("NoteFragment", "Edit Text button (btn_edit_text_content) or noteEditText not found"); }


        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (currentNoteId != -1) {
                                deleteNoteFromDB(currentNoteId);
                            }

                            if (noteEditText != null) noteEditText.setText("");
                            if (etNoteTitle != null) etNoteTitle.setText("");
                            undoStack.clear();
                            redoStack.clear();
                            isFavorite = false;
                            isUrgent = false;
                            updateStatusIcons();
                            currentNoteId = -1;
                            Toast.makeText(getContext(), "Note Deleted", Toast.LENGTH_SHORT).show();
                            requireActivity().finish();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else { Log.e("NoteFragment", "Delete button (btn_delete) not found"); }


        if (btnSaveNoteSidebar != null) {
            btnSaveNoteSidebar.setOnClickListener(v -> {
                saveNote();
            });
        } else {
            Log.e("NoteFragment", "Sidebar Save Button (btn_save_note) not found in layout!");
        }

        // --- Set Listener for the Back button (btnBack) ---
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                saveNote();
                requireActivity().finish();
            });
        } else {
            Log.e("NoteFragment", "Back button (btnBack) not found in layout!");
        }


        // Floating bar section toggle listeners (Find buttons and LinearLayout sections)
        ImageButton btnTextA = view.findViewById(R.id.btn_text);
        ImageButton btnFontsA = view.findViewById(R.id.btn_fonts);
        ImageButton btnListA = view.findViewById(R.id.btn_list);

        ImageButton btnTextB_Back = view.findViewById(R.id.btn_text2);
        ImageButton btnFontsC_Back = view.findViewById(R.id.btn_fonts1);
        ImageButton btnListD_Back = view.findViewById(R.id.btn_list1);

        // Set listeners with null checks for buttons and sections
        if (btnTextA != null && sectionB != null) btnTextA.setOnClickListener(v -> showSection(sectionB)); else { Log.e("NoteFragment", "btnTextA or sectionB not found"); }
        if (btnFontsA != null && sectionC != null) btnFontsA.setOnClickListener(v -> showSection(sectionC)); else { Log.e("NoteFragment", "btnFontsA or sectionC not found"); }
        if (btnListA != null && sectionD != null) btnListA.setOnClickListener(v -> showSection(sectionD)); else { Log.e("NoteFragment", "btnListA or sectionD not found"); }
        if (btnTextB_Back != null && sectionA != null) btnTextB_Back.setOnClickListener(v -> showSection(sectionA)); else { Log.e("NoteFragment", "btnTextB_Back or sectionA not found"); }
        if (btnFontsC_Back != null && sectionA != null) btnFontsC_Back.setOnClickListener(v -> showSection(sectionA)); else { Log.e("NoteFragment", "btnFontsC_Back or sectionA not found"); }
        if (btnListD_Back != null && sectionA != null) btnListD_Back.setOnClickListener(v -> showSection(sectionA)); else { Log.e("NoteFragment", "btnListD_Back or sectionA not found"); }


        // Section B buttons - Text Formatting
        ImageButton btnBold = view.findViewById(R.id.btn_bold);
        ImageButton btnItalic = view.findViewById(R.id.btn_italic);
        ImageButton btnUnderline = view.findViewById(R.id.btn_underline);
        ImageButton btnStrikeThrough = view.findViewById(R.id.btn_strike_through);

        // Ensure noteEditText is not null before setting format listeners
        if (noteEditText != null) {

            if (btnBold != null) {
                btnBold.setOnClickListener(v -> setFont(R.font.bold));
            } else {
                Log.e("NoteFragment", "btnBold not found");
            }

            if (btnItalic != null) {
                btnItalic.setOnClickListener(v -> setFont(R.font.italic));
            } else {
                Log.e("NoteFragment", "btnItalic not found");
            }

            if (btnUnderline != null) {
                btnUnderline.setOnClickListener(v -> setFont(R.font.underline));
            } else {
                Log.e("NoteFragment", "btnUnderline not found");
            }

            if (btnStrikeThrough != null) {
                btnStrikeThrough.setOnClickListener(v -> setFont(R.font.strikethrough));
            } else {
                Log.e("NoteFragment", "btnStrikeThrough not found");
            }

        } else {
            Log.e("NoteFragment", "noteEditText is null, text formatting buttons won't work.");
        }


        // Section C buttons - Font Styles
        ImageButton btnFontSlabSerif = view.findViewById(R.id.btn_font_slab_serif);
        ImageButton btnFontSerif = view.findViewById(R.id.btn_font_serif);
        ImageButton btnFontScript = view.findViewById(R.id.font_script);

        // Ensure noteEditText is not null before setting font listeners
        if (noteEditText != null) {
            if (btnFontSlabSerif != null) btnFontSlabSerif.setOnClickListener(v -> setFont(R.font.slabserif)); else { Log.e("NoteFragment", "btnFontSlabSerif not found"); }
            if (btnFontSerif != null) btnFontSerif.setOnClickListener(v -> setFont(R.font.serif)); else { Log.e("NoteFragment", "btnFontSerif not found"); }
            if (btnFontScript != null) btnFontScript.setOnClickListener(v -> setFont(R.font.script)); else { Log.e("NoteFragment", "btnFontScript not found"); }
        } else {
            Log.e("NoteFragment", "noteEditText is null, font style buttons won't work.");
        }


        // Section A buttons
        ImageButton btnCheckList = view.findViewById(R.id.btn_checklist);
        ImageButton btnImage = view.findViewById(R.id.btn_image);


        // Ensure noteEditText is not null before setting list/image listeners
        if (noteEditText != null) {
            if (btnCheckList != null) btnCheckList.setOnClickListener(v -> insertCheckList()); else { Log.e("NoteFragment", "btnCheckList not found"); }
            if (btnImage != null) {
                btnImage.setOnClickListener(v -> insertImage());
            }        } else {
            Log.e("NoteFragment", "noteEditText is null, checklist/image buttons won't work.");
        }

        // Section D buttons - list types
        ImageButton btnCircleList = view.findViewById(R.id.btn_circlelist);
        ImageButton btnSquareList = view.findViewById(R.id.btn_squarelist);
        ImageButton btnNumberList = view.findViewById(R.id.btn_numberlist);

        // Ensure noteEditText is not null before setting list type listeners
        if (noteEditText != null) {
            if (btnCircleList != null) btnCircleList.setOnClickListener(v -> applyListStyle("circle")); else { Log.e("NoteFragment", "btnCircleList not found"); }
            if (btnSquareList != null) btnSquareList.setOnClickListener(v -> applyListStyle("square")); else { Log.e("NoteFragment", "btnSquareList not found"); }
            if (btnNumberList != null) btnNumberList.setOnClickListener(v -> applyListStyle("number")); else { Log.e("NoteFragment", "btnNumberList not found"); }
        } else {
            Log.e("NoteFragment", "noteEditText is null, list type buttons won't work.");
        }


        return view;
    }

    // Helper method to toggle floating bar sections visibility
    private void showSection(LinearLayout sectionToShow) {
        if (sectionA != null) sectionA.setVisibility(sectionA == sectionToShow ? View.VISIBLE : View.GONE);
        if (sectionB != null) sectionB.setVisibility(sectionB == sectionToShow ? View.VISIBLE : View.GONE);
        if (sectionC != null) sectionC.setVisibility(sectionC == sectionToShow ? View.VISIBLE : View.GONE);
        if (sectionD != null) sectionD.setVisibility(sectionD == sectionToShow ? View.VISIBLE : View.GONE);
    }

    // Helper method to set typeface
    private void setFont(int fontResId) {
        Context context = getContext();
        if (context != null && noteEditText != null && noteEditText.getText() != null) { // Add null checks
            try {
                Typeface typeface = ResourcesCompat.getFont(context, fontResId);
                if (typeface != null) {
                    noteEditText.setTypeface(typeface);
                } else {
                    Toast.makeText(context, "Font not found", Toast.LENGTH_SHORT).show();
                    Log.e("NoteFragment", "Font resource not found: " + fontResId);
                }
            } catch (Exception e) {
                Toast.makeText(context, "Error loading font", Toast.LENGTH_SHORT).show();
                Log.e("NoteFragment", "Error setting font: " + e.getMessage(), e);
            }
        }
    }

    private int getLineStart(Editable text, int pos) {
        if (pos < 0 || pos >= text.length()) {
            return 0;
        }

        for (int i = pos; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                return i + 1;
            }
        }
        return 0;
    }


    private int getLoggedInUserId() {

        int userId = sharedPreferences.getInt("userId", -1);
        if (userId == -1) {
            Log.e("NoteDetailFragment", "Logged-in user ID not found in SharedPreferences!");
            Toast.makeText(getContext(), "User session expired or invalid. Please log in again.", Toast.LENGTH_LONG).show();

            if (getActivity() != null) {
                getActivity().finish();
            }
        }
        return userId;
    }


    // --- Load note from DB ---
    private void loadNoteFromDB(int noteId) {
        Note loadedNote = dbHelper.getNote(noteId);
        if (loadedNote != null) {
            if (etNoteTitle != null) etNoteTitle.setText(loadedNote.getTitle());
            if (noteEditText != null) noteEditText.setText(loadedNote.getContent());

            isFavorite = loadedNote.isFavorite();
            isUrgent = loadedNote.isUrgent();
            updateStatusIcons();


        } else {
            Log.w("NoteDetailFragment", "Note with ID " + noteId + " not found for loading.");
            if (etNoteTitle != null) etNoteTitle.setText("");
            if (noteEditText != null) noteEditText.setText("");
            isFavorite = false;
            isUrgent = false;
            currentNoteId = -1;
            updateStatusIcons();
            Toast.makeText(getContext(), "Note not found, creating new note...", Toast.LENGTH_SHORT).show(); // Indicate new note creation
        }
    }


    // --- Save or Update Note ---
    private void saveNote() {
        if (etNoteTitle == null || noteEditText == null) {
            Log.e("NoteFragment", "Cannot save note, EditTexts are null.");
            Toast.makeText(getContext(), "Error: Cannot save note.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etNoteTitle.getText().toString().trim();
        String content = noteEditText.getText().toString().trim();

        int userId = getLoggedInUserId();
        if (userId == -1) {
            Toast.makeText(getContext(), "Error: User not identified for saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty() && content.isEmpty()) {
            if (currentNoteId != -1) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Empty Note")
                        .setMessage("Save an empty note or delete it?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteNoteFromDB(currentNoteId);
                            Toast.makeText(getContext(), "Empty Note Deleted", Toast.LENGTH_SHORT).show();
                            if (getActivity() != null) {
                                requireActivity().finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(getContext(), "Note is empty!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Create a Note object to pass to DBHelper
        Note noteToSave = new Note(currentNoteId, title, content);
        noteToSave.setFavorite(isFavorite);
        noteToSave.setUrgent(isUrgent);
        noteToSave.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        noteToSave.setUserId(userId);


        if (currentNoteId == -1) {
            // Inserting a new note
            long newRowId = dbHelper.insertNote(noteToSave);
            if (newRowId != -1) {
                currentNoteId = (int) newRowId;
                Toast.makeText(getContext(), "Note Saved", Toast.LENGTH_SHORT).show();

               Activity hostActivity = getActivity();
                if (hostActivity instanceof MainActivity2) {
                    ((MainActivity2) hostActivity).onNewNoteSaved(currentNoteId);
                }

                if (btnEdit1 != null) {
                    btnEdit1.setText("EDIT NOTE");
                }

            } else {
                Toast.makeText(getContext(), "Error saving new note", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Updating an existing note
            noteToSave.setId(currentNoteId);
            int rowsAffected = dbHelper.updateNote(noteToSave);
            if (rowsAffected > 0) {
                Toast.makeText(getContext(), "Note Updated", Toast.LENGTH_SHORT).show();

            } else {
                Log.w("NoteDetailFragment", "Note update failed or no changes for ID: " + currentNoteId);
                Toast.makeText(getContext(), "Note update failed or no changes", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteNoteFromDB(int noteId) {
        int rowsDeleted = dbHelper.deleteNote(noteId);
        if (rowsDeleted > 0) {
            Log.d("NoteDetailFragment", "Note deleted successfully with ID: " + noteId);
        } else {
            Log.w("NoteDetailFragment", "Note with ID " + noteId + " not found for deletion.");
            Toast.makeText(getContext(), "Error deleting note.", Toast.LENGTH_SHORT).show(); // Provide feedback if delete failed in DB
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && noteEditText != null) { // Add null check for noteEditText and Activity
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                int start = noteEditText.getSelectionStart();
                noteEditText.getText().insert(start, spokenText + " ");
            }
        }
    }

    private void insertCheckList() {
        if (noteEditText == null || noteEditText.getText() == null) return;
        int start = noteEditText.getSelectionStart();
        noteEditText.getText().insert(start, "\u2610 ");
    }

    private void insertImage() {
        Toast.makeText(getContext(), "Image Insertion Coming Soon", Toast.LENGTH_SHORT).show();
    }

    private void applyListStyle(String style) {
        if (noteEditText == null || noteEditText.getText() == null) return;
        int start = noteEditText.getSelectionStart();
        Editable text = noteEditText.getText();

        int lineStart = getLineStart(text, start);
        String bullet = "";
        switch (style) {
            case "circle":
                bullet = "\u2022 ";
                break;
            case "square":
                bullet = "\u25AA ";
                break;
            case "number":
                int prevLineEnd = lineStart - 1;
                int prevLineStart = getLineStart(text, prevLineEnd);
                int lineNumber = 1;

                if (prevLineStart >= 0 && prevLineEnd >= prevLineStart && text != null) {
                    String prevLine = text.subSequence(prevLineStart, prevLineEnd + 1).toString().trim();
                    if (prevLine.matches("^\\d+\\.\\s.*")) {
                        try {
                            lineNumber = Integer.parseInt(prevLine.split("\\.")[0]) + 1;
                        } catch (NumberFormatException e) {
                            Log.e("NoteDetailFragment", "Error parsing previous line number for list: " + e.getMessage());
                        }
                    }
                }
                bullet = lineNumber + ". ";
                break;
            default:
                return;
        }

        if (lineStart >= 0 && lineStart <= text.length() && text != null) {
            text.insert(lineStart, bullet);
        }
    }

    private void updateStatusIcons() {
        if (btnFavorite != null) {
            btnFavorite.setImageResource(isFavorite ? R.drawable.heart2 : R.drawable.favourite);
        } else {
            Log.w("NoteDetailFragment", "btnFavorite is null, cannot update icon.");
        }
        if (btnUrgent != null) {
            btnUrgent.setImageResource(isUrgent ? R.drawable.urgent_on1 : R.drawable.urgent_off1);
        } else {
            Log.w("NoteDetailFragment", "btnUrgent is null, cannot update icon.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        etNoteTitle = null;
        noteEditText = null;
        btnEdit1 = null;
        btnFavorite = null;
        btnUrgent = null;
        btnZoomIn = null;
        btnZoomOut = null;
        btnUndo = null;
        btnRedo = null;
        btnBackground = null;
        btnMic = null;
        btnEditTextContent = null;
        btnDelete = null;
        btnBack = null;
        btnSaveNoteSidebar = null;
        rightSidebar = null;
        btnToggleSidebar = null;
        sectionA = null;
        sectionB = null;
        sectionC = null;
        sectionD = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}