package com.example.kachin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTransactionActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private TextView tvAmount, textViewSelectedDate, tvTitle;
    private EditText editTextAmount, editTextDescription;
    private Spinner spinnerCategory;
    private Button buttonSelectDate, buttonContinue, btnExpense, btnIncome, buttonAddAttachment;
    private String selectedDate, uid;
    private boolean isExpense = true;
    private Uri fileUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            Toast.makeText(this, "No user is currently signed in", Toast.LENGTH_SHORT).show();
            finish();
        }

        initializeViews();
        initializeDefaultSettings();


        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvAmount.setText("RM" + s.toString());
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
        Intent intent = new Intent(AddTransactionActivity.this, HomePageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void updateUIForTransactionType(boolean isExpense) {
        this.isExpense = isExpense;

        // Update the title text
        tvTitle.setText(isExpense ? "Expense" : "Income");

        // Get reference to the root layout
        ConstraintLayout rootLayout = findViewById(R.id.rootLayout);

        // Change the background color of the root layout
        if (isExpense) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));  // Set background to red
            loadCategoriesFromFirebase();
        } else {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));  // Set background to green
            loadIncomeCategoriesFromFirebase();
        }
    }


    private void loadCategoriesFromFirebase() {
        databaseReference.child("category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> categories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String category = categorySnapshot.getValue(String.class);
                    categories.add(category);
                }
                loadCategories(categories.toArray(new String[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTransactionActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadIncomeCategoriesFromFirebase() {
        databaseReference.child("goal").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> incomeCategories = new ArrayList<>();
                incomeCategories.add("Salary");
                incomeCategories.add("Savings");

                for (DataSnapshot goalSnapshot : snapshot.getChildren()) {
                    String goalName = goalSnapshot.child("goalName").getValue(String.class);
                    if (goalName != null && !goalName.isEmpty()) {
                        incomeCategories.add(goalName);
                    }
                }
                loadCategories(incomeCategories.toArray(new String[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTransactionActivity.this, "Failed to load income categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories(String[] categories) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
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
                    selectedDate = selectedYear + "-" + (selectedMonth < 10 ? "0" + selectedMonth : selectedMonth)
                            + "-" + (selectedDay < 10 ? "0" + selectedDay : selectedDay);
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
            fileUri = data.getData();
            Toast.makeText(this, "File selected: " + fileUri.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveTransaction() {
        String amountText = editTextAmount.getText().toString();
        String category = spinnerCategory.getSelectedItem().toString();
        String description = editTextDescription.getText().toString();

        if (amountText.isEmpty() || category.isEmpty() || selectedDate == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> transaction = new HashMap<>();
        transaction.put("uid", uid);
        transaction.put("amount", amount);
        transaction.put("category", category);
        transaction.put("description", description);
        transaction.put("date", selectedDate);

        DatabaseReference reference = isExpense ? databaseReference.child("expense") : databaseReference.child("income");

        if (fileUri != null) {
            String fileName = getFileName(fileUri); // Retrieve the original file name
            StorageReference fileReference = storageReference.child(fileName); // Use original file name

            UploadTask uploadTask = fileReference.putFile(fileUri);

            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    transaction.put("imageUrl", downloadUri.toString());
                    saveTransactionToDatabase(reference, transaction, isExpense, amount, category);
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveTransactionToDatabase(reference, transaction, isExpense, amount, category);
        }
    }

    private void saveTransactionToDatabase(DatabaseReference reference, Map<String, Object> transaction, boolean isExpense, double amount, String category) {
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long count = task.getResult().getChildrenCount();
                String key = (isExpense ? "expense" : "income") + (count + 1);
                reference.child(key).setValue(transaction)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AddTransactionActivity.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                            // Update the budget or goal based on the type of transaction
                            updateBudgetOrGoal(category, amount, isExpense);
                            resetForm();
                            showConfirmationDialog();
                        })
                        .addOnFailureListener(e -> Toast.makeText(AddTransactionActivity.this, "Failed to save transaction", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(AddTransactionActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateBudgetOrGoal(String category, double amount, boolean isExpense) {
        if (isExpense) {
            databaseReference.child("budget").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot budgetSnapshot : snapshot.getChildren()) {
                        String categoryName = budgetSnapshot.child("category").getValue(String.class);

                        if (categoryName != null && categoryName.equals(category)) {
                            Double currentBudget = budgetSnapshot.child("currentBudget").getValue(Double.class);
                            Double budgetLimit = budgetSnapshot.child("budgetLimit").getValue(Double.class);

                            if (currentBudget == null || budgetLimit == null) {
                                Toast.makeText(AddTransactionActivity.this, "Error: Missing goal data for category " + category, Toast.LENGTH_SHORT).show();
                                continue;
                            }

                            currentBudget += amount;
                            double budgetProgress = (currentBudget / budgetLimit) * 100;
                            budgetSnapshot.getRef().child("currentBudget").setValue(currentBudget);
                            budgetSnapshot.getRef().child("progress").setValue(budgetProgress);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddTransactionActivity.this, "Failed to update budget limit", Toast.LENGTH_SHORT).show();
                }
            });
        } else { // income
            databaseReference.child("goal").orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // If no data matches the query
                        Toast.makeText(AddTransactionActivity.this, "No matching goal found for this category", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (DataSnapshot goalSnapshot : snapshot.getChildren()) {
                        String categoryName = goalSnapshot.child("goalName").getValue(String.class);

                        if (categoryName != null && categoryName.equals(category)) {
                            Double currentAmount = goalSnapshot.child("currentAmount").getValue(Double.class);
                            Double targetAmount = goalSnapshot.child("targetAmount").getValue(Double.class);

                            if (currentAmount == null || targetAmount == null) {
                                Toast.makeText(AddTransactionActivity.this, "Error: Missing goal data for category " + category, Toast.LENGTH_SHORT).show();
                                continue;
                            }

                            currentAmount += amount;
                            double goalProgress = (currentAmount / targetAmount) * 100;
                            goalSnapshot.getRef().child("currentAmount").setValue(currentAmount);
                            goalSnapshot.getRef().child("progress").setValue(goalProgress);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddTransactionActivity.this, "Failed to update goal progress", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void resetForm() {
        editTextAmount.setText("");
        tvAmount.setText("RM0");
        editTextDescription.setText("");
        spinnerCategory.setSelection(0);
        textViewSelectedDate.setText("Select Date");
        selectedDate = null;
        fileUri = null;
    }
    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Transaction Saved")
                .setMessage("Do you want to add another transaction?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    resetForm();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    goToHomePage();
                })
                .setCancelable(false)
                .show();
    }
}
