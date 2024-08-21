package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

public class report extends AppCompatActivity {

    private Button btnExpense, btnIncome;
    private ImageButton btnBack;
    private TextView totalAmount;
    private Spinner timeFrame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);
        btnBack = findViewById(R.id.btnBack);
        totalAmount = findViewById(R.id.totalAmount);
        timeFrame = findViewById(R.id.timeFrame);

        String[] timeFrames = {"Week", "Month"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, timeFrames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeFrame.setAdapter(adapter);

        btnExpense.setOnClickListener(v -> {
            btnExpense.setSelected(true);
            btnIncome.setSelected(false);
        });

        btnIncome.setOnClickListener(v -> {
            btnIncome.setSelected(true);
            btnExpense.setSelected(false);
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
}