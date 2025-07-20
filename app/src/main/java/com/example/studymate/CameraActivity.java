package com.example.studymate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class CameraActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private EditText noteTitleInput;
    private TextView locationText, selectedDateText;
    private Button takePhotoBtn, pickDateBtn, fetchLocationBtn, saveNoteBtn, viewNotesBtn, backBtn;

    private Bitmap imageBitmap;
    private String selectedDate = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String locationName = "";

    private FusedLocationProviderClient locationClient;
    private FirebaseFirestore db;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int LOCATION_PERMISSION_CODE = 1001;
    private static final int CAMERA_PERMISSION_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        db = FirebaseFirestore.getInstance();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        imagePreview = findViewById(R.id.imagePreview);
        noteTitleInput = findViewById(R.id.noteTitleInput);
        locationText = findViewById(R.id.locationText);
        selectedDateText = findViewById(R.id.selectedDateText);

        takePhotoBtn = findViewById(R.id.takePhotoBtn);
        pickDateBtn = findViewById(R.id.pickDateBtn);
        fetchLocationBtn = findViewById(R.id.fetchLocationBtn);
        saveNoteBtn = findViewById(R.id.saveNoteBtn);
        viewNotesBtn = findViewById(R.id.viewNotesBtn);
        backBtn = findViewById(R.id.backBtn);

        takePhotoBtn.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            }
        });

        pickDateBtn.setOnClickListener(v -> pickDateTime());

        fetchLocationBtn.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_CODE);
            }
        });

        saveNoteBtn.setOnClickListener(v -> saveNoteToFirestore());
        viewNotesBtn.setOnClickListener(v -> startActivity(new Intent(this, MyNotesActivity.class)));
        backBtn.setOnClickListener(v -> finish());
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            imageBitmap = (Bitmap) data.getExtras().get("data");
            imagePreview.setImageBitmap(imageBitmap);
        }
    }

    private void pickDateTime() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    TimePickerDialog timePicker = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(year, month, dayOfMonth, hourOfDay, minute);
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                                selectedDate = sdf.format(calendar.getTime());
                                selectedDateText.setText("📅 " + selectedDate);
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                    timePicker.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        locationName = addresses.get(0).getAddressLine(0);
                        locationText.setText("📍 " + locationName);
                    } else {
                        locationName = "";
                        locationText.setText("📍 Lat: " + latitude + ", Lng: " + longitude);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    locationName = "";
                    locationText.setText("📍 Lat: " + latitude + ", Lng: " + longitude);
                }
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNoteToFirestore() {
        String title = noteTitleInput.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Note title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageBitmap == null) {
            Toast.makeText(this, "Please take a photo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please pick a date", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        Map<String, Object> note = new HashMap<>();
        note.put("title", title);
        note.put("date", selectedDate);
        note.put("imagePath", base64Image);
        note.put("latitude", latitude);
        note.put("longitude", longitude);
        note.put("locationName", locationName);

        db.collection("study_notes").add(note)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                    clearInputs();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearInputs() {
        noteTitleInput.setText("");
        selectedDateText.setText("📅 No date selected");
        locationText.setText("📍 No location selected");
        imagePreview.setImageResource(android.R.color.transparent);
        imageBitmap = null;
        selectedDate = "";
        latitude = 0.0;
        longitude = 0.0;
        locationName = "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
