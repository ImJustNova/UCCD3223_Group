package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CurrencyConverter extends AppCompatActivity {

    private ListView currencyListView;
    private DatabaseReference database;
    private String uid;

    // List of currency options
    private final List<String> currencies = Arrays.asList("USD", "SGD", "MYR", "EUR", "GBP");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        currencyListView = findViewById(R.id.currencyListView);
        ImageButton backButton = findViewById(R.id.backButton);


        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currencies);
        currencyListView.setAdapter(adapter);

        currencyListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCurrency = currencies.get(position);
            double amountToConvert = 100; // Replace with the actual amount to be converted
            new CurrencyConversionTask(amountToConvert, "MYR", selectedCurrency).execute(); // Execute AsyncTask
            Toast.makeText(this, "Selected currency is " + selectedCurrency, Toast.LENGTH_SHORT).show();
        });

        backButton.setOnClickListener(v -> finish());
    }

    // Method to perform currency conversion
    private double convertCurrency(double amount, String baseCurrency, String targetCurrency) throws IOException {
        ExchangeRateService exchangeRateService = new ExchangeRateService();
        ExchangeRateResponse exchangeRateResponse = exchangeRateService.getExchangeRate(baseCurrency);

        if (exchangeRateResponse.conversion_rates.containsKey(targetCurrency)) {
            double conversionRate = exchangeRateResponse.conversion_rates.get(targetCurrency);
            return amount * conversionRate;
        } else {
            throw new IllegalArgumentException("Target currency not found: " + targetCurrency);
        }
    }

    // AsyncTask to handle currency conversion in the background
    private class CurrencyConversionTask extends AsyncTask<Void, Void, Double> {
        private String baseCurrency;
        private String targetCurrency;
        private double amount;

        CurrencyConversionTask(double amount, String baseCurrency, String targetCurrency) {
            this.amount = amount;
            this.baseCurrency = baseCurrency;
            this.targetCurrency = targetCurrency;
        }

        @Override
        protected Double doInBackground(Void... voids) {
            try {
                // Perform currency conversion in the background
                return convertCurrency(amount, baseCurrency, targetCurrency);
            } catch (IOException e) {
                e.printStackTrace();
                return null; // Return null if there's an error
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null; // Handle the case where target currency is not found
            }
        }

        @Override
        protected void onPostExecute(Double convertedAmount) {
            if (convertedAmount != null) {
                // Use the converted amount to update the database or display results
                updateDatabaseWithConvertedValues(uid, convertedAmount); // You might need to adjust this method accordingly
                Toast.makeText(CurrencyConverter.this, "Converted amount: " + convertedAmount, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CurrencyConverter.this, "Error converting currency", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to update the database with converted values
    private void updateDatabaseWithConvertedValues(String uid, double conversionRate) {
        database.child("budget").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double budgetLimit = snapshot.child("budgetLimit").getValue(Double.class);
                    double currentBudget = snapshot.child("currentBudget").getValue(Double.class);
                    double convertedBudgetLimit = budgetLimit * conversionRate;
                    double convertedCurrentBudget = currentBudget * conversionRate;

                    snapshot.getRef().child("convertedBudgetLimit").setValue(convertedBudgetLimit);
                    snapshot.getRef().child("convertedCurrentBudget").setValue(convertedCurrentBudget);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        database.child("budget").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double budgetLimit = snapshot.child("budgetLimit").getValue(Double.class);
                    double currentBudget = snapshot.child("currentBudget").getValue(Double.class);
                    double convertedBudgetLimit = budgetLimit * conversionRate;
                    double convertedCurrentBudget = currentBudget * conversionRate;


                    snapshot.getRef().child("convertedBudgetLimit").setValue(convertedBudgetLimit);
                    snapshot.getRef().child("convertedCurrentBudget").setValue(convertedCurrentBudget);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        database.child("goal").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double currentAmount = snapshot.child("currentAmount").getValue(Double.class);
                    double targetAmount = snapshot.child("targetAmount").getValue(Double.class);
                    double convertedCurrentAmount = currentAmount * conversionRate;
                    double convertedTargetAmount = targetAmount * conversionRate;

                    snapshot.getRef().child("convertedCurrentAmount").setValue(convertedCurrentAmount);
                    snapshot.getRef().child("convertedTargetAmount").setValue(convertedTargetAmount);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        database.child("expense").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double amount = snapshot.child("amount").getValue(Double.class);
                    double convertedAmount = amount * conversionRate;

                    snapshot.getRef().child("convertedAmount").setValue(convertedAmount);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        database.child("income").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double amount = snapshot.child("amount").getValue(Double.class);
                    double convertedIncome = amount * conversionRate;

                    snapshot.getRef().child("convertedIncome").setValue(convertedIncome);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
