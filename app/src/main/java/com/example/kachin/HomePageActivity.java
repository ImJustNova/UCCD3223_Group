package com.example.kachin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private Button seeAllButton,seeAllbtn;
    private DatabaseReference expensesRef;
    private DatabaseReference incomeRef;
    private DatabaseReference goalRef;
    private ProgressBar progressBarGoal;
    private TextView goalNameText, progressText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

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

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(transactionAdapter);

        // Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        expensesRef = database.getReference("expense");
        incomeRef = database.getReference("income");

        // Set the current month in the UI
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String currentMonth = monthFormat.format(calendar.getTime());
        monthView.setText(currentMonth);

        setupButtonListeners();

        fetchTotalIncome();
        fetchTotalExpense();
        fetchRecentTransactions();

        seeAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, AllTransactionsActivity.class);
            intent.putExtra("date", getCurrentDate());
            startActivity(intent);
        });

        progressBarGoal = findViewById(R.id.progressBarGoal);
        goalNameText = findViewById(R.id.goalNameText);
        progressText = findViewById(R.id.progressText);


        goalRef = FirebaseDatabase.getInstance().getReference("goal");

        fetchGoalProgress();

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
        incomeRef.addValueEventListener(new ValueEventListener() {
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
        expensesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalExpense = 0;
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                String currentMonth = dateFormat.format(calendar.getTime());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    Object amountObj = snapshot.child("amount").getValue();

                    if (date != null && amountObj != null && date.startsWith(currentMonth)) {
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

        expensesRef.orderByChild("date").equalTo(currentDate).limitToLast(3).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    Object amountObj = snapshot.child("amount").getValue();
                    String description = snapshot.child("description").getValue(String.class);

                    if (date != null && category != null && amountObj != null) {
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
                combineTransactionsAndNotifyAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomePageActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });

        incomeRef.orderByChild("date").equalTo(currentDate).limitToLast(3).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    String category = snapshot.child("category").getValue(String.class);
                    Object amountObj = snapshot.child("amount").getValue();
                    String description = snapshot.child("description").getValue(String.class);

                    if (date != null && category != null && amountObj != null) {
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
        Collections.sort(transactionList, new Comparator<FinancialTransaction>() {
            @Override
            public int compare(FinancialTransaction t1, FinancialTransaction t2) {
                return t2.getDate().compareTo(t1.getDate());
            }
        });

        if (transactionList.size() > 3) {
            transactionList = transactionList.subList(0, 3);
        }

        transactionAdapter.notifyDataSetChanged();
        Log.d("DEBUG", "Number of transactions to display: " + transactionList.size());
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private void fetchGoalProgress() {
        goalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                    String goalName = goalSnapshot.child("goalName").getValue(String.class);
                    double currentAmount = goalSnapshot.child("currentAmount").getValue(Double.class);
                    double targetAmount = goalSnapshot.child("targetAmount").getValue(Double.class);

                    double progress = (currentAmount / targetAmount) * 100;

                    goalNameText.setText(goalName);
                    progressBarGoal.setProgress((int) progress);
                    progressText.setText(String.format(Locale.getDefault(), "%.0f%%", progress));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomePageActivity.this, "Failed to load goal progress", Toast.LENGTH_SHORT).show();
            }
        });
    }
}