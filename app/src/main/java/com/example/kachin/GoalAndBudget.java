package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GoalAndBudget extends AppCompatActivity {

    private ImageButton addGoal, addBudget, backButton, btnHome, btnAdd, btnHistory, btnReport, btnProfile;
    private DatabaseReference database;
    private String uid, currencyUnit;
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
        backButton = findViewById(R.id.backButton);
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
            Toast.makeText(GoalAndBudget.this, "Home Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GoalAndBudget.this, HomePageActivity.class);
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> {
            Toast.makeText(GoalAndBudget.this, "Add Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GoalAndBudget.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Toast.makeText(GoalAndBudget.this, "History Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GoalAndBudget.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            Toast.makeText(GoalAndBudget.this, "Report Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GoalAndBudget.this, report.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Toast.makeText(GoalAndBudget.this, "Profile Clicked", Toast.LENGTH_SHORT).show();
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
                        double currentBudget = snapshot.child("convertedCurrentBudget").getValue(Integer.class);
                        double budgetLimit = snapshot.child("convertedBudgetLimit").getValue(Integer.class);
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

                        // ProgressBar Layout
                        RelativeLayout progressBarLayout = new RelativeLayout(GoalAndBudget.this);

                        // ProgressBar
                        ProgressBar progressBar = new ProgressBar(GoalAndBudget.this, null, android.R.attr.progressBarStyleHorizontal);
                        RelativeLayout.LayoutParams progressBarParams = new RelativeLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                20 // Set the thickness of the ProgressBar
                        );
                        progressBar.setLayoutParams(progressBarParams);
                        progressBar.setMax(100);
                        progressBar.setProgress(progress);

                        if (progress >= 100) {
                            progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.red_progress_bar)); // Set a red progress bar drawable
                        }

                        // Current progress text and warning icon in a horizontal layout
                        LinearLayout progressInfoLayout = new LinearLayout(GoalAndBudget.this);
                        progressInfoLayout.setOrientation(LinearLayout.HORIZONTAL);
                        progressInfoLayout.setGravity(Gravity.CENTER_VERTICAL); // Align items vertically centered

                        // Current progress text
                        TextView currProgress = new TextView(GoalAndBudget.this);
                        currProgress.setText(String.format(currencyUnit + " %.2f of " + currencyUnit + " %.2f", currentBudget, budgetLimit));
                        currProgress.setTextSize(15);
                        currProgress.setTextColor(progress >= 100 ? getResources().getColor(R.color.red) : getResources().getColor(R.color.grey));
                        currProgress.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)); // Weight 1

                        // Add the red circle with exclamation mark if the limit is reached
                        ImageView redCircleWarning = null;
                        if (progress >= 100) {
                            redCircleWarning = new ImageView(GoalAndBudget.this);
                            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(40, 40); // Set the size of the red circle
                            circleParams.setMargins(10, 0, 0, 0); // Add some margin to the left
                            redCircleWarning.setLayoutParams(circleParams);
                            redCircleWarning.setImageDrawable(getResources().getDrawable(R.drawable.ic_warning_red_24dp)); // Set the drawable resource for the red circle with exclamation mark
                        }

                        // Add TextView and ImageView to progressInfoLayout
                        progressInfoLayout.addView(currProgress);
                        if (redCircleWarning != null) {
                            progressInfoLayout.addView(redCircleWarning);
                        }

                        // Adding views to layout
                        titleLayout.addView(budgetText);
                        titleLayout.addView(remove);
                        budgetItemLayout.addView(titleLayout);
                        budgetItemLayout.addView(remaining);
                        budgetItemLayout.addView(progressBar);
                        budgetItemLayout.addView(progressInfoLayout);

                        // Warning if the budget is exceeded
                        if (progress >= 100) {
                            TextView warning = new TextView(GoalAndBudget.this);
                            warning.setText("You have exceeded your budget limit!");
                            warning.setTextSize(15);
                            warning.setTextColor(getResources().getColor(R.color.red));
                            budgetItemLayout.addView(warning);
                        }

                        // Add the budget item layout to the main budget layout
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

    private String getCurrencyUnit(String selectedCurrency, String[] currencyUnits) {
        for (String unit : currencyUnits) {
            if (unit.startsWith(selectedCurrency)) {
                return unit.split(" - ")[1];
            }
        }
        return "RM"; // Default to RM if not found
    }
}