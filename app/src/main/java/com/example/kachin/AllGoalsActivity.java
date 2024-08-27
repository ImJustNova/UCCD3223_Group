package com.example.kachin;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;



// this is not needed, will be deleted (along with the xml file)



public class AllGoalsActivity extends AppCompatActivity {

    private LinearLayout goalsLayout;
    private DatabaseReference goalRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_goals);

        // Initialize UI components
        goalsLayout = findViewById(R.id.goalsLayout);

        // Initialize Firebase reference
        goalRef = FirebaseDatabase.getInstance().getReference("goal");

        fetchAllGoals();
    }

    private void fetchAllGoals() {
        goalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goalsLayout.removeAllViews(); // Clear previous views

                for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                    String goalName = goalSnapshot.child("goalName").getValue(String.class);
                    double currentAmount = goalSnapshot.child("currentAmount").getValue(Double.class);
                    double targetAmount = goalSnapshot.child("targetAmount").getValue(Double.class);

                    double progress = (currentAmount / targetAmount) * 100;

                    // Create a new layout for each goal
                    LinearLayout goalItemLayout = new LinearLayout(AllGoalsActivity.this);
                    goalItemLayout.setOrientation(LinearLayout.VERTICAL);
                    goalItemLayout.setPadding(10, 10, 10, 10);

                    TextView goalNameText = new TextView(AllGoalsActivity.this);
                    goalNameText.setText(goalName);
                    goalNameText.setTextSize(18);
                    goalNameText.setTextColor(getResources().getColor(R.color.black));
                    goalItemLayout.addView(goalNameText);

                    ProgressBar progressBar = new ProgressBar(AllGoalsActivity.this, null, android.R.attr.progressBarStyleHorizontal);
                    progressBar.setMax(100);
                    progressBar.setProgress((int) progress);
                    goalItemLayout.addView(progressBar);

                    TextView progressText = new TextView(AllGoalsActivity.this);
                    progressText.setText(String.format(Locale.getDefault(), "%.0f%%", progress));
                    progressText.setTextSize(16);
                    progressText.setTextColor(getResources().getColor(R.color.black));
                    goalItemLayout.addView(progressText);

                    goalsLayout.addView(goalItemLayout);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AllGoalsActivity.this, "Failed to load goals", Toast.LENGTH_SHORT).show();
            }
        });
    }
}