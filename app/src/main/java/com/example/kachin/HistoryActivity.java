package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ImageButton btnHome, btnAdd, btnHistory, btnReport, btnProfile;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageButton backButton = findViewById(R.id.backButton);
        CalendarView calendar = findViewById(R.id.calendar);
        Button submitButton = findViewById(R.id.submitButton);
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
            Intent intent = new Intent(HistoryActivity.this, HomePageActivity.class);
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, report.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}