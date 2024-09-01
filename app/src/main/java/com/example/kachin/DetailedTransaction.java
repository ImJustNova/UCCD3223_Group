package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DetailedTransaction extends AppCompatActivity {

    private String currencyUnit;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_transaction);

        TextView amount = findViewById(R.id.amount);
        TextView date = findViewById(R.id.date);
        TextView transactionType = findViewById(R.id.transactionType);
        TextView category = findViewById(R.id.category);
        TextView description = findViewById(R.id.description);
        TextView noAttachment = findViewById(R.id.noAttachment);
        ImageView attachment = findViewById(R.id.attachment);
        LinearLayout transactionColor = findViewById(R.id.transactionColor);

        SharedPreferences currencyPref = getSharedPreferences("CurrencyPrefs", Context.MODE_PRIVATE);
        String selectedCurrency = currencyPref.getString("selectedCurrency", "MYR");
        String[] currencyUnits = getResources().getStringArray(R.array.currency_units);
        currencyUnit = getCurrencyUnit(selectedCurrency, currencyUnits);

        Intent intent = getIntent();
        double amountValue = intent.getDoubleExtra("amount", 0.0);  // Retrieving the double value
        String dateText = intent.getStringExtra("date");
        String transactionTypeText = intent.getStringExtra("type");
        String categoryText = intent.getStringExtra("category");
        String descriptionText = intent.getStringExtra("description");
        String imageUrl = intent.getStringExtra("image");

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
            finish();
        }

        amount.setText(String.format(currencyUnit + " %.2f", amountValue));  // Formatting the double value as String
        date.setText(dateText);
        transactionType.setText(transactionTypeText);
        category.setText(categoryText);

        if (descriptionText != null && !descriptionText.isEmpty()) {
            description.setText(descriptionText);
            description.setTextColor(getResources().getColor(android.R.color.black));
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(attachment);
            noAttachment.setVisibility(View.GONE);
        } else {
            attachment.setVisibility(View.GONE);
        }
        if (transactionTypeText.equals("income")) {
            transactionColor.setBackgroundResource(R.drawable.half_screen_background);
        } else {
            transactionColor.setBackgroundResource(R.drawable.half_screen_background_red);
        }
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
