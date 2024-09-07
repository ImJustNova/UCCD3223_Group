package com.example.kachin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

    private ImageButton btnHome, btnAdd, btnHistory, btnReport, btnProfile;
    private DatabaseReference database;
    private String uid, currencyUnit;
    private LinearLayout goalLayout, budgetLayout;
    private TextView goalText, budgetText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_and_budget);

        ImageButton addGoal = findViewById(R.id.addGoalButton);
        ImageButton addBudget = findViewById(R.id.addBudgetButton);
        goalLayout = findViewById(R.id.goalLayout);
        budgetLayout = findViewById(R.id.budgetLayout);
        goalText = findViewById(R.id.GoalsText);
        budgetText = findViewById(R.id.BudgetText);
        ImageButton backButton = findViewById(R.id.backButton);
        btnHome = findViewById(R.id.btnHome);
        btnAdd = findViewById(R.id.btnAdd);
        btnHistory = findViewById(R.id.btnHistory);
        btnReport = findViewById(R.id.btnReport);
        btnProfile = findViewById(R.id.btnProfile);

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences currencyPref = getSharedPreferences("CurrencyPrefs", Context.MODE_PRIVATE);
        String selectedCurrency = currencyPref.getString("selectedCurrency", "MYR");
        String[] currencyUnits = getResources().getStringArray(R.array.currency_units);
        currencyUnit = getCurrencyUnit(selectedCurrency, currencyUnits);

        displayGoals();
        displayBudgets();
        setupButtonListeners();

        addGoal.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, addGoalAndBudget.class);
            intent.putExtra("title", "goal");
            startActivity(intent);
            finish();
        });

        addBudget.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, addGoalAndBudget.class);
            intent.putExtra("title", "budget");
            startActivity(intent);
            finish();
        });

        backButton.setOnClickListener(v -> {
            finish();
        });

    }

    private void setupButtonListeners() {
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, HomePageActivity.class);
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, report.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(GoalAndBudget.this, ProfileActivity.class);
            startActivity(intent);
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
                        double currentAmount = snapshot.child("convertedCurrentAmount").getValue(Double.class);
                        double targetAmount = snapshot.child("convertedTargetAmount").getValue(Double.class);

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
                        remove.setGravity(Gravity.END);

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
                        remaining.setText("Remaining " + currencyUnit + String.format(" %.2f", targetAmount - currentAmount));
                        remaining.setTextSize(25);

                        TextView currProgress = new TextView(GoalAndBudget.this);
                        currProgress.setText(String.format(currencyUnit + " %.2f of " + currencyUnit + " %.2f", currentAmount, targetAmount));
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GoalAndBudget.this, "Failed to load goals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayBudgets() {
        budgetLayout.removeAllViews();
        DatabaseReference budgetsRef = database.child("budget");
        budgetsRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    budgetText.setVisibility(View.GONE);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String category = snapshot.child("category").getValue(String.class);
                        double currentBudget = snapshot.child("convertedCurrentBudget").getValue(Double.class);
                        double budgetLimit = snapshot.child("convertedBudgetLimit").getValue(Double.class);
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

                        TextView budgetText = new TextView(GoalAndBudget.this);
                        budgetText.setText(category);
                        budgetText.setTextSize(20);
                        budgetText.setTypeface(null, Typeface.BOLD);

                        TextView remove = new TextView(GoalAndBudget.this);
                        remove.setText("Remove");
                        remove.setTextSize(15);
                        remove.setGravity(Gravity.END);

                        remove.setOnClickListener(v -> {
                            DatabaseReference budgetToRemoveRef = budgetsRef.child(snapshot.getKey());
                            budgetToRemoveRef.removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(GoalAndBudget.this, "Budget limit removed successfully", Toast.LENGTH_SHORT).show();
                                    budgetLayout.removeView(budgetItemLayout);
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
                        budgetText.setLayoutParams(goalNameParams);

                        TextView remaining = new TextView(GoalAndBudget.this);
                        remaining.setText("Remaining " + currencyUnit + String.format(" %.2f", budgetLimit - currentBudget));
                        remaining.setTextSize(25);
                        remaining.setTypeface(null, Typeface.BOLD);

                        ProgressBar progressBar = new ProgressBar(GoalAndBudget.this, null, android.R.attr.progressBarStyleHorizontal);
                        RelativeLayout.LayoutParams progressBarParams = new RelativeLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                20
                        );
                        progressBar.setLayoutParams(progressBarParams);
                        progressBar.setMax(100);
                        progressBar.setProgress(progress);

                        if (progress >= 100) {
                            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.red_progress_bar));
                        }

                        LinearLayout progressInfoLayout = new LinearLayout(GoalAndBudget.this);
                        progressInfoLayout.setOrientation(LinearLayout.HORIZONTAL);
                        progressInfoLayout.setGravity(Gravity.CENTER_VERTICAL);

                        TextView currProgress = new TextView(GoalAndBudget.this);
                        currProgress.setText(String.format(currencyUnit + " %.2f of " + currencyUnit + " %.2f", currentBudget, budgetLimit));
                        currProgress.setTextSize(15);
                        currProgress.setTextColor(progress >= 100 ? getResources().getColor(R.color.red) : getResources().getColor(R.color.grey));
                        currProgress.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

                        ImageView redCircleWarning = null;
                        if (progress >= 100) {
                            redCircleWarning = new ImageView(GoalAndBudget.this);
                            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(40, 40);
                            circleParams.setMargins(10, 0, 0, 0);
                            redCircleWarning.setLayoutParams(circleParams);
                            redCircleWarning.setImageDrawable(getResources().getDrawable(R.drawable.ic_warning_red_24dp));
                        }

                        progressInfoLayout.addView(currProgress);
                        if (redCircleWarning != null) {
                            progressInfoLayout.addView(redCircleWarning);
                        }

                        titleLayout.addView(budgetText);
                        titleLayout.addView(remove);
                        budgetItemLayout.addView(titleLayout);
                        budgetItemLayout.addView(remaining);
                        budgetItemLayout.addView(progressBar);
                        budgetItemLayout.addView(progressInfoLayout);

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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GoalAndBudget.this, "Failed to load budgets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrencyUnit(String selectedCurrency, String[] currencyUnits) {
        for (String unit : currencyUnits) {
            if (unit.startsWith(selectedCurrency)) {
                return unit.split(" - ")[1];
            }
        }
        return "RM"; // Default to RM if not found
    }
}