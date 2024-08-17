package com.example.kachin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private SharedPreferences sharedPreferences;
    private EditText editName, editEmail, editPassword;
    private ImageView profileImage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        profileImage = findViewById(R.id.edit_profile_image);
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnChangePicture = findViewById(R.id.btn_change_picture);

        loadProfileData();

        btnChangePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
                finish();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profile_image_uri", imageUri.toString());
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadProfileData() {
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String profileImageUri = sharedPreferences.getString("profile_image_uri", "");

        editName.setText(name);
        editEmail.setText(email);

        if (!profileImageUri.isEmpty()) {
            imageUri = Uri.parse(profileImageUri);
            profileImage.setImageURI(imageUri);
        }
    }
    private void saveProfileData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", editName.getText().toString());
        editor.putString("email", editEmail.getText().toString());
        editor.apply();
    }
}


