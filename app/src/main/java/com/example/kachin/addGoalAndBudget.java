package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class addGoalAndBudget extends AppCompatActivity {

    private DatabaseReference database;
    private String uid;
    private EditText goalName, targetAmount, budgetLimit;
    private double currentAmount, progress;
    private Spinner category, timeFrame, recurring;
    private List<String> categoriesList;
    private Button addButton;
    private TextView pageTitle, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal_and_budget);

        pageTitle = findViewById(R.id.pageTitle);
        cancelButton = findViewById(R.id.cancelButton);
        goalName = findViewById(R.id.goalName);
        targetAmount = findViewById(R.id.targetAmount);
        budgetLimit = findViewById(R.id.budgetLimit);
        category = findViewById(R.id.category);
        timeFrame = findViewById(R.id.timeFrame);
        recurring = findViewById(R.id.recurring);
        addButton = findViewById(R.id.addButton);

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        categoriesList = new ArrayList<>();
        fetchCategories();

        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.time_frame_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeFrame.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this,
                R.array.recurring_array,
                android.R.layout.simple_spinner_item
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurring.setAdapter(adapter2);

        if ("goal".equals(title)) {
            pageTitle.setText("Set Goal");
            goalName.setVisibility(View.VISIBLE);
            targetAmount.setVisibility(View.VISIBLE);
        } else if ("budget".equals(title)) {
            pageTitle.setText("Add Budget Limit");
            budgetLimit.setVisibility(View.VISIBLE);
            category.setVisibility(View.VISIBLE);
            timeFrame.setVisibility(View.VISIBLE);
            recurring.setVisibility(View.VISIBLE);
        }

        cancelButton.setOnClickListener(v -> {
            finish();
        });

        addButton.setOnClickListener(v -> {
            if (title == "goal") {
                getNextGoalIdAndWriteNewGoal(uid, goalName.getText().toString(), Double.parseDouble(targetAmount.getText().toString()), currentAmount);
            } else if (title == "budget") {
                getNextBudgetIdAndWriteNewBudget(uid, Double.parseDouble(targetAmount.getText().toString()),
                        currentAmount, category.getSelectedItem().toString(), timeFrame.getSelectedItem().toString(),
                        recurring.getSelectedItem().toString());
            }
        });
    }

    private void fetchCategories() {
        database = FirebaseDatabase.getInstance().getReference("category");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoriesList.clear(); // Clear list before adding new data

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String category = categorySnapshot.getValue(String.class);
                    categoriesList.add(category);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(addGoalAndBudget.this, android.R.layout.simple_spinner_item, categoriesList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                category.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(addGoalAndBudget.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addNewGoal(String goalId, String uid, String goalName, double targetAmount, double currentAmount) {
        double progress = (currentAmount / targetAmount) * 100;
        progress = Math.round(progress * 100.0) / 100.0;
        Goal goal = new Goal(uid, goalName, targetAmount, currentAmount, progress);
        database.child("goal").child(goalId).setValue(goal)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Goal added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add goal", Toast.LENGTH_SHORT).show();
                });
    }

    private void getNextGoalIdAndWriteNewGoal(String uid, String goalName, double targetAmount, double currentAmount) {
        database.child("goal").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long nextGoalId = 1; // Default to 1 if there are no existing users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String key = userSnapshot.getKey();
                    long goalId = Long.parseLong(key.replace("goal", ""));
                    nextGoalId = Math.max(nextGoalId, goalId + 1);
                }

                String goalId = "goal" + nextGoalId;
                addNewGoal(goalId, uid, goalName, targetAmount, currentAmount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(addGoalAndBudget.this, "Failed to read user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class Goal {
        public String uid;
        public String goalName;
        public double targetAmount;
        public double currentAmount;
        public double progress;

        public Goal() {
        }

        public Goal(String uid, String goalName, double targetAmount, double currentAmount, double progress) {
            this.uid = uid;
            this.goalName = goalName;
            this.targetAmount = targetAmount;
            this.currentAmount = currentAmount;
            this.progress = progress;
        }
    }

    private void addNewBudget(String budgetId, String uid, double budgetLimit, double currentBudget, String category, String timeFrame, String recurring) {
        String currentTimeFrame;
        if (timeFrame.equals("daily")) {
            currentTimeFrame = "currentDay";
        } else if (timeFrame.equals("weekly")) {
            currentTimeFrame = "currentWeek";
        } else {
            currentTimeFrame = "currentMonth";
        }

        Budget budget = new Budget(uid, budgetLimit, currentBudget, category, timeFrame, recurring, currentTimeFrame);
        database.child("budget").child(budgetId).setValue(budget)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Budget added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add budget", Toast.LENGTH_SHORT).show();
                });
    }

    private void getNextBudgetIdAndWriteNewBudget(String uid, double budgetLimit, double currentBudget, String category, String timeFrame, String recurring) {
        database.child("budget").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long nextBudgetId = 1; // Default to 1 if there are no existing users
                for (DataSnapshot budgetSnapshot : dataSnapshot.getChildren()) {
                    String key = budgetSnapshot.getKey();
                    long budgetId = Long.parseLong(key.replace("budget", ""));
                    nextBudgetId = Math.max(nextBudgetId, budgetId + 1);
                }

                String budgetId = "budget" + nextBudgetId;
                addNewBudget(budgetId, uid, budgetLimit, currentBudget, category, timeFrame, recurring);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(addGoalAndBudget.this, "Failed to read budget data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class Budget {
        public String uid;
        public double budgetLimit;
        public double currentBudget;
        public String category;
        public String timeFrame;
        public String recurring;
        public String currentTimeFrame;

        public Budget() {
        }

        public Budget(String uid, double budgetLimit, double currentBudget, String category, String timeFrame, String recurring, String currentTimeFrame) {
            this.uid = uid;
            this.budgetLimit = budgetLimit;
            this.currentBudget = currentBudget;
            this.category = category;
            this.timeFrame = timeFrame;
            this.recurring = recurring;
            this.currentTimeFrame = currentTimeFrame;
        }
    }
}

// to update:
// - check empty value
// - check valid value
// - setup logics for add goal and add budget