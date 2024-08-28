package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ImageButton backButton, btnHome, btnAdd, btnHistory, btnReport, btnProfile;
    private CalendarView calendar;
    private Button submitButton;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        backButton = findViewById(R.id.backButton);
        calendar = findViewById(R.id.calendar);
        submitButton = findViewById(R.id.submitButton);
        btnHome = findViewById(R.id.btnHome);
        btnAdd = findViewById(R.id.btnAdd);
        btnHistory = findViewById(R.id.btnHistory);
        btnReport = findViewById(R.id.btnReport);
        btnProfile = findViewById(R.id.btnProfile);

        Calendar todayDate = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayDate.getTime());
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar= Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(selectedCalendar.getTime());
        });

        setupButtonListeners();

        backButton.setOnClickListener(v -> {
            finish();
        });

        submitButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, DisplayHistoryActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent);
        });
    }

    private void setupButtonListeners() {
        btnHome.setOnClickListener(v -> {
            Toast.makeText(HistoryActivity.this, "Home Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HistoryActivity.this, HomePageActivity.class);
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> {
            Toast.makeText(HistoryActivity.this, "Add Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HistoryActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Toast.makeText(HistoryActivity.this, "History Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HistoryActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            Toast.makeText(HistoryActivity.this, "Report Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HistoryActivity.this, report.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Toast.makeText(HistoryActivity.this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}