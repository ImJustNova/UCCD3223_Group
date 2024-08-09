package com.example.kachin;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.FirebaseApp;

public class SignUpAccActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private CheckBox checkBoxTerms;
    private Button buttonSignUpAcc;
    private ImageButton buttonSignUpGoogle;
    private ImageView passwordToggle;
    private TextView textViewLogin; // Reference to TextView for login
    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    //private GoogleSignInClient googleSignInClient;

    // ActivityResultLauncher for Google Sign-In
//    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
//        new ActivityResultContracts.StartActivityForResult(),
//        new ActivityResultCallback<ActivityResult>() {
//            @Override
//            public void onActivityResult(ActivityResult result) {
//                if (result.getResultCode() == RESULT_OK) {
//                    Intent data = result.getData();
//                    if (data != null) {
//                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
//                        try {
//                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
//                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
//                            mAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if (task.isSuccessful()) {
//                                        FirebaseUser user = mAuth.getCurrentUser();
//                                        if (user != null) {
//                                            saveUserToDatabase(user, user.getDisplayName(), user.getEmail());
//                                        }
//                                    } else {
//                                        Log.e("SignUpAccActivity", "Google sign in failed", task.getException());
//                                        Toast.makeText(SignUpAccActivity.this, "Failed to sign in: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            });
//                        } catch (ApiException e) {
//                            e.printStackTrace();
//                            Toast.makeText(SignUpAccActivity.this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//            }
//        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_acc);

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
//        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        googleSignInClient = GoogleSignIn.getClient(this, options);

        // Initialize UI elements
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        checkBoxTerms = findViewById(R.id.checkBoxTerms);
        buttonSignUpAcc = findViewById(R.id.buttonSignUpAcc);
        passwordToggle = findViewById(R.id.passwordToggle);
        textViewLogin = findViewById(R.id.textViewLogin); // Initialize TextView for login
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        checkBoxTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonSignUpAcc.setEnabled(isChecked);
        });

        buttonSignUpAcc.setOnClickListener(v -> {
            if (validateInputs()) {
                String name = editTextName.getText().toString();
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                signUpUser(name, email, password);
            }
        });

        passwordToggle.setOnClickListener(view -> {
            if (isPasswordVisible) {
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye_closed);
            } else {
                editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye_open);
            }
            isPasswordVisible = !isPasswordVisible;
            editTextPassword.setSelection(editTextPassword.length());
        });

//        buttonSignUpGoogle.setOnClickListener(view -> {
//            Intent intent = googleSignInClient.getSignInIntent();
//            activityResultLauncher.launch(intent);
//        });

        // Set OnClickListener for TextView to navigate to MainActivity
        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpAccActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional: Close the SignUpAccActivity
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        if (name.isEmpty()) {
            editTextName.setError("Name cannot be empty");
            isValid = false;
        } else {
            editTextName.setError(null);
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email cannot be empty");
            isValid = false;
        } else {
            editTextEmail.setError(null);
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password cannot be empty");
            isValid = false;
        } else {
            editTextPassword.setError(null);
        }

        if (!checkBoxTerms.isChecked()) {
            Toast.makeText(this, "You must agree to the terms", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void signUpUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToDatabase(user, name, email);
                            }
                        } else {
                            Toast.makeText(SignUpAccActivity.this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user, String name, String email) {
        String userId = user.getUid();
        User newUser = new User(name, email);
        databaseReference.child(userId).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpAccActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignUpAccActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Optional: Close the SignUpAccActivity
                    } else {
                        Toast.makeText(SignUpAccActivity.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class User {
        public String name;
        public String email;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}
