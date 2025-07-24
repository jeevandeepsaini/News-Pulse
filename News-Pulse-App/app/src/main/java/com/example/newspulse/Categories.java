package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Categories extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
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
        setContentView(R.layout.activity_categories);

        MaterialButton next = findViewById(R.id.nextActivity);
        TextView textView = findViewById(R.id.textSample);
        ChipGroup chipGroup = findViewById(R.id.categoryChipGroup);

        next.setOnClickListener(view -> {
            ArrayList<String> selectedCategories = new ArrayList<>();

            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    selectedCategories.add(chip.getText().toString());
                }
            }

            Intent intent = new Intent(getApplicationContext(), SignUp.class);
            intent.putStringArrayListExtra("selectedCategories", selectedCategories);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Automotive");
        arrayList.add("Business");
        arrayList.add("Education");
        arrayList.add("Entertainment");
        arrayList.add("Health");
        arrayList.add("Politics");
        arrayList.add("Science");
        arrayList.add("Sports");
        arrayList.add("Technology");
        arrayList.add("Travel");

        for (int i = 0; i < arrayList.size(); i++) {
            Chip chip = (Chip) LayoutInflater.from(Categories.this).inflate(R.layout.chip_layout, null);
            chip.setText(arrayList.get(i));
            chip.setId(View.generateViewId()); // Use a unique ID for each chip
            chipGroup.addView(chip);

            chip.setTextColor(Color.WHITE); // Text color
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#a38ecb"))); // Background color
        }

        final int selectedTextColor = Color.WHITE;
        final int selectedBackgroundColor = Color.parseColor("#543986");
        final int unselectedTextColor = Color.WHITE;
        final int unselectedBackgroundColor = Color.parseColor("#a38ecb");

        chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
//                if (checkedIds.isEmpty()) {
//                    textView.setText("No categories selected"); // Updated message
//                } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i : checkedIds) {
                    Chip chip = findViewById(i);
                    stringBuilder.append(", ").append(chip.getText());

                    chip.setTextColor(selectedTextColor);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(selectedBackgroundColor));
                }
//                    textView.setText("Selected categories: " + stringBuilder.toString().replaceFirst(",", ""));
//                }

                // Reset the colors for unselected chips
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroup.getChildAt(i);
                    if (!checkedIds.contains(chip.getId())) {
                        chip.setTextColor(unselectedTextColor);
                        chip.setChipBackgroundColor(ColorStateList.valueOf(unselectedBackgroundColor));
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(Categories.this, Login.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}