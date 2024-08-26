package com.example.kachin;

import android.os.Bundle;
import android.util.Log;
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

public class AllTransactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<FinancialTransaction> transactionList = new ArrayList<>();
    private DatabaseReference expensesRef;
    private DatabaseReference incomeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transactions);

        recyclerView = findViewById(R.id.recyclerViewAllTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        transactionAdapter = new TransactionAdapter(transactionList);
        recyclerView.setAdapter(transactionAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        expensesRef = database.getReference("expense");
        incomeRef = database.getReference("income");

        fetchAllTransactionsForToday();
    }

    private void fetchAllTransactionsForToday() {
        transactionList.clear();

        String currentDate = getCurrentDate();

        // Fetch expenses
        expensesRef.orderByChild("date").equalTo(currentDate).addValueEventListener(new ValueEventListener() {
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
                                continue; // Skip this transaction if parsing fails
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
                Toast.makeText(AllTransactionsActivity.this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch income
        incomeRef.orderByChild("date").equalTo(currentDate).addValueEventListener(new ValueEventListener() {
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
                                continue; // Skip this transaction if parsing fails
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
                Toast.makeText(AllTransactionsActivity.this, "Failed to load income", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void combineTransactionsAndNotifyAdapter() {
        // Sort transactions by date (assuming the date is in the format yyyy-MM-dd)
        Collections.sort(transactionList, new Comparator<FinancialTransaction>() {
            @Override
            public int compare(FinancialTransaction t1, FinancialTransaction t2) {
                return t2.getDate().compareTo(t1.getDate()); // Sort descending by date
            }
        });

        transactionAdapter.notifyDataSetChanged();
        Log.d("DEBUG", "Number of transactions to display: " + transactionList.size());
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}