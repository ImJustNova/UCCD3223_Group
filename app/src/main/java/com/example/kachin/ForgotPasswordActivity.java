package com.example.kachin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText emailEditText;
    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.emailEditText);
        continueButton = findViewById(R.id.continueButton);

        continueButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                // Logic to send email instruction
                Toast.makeText(ForgotPasswordActivity.this, "Instruction sent to your email", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
