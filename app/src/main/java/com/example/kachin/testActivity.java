package com.example.kachin;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.os.Bundle;

public class testActivity extends AppCompatActivity {   //IMPORTANT: This class is for testing purposes only

    private DatabaseReference database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        database = FirebaseDatabase.getInstance().getReference();

//        String uid = "uid1";
//        User user = new User(uid, "John", 21, "reading");

        //writeNewUser("123test", "John", 21, "reading");
    }

//    private void writeNewUser(String userId, String name, int age, String hobby) {
//        User user = new User(userId, name, age, hobby);
//        database.child("user").child(userId).setValue(user)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(testActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(testActivity.this, "Failed to add user", Toast.LENGTH_SHORT).show();
//                });
//    }

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