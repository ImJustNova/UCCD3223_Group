package com.example.kachin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomePageActivity extends BaseActivity {

    private TextView textIncomeAmount, textExpensesAmount;
    private ImageButton btnHome, btnAdd, btnHistory, btnReport, btnProfile;
    private TransactionAdapter transactionAdapter;
    private List<FinancialTransaction> transactionList = new ArrayList<>();
    private DatabaseReference expensesRef, incomeRef, goalRef, budgetRef;
    private String uid, currencyUnit;
    private LinearLayout goalLayout;

    public HomePageActivity() {
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        expensesRef = database.getReference("expense");
        incomeRef = database.getReference("income");
        goalRef = database.getReference("goal");
        budgetRef = database.getReference("budget");

        SharedPreferences currencyPref = getSharedPreferences("CurrencyPrefs", Context.MODE_PRIVATE);
        String selectedCurrency = currencyPref.getString("selectedCurrency", "MYR");
        String[] currencyUnits = getResources().getStringArray(R.array.currency_units);
        currencyUnit = getCurrencyUnit(selectedCurrency, currencyUnits);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView monthView = findViewById(R.id.monthView);
        textIncomeAmount = findViewById(R.id.textIncomeAmount);
        textExpensesAmount = findViewById(R.id.textExpensesAmount);
        btnHome = findViewById(R.id.btnHome);
        btnAdd = findViewById(R.id.btnAdd);
        btnHistory = findViewById(R.id.btnHistory);
        btnReport = findViewById(R.id.btnReport);
        btnProfile = findViewById(R.id.btnProfile);
        RecyclerView recyclerView = findViewById(R.id.transactionRecyclerView);
        Button seeAllButton = findViewById(R.id.seeAllText);
        Button seeAllbtn = findViewById(R.id.btnSeeAllGoals);
        goalLayout = findViewById(R.id.goalLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(this, transactionList);
        recyclerView.setAdapter(transactionAdapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String currentMonth = monthFormat.format(calendar.getTime());
        monthView.setText(currentMonth);

        setupButtonListeners();
        resetBudgetIfNecessary(uid);
        fetchTotalIncome();
        fetchTotalExpense();
        fetchRecentTransactions();
        displayGoals();
        displayCurrentMonth();

        seeAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, HistoryActivity.class);
            intent.putExtra("date", getCurrentDate());
            startActivity(intent);
        });

        seeAllbtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, GoalAndBudget.class);
            startActivity(intent);
        });
    }

    private void displayCurrentMonth() {
        TextView monthTextView = findViewById(R.id.monthView);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentMonth = monthFormat.format(calendar.getTime());

        monthTextView.setText(currentMonth);
    }

    private void updateTransactionView(List<FinancialTransaction> transactions) {
        RecyclerView recyclerView = findViewById(R.id.transactionRecyclerView);
        TextView noRecordsTextView = findViewById(R.id.noRecordsTextView);

        if (transactions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            noRecordsTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noRecordsTextView.setVisibility(View.GONE);

            TransactionAdapter adapter = new TransactionAdapter(this, transactions);
            recyclerView.setAdapter(adapter);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setupButtonListeners() {
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, HomePageActivity.class);
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, report.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
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
                    Object amountObj = snapshot.child("convertedIncome").getValue();

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
                textIncomeAmount.setText(String.format(currencyUnit + " %.2f", totalIncome));
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
                    Object amountObj = snapshot.child("convertedAmount").getValue();

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
                textExpensesAmount.setText(String.format(currencyUnit + " %.2f", totalExpense));
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
                    Object amountObj = snapshot.child("convertedAmount").getValue();
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
                updateTransactionView(transactionList);
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
                    Object amountObj = snapshot.child("convertedIncome").getValue();
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
    }

    private void displayGoals() {
        goalRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalGoalsAmount = 0;
                goalLayout.removeAllViews();

                boolean isFirstGoal = true;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (!isFirstGoal) break;

                    String goalName = snapshot.child("goalName").getValue(String.class);
                    int progress = snapshot.child("progress").getValue(Integer.class);
                    double convertedCurrentAmount = snapshot.child("convertedCurrentAmount").getValue(Double.class);
                    double convertedTargetAmount = snapshot.child("convertedTargetAmount").getValue(Double.class);

                    totalGoalsAmount += convertedTargetAmount;

                    LinearLayout goalItemLayout = new LinearLayout(HomePageActivity.this);
                    goalItemLayout.setOrientation(LinearLayout.VERTICAL);
                    goalItemLayout.setPadding(10, 10, 10, 10);

                    TextView goalNameView = new TextView(HomePageActivity.this);
                    goalNameView.setText(goalName);
                    goalNameView.setTextSize(20);
                    goalNameView.setTypeface(null, Typeface.BOLD);

                    ProgressBar progressBar = new ProgressBar(HomePageActivity.this, null, android.R.attr.progressBarStyleHorizontal);
                    progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    progressBar.setMax(100);
                    progressBar.setProgress(progress);

                    goalItemLayout.addView(goalNameView);
                    goalItemLayout.addView(progressBar);

                    if (progress >= 100) {
                        TextView warning = new TextView(HomePageActivity.this);
                        warning.setText("You have exceeded your goal!");
                        warning.setTextSize(15);
                        warning.setTextColor(getResources().getColor(R.color.red));
                        goalItemLayout.addView(warning);
                    }

                    goalLayout.addView(goalItemLayout);

                    isFirstGoal = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomePageActivity.this, "Failed to load goals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetBudgetIfNecessary(String uid) {
        budgetRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Calendar calendar = Calendar.getInstance();
                    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                    String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.getTime());

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String timeFrame = snapshot.child("timeFrame").getValue(String.class);
                        String lastResetDate = snapshot.child("lastResetDate").getValue(String.class);

                        if (timeFrame != null && lastResetDate != null) {
                            boolean shouldReset = false;

                            if (timeFrame.equals("daily")) {
                                shouldReset = !lastResetDate.equals(currentDate);
                            } else if (timeFrame.equals("monthly")) {
                                shouldReset = !lastResetDate.startsWith(currentMonth);
                            }

                            if (shouldReset) {
                                Double convertedCurrentBudget = snapshot.child("convertedCurrentBudget").getValue(Double.class);
                                Double currentBudget = snapshot.child("currentBudget").getValue(Double.class);
                                Double progress = snapshot.child("progress").getValue(Double.class);

                                if (convertedCurrentBudget == null) convertedCurrentBudget = 0.0;
                                if (currentBudget == null) currentBudget = 0.0;
                                if (progress == null) progress = 0.0;

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("convertedCurrentBudget", 0);
                                updates.put("currentBudget", 0);
                                updates.put("progress", 0);
                                updates.put("lastResetDate", currentDate);

                                snapshot.getRef().updateChildren(updates);

                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivity.this, "Failed to reset budgets", Toast.LENGTH_SHORT).show();
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

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(Calendar.getInstance().getTime());
    }
}