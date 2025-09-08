package com.example.assignmentnotetakingapp.activities;

import android.content.Context;
import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignmentnotetakingapp.adapters.NoteAdapter; // Make sure NoteAdapter is imported
import com.example.assignmentnotetakingapp.R;
import com.example.assignmentnotetakingapp.models.Note; // Make sure Note model is imported
// Import the consolidated database helper
import com.example.assignmentnotetakingapp.database.AppDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NotesFragment extends Fragment {

    private EditText searchInput;
    private NoteAdapter noteAdapter;
    private AppDatabaseHelper dbHelper;
    private List<Note> noteList = new ArrayList<>();
    private RecyclerView recyclerView;
    private String category = "All";
    private int userId = -1;


    public NotesFragment() {

    }


    public static NotesFragment newInstance(String category, int userId) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putString("category", category);
        args.putInt("user_id", userId);
        fragment.setArguments(args);
        return fragment;
    }

    public String getFilterType() {
        return category;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category", "All");
            userId = getArguments().getInt("user_id", -1);
        }

        if (userId == -1) {
            Log.e("NotesFragment", "User ID was not passed to NotesFragment arguments!");
        } else {
            Log.d("NotesFragment", "NotesFragment created for User ID: " + userId + " with category: " + category);
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.recycler_notes);
        searchInput = view.findViewById(R.id.search_input);

        dbHelper = new AppDatabaseHelper(getContext());

        // --- Setup RecyclerView and Adapter ---
        if (recyclerView != null && getContext() != null) {
            noteAdapter = new NoteAdapter(getContext(), noteList, new NoteAdapter.OnNoteActionListener() {
                @Override
                public void onEdit(Note note) {

                    Intent intent = new Intent(getActivity(), MainActivity2.class);
                    intent.putExtra(MainActivity2.EXTRA_ACTION, MainActivity2.ACTION_VIEW_EDIT);
                    intent.putExtra(MainActivity2.EXTRA_NOTE_ID, note.getId());
                    startActivity(intent);
                }

                @Override
                public void onDelete(Note note) {
                    if (dbHelper != null) {
                        dbHelper.deleteNote(note.getId());
                        loadNotes();
                        Toast.makeText(getContext(), "Note deleted", Toast.LENGTH_SHORT).show(); // Show feedback
                    } else {
                        Log.e("NotesFragment", "dbHelper is null, cannot delete note!");
                        Toast.makeText(getContext(), "Error deleting note.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFavorite(Note note) {
                    note.setFavorite(!note.isFavorite());
                    if (dbHelper != null) {
                        dbHelper.updateNote(note);
                        loadNotes();
                        Toast.makeText(getContext(), note.isFavorite() ? "Marked as Favorite" : "Unmarked as Favorite", Toast.LENGTH_SHORT).show(); // Show feedback
                    } else {
                        Log.e("NotesFragment", "dbHelper is null, cannot update favorite status!");
                        Toast.makeText(getContext(), "Error updating note.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onUrgent(Note note) {
                    note.setUrgent(!note.isUrgent());
                    if (dbHelper != null) {
                        dbHelper.updateNote(note);
                        loadNotes();
                        Toast.makeText(getContext(), note.isUrgent() ? "Marked as Urgent" : "Unmarked as Urgent", Toast.LENGTH_SHORT).show(); // Show feedback
                    } else {
                        Log.e("NotesFragment", "dbHelper is null, cannot update urgent status!");
                        Toast.makeText(getContext(), "Error updating note.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(noteAdapter);
        } else {
            Log.e("NotesFragment", "RecyclerView not found or Context is null!");
        }


        // --- Setup Search Input Listener ---
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterNotes(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        } else {
            Log.e("NotesFragment", "Search input EditText not found in layout!");
        }

        loadNotes();
        return view;
    }

    private void loadNotes() {
        if (userId == -1 || dbHelper == null) {
            if (userId == -1) Log.w("NotesFragment", "Attempted to load notes with invalid User ID (-1). Aborting load.");
            if (dbHelper == null) Log.w("NotesFragment", "Attempted to load notes with null dbHelper. Aborting load.");

            noteList.clear();
            if (noteAdapter != null) {
                noteAdapter.notifyDataSetChanged();
            }
            return;
        }

        Log.d("NotesFragment", "Loading notes for User ID: " + userId + " in category: " + category);

        List<Note> loadedNotes = new ArrayList<>();
        switch (category) {
            case "Favorite":
                loadedNotes.addAll(dbHelper.getFavoriteNotes(userId));
                break;
            case "Urgent":
                loadedNotes.addAll(dbHelper.getUrgentNotes(userId));
                break;
            default:
                loadedNotes.addAll(dbHelper.getAllNotesForUser(userId));
        }

        noteList.clear();
        noteList.addAll(loadedNotes);
        if (noteAdapter != null) {
            noteAdapter.notifyDataSetChanged();
        } else {
            Log.e("NotesFragment", "NoteAdapter is null, cannot notifyDataSetChanged after loadNotes!");
        }
    }

    private void filterNotes(String query) {
        if (userId == -1 || dbHelper == null) {
            if (userId == -1) Log.w("NotesFragment", "Attempted to filter notes with invalid User ID (-1). Aborting filter.");
            if (dbHelper == null) Log.w("NotesFragment", "Attempted to filter notes with null dbHelper. Aborting filter.");
            noteList.clear();
            if (noteAdapter != null) {
                noteAdapter.notifyDataSetChanged();
            }
            return;
        }


        Log.d("NotesFragment", "Filtering notes for User ID: " + userId + " in category: " + category + " with query: '" + query + "'");

        List<Note> notesToFilter = new ArrayList<>();
        switch (category) {
            case "Favorite":
                notesToFilter.addAll(dbHelper.getFavoriteNotes(userId));
                break;
            case "Urgent":
                notesToFilter.addAll(dbHelper.getUrgentNotes(userId));
                break;
            default:
                notesToFilter.addAll(dbHelper.getAllNotesForUser(userId));
        }


        List<Note> filtered = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(notesToFilter);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Note note : notesToFilter) {
                if (note.getTitle() != null && note.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filtered.add(note);
                }
            }
        }

        noteList.clear();
        noteList.addAll(filtered);
        if (noteAdapter != null) {
            noteAdapter.notifyDataSetChanged();
        } else {
            Log.e("NotesFragment", "NoteAdapter is null, cannot notifyDataSetChanged after filter!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
        if (searchInput != null && searchInput.getText() != null && !searchInput.getText().toString().isEmpty()) {
            filterNotes(searchInput.getText().toString());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        searchInput = null;
        noteAdapter = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
        noteList.clear();
        Log.d("NotesFragment", "onDestroy: Database helper closed, note list cleared.");
    }
}