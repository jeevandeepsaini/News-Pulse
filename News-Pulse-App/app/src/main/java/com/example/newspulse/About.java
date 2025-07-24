package com.example.newspulse;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class About extends AppCompatActivity {

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
        setContentView(R.layout.activity_about);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(About.this, Account.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}