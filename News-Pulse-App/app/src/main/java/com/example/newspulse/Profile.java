package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Profile extends AppCompatActivity {

    private final List<String> allCategories = new ArrayList<>(); // All categories
    private TextInputLayout textInputName, textInputEmail;
    private ChipGroup categoriesChipGroup;
    private MaterialButton edit;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;
    private List<String> userSelectedCategories = new ArrayList<>(); // User-selected categories
    private boolean isEditing = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Objects.requireNonNull(getWindow().getInsetsController()).setSystemBarsAppearance(
                    0, // Remove the LIGHT_STATUS_BAR flag
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            );
        }  //for white icons in status bar
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textInputName = findViewById(R.id.nameLayout);
        textInputEmail = findViewById(R.id.emailLayout);
        categoriesChipGroup = findViewById(R.id.categoryChipGroup);
        edit = findViewById(R.id.edit);

        loadAllCategories();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            fetchUserData();
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
        }

        edit.setOnClickListener(view -> {
            if (!isEditing) {
                // Enable editing (show all categories)
                populateChipGroup(allCategories, true);
                textInputName.setEnabled(true);
                textInputEmail.setEnabled(false);
                edit.setText("Update");
            } else {
                // Save updated categories
                saveUpdatedData();
                textInputName.setEnabled(false);
                textInputEmail.setEnabled(false);
                edit.setText("Edit Profile");
            }
            isEditing = !isEditing;
        });
    }

    private void fetchUserData() {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                userSelectedCategories = (List<String>) documentSnapshot.get("categories");

                // Set values to UI
                textInputName.getEditText().setText(name);
                textInputEmail.getEditText().setText(email);

                if (userSelectedCategories != null) {
                    populateChipGroup(userSelectedCategories, false);
                } else {
                    Toast.makeText(Profile.this, "No categories selected", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Profile.this, "User data not found!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("FireStoreError", "Error fetching user data", e);
            Toast.makeText(Profile.this, "Error fetching data", Toast.LENGTH_SHORT).show();
        });
    }

    private void populateChipGroup(List<String> categories, boolean allowEditing) {
        categoriesChipGroup.removeAllViews(); // Clear previous chips

        for (String category : categories) {
            Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.chip_layout, null);
            chip.setText(category);
            chip.setId(View.generateViewId()); // Unique ID

            // Set selection state
            boolean isSelected = userSelectedCategories.contains(category);
            chip.setChecked(isSelected);
            styleChip(chip, isSelected);

            if (allowEditing) {
                chip.setCheckable(true);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> styleChip(chip, isChecked));
            } else {
                chip.setCheckable(false);
            }

            categoriesChipGroup.addView(chip);
        }
    }

    private void saveUpdatedData() {
        ArrayList<String> updatedCategories = new ArrayList<>();

        for (int i = 0; i < categoriesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) categoriesChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                updatedCategories.add(chip.getText().toString());
            }
        }

        String updatedName = textInputName.getEditText().getText().toString();

        if (updatedName.isEmpty()) {
            textInputName.setError("Invalid Name");
        } else {
            textInputName.setError(null);
        }

        DocumentReference userRef = db.collection("users").document(userId);
        userRef.update("name", updatedName, "categories", updatedCategories)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Profile.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    userSelectedCategories = new ArrayList<>(updatedCategories);
                    populateChipGroup(updatedCategories, false); // Lock categories again
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Profile.this, "Update failed", Toast.LENGTH_SHORT).show();
                    Log.e("FireStoreError", "Error updating data", e);
                });
    }

    private void styleChip(Chip chip, boolean isSelected) {
        final int selectedTextColor = Color.WHITE;
        final int selectedBackgroundColor = Color.parseColor("#543986");
        final int unselectedTextColor = Color.WHITE;
        final int unselectedBackgroundColor = Color.parseColor("#a38ecb");

        if (isSelected) {
            chip.setTextColor(selectedTextColor);
            chip.setChipBackgroundColor(ColorStateList.valueOf(selectedBackgroundColor));
        } else {
            chip.setTextColor(unselectedTextColor);
            chip.setChipBackgroundColor(ColorStateList.valueOf(unselectedBackgroundColor));
        }
    }

    private void loadAllCategories() {
        allCategories.add("Automotive");
        allCategories.add("Business");
        allCategories.add("Education");
        allCategories.add("Entertainment");
        allCategories.add("Health");
        allCategories.add("Politics");
        allCategories.add("Science");
        allCategories.add("Sports");
        allCategories.add("Technology");
        allCategories.add("Travel");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(Profile.this, Account.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}