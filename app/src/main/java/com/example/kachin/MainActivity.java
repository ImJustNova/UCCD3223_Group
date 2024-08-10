package com.example.kachin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kachin.ForgotPasswordActivity;
import com.example.kachin.SignUpAccActivity;
import com.example.kachin.UserDashBoardActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText email, password;
    private CheckBox rememberMe;
    private ImageButton buttonSignUpGoogle;
    private Button loginButton;
    private TextView forgotPassword, signUp;
    private ImageView passwordToggle;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        rememberMe = findViewById(R.id.checkBoxTerms);
        buttonSignUpGoogle = findViewById(R.id.buttonSignUpGoogle);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUp = findViewById(R.id.signUp);
        passwordToggle = findViewById(R.id.passwordToggle);

        // Load saved email and password if "Remember Me" was checked
        SharedPreferences preferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        boolean isRemembered = preferences.getBoolean("rememberMe", false);

        if (isRemembered) {
            String savedEmail = preferences.getString("email", "");
            String savedPassword = preferences.getString("password", "");
            email.setText(savedEmail);
            password.setText(savedPassword);
            rememberMe.setChecked(true);
        }

        // Toggle password visibility
        passwordToggle.setOnClickListener(v -> {
            if (password.getInputType() == 128) { // Password visible
                password.setInputType(129); // Password hidden
                passwordToggle.setImageResource(R.drawable.ic_eye_closed); // Update icon
            } else {
                password.setInputType(128); // Password visible
                passwordToggle.setImageResource(R.drawable.ic_eye_open); // Update icon
            }
            // Move the cursor to the end of the text
            password.setSelection(password.getText().length());
        });

        // Handle Login button click
        loginButton.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            } else {
                // Sign in using FirebaseAuth
                mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (rememberMe.isChecked()) {
                                    // Save email and password
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("email", userEmail);
                                    editor.putString("password", userPassword);
                                    editor.putBoolean("rememberMe", true);
                                    editor.apply();
                                } else {
                                    // Clear saved email and password
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.clear();
                                    editor.apply();
                                }
                                // Navigate to the next activity
                                startActivity(new Intent(MainActivity.this, UserDashBoardActivity.class));
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Handle Google Sign-In button click
        buttonSignUpGoogle.setOnClickListener(v -> {
            // Handle Google Sign-In logic
        });

        // Handle Forgot Password click
        forgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
        });

        // Handle Sign-Up click
        signUp.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpAccActivity.class));
        });
    }
}
