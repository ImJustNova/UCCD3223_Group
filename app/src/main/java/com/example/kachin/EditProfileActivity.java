package com.example.kachin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView editProfileImage;
    private EditText editName, editEmail, editPassword;
    private Button btnSave, btnChangePicture;

    private DatabaseReference userRef;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editProfileImage = findViewById(R.id.edit_profile_image);
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnSave = findViewById(R.id.btn_save);
        btnChangePicture = findViewById(R.id.btn_change_picture);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            userRef = database.getReference("users").child(userId);

            storageRef = FirebaseStorage.getInstance().getReference("profile_pictures").child(userId);
        } else {
            Toast.makeText(EditProfileActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnChangePicture.setOnClickListener(v -> selectProfilePicture());

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void selectProfilePicture() {
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
            editProfileImage.setImageURI(imageUri);

            Glide.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .into(editProfileImage);
        }
    }

    private void saveProfile() {
        String newName = editName.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();

        if (!TextUtils.isEmpty(newName)) {
            updates.put("name", newName);
        }
        if (!TextUtils.isEmpty(newEmail)) {
            updates.put("email", newEmail);
        }
        if (!TextUtils.isEmpty(newPassword)) {
            updates.put("password", newPassword);
        }

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    updates.put("profilePictureUrl", uri.toString());
                                    updateFirebaseProfile(updates);
                                });
                            } else {
                                Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            updateFirebaseProfile(updates);
        }
    }

    private void updateFirebaseProfile(Map<String, Object> updates) {
        if (!updates.isEmpty()) {
            userRef.updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(EditProfileActivity.this, "No changes to update", Toast.LENGTH_SHORT).show();
        }
    }
}
