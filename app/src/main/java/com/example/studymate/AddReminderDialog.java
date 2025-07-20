package com.example.studymate;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.studymate.models.Reminder;

import java.util.Calendar;
import java.util.Random;

public class AddReminderDialog extends DialogFragment {

    private EditText titleInput, dateInput;
    private Spinner tagSpinner;
    private Calendar selectedDateTime;

    private final OnReminderSavedListener listener;

    public interface OnReminderSavedListener {
        void onReminderSaved(Reminder reminder);
    }

    public AddReminderDialog(OnReminderSavedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_reminder, null);

        titleInput = view.findViewById(R.id.titleInput);
        dateInput = view.findViewById(R.id.dateInput);
        tagSpinner = view.findViewById(R.id.tagSpinner);
        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        selectedDateTime = Calendar.getInstance();

        // Setup tag dropdown (Spinner)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.tags_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(adapter);

        // Open date/time picker
        dateInput.setOnClickListener(v -> openDateTimePicker());

        // Save reminder
        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String dateTime = dateInput.getText().toString().trim();
            String tag = tagSpinner.getSelectedItem().toString();

            if (title.isEmpty() || dateTime.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Reminder reminder = new Reminder(title, dateTime, tag);
            listener.onReminderSaved(reminder);
            setAlarm(title, tag);
            dismiss();
        });

        // Cancel dialog
        cancelButton.setOnClickListener(v -> dismiss());

        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    // 👇 This method resizes the dialog to match_parent width
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void openDateTimePicker() {
        Calendar currentDate = Calendar.getInstance();
        selectedDateTime = Calendar.getInstance();

        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, day);

            new TimePickerDialog(requireContext(), (view1, hour, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedDateTime.set(Calendar.MINUTE, minute);
                selectedDateTime.set(Calendar.SECOND, 0);

                dateInput.setText(DateFormat.format("yyyy-MM-dd HH:mm", selectedDateTime));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setAlarm(String title, String tag) {
        Context context = requireContext();
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("msg", title + " [" + tag + "]");

        int requestCode = new Random().nextInt(100000);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Toast.makeText(context, "AlarmManager not available", Toast.LENGTH_SHORT).show();
            return;
        }

        long timeInMillis = selectedDateTime.getTimeInMillis();
        if (timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(context, "Time must be in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    Toast.makeText(context, "App needs permission to schedule exact alarms", Toast.LENGTH_LONG).show();

                    Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(settingsIntent);
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(context, "SecurityException: Cannot set exact alarm", Toast.LENGTH_LONG).show();
        }
    }
}
