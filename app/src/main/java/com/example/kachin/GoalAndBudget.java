package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GoalAndBudget extends AppCompatActivity {

    private ImageButton addGoal, addBudget;
    private DatabaseReference database;
    private String uid, goalName;
    private double targetAmount, currentAmount, progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_and_budget);

        addGoal = findViewById(R.id.addGoalButton);
        addBudget = findViewById(R.id.addBudgetButton);

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }

        addGoal.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, addGoalAndBudget.class);
            intent.putExtra("title", "goal");
            startActivity(intent);
        });

        addBudget.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, addGoalAndBudget.class);
            intent.putExtra("title", "budget");
            startActivity(intent);
        });
    }
}