package com.example.kachin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HomePageActivity extends AppCompatActivity {

    private TextView monthView;
    private TextView textIncomeAmount;
    private TextView textExpensesAmount;
    private ImageButton btnHome, btnAdd, btnHistory, btnReport, btnProfile;
    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<FinancialTransaction> transactionList = new ArrayList<>();
    private Button seeAllButton, seeAllbtn;
    private DatabaseReference expensesRef;
    private DatabaseReference incomeRef;
    private DatabaseReference goalRef;
    private ProgressBar progressBarGoal;
    private TextView goalNameText, progressText;
    private String uid;

    private ImageButton addGoal, addBudget, backButton;
    private DatabaseReference database;
    private LinearLayout goalLayout, budgetLayout;
    private TextView goalText, budgetText;
    private Button refresh;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        expensesRef = database.getReference("expense");
        incomeRef = database.getReference("income");
        goalRef = database.getReference("goal");

        // Get the current user's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
            return; // Exit the activity or handle this scenario as appropriate
        }

        // Initialize UI components
        monthView = findViewById(R.id.monthView);
        textIncomeAmount = findViewById(R.id.textIncomeAmount);
        textExpensesAmount = findViewById(R.id.textExpensesAmount);
        btnHome = findViewById(R.id.btnHome);
        btnAdd = findViewById(R.id.btnAdd);
        btnHistory = findViewById(R.id.btnHistory);
        btnReport = findViewById(R.id.btnReport);
        btnProfile = findViewById(R.id.btnProfile);
        recyclerView = findViewById(R.id.transactionRecyclerView);
        seeAllButton = findViewById(R.id.seeAllText);
        seeAllbtn = findViewById(R.id.btnSeeAllGoals);
        addGoal = findViewById(R.id.addGoalButton);
        addBudget = findViewById(R.id.addBudgetButton);
        goalLayout = findViewById(R.id.goalLayout);
        budgetLayout = findViewById(R.id.budgetLayout);
        goalText = findViewById(R.id.GoalsText);
        budgetText = findViewById(R.id.BudgetText);
        backButton = findViewById(R.id.backButton);
        refresh = findViewById(R.id.refresh);

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(transactionAdapter);

        // Set the current month in the UI
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String currentMonth = monthFormat.format(calendar.getTime());
        monthView.setText(currentMonth);

        setupButtonListeners();

        fetchTotalIncome();
        fetchTotalExpense();
        fetchRecentTransactions();
        displayGoals();

        seeAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, AllTransactionsActivity.class);
            intent.putExtra("date", getCurrentDate());
            startActivity(intent);
        });

        seeAllbtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, AllGoalsActivity.class);
            startActivity(intent);
        });
    }

    private void setupButtonListeners() {
        btnHome.setOnClickListener(v -> {
            Toast.makeText(HomePageActivity.this, "Home Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomePageActivity.this, HomePageActivity.class);
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> {
            Toast.makeText(HomePageActivity.this, "Add Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomePageActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Toast.makeText(HomePageActivity.this, "History Clicked", Toast.LENGTH_SHORT).show();
            // Uncomment this when HistoryActivity is available
            // Intent intent = new Intent(HomePageActivity.this, HistoryActivity.class);
            // startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            Toast.makeText(HomePageActivity.this, "Report Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomePageActivity.this, GoalAndBudget.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Toast.makeText(HomePageActivity.this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomePageActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    private void fetchTotalIncome() {
        incomeRef.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalIncome = 0;
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                String currentMonth = dateFormat.format(calendar.getTime());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    Object amountObj = snapshot.child("amount").getValue();

                    if (date != null && date.startsWith(currentMonth)) {
                        double amount = 0;

                        if (amountObj instanceof Double) {
                            amount = (Double) amountObj;
                        } else if (amountObj instanceof String) {
                            try {
                                amount = Double.parseDouble((String) amountObj);
                            } catch (NumberFormatException e) {
                                Log.e("DEBUG", "Failed to parse amount: " + amountObj, e);
                            }
                        } else if (amountObj instanceof Long) {
                            amount = ((Long) amountObj).doubleValue();
                        }

                        totalIncome += amount;
                    }
                }
                textIncomeAmount.setText(String.format("RM %.2f", totalIncome));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomePageActivity.this, "Failed to load income", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTotalExpense() {
        expensesRef.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalExpense = 0;
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                String currentMonth = dateFormat.format(calendar.getTime());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    Object amountObj = snapshot.child("amount").getValue();

                    if (date != null && date.startsWith(currentMonth)) {
                        double amount = 0;

                        if (amountObj instanceof Double) {
                            amount = (Double) amountObj;
                        } else if (amountObj instanceof String) {
                            String amountStr = (String) amountObj;
                            try {
                                amount = Double.parseDouble(amountStr);
                            } catch (NumberFormatException e) {
                                Log.e("DEBUG", "Failed to parse amount: '" + amountStr + "'. Skipping this entry.", e);
                                continue;
                            }
                        } else if (amountObj instanceof Long) {
                            amount = ((Long) amountObj).doubleValue();
                        } else {
                            Log.e("DEBUG", "Unexpected data type for amount: " + amountObj.getClass().getSimpleName());
                            continue;
                        }

                        totalExpense += amount;
                    }
                }
                textExpensesAmount.setText(String.format("RM %.2f", totalExpense));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomePageActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecentTransactions() {
        transactionList.clear();

        String currentDate = getCurrentDate();

        expensesRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    Object amountObj = snapshot.child("amount").getValue();
                    String description = snapshot.child("description").getValue(String.class);

                    if (date != null && date.startsWith(currentDate) && category != null && amountObj != null) {
                        double amount = 0;

                        if (amountObj instanceof Double) {
                            amount = (Double) amountObj;
                        } else if (amountObj instanceof Long) {
                            amount = ((Long) amountObj).doubleValue();
                        } else if (amountObj instanceof String) {
                            try {
                                amount = Double.parseDouble((String) amountObj);
                            } catch (NumberFormatException e) {
                                Log.e("DEBUG", "Failed to parse amount: " + amountObj, e);
                                continue;
                            }
                        }

                        transactionList.add(new FinancialTransaction(category, amount, description, date));
                        Log.d("DEBUG", "Added expense transaction: " + category + " " + amount + " " + description + " " + date);
                    }
                }
                fetchIncomeTransactions();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomePageActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchIncomeTransactions() {
        incomeRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    Object amountObj = snapshot.child("amount").getValue();
                    String description = snapshot.child("description").getValue(String.class);

                    if (date != null && date.startsWith(getCurrentDate()) && category != null && amountObj != null) {
                        double amount = 0;

                        if (amountObj instanceof Double) {
                            amount = (Double) amountObj;
                        } else if (amountObj instanceof Long) {
                            amount = ((Long) amountObj).doubleValue();
                        } else if (amountObj instanceof String) {
                            try {
                                amount = Double.parseDouble((String) amountObj);
                            } catch (NumberFormatException e) {
                                Log.e("DEBUG", "Failed to parse amount: " + amountObj, e);
                                continue;
                            }
                        }

                        transactionList.add(new FinancialTransaction(category, amount, description, date));
                        Log.d("DEBUG", "Added income transaction: " + category + " " + amount + " " + description + " " + date);
                    }
                }
                combineTransactionsAndNotifyAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomePageActivity.this, "Failed to load income", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void combineTransactionsAndNotifyAdapter() {
        // Sort the transactionList by date in descending order
        Collections.sort(transactionList, new Comparator<FinancialTransaction>() {
            @Override
            public int compare(FinancialTransaction t1, FinancialTransaction t2) {
                return t2.getDate().compareTo(t1.getDate());
            }
        });

        // Limit the list to the last 3 transactions
        if (transactionList.size() > 3) {
            transactionList = transactionList.subList(0, 3);
        }

        transactionAdapter.notifyDataSetChanged();
    }

    public void displayGoals() {
        // Remove old views
        goalLayout.removeAllViews();

        // Reference to Firebase database for goals
        DatabaseReference goalsRef = FirebaseDatabase.getInstance().getReference("goal");
        goalsRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    goalText.setVisibility(View.GONE); // Hide the placeholder text if goals exist

                    // Loop through the goals from Firebase
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String goalName = snapshot.child("goalName").getValue(String.class);
                        int progress = snapshot.child("progress").getValue(Integer.class);
                        double currentAmount = snapshot.child("currentAmount").getValue(Double.class);
                        double targetAmount = snapshot.child("targetAmount").getValue(Double.class);

                        // Inflate the goal layout
                        LinearLayout goalItemLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_homepage, null);

                        // Reference to the views
                        TextView goalNameView = goalItemLayout.findViewById(R.id.goalNameText);
                        ProgressBar progressBar = goalItemLayout.findViewById(R.id.progressBarGoal);
                        TextView progressText = goalItemLayout.findViewById(R.id.progressText);

                        // Set goal data
                        goalNameView.setText(goalName);
                        progressBar.setMax(100);
                        progressBar.setProgress(progress);
                        progressText.setText(String.format("RM %.2f of RM %.2f", currentAmount, targetAmount));

                        // Check if the goal is complete
                        if (progress >= 100) {
                            TextView complete = new TextView(HomePageActivity.this);
                            complete.setText("You have exceeded your budget limit!");
                            complete.setTextSize(15);
                            complete.setTextColor(getResources().getColor(R.color.green));
                            goalItemLayout.addView(complete);
                        }

                        // Add the goal layout to the parent layout
                        goalLayout.addView(goalItemLayout);
                    }
                } else {
                    goalText.setVisibility(View.VISIBLE); // Show placeholder text if no goals
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomePageActivity.this, "Failed to load goals", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(Calendar.getInstance().getTime());
    }
}
