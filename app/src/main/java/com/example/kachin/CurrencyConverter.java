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
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

// Note: Currency converter only works when user changes a currency to save the request rate (request rate is limited)

public class CurrencyConverter extends AppCompatActivity {

    private ListView currencyListView;
    private DatabaseReference database;
    private String uid;

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
            new CurrencyConversionTask("MYR", selectedCurrency).execute();
            Toast.makeText(this, "The selected currency is " + selectedCurrency, Toast.LENGTH_SHORT).show();
            finish();
        });

        backButton.setOnClickListener(v -> finish());
    }

    private double getConversionRate(String baseCurrency, String targetCurrency) throws IOException {
        ExchangeRateService exchangeRateService = new ExchangeRateService();
        ExchangeRateResponse exchangeRateResponse = exchangeRateService.getExchangeRate(baseCurrency);

        if (exchangeRateResponse.data.containsKey(targetCurrency)) {
            return exchangeRateResponse.data.get(targetCurrency);
        } else {
            throw new IllegalArgumentException("Target currency not found: " + targetCurrency);
        }
    }

    private class CurrencyConversionTask extends AsyncTask<Void, Void, Double> {
        private String baseCurrency;
        private String targetCurrency;

        CurrencyConversionTask(String baseCurrency, String targetCurrency) {
            this.baseCurrency = baseCurrency;
            this.targetCurrency = targetCurrency;
        }

        @Override
        protected Double doInBackground(Void... voids) {
            try {
                // Get conversion rate in the background
                return getConversionRate(baseCurrency, targetCurrency);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Double conversionRate) {
            if (conversionRate != null) {
                updateDatabaseWithConvertedValues(uid, conversionRate);
            } else {
                Toast.makeText(CurrencyConverter.this, "Error getting conversion rate", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateDatabaseWithConvertedValues(String uid, double conversionRate) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        database.child("budget").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double budgetLimit = snapshot.child("budgetLimit").getValue(Double.class);
                    double currentBudget = snapshot.child("currentBudget").getValue(Double.class);
                    double convertedBudgetLimit = budgetLimit * conversionRate;
                    double convertedCurrentBudget = currentBudget * conversionRate;

                    // Format the values to 2 decimal places
                    convertedBudgetLimit = Double.parseDouble(decimalFormat.format(convertedBudgetLimit));
                    convertedCurrentBudget = Double.parseDouble(decimalFormat.format(convertedCurrentBudget));

                    snapshot.getRef().child("convertedBudgetLimit").setValue(convertedBudgetLimit);
                    snapshot.getRef().child("convertedCurrentBudget").setValue(convertedCurrentBudget);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Update Goals
        database.child("goal").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double currentAmount = snapshot.child("currentAmount").getValue(Double.class);
                    double targetAmount = snapshot.child("targetAmount").getValue(Double.class);
                    double convertedCurrentAmount = currentAmount * conversionRate;
                    double convertedTargetAmount = targetAmount * conversionRate;

                    // Format the values to 2 decimal places
                    convertedCurrentAmount = Double.parseDouble(decimalFormat.format(convertedCurrentAmount));
                    convertedTargetAmount = Double.parseDouble(decimalFormat.format(convertedTargetAmount));

                    snapshot.getRef().child("convertedCurrentAmount").setValue(convertedCurrentAmount);
                    snapshot.getRef().child("convertedTargetAmount").setValue(convertedTargetAmount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Update Expenses
        database.child("expense").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double amount = snapshot.child("amount").getValue(Double.class);
                    double convertedAmount = amount * conversionRate;

                    // Format the value to 2 decimal places
                    convertedAmount = Double.parseDouble(decimalFormat.format(convertedAmount));

                    snapshot.getRef().child("convertedAmount").setValue(convertedAmount);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Update Incomes
        database.child("income").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double amount = snapshot.child("amount").getValue(Double.class);
                    double convertedIncome = amount * conversionRate;

                    // Format the value to 2 decimal places
                    convertedIncome = Double.parseDouble(decimalFormat.format(convertedIncome));

                    snapshot.getRef().child("convertedIncome").setValue(convertedIncome);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
