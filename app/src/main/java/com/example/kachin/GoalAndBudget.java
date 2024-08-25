package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
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

    private ImageButton addGoal, addBudget, backButton;
    private DatabaseReference database;
    private String uid;
    private LinearLayout goalLayout, budgetLayout;
    private TextView goalText, budgetText;
    private Button refresh;

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
        backButton = findViewById(R.id.backButton);
        refresh = findViewById(R.id.refresh);

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }

        displayGoals();
        displayBudgets();

        addGoal.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, addGoalAndBudget.class);
            intent.putExtra("title", "goal");
            startActivity(intent);
            //finish();
        });

        addBudget.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, addGoalAndBudget.class);
            intent.putExtra("title", "budget");
            startActivity(intent);
            //finish();
        });

        backButton.setOnClickListener(v -> {
            finish();
        });

        refresh.setOnClickListener(v -> {
            displayGoals();
            displayBudgets();
        });
    }

    public void displayGoals() {
        goalLayout.removeAllViews();
        DatabaseReference goalsRef = database.child("goal");
        goalsRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    goalText.setVisibility(View.GONE);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String goalName = snapshot.child("goalName").getValue(String.class);
                        int progress = snapshot.child("progress").getValue(Integer.class);
                        double currentAmount = snapshot.child("currentAmount").getValue(Double.class);
                        double targetAmount = snapshot.child("targetAmount").getValue(Double.class);

                        LinearLayout goalItemLayout = new LinearLayout(GoalAndBudget.this);
                        goalItemLayout.setOrientation(LinearLayout.VERTICAL);
                        goalItemLayout.setPadding(10, 10, 10, 10);

                        LinearLayout titleLayout = new LinearLayout(GoalAndBudget.this);
                        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
                        titleLayout.setPadding(10, 10, 10, 10);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                        );
                        titleLayout.setLayoutParams(layoutParams);

                        TextView goalNameView = new TextView(GoalAndBudget.this);
                        goalNameView.setText(goalName);
                        goalNameView.setTextSize(20);
                        goalNameView.setTypeface(null, Typeface.BOLD);

                        TextView remove = new TextView(GoalAndBudget.this);
                        remove.setText("Remove");
                        remove.setTextSize(15);
                        remove.setGravity(Gravity.RIGHT);

                        remove.setOnClickListener(v -> {
                            DatabaseReference goalToRemoveRef = goalsRef.child(snapshot.getKey());
                            goalToRemoveRef.removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(GoalAndBudget.this, "Goal removed successfully", Toast.LENGTH_SHORT).show();
                                    goalLayout.removeView(goalItemLayout);
                                } else {
                                    Toast.makeText(GoalAndBudget.this, "Failed to remove goal", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });

                        LinearLayout.LayoutParams goalNameParams = new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1.0f
                        );
                        goalNameView.setLayoutParams(goalNameParams);

                        TextView remaining = new TextView(GoalAndBudget.this);
                        remaining.setText("Remaining RM " + String.format("%.2f", targetAmount - currentAmount));
                        remaining.setTextSize(25);

                        TextView currProgress = new TextView(GoalAndBudget.this);
                        currProgress.setText(String.format("RM %.2f of RM %.2f", currentAmount, targetAmount));
                        currProgress.setTextSize(15);

                        ProgressBar progressBar = new ProgressBar(GoalAndBudget.this, null, android.R.attr.progressBarStyleHorizontal);
                        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        progressBar.setMax(100);
                        progressBar.setProgress(progress);

                        titleLayout.addView(goalNameView);
                        titleLayout.addView(remove);
                        goalItemLayout.addView(titleLayout);
                        goalItemLayout.addView(remaining);
                        goalItemLayout.addView(progressBar);
                        goalItemLayout.addView(currProgress);

                        if (progress >= 100) {
                            TextView complete = new TextView(GoalAndBudget.this);
                            complete.setText("You have exceeded your budget limit!");
                            complete.setTextSize(15);
                            complete.setTextColor(getResources().getColor(R.color.green));
                            goalItemLayout.addView(complete);
                        }

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
    }


    public void displayBudgets() {
        budgetLayout.removeAllViews();
        DatabaseReference budgetsRef = database.child("budget");
        budgetsRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    budgetText.setVisibility(View.GONE);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String category = snapshot.child("category").getValue(String.class);
                        double currentBudget = snapshot.child("currentBudget").getValue(Integer.class);
                        double budgetLimit = snapshot.child("budgetLimit").getValue(Integer.class);
                        int progress = (int) ((currentBudget / (float) budgetLimit) * 100);

                        LinearLayout budgetItemLayout = new LinearLayout(GoalAndBudget.this);
                        budgetItemLayout.setOrientation(LinearLayout.VERTICAL);
                        budgetItemLayout.setPadding(10, 10, 10, 10);

                        LinearLayout titleLayout = new LinearLayout(GoalAndBudget.this);
                        titleLayout.setOrientation(LinearLayout.HORIZONTAL);
                        titleLayout.setPadding(10, 10, 10, 10);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                        );
                        titleLayout.setLayoutParams(layoutParams);

                        TextView budgetCategoryView = new TextView(GoalAndBudget.this);
                        budgetCategoryView.setText(category);
                        budgetCategoryView.setTextSize(20);
                        budgetCategoryView.setTypeface(null, Typeface.BOLD);

                        TextView remove = new TextView(GoalAndBudget.this);
                        remove.setText("Remove");
                        remove.setTextSize(15);
                        remove.setGravity(Gravity.RIGHT);

                        remove.setOnClickListener(v -> {
                            DatabaseReference budgetToRemoveRef = budgetsRef.child(snapshot.getKey());
                            budgetToRemoveRef.removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(GoalAndBudget.this, "Budget removed successfully", Toast.LENGTH_SHORT).show();
                                    budgetLayout.removeView(budgetItemLayout);
                                } else {
                                    Toast.makeText(GoalAndBudget.this, "Failed to remove budget", Toast.LENGTH_SHORT).show();
                                }
                            });
                        });

                        TextView remaining = new TextView(GoalAndBudget.this);
                        remaining.setText("Remaining RM " + String.format("%.2f", budgetLimit - currentBudget));
                        remaining.setTextSize(25);

                        LinearLayout.LayoutParams categoryParams = new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1.0f
                        );
                        budgetCategoryView.setLayoutParams(categoryParams);

                        TextView currProgress = new TextView(GoalAndBudget.this);
                        currProgress.setText(String.format("RM %.2f of RM %.2f", currentBudget, budgetLimit));
                        currProgress.setTextSize(15);

                        ProgressBar progressBar = new ProgressBar(GoalAndBudget.this, null, android.R.attr.progressBarStyleHorizontal);
                        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        progressBar.setMax(100);
                        progressBar.setProgress(progress);

                        titleLayout.addView(budgetCategoryView);
                        titleLayout.addView(remove);
                        budgetItemLayout.addView(titleLayout);
                        budgetItemLayout.addView(remaining);
                        budgetItemLayout.addView(progressBar);
                        budgetItemLayout.addView(currProgress);

                        if (progress >= 100) {
                            TextView warning = new TextView(GoalAndBudget.this);
                            warning.setText("You have exceeded your budget limit!");
                            warning.setTextSize(15);
                            warning.setTextColor(getResources().getColor(R.color.red));
                            budgetItemLayout.addView(warning);
                        }

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
    }

}