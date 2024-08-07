package com.example.kachin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize buttons or other UI elements if needed
        Button signUpButton = findViewById(R.id.buttonSignUp);
        Button loginButton = findViewById(R.id.buttonLogin);

        signUpButton.setOnClickListener(v -> {
            // Handle Sign Up button click
            Intent intent = new Intent(SignUpActivity.this, SignUpAccActivity.class);
            startActivity(intent);
            finish(); // Optionally finish this activity if you don't want to keep it in the back stack
        });

        loginButton.setOnClickListener(v -> {
            // Handle Login button click
            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optionally finish this activity if you don't want to keep it in the back stack
        });
    }
}

