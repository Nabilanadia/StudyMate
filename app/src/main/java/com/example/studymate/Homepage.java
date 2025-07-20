package com.example.studymate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Homepage extends AppCompatActivity {

    private Button btnMap, btnCamera, btnReminder, btnProfile, btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage); // ✅ Make sure your layout file is named correctly

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // 🔒 Redirect to login page if not signed in
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // 🔘 Button initialization
        btnMap = findViewById(R.id.btnMap);
        btnCamera = findViewById(R.id.btnCamera);
        btnReminder = findViewById(R.id.btnReminder);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // 🗺️ Map Page
        btnMap.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));

        // 📷 Camera Page
        btnCamera.setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));

        // ⏰ Reminder Page
        btnReminder.setOnClickListener(v -> startActivity(new Intent(this, ReminderActivity.class)));

        // 👤 Profile Page
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // 🔓 Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
