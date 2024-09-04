package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
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
    private String uid, currentCurrency;

    // List of currencies that the user can select
    private final List<String> currencies = Arrays.asList("USD", "SGD", "MYR", "EUR", "GBP", "JPY", "CNY", "KRW", "AUD", "CAD", "INR", "IDR", "THB", "VND", "PHP", "HKD", "TWD");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        currencyListView = findViewById(R.id.currencyListView);
        ImageButton backButton = findViewById(R.id.backButton);

        SharedPreferences currencyPref = getSharedPreferences("CurrencyPrefs", Context.MODE_PRIVATE);
        currentCurrency = currencyPref.getString("selectedCurrency", "MYR");
        String[] currencyUnits = getResources().getStringArray(R.array.currency_units);
        String currencyUnit = getCurrencyUnit(currentCurrency, currencyUnits);

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
            saveSelectedCurrency(selectedCurrency);
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

    private double getBaseRate(String targetCurrency) throws IOException {
        ExchangeRateService exchangeRateService = new ExchangeRateService();
        ExchangeRateResponse exchangeRateResponse = exchangeRateService.getExchangeRate(currentCurrency);

        if (exchangeRateResponse.data.containsKey(targetCurrency)) {
            return exchangeRateResponse.data.get(targetCurrency);
        } else {
            throw new IllegalArgumentException("Target currency not found: " + targetCurrency);
        }
    }



    private class CurrencyConversionTask extends AsyncTask<Void, Void, Double[]> {
        private String baseCurrency;
        private String targetCurrency;

        CurrencyConversionTask(String baseCurrency, String targetCurrency) {
            this.baseCurrency = baseCurrency;
            this.targetCurrency = targetCurrency;
        }

        @Override
        protected Double[] doInBackground(Void... voids) {
            try {
                double conversionRate = getConversionRate(baseCurrency, targetCurrency);
                double baseRate = getBaseRate(targetCurrency);
                return new Double[]{conversionRate, baseRate};
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Double[] rates) {
            if (rates != null) {
                double conversionRate = rates[0];
                double baseRate = rates[1];
                updateDatabaseWithConvertedValues(uid, conversionRate, baseRate);
            } else {
                Toast.makeText(CurrencyConverter.this, "Error getting conversion rate", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateDatabaseWithConvertedValues(String uid, double conversionRate, double baseRate) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        // Update Budgets
        database.child("budget").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double convertedCurrentBudget = snapshot.child("convertedCurrentBudget").getValue(Double.class);
                    Double convertedBudgetLimit = snapshot.child("convertedBudgetLimit").getValue(Double.class);

                    if (convertedCurrentBudget != null && convertedBudgetLimit != null) {
                        if (!snapshot.hasChild("currentBudget") || !snapshot.hasChild("budgetLimit")) {
                            double currentBudget = convertedCurrentBudget * baseRate;
                            double budgetLimit = convertedBudgetLimit * baseRate;
                            currentBudget = Double.parseDouble(decimalFormat.format(currentBudget));
                            budgetLimit = Double.parseDouble(decimalFormat.format(budgetLimit));
                            snapshot.getRef().child("currentBudget").setValue(currentBudget);
                            snapshot.getRef().child("budgetLimit").setValue(budgetLimit);
                        }

                        Double currentBudget = snapshot.child("currentBudget").getValue(Double.class);
                        Double budgetLimit = snapshot.child("budgetLimit").getValue(Double.class);

                        if (currentBudget != null && budgetLimit != null) {
                            double newConvertedCurrentBudget = currentBudget * conversionRate;
                            double newConvertedBudgetLimit = budgetLimit * conversionRate;
                            newConvertedCurrentBudget = Double.parseDouble(decimalFormat.format(newConvertedCurrentBudget));
                            newConvertedBudgetLimit = Double.parseDouble(decimalFormat.format(newConvertedBudgetLimit));
                            snapshot.getRef().child("convertedCurrentBudget").setValue(newConvertedCurrentBudget);
                            snapshot.getRef().child("convertedBudgetLimit").setValue(newConvertedBudgetLimit);
                        }
                    } else {
                        Toast.makeText(CurrencyConverter.this, "Converted budget values are missing for some budgets", Toast.LENGTH_SHORT).show();
                    }
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
                    Double convertedCurrentAmount = snapshot.child("convertedCurrentAmount").getValue(Double.class);
                    Double convertedTargetAmount = snapshot.child("convertedTargetAmount").getValue(Double.class);

                    if (convertedCurrentAmount != null && convertedTargetAmount != null) {
                        if (!snapshot.hasChild("currentAmount") || !snapshot.hasChild("targetAmount")) {
                            double currentAmount = convertedCurrentAmount * baseRate;
                            double targetAmount = convertedTargetAmount * baseRate;
                            currentAmount = Double.parseDouble(decimalFormat.format(currentAmount));
                            targetAmount = Double.parseDouble(decimalFormat.format(targetAmount));
                            snapshot.getRef().child("currentAmount").setValue(currentAmount);
                            snapshot.getRef().child("targetAmount").setValue(targetAmount);
                        }

                        Double currentAmount = snapshot.child("currentAmount").getValue(Double.class);
                        Double targetAmount = snapshot.child("targetAmount").getValue(Double.class);

                        if (currentAmount != null && targetAmount != null) {
                            double newConvertedCurrentAmount = currentAmount * conversionRate;
                            double newConvertedTargetAmount = targetAmount * conversionRate;
                            newConvertedCurrentAmount = Double.parseDouble(decimalFormat.format(newConvertedCurrentAmount));
                            newConvertedTargetAmount = Double.parseDouble(decimalFormat.format(newConvertedTargetAmount));
                            snapshot.getRef().child("convertedCurrentAmount").setValue(newConvertedCurrentAmount);
                            snapshot.getRef().child("convertedTargetAmount").setValue(newConvertedTargetAmount);
                        }
                    } else {
                        Toast.makeText(CurrencyConverter.this, "Converted values are missing for some goals", Toast.LENGTH_SHORT).show();
                    }
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
                    Double convertedAmount = snapshot.child("convertedAmount").getValue(Double.class);

                    if (convertedAmount != null) {
                        if (!snapshot.hasChild("amount")) {
                            double amount = convertedAmount * baseRate;
                            amount = Double.parseDouble(decimalFormat.format(amount));
                            snapshot.getRef().child("amount").setValue(amount);
                        }

                        Double amount = snapshot.child("amount").getValue(Double.class);
                        if (amount != null) {
                            double newConvertedAmount = amount * conversionRate;
                            newConvertedAmount = Double.parseDouble(decimalFormat.format(newConvertedAmount));
                            snapshot.getRef().child("convertedAmount").setValue(newConvertedAmount);
                        }
                    } else {
                        Toast.makeText(CurrencyConverter.this, "Converted amount is missing for some expenses", Toast.LENGTH_SHORT).show();
                    }
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
                    Double convertedIncome = snapshot.child("convertedIncome").getValue(Double.class);

                    if (convertedIncome != null) {
                        if (!snapshot.hasChild("amount")) {
                            double amount = convertedIncome * baseRate;
                            amount = Double.parseDouble(decimalFormat.format(amount));
                            snapshot.getRef().child("amount").setValue(amount);
                        }

                        Double amount = snapshot.child("amount").getValue(Double.class);
                        if (amount != null) {
                            double newConvertedIncome = amount * conversionRate;
                            newConvertedIncome = Double.parseDouble(decimalFormat.format(newConvertedIncome));
                            snapshot.getRef().child("convertedIncome").setValue(newConvertedIncome);
                        }
                    } else {
                        Toast.makeText(CurrencyConverter.this, "Converted income is missing for some incomes", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CurrencyConverter.this, "Currency Conversion Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSelectedCurrency(String currency) {
        getSharedPreferences("CurrencyPrefs", MODE_PRIVATE)
                .edit()
                .putString("selectedCurrency", currency)
                .apply();
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
