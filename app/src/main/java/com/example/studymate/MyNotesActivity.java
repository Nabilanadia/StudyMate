package com.example.studymate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.*;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.studymate.adapters.NoteAdapter;
import com.example.studymate.models.NoteModel;
import com.google.firebase.firestore.*;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyNotesActivity extends AppCompatActivity {

    private static final int REQUEST_RETAKE_PHOTO = 200;

    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private ArrayList<NoteModel> noteList;
    private FirebaseFirestore db;
    private Button backBtn;

    private NoteModel noteBeingEdited;
    private AlertDialog currentEditDialog;
    private ImageView editImageView;
    private TextView dateText;

    private Calendar selectedCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notes);

        db = FirebaseFirestore.getInstance();
        noteList = new ArrayList<>();

        recyclerView = findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NoteAdapter(noteList, new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onClick(NoteModel note) {
                showEditDialog(note);
            }

            @Override
            public void onLongClick(NoteModel note) {
                showDeleteDialog(note);
            }
        });

        recyclerView.setAdapter(adapter);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        loadNotes(); // Load notes from Firestore
    }

    private void loadNotes() {
        db.collection("study_notes") // ✅ correct collection name
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        showPopup("Error", "Failed to load notes.");
                        return;
                    }

                    noteList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        NoteModel note = doc.toObject(NoteModel.class);
                        note.setId(doc.getId());
                        noteList.add(note);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showEditDialog(NoteModel note) {
        this.noteBeingEdited = note;

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_note, null);
        EditText titleInput = view.findViewById(R.id.editNoteTitle);
        editImageView = view.findViewById(R.id.editNoteImage);
        dateText = view.findViewById(R.id.editNoteDate);

        titleInput.setText(note.getTitle());
        dateText.setText(note.getDate());

        try {
            byte[] decodedBytes = Base64.decode(note.getImagePath(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            editImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            editImageView.setImageResource(R.drawable.ic_image_placeholder);
        }

        dateText.setOnClickListener(v -> showDateTimePicker());

        currentEditDialog = new AlertDialog.Builder(this)
                .setTitle("Edit Note")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    note.setTitle(titleInput.getText().toString());
                    note.setDate(dateText.getText().toString());
                    db.collection("study_notes").document(note.getId())
                            .set(note)
                            .addOnSuccessListener(unused -> showPopup("Success", "✅ Note updated successfully."))
                            .addOnFailureListener(e -> showPopup("Error", "❌ Failed to update note."));
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Retake Photo", (dialog, which) -> openRetakeCamera())
                .create();

        currentEditDialog.show();
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String formatted = sdf.format(calendar.getTime());
                dateText.setText(formatted);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openRetakeCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_RETAKE_PHOTO);
        } else {
            showPopup("Camera Error", "⚠️ Camera not available.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_RETAKE_PHOTO && resultCode == RESULT_OK && data != null) {
            Bitmap newImage = (Bitmap) data.getExtras().get("data");
            editImageView.setImageBitmap(newImage);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            newImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageData = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageData, Base64.DEFAULT);

            noteBeingEdited.setImagePath(base64Image);

            db.collection("study_notes").document(noteBeingEdited.getId())
                    .set(noteBeingEdited)
                    .addOnSuccessListener(unused -> showPopup("Photo Updated", "📸 Image updated successfully."))
                    .addOnFailureListener(e -> showPopup("Error", "❌ Failed to update image."));
        }
    }

    private void showDeleteDialog(NoteModel note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("study_notes").document(note.getId())
                            .delete()
                            .addOnSuccessListener(unused -> showPopup("Deleted", "🗑️ Note deleted successfully."))
                            .addOnFailureListener(e -> showPopup("Error", "❌ Failed to delete note."));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPopup(String title, String message) {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", null)
                .create();
        alert.show();
    }
}
