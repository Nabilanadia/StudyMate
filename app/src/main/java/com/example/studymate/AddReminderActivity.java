package com.example.studymate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Random;

public class AddReminderActivity extends AppCompatActivity {

    private EditText titleInput, dateInput;
    private Spinner tagSpinner;
    private Button saveButton;

    private Calendar selectedDateTime;
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_reminder); // Your add reminder form layout

        titleInput = findViewById(R.id.titleInput);
        dateInput = findViewById(R.id.dateInput);
        tagSpinner = findViewById(R.id.tagSpinner);
        saveButton = findViewById(R.id.saveButton);

        selectedDateTime = Calendar.getInstance();

        // Request permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.tags_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(adapter);

        // DateTime picker
        dateInput.setOnClickListener(v -> openDateTimePicker());

        // Save and set alarm
        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String dateTime = dateInput.getText().toString().trim();
            String tag = tagSpinner.getSelectedItem().toString();

            if (title.isEmpty() || dateTime.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            setAlarm(title, tag);
            Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show();
            finish(); // go back to ReminderActivity
        });
    }

    private void openDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        selectedDateTime = Calendar.getInstance();

        new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (TimePicker view1, int hourOfDay, int minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                selectedDateTime.set(Calendar.SECOND, 0);

                dateInput.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", selectedDateTime));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(String title, String tag) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg", title + " [" + tag + "]");

        int requestCode = new Random().nextInt(100000);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    selectedDateTime.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    // Optional: handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
