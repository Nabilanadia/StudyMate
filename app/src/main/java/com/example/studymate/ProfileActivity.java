package com.example.studymate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView profileEmail;
    EditText editName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        profileEmail = findViewById(R.id.textViewEmail);
        editName = findViewById(R.id.editTextName);

        user = auth.getCurrentUser();
        if (user != null) {
            profileEmail.setText(user.getEmail());
        }

        // Load saved name if exists
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String savedName = prefs.getString("name", "");
        editName.setText(savedName);
    }

    public void enableEdit(View v) {
        editName.setEnabled(true);
        findViewById(R.id.buttonSave).setEnabled(true);
        Toast.makeText(this, "You can now edit your profile", Toast.LENGTH_SHORT).show();
    }

    public void saveProfile(View v) {
        String name = editName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save name to SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences("UserProfile", MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.apply();

        editName.setEnabled(false);
        findViewById(R.id.buttonSave).setEnabled(false);

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    public void goBack(View v) {
        // Navigate back to previous activity (likely HomePage)
        finish();
    }
}
