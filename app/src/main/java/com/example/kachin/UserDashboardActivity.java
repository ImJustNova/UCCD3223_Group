package com.example.kachin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView greetingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        greetingTextView = findViewById(R.id.greetingTextView);

        // Retrieve the user's name from the intent
        String name = getIntent().getStringExtra("name");

        // Display the greeting message
        greetingTextView.setText("Hello " + name);
    }
}
