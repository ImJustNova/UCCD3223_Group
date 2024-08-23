package com.example.kachin;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private TextView tvAmount, textViewSelectedDate, tvTitle;
    private EditText editTextAmount, editTextDescription;
    private Spinner spinnerCategory;
    private Button buttonSelectDate, buttonContinue, btnExpense, btnIncome, buttonAddAttachment;
    private String selectedDate;
    private boolean isExpense = true;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        databaseReference = FirebaseDatabase.getInstance().getReference("transactions");

        initializeViews();
        initializeDefaultSettings();

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> goToHomePage());

        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvAmount.setText("$" + s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnExpense.setOnClickListener(v -> updateUIForTransactionType(true));
        btnIncome.setOnClickListener(v -> updateUIForTransactionType(false));
        buttonSelectDate.setOnClickListener(v -> openDatePicker());
        buttonContinue.setOnClickListener(v -> saveTransaction());
        buttonAddAttachment.setOnClickListener(v -> openFilePicker());
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvAmount = findViewById(R.id.tvAmount);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        buttonSelectDate = findViewById(R.id.buttonSelectDate);
        buttonContinue = findViewById(R.id.buttonContinue);
        btnExpense = findViewById(R.id.btnExpense);
        btnIncome = findViewById(R.id.btnIncome);
        buttonAddAttachment = findViewById(R.id.buttonAddAttachment);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
    }

    private void initializeDefaultSettings() {
        updateUIForTransactionType(true);
    }

    private void goToHomePage() {
        //Intent intent = new Intent(AddTransactionActivity.this, HomePageActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(intent);
        //finish();
    }

    private void updateUIForTransactionType(boolean isExpense) {
        this.isExpense = isExpense;
        tvTitle.setText(isExpense ? "Expense" : "Income");
        loadCategories(isExpense ? getExpenseCategories() : getIncomeCategories());
    }

    private void loadCategories(String[] categories) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private String[] getExpenseCategories() {
        return new String[]{"Food and Drinks", "Transport", "Groceries", "Vehicle",
                "Entertainment", "Shopping", "Rent", "Travel"};
    }

    private String[] getIncomeCategories() {
        return new String[]{"Salary", "Savings", "Others"};
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddTransactionActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedMonth += 1;
                    selectedDate = selectedDay + "/" + selectedMonth + "/" + selectedYear;
                    textViewSelectedDate.setText("Selected Date: " + selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            Toast.makeText(this, "File selected: " + fileUri.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveTransaction() {
        String amount = editTextAmount.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String description = editTextDescription.getText().toString();

        if (amount.isEmpty() || category.isEmpty() || selectedDate == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("amount", amount);
        transaction.put("category", category);
        transaction.put("description", description);
        transaction.put("date", selectedDate);
        transaction.put("type", isExpense ? "Expense" : "Income");

        databaseReference.push().setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddTransactionActivity.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e -> Toast.makeText(AddTransactionActivity.this, "Failed to save transaction", Toast.LENGTH_SHORT).show());
    }

    private void resetForm() {
        editTextAmount.setText("");
        tvAmount.setText("$0");
        editTextDescription.setText("");
        spinnerCategory.setSelection(0);
        textViewSelectedDate.setText("Select Date");
        selectedDate = null;
    }
}

//the detail display in homepage
//the icon button go back to home page