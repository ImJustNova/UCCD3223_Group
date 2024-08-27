package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ImageButton backButton;
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

        Calendar todayDate = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayDate.getTime());
        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar= Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(selectedCalendar.getTime());
        });

        backButton.setOnClickListener(v -> {
            finish();
        });

        submitButton.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, DisplayHistoryActivity.class);
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent);
        });
    }
}