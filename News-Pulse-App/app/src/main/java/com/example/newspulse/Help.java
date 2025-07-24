package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class Help extends AppCompatActivity {

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
        setContentView(R.layout.activity_help);

        MaterialButton contactUs;

        contactUs = findViewById(R.id.contactUs);

        contactUs.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:jeevandeepsaini@gmail.com"));
                try {
                    startActivity(Intent.createChooser(intent, "Choose an email app"));
                } catch (Exception e) {
                    Toast.makeText(Help.this, "No email app found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(Help.this, Account.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}