package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisplayHistoryActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextView pageTitle, transactionText;
    private DatabaseReference database;
    private String uid;
    private LinearLayout transactionContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_history);

        backButton = findViewById(R.id.backButton);
        pageTitle = findViewById(R.id.pageTitle);
        transactionContainer = findViewById(R.id.transactionContainer);
        transactionText = findViewById(R.id.transactionText);

        Intent intent = getIntent();
        String selectedDate = intent.getStringExtra("selectedDate");

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            loadTransactions(selectedDate);
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
            finish();
        }

        pageTitle.setText("History for " + selectedDate);

        backButton.setOnClickListener(v -> finish());


    }

    private void loadTransactions(String date) {
        loadExpenses(date);
        loadIncome(date);
    }

    private void loadExpenses(String date) {
        database.child("expense").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String transactionDate = snapshot.child("date").getValue(String.class);
                    if (transactionDate != null && transactionDate.equals(date)) {
                        double amount = snapshot.child("amount").getValue(Double.class);
                        String category = snapshot.child("category").getValue(String.class);
                        String description = snapshot.child("description").getValue(String.class);
                        String date = snapshot.child("date").getValue(String.class);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                        addTransactionView(amount, category, description, date, imageUrl, "expense");
                        transactionText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DisplayHistoryActivity.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadIncome(String date) {
        database.child("income").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String transactionDate = snapshot.child("date").getValue(String.class);
                    if (transactionDate != null && transactionDate.equals(date)) {
                        double amount = snapshot.child("amount").getValue(Double.class);
                        String category = snapshot.child("category").getValue(String.class);
                        String date = snapshot.child("date").getValue(String.class);
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                        addTransactionView(amount, category, null, date, imageUrl, "income");
                        transactionText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DisplayHistoryActivity.this, "Failed to load income.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTransactionView(double amount, String category, String description, String date, String image, String type) {
        View transactionView = LayoutInflater.from(this).inflate(R.layout.transaction_item, transactionContainer, false);

        TextView amountView = transactionView.findViewById(R.id.amountView);
        TextView categoryView = transactionView.findViewById(R.id.categoryView);
        TextView descriptionView = transactionView.findViewById(R.id.descriptionView);
        TextView dateView = transactionView.findViewById(R.id.dateView);
        ImageView categoryIcon = transactionView.findViewById(R.id.transactionCategoryIcon);

        categoryView.setText(category);
        amountView.setText(String.format("RM %.2f", amount));
        dateView.setText(date);


        if (type.equals("expense")) {
            amountView.setTextColor(getResources().getColor(R.color.red));
            switch (category) {
                case "Food and Drinks":
                    categoryIcon.setImageResource(R.drawable.food_and_drinks_icon);
                    break;
                case "Transport":
                    categoryIcon.setImageResource(R.drawable.transport_icon);
                    break;
                case "Groceries":
                    categoryIcon.setImageResource(R.drawable.groceries_icon);
                    break;
                case "Entertainment":
                    categoryIcon.setImageResource(R.drawable.entertainment_icon);
                    break;
                case "Rent":
                    categoryIcon.setImageResource(R.drawable.rent_icon);
                    break;
                case "Others":
                    categoryIcon.setImageResource(R.drawable.others_icon);
                    break;
                default:
                    categoryIcon.setImageResource(R.drawable.profile); // Use a default icon for unknown categories
                    break;
            }
        } else {
            amountView.setTextColor(getResources().getColor(R.color.green));
            switch(category){
                case "Salary":
                    categoryIcon.setImageResource(R.drawable.income);
                    break;
                case "Savings":
                    categoryIcon.setImageResource(R.drawable.wallet);
                    break;
                default:
                    categoryIcon.setImageResource(R.drawable.coin);
                    break;
            }
        }

        if (description != null && !description.isEmpty()) {
            descriptionView.setText(description);
        } else {
            descriptionView.setVisibility(View.GONE);
        }
        transactionContainer.addView(transactionView);

        transactionView.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayHistoryActivity.this, DetailedTransaction.class);
            intent.putExtra("amount", amount);
            intent.putExtra("category", category);
            intent.putExtra("description", description);
            intent.putExtra("date", date);
            intent.putExtra("type", type);
            intent.putExtra("uid", uid);
            intent.putExtra("image", image);
            startActivity(intent);
        });
    }
}