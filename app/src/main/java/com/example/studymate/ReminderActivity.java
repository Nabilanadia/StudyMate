package com.example.studymate;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studymate.adapters.ReminderAdapter;
import com.example.studymate.models.Reminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ReminderActivity extends AppCompatActivity {

    private ArrayList<Reminder> reminderList;
    private ReminderAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton addReminderBtn;
    private Button backToMainBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder); // ✅ use correct layout

        reminderList = new ArrayList<>();
        adapter = new ReminderAdapter(reminderList, this);

        recyclerView = findViewById(R.id.reminderRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addReminderBtn = findViewById(R.id.addReminderBtn);
        addReminderBtn.setOnClickListener(v -> {
            AddReminderDialog dialog = new AddReminderDialog(reminder -> {
                reminderList.add(reminder);
                adapter.notifyItemInserted(reminderList.size() - 1);
            });
            dialog.show(getSupportFragmentManager(), "AddReminderDialog");
        });

        backToMainBtn = findViewById(R.id.backToMainBtn);
        backToMainBtn.setOnClickListener(v -> finish());
    }
}