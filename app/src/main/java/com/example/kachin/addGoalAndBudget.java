package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class addGoalAndBudget extends AppCompatActivity {

    private DatabaseReference database;
    private String uid;
    private EditText goalName, targetAmount, budgetLimit;
    private Spinner category, timeFrame;
    private List<String> categoriesList;
    private Button addButton;
    private TextView pageTitle, cancelButton;

    @SuppressLint("SetTextI18n")
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.time_frame,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeFrame.setAdapter(adapter);

        if ("goal".equals(title)) {
            pageTitle.setText("Set Goal");
            goalName.setVisibility(View.VISIBLE);
            targetAmount.setVisibility(View.VISIBLE);
        } else if ("budget".equals(title)) {
            pageTitle.setText("Add Budget Limit");
            budgetLimit.setVisibility(View.VISIBLE);
            category.setVisibility(View.VISIBLE);
            timeFrame.setVisibility(View.VISIBLE);
        }

        cancelButton.setOnClickListener(v -> {
            finish();
        });

        addButton.setOnClickListener(v -> {
            if ("goal".equals(title)) {
                String goalNameText = goalName.getText().toString().trim();
                String targetAmountText = targetAmount.getText().toString().trim();

                if (goalNameText.isEmpty() || targetAmountText.isEmpty()) {
                    Toast.makeText(addGoalAndBudget.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                getNextGoalIdAndWriteNewGoal(uid, goalName.getText().toString(), Double.parseDouble(targetAmount.getText().toString()), 0);
            } else if ("budget".equals(title)) {
                String budgetLimitText = budgetLimit.getText().toString().trim();
                String selectedCategory = category.getSelectedItem().toString();
                String selectedTimeFrame = timeFrame.getSelectedItem().toString();

                if (budgetLimitText.isEmpty() || selectedCategory.isEmpty() || selectedTimeFrame.isEmpty()) {
                    Toast.makeText(addGoalAndBudget.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                getNextBudgetIdAndWriteNewBudget(uid, Double.parseDouble(budgetLimit.getText().toString()),
                        0, category.getSelectedItem().toString(), timeFrame.getSelectedItem().toString());
            }
            finish();
        });
    }

    private void fetchCategories() {
        DatabaseReference databaseCategories = FirebaseDatabase.getInstance().getReference("category");
        databaseCategories.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categoriesList.clear();

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    if (categorySnapshot.getValue() instanceof String) {
                        String category = categorySnapshot.getValue(String.class);
                        categoriesList.add(category);
                    }
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

    private void addNewGoal(String goalId, String uid, String goalName, double convertedTargetAmount, double convertedCurrentAmount) {
        double progress = (convertedCurrentAmount / convertedTargetAmount) * 100;
        progress = Math.round(progress * 100.0) / 100.0;
        Goal goal = new Goal(uid, goalName, convertedTargetAmount, convertedCurrentAmount, progress);

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
                long nextGoalId = 1; // Default to 1 if there are no existing goals
                for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                    String key = goalSnapshot.getKey();
                    long goalId = Long.parseLong(key.replace("goal", ""));
                    nextGoalId = Math.max(nextGoalId, goalId + 1);
                }

                String goalId = "goal" + nextGoalId;
                addNewGoal(goalId, uid, goalName, targetAmount, currentAmount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(addGoalAndBudget.this, "Failed to read goal data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class Goal {
        public String uid;
        public String goalName;
        public double convertedTargetAmount;
        public double convertedCurrentAmount;
        public double progress;

        public Goal() {
        }

        public Goal(String uid, String goalName, double convertedTargetAmount, double convertedCurrentAmount, double progress) {
            this.uid = uid;
            this.goalName = goalName;
            this.convertedTargetAmount = convertedTargetAmount;
            this.convertedCurrentAmount = convertedCurrentAmount;
            this.progress = progress;
        }
    }

    private void addNewBudget(String budgetId, String uid, double convertedBudgetLimit, double convertedCurrentBudget, String category, String timeFrame, String lastResetDate) {
        double progress = (convertedCurrentBudget / convertedBudgetLimit) * 100;
        progress = Math.round(progress * 100.0) / 100.0;

        String currentTimeFrame;
        if (timeFrame.equals("daily")) {
            currentTimeFrame = "currentDay";
        } else if (timeFrame.equals("weekly")) {
            currentTimeFrame = "currentWeek";
        } else {
            currentTimeFrame = "currentMonth";
        }

        Budget budget = new Budget(uid, convertedBudgetLimit, convertedCurrentBudget, category, timeFrame, currentTimeFrame, lastResetDate, progress);
        database.child("budget").child(budgetId).setValue(budget)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Budget added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add budget", Toast.LENGTH_SHORT).show();
                });
    }

    private void getNextBudgetIdAndWriteNewBudget(String uid, double budgetLimit, double currentBudget, String category, String timeFrame) {
        database.child("budget").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long nextBudgetId = 1;
                for (DataSnapshot budgetSnapshot : dataSnapshot.getChildren()) {
                    String key = budgetSnapshot.getKey();
                    long budgetId = Long.parseLong(key.replace("budget", ""));
                    nextBudgetId = Math.max(nextBudgetId, budgetId + 1);
                }

                String budgetId = "budget" + nextBudgetId;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String lastResetDate = sdf.format(new Date());

                addNewBudget(budgetId, uid, budgetLimit, currentBudget, category, timeFrame, lastResetDate);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(addGoalAndBudget.this, "Failed to read budget data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class Budget {
        public String uid;
        public double convertedBudgetLimit;
        public double convertedCurrentBudget;
        public String category;
        public String timeFrame;
        public String currentTimeFrame;
        public String lastResetDate;
        public double progress;

        public Budget() {
        }

        public Budget(String uid, double convertedBudgetLimit, double convertedCurrentBudget, String category, String timeFrame, String currentTimeFrame, String lastResetDate, double progress) {
            this.uid = uid;
            this.convertedBudgetLimit = convertedBudgetLimit;
            this.convertedCurrentBudget = convertedCurrentBudget;
            this.category = category;
            this.timeFrame = timeFrame;
            this.currentTimeFrame = currentTimeFrame;
            this.lastResetDate = lastResetDate;
            this.progress = progress;
        }
    }
}
