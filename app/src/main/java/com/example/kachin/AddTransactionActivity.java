package com.example.kachin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> goToHomePage());

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
        tvTitle.setText(isExpense ? "Expense" : "Income");
        if (isExpense) {
            loadCategoriesFromFirebase();
        } else {
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
                    saveTransactionToDatabase(reference, transaction, isExpense);
                } else {
                    Toast.makeText(AddTransactionActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            saveTransactionToDatabase(reference, transaction, isExpense);
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


    private void saveTransactionToDatabase(DatabaseReference reference, Map<String, Object> transaction, boolean isExpense) {
        reference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long count = task.getResult().getChildrenCount();
                String key = (isExpense ? "expense" : "income") + (count + 1);
                reference.child(key).setValue(transaction)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(AddTransactionActivity.this, "Transaction saved", Toast.LENGTH_SHORT).show();
                            resetForm();
                        })
                        .addOnFailureListener(e -> Toast.makeText(AddTransactionActivity.this, "Failed to save transaction", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(AddTransactionActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
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
}
