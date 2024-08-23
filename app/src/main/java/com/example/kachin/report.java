package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class report extends AppCompatActivity {

    private Button btnExpense, btnIncome;
    private ImageButton btnBack;
    private TextView totalAmount, monthView;
    private PieChart pieChart;
    private DatabaseReference database;
    private String uid;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);
        btnBack = findViewById(R.id.btnBack);
        totalAmount = findViewById(R.id.totalAmount);
        monthView = findViewById(R.id.monthView);
        pieChart = findViewById(R.id.pieChart);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentMonthName = dateFormat.format(calendar.getTime()).toUpperCase(Locale.getDefault());
        int monthNumber = calendar.get(Calendar.MONTH) + 1;
        currentMonth = String.format(Locale.getDefault(), "%02d", monthNumber);

        // Set the month name to the TextView
        monthView.setText(currentMonthName);

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }

        // Default display
        btnExpense.setSelected(true);
        btnIncome.setSelected(false);
        expensePieChart("expense");
        displayTotal("expense");

        btnExpense.setOnClickListener(v -> {
            btnExpense.setSelected(true);
            btnIncome.setSelected(false);
            expensePieChart("expense");
            displayTotal("expense");
        });

        btnIncome.setOnClickListener(v -> {
            btnIncome.setSelected(true);
            btnExpense.setSelected(false);
            expensePieChart("income");
            displayTotal("income");
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }


    public void displayTotal(String ref) {
        DatabaseReference cashFlowRef;
        if (ref.equals("expense")) {
            cashFlowRef = database.child("expense");
        } else {
            cashFlowRef = database.child("income");
        }

        cashFlowRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double total = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    if (date != null && date.substring(5, 7).equals(currentMonth)) {
                        total += snapshot.child("amount").getValue(Double.class);
                    }
                }
                totalAmount.setText("RM " + String.format(Locale.getDefault(), "%.2f", total));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void expensePieChart(String ref) {
        DatabaseReference expenseRef;
        if (ref.equals("expense")) {
            expenseRef = database.child("expense");
        } else {
            expenseRef = database.child("income");
        }
        expenseRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Double> categoryAmount = new HashMap<>();
                    double totalAmount = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String date = snapshot.child("date").getValue(String.class);
                        String category = snapshot.child("category").getValue(String.class);
                        double amount = snapshot.child("amount").getValue(Double.class);

                        // Filter data by current month
                        if (date != null && date.substring(5, 7).equals(currentMonth)) {
                            totalAmount += amount;

                            if (categoryAmount.containsKey(category)) {
                                categoryAmount.put(category, categoryAmount.get(category) + amount);
                            } else {
                                categoryAmount.put(category, amount);
                            }
                        }
                    }

                    List<PieEntry> entries = new ArrayList<>();
                    for (Map.Entry<String, Double> entry : categoryAmount.entrySet()) {
                        float percentage = (float) (entry.getValue() / totalAmount * 100);
                        entries.add(new PieEntry(percentage, entry.getKey()));
                    }

                    PieDataSet dataSet = new PieDataSet(entries, "");
                    dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                    dataSet.setValueTextSize(16f);

                    // Set the value formatter to display percentages
                    dataSet.setValueFormatter(new PercentFormatter(pieChart));

                    PieData data = new PieData(dataSet);
                    pieChart.setData(data);

                    // Set the chart to use percentage values
                    pieChart.setUsePercentValues(true);

                    // Disable the description label and entry labels
                    pieChart.getDescription().setEnabled(false);
                    pieChart.setDrawEntryLabels(false);

                    // Customize the legend to be centered horizontally
                    Legend legend = pieChart.getLegend();
                    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                    legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                    legend.setDrawInside(false); // Ensure legend is outside the chart
                    legend.setTextSize(12f);

                    // Refresh the chart
                    pieChart.invalidate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", "Failed to read value.", databaseError.toException());
            }
        });
    }



}
