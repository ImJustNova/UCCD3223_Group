package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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
    private ImageButton btnBack, btnHome, btnAdd, btnHistory, btnReport, btnProfile;
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
        btnHome = findViewById(R.id.btnHome);
        btnAdd = findViewById(R.id.btnAdd);
        btnHistory = findViewById(R.id.btnHistory);
        btnReport = findViewById(R.id.btnReport);
        btnProfile = findViewById(R.id.btnProfile);
        totalAmount = findViewById(R.id.totalAmount);
        monthView = findViewById(R.id.monthView);
        pieChart = findViewById(R.id.pieChart);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        String currentMonthName = dateFormat.format(calendar.getTime()).toUpperCase(Locale.getDefault());
        int monthNumber = calendar.get(Calendar.MONTH) + 1;
        currentMonth = String.format(Locale.getDefault(), "%02d", monthNumber);

        monthView.setText(currentMonthName);

        database = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
        }

        setupButtonListeners();

        // Display expense as default
        btnExpense.setSelected(true);
        btnIncome.setSelected(false);
        expensePieChart("expense");
        displayTotal("expense");
        displayGroupedList("expense");

        btnExpense.setOnClickListener(v -> {
            btnExpense.setSelected(true);
            btnIncome.setSelected(false);
            expensePieChart("expense");
            displayTotal("expense");
            displayGroupedList("expense");
        });

        btnIncome.setOnClickListener(v -> {
            btnIncome.setSelected(true);
            btnExpense.setSelected(false);
            expensePieChart("income");
            displayTotal("income");
            displayGroupedList("income");
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupButtonListeners() {
        btnHome.setOnClickListener(v -> {
            Toast.makeText(report.this, "Home Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(report.this, HomePageActivity.class);
            startActivity(intent);
        });

        btnAdd.setOnClickListener(v -> {
            Toast.makeText(report.this, "Add Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(report.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        btnHistory.setOnClickListener(v -> {
            Toast.makeText(report.this, "History Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(report.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnReport.setOnClickListener(v -> {
            Toast.makeText(report.this, "Report Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(report.this, report.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Toast.makeText(report.this, "Profile Clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(report.this, ProfileActivity.class);
            startActivity(intent);
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

                    dataSet.setValueFormatter(new PercentFormatter(pieChart));

                    PieData data = new PieData(dataSet);
                    pieChart.setData(data);

                    pieChart.setUsePercentValues(true);

                    pieChart.getDescription().setEnabled(false);
                    pieChart.setDrawEntryLabels(false);

                    Legend legend = pieChart.getLegend();
                    legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                    legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                    legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                    legend.setDrawInside(false);
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

    public void displayGroupedList(String ref) {
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

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String date = snapshot.child("date").getValue(String.class);
                        String category = snapshot.child("category").getValue(String.class);
                        double amount = snapshot.child("amount").getValue(Double.class);

                        if (date != null && date.substring(5, 7).equals(currentMonth)) {
                            if (categoryAmount.containsKey(category)) {
                                categoryAmount.put(category, categoryAmount.get(category) + amount);
                            } else {
                                categoryAmount.put(category, amount);
                            }
                        }
                    }

                    List<Pair<String, Double>> groupedList = new ArrayList<>();
                    for (Map.Entry<String, Double> entry : categoryAmount.entrySet()) {
                        groupedList.add(new Pair<>(entry.getKey(), entry.getValue()));
                    }

                    updateListView(groupedList);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void updateListView(List<Pair<String, Double>> groupedList) {
        ListView listView = findViewById(R.id.listView);
        GroupedListAdapter adapter = new GroupedListAdapter(this, groupedList);
        listView.setAdapter(adapter);
    }

    public class GroupedListAdapter extends ArrayAdapter<Pair<String, Double>> {
        private final Context context;
        private final List<Pair<String, Double>> values;

        public GroupedListAdapter(Context context, List<Pair<String, Double>> values) {
            super(context, R.layout.grouped_list_item, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.grouped_list_item, parent, false);

            TextView categoryName = rowView.findViewById(R.id.categoryName);
            TextView totalAmount = rowView.findViewById(R.id.totalAmount);

            Pair<String, Double> item = values.get(position);
            categoryName.setText(item.first);
            totalAmount.setText("RM " + String.format(Locale.getDefault(), "%.2f", item.second));

            return rowView;
        }
    }

}
