package com.example.studymate.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.R;
import com.example.studymate.models.NoteModel;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private ArrayList<NoteModel> noteList;
    private OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onClick(NoteModel note);
        void onLongClick(NoteModel note);
    }

    public NoteAdapter(ArrayList<NoteModel> noteList, OnNoteClickListener listener) {
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteModel note = noteList.get(position);
        holder.titleText.setText(note.getTitle());
        holder.dateText.setText("📅 " + note.getDate());

        String locationText = "📍 No location";
        if (note.getLatitude() != 0.0 || note.getLongitude() != 0.0) {
            locationText = String.format("📍 Lat: %.4f, Lng: %.4f", note.getLatitude(), note.getLongitude());
        }
        holder.locationText.setText(locationText);

        try {
            byte[] imageBytes = Base64.decode(note.getImagePath(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            holder.imageView.setImageResource(R.drawable.ic_image_placeholder); // fallback image
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(note));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(note);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleText, dateText, locationText;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.noteImage);
            titleText = itemView.findViewById(R.id.noteTitle);
            dateText = itemView.findViewById(R.id.noteDate);
            locationText = itemView.findViewById(R.id.noteLocation); // Must exist in item_note.xml
        }
    }
}
