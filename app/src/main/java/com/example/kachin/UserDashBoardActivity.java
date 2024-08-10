package com.example.kachin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDashBoardActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private GoogleSignInClient googleSignInClient;
    private Button signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        signOutButton = findViewById(R.id.signOutButton);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            fetchUserName(user.getUid());
        }

        // Configure Google Sign-In
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        signOutButton.setOnClickListener(view -> {
            signOut();
        });
    }

    private void fetchUserName(String userId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    welcomeTextView.setText("Hi " + name + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                welcomeTextView.setText("Failed to retrieve user data.");
            }
        });
    }

    private void signOut() {
        // Sign out from Firebase
        mAuth.signOut();

        // Sign out from Google
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent intent = new Intent(UserDashBoardActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

