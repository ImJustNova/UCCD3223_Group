package com.example.kachin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private TextView tvUsername;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        tvUsername = findViewById(R.id.username);
        profileImage = findViewById(R.id.profile_image);

        loadProfileData();

        Button btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        Button btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        ImageView btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadProfileData() {
        String name = sharedPreferences.getString("name", "Default Name");
        String profileImageUri = sharedPreferences.getString("profile_image_uri", "");

        tvUsername.setText(name);

        if (!profileImageUri.isEmpty()) {
            Uri imageUri = Uri.parse(profileImageUri);
            profileImage.setImageURI(imageUri);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }
}
