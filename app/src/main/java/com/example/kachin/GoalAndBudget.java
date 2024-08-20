package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
    private String uid;
    private LinearLayout goalLayout, budgetLayout;
    private TextView goalText, budgetText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_and_budget);

        addGoal = findViewById(R.id.addGoalButton);
        addBudget = findViewById(R.id.addBudgetButton);
        goalLayout = findViewById(R.id.goalLayout);
        budgetLayout = findViewById(R.id.budgetLayout);
        goalText = findViewById(R.id.GoalsText);
        budgetText = findViewById(R.id.BudgetText);

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }

        // Load Goals
        DatabaseReference goalsRef = database.child("goal");
        goalsRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    goalText.setVisibility(View.GONE);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String goalName = snapshot.child("goalName").getValue(String.class);
                        int progress = snapshot.child("progress").getValue(Integer.class);

                        // Create a LinearLayout to hold the TextView and ProgressBar
                        LinearLayout goalItemLayout = new LinearLayout(GoalAndBudget.this);
                        goalItemLayout.setOrientation(LinearLayout.VERTICAL);
                        goalItemLayout.setPadding(10, 10, 10, 10);

                        // Create TextView for goal name
                        TextView goalNameView = new TextView(GoalAndBudget.this);
                        goalNameView.setText(goalName);
                        goalNameView.setTextSize(20);

                        // Create ProgressBar for progress
                        ProgressBar progressBar = new ProgressBar(GoalAndBudget.this, null, android.R.attr.progressBarStyleHorizontal);
                        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        progressBar.setMax(100);
                        progressBar.setProgress(progress);

                        // Add TextView and ProgressBar to the LinearLayout
                        goalItemLayout.addView(goalNameView);
                        goalItemLayout.addView(progressBar);

                        // Add this layout above the "Add Goal" button
                        goalLayout.addView(goalItemLayout, goalLayout.getChildCount() - 1);
                    }
                } else {
                    goalText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(GoalAndBudget.this, "Failed to load goals", Toast.LENGTH_SHORT).show();
            }
        });

        // Load Budgets
        DatabaseReference budgetsRef = database.child("budget");
        budgetsRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    budgetText.setVisibility(View.GONE);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String category = snapshot.child("category").getValue(String.class);
                        int currentBudget = snapshot.child("currentBudget").getValue(Integer.class);
                        int budgetLimit = snapshot.child("budgetLimit").getValue(Integer.class);
                        int progress = (int) ((currentBudget / (float) budgetLimit) * 100);

                        // Create a LinearLayout to hold the TextView and ProgressBar
                        LinearLayout budgetItemLayout = new LinearLayout(GoalAndBudget.this);
                        budgetItemLayout.setOrientation(LinearLayout.VERTICAL);
                        budgetItemLayout.setPadding(10, 10, 10, 10);

                        // Create TextView for budget category
                        TextView budgetCategoryView = new TextView(GoalAndBudget.this);
                        budgetCategoryView.setText(category);
                        budgetCategoryView.setTextSize(20);

                        // Create ProgressBar for budget progress
                        ProgressBar progressBar = new ProgressBar(GoalAndBudget.this, null, android.R.attr.progressBarStyleHorizontal);
                        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        progressBar.setMax(100);
                        progressBar.setProgress(progress);

                        // Add TextView and ProgressBar to the LinearLayout
                        budgetItemLayout.addView(budgetCategoryView);
                        budgetItemLayout.addView(progressBar);

                        // Add this layout above the "Add Budget" button
                        budgetLayout.addView(budgetItemLayout, budgetLayout.getChildCount() - 1);
                    }
                } else {
                    budgetText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(GoalAndBudget.this, "Failed to load budgets", Toast.LENGTH_SHORT).show();
            }
        });

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