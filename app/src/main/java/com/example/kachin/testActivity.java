package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class testActivity extends AppCompatActivity {   //IMPORTANT: This class is for testing purposes only

    private EditText nameEditText, ageEditText, hobbyEditText;
    private Button submitButton;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        nameEditText = findViewById(R.id.nameEdit);
        ageEditText = findViewById(R.id.ageEdit);
        hobbyEditText = findViewById(R.id.hobbyEdit);
        submitButton = findViewById(R.id.submitButton);

        database = FirebaseDatabase.getInstance().getReference();

        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            int age = Integer.parseInt(ageEditText.getText().toString());
            String hobby = hobbyEditText.getText().toString();
            getNextUserIdAndWriteNewUser(name, age, hobby);
        });
    }

    private void getNextUserIdAndWriteNewUser(String name, int age, String hobby) {
        database.child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long nextUserId = 1; // Default to 1 if there are no existing users
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String key = userSnapshot.getKey();
                    long userId = Long.parseLong(key.replace("user", ""));
                    nextUserId = Math.max(nextUserId, userId + 1);
                }

                String uid = String.valueOf(nextUserId);
                writeNewUser(uid, name, age, hobby);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(testActivity.this, "Failed to read user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeNewUser(String userId, String name, int age, String hobby) {
        User user = new User(userId, name, age, hobby);
        database.child("user").child("user" + userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(testActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(testActivity.this, "Failed to add user", Toast.LENGTH_SHORT).show();
                });
    }

    public static class User {
        public String uid;
        public String name;
        public int age;
        public String hobby;

        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        public User() {}

        public User(String uid, String name, int age, String hobby) {
            this.uid = uid;
            this.name = name;
            this.age = age;
            this.hobby = hobby;
        }
    }
}
