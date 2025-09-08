package com.example.assignmentnotetakingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignmentnotetakingapp.R;
import com.example.assignmentnotetakingapp.models.Note;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
        private Context context;
        private List<Note> noteList;
        private OnNoteActionListener listener;

        public interface OnNoteActionListener {
            void onEdit(Note note);
            void onDelete(Note note);
            void onFavorite(Note note);

            void onUrgent(Note note);
        }

        public NoteAdapter(Context context, List<Note> noteList, OnNoteActionListener onNoteActionListener) {
            this.context = context;
            this.noteList = noteList;
            this.listener = onNoteActionListener;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
                Note note = noteList.get(position);
                holder.title.setText(note.getTitle());
                holder.date.setText(note.getDate());

                // Favorite icon logic
                holder.star.setImageResource(note.isFavorite() ? R.drawable.heart2 : R.drawable.favourite);
                holder.star.setOnClickListener(v -> {
                    v.setAlpha(0.6f); // Pressed effect
                    v.postDelayed(() -> v.setAlpha(1f), 150);
                    listener.onFavorite(note);
                });

                //Urgent icon logic
                holder.urgent.setImageResource(note.isUrgent() ? R.drawable.urgent_on : R.drawable.urgent_off);
                holder.urgent.setOnClickListener(v -> {
                v.setAlpha(0.6f);
                v.postDelayed(() -> v.setAlpha(1f), 150);
                listener.onUrgent(note);
                });

                // More menu (edit/delete)
                holder.more.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(context, holder.more);
                    MenuInflater inflater = popupMenu.getMenuInflater();
                    inflater.inflate(R.menu.note_menu, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_edit) {
                            listener.onEdit(note);
                            return true;
                        } else if (item.getItemId() == R.id.action_delete) {
                            listener.onDelete(note);
                            return true;
                        }
                        return false;
                    });

                    popupMenu.show();
                });

            }

        @Override
        public int getItemCount() {
            return noteList != null ? noteList.size() : 0;
        }

        public static class NoteViewHolder extends RecyclerView.ViewHolder {
            TextView title, date;
            ImageView star, urgent, more;

            public NoteViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.note_title);
                date = itemView.findViewById(R.id.note_date);
                star = itemView.findViewById(R.id.note_favorite_icon);
                urgent = itemView.findViewById(R.id.note_urgent_icon);
                more = itemView.findViewById(R.id.note_more_icon);
            }
        }
    }


