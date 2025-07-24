package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPassword extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextInputLayout textInputLayout;
    private MaterialButton send;
    private FirebaseAuth firebaseAuth;

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
        setContentView(R.layout.activity_forgot_password);

        TextView goToLogin;
        goToLogin = findViewById(R.id.text2);
        progressBar = findViewById(R.id.progress);
        textInputLayout = findViewById(R.id.emailLayout);
        send = findViewById(R.id.send);
        firebaseAuth = FirebaseAuth.getInstance();

        send.setOnClickListener(view -> {
            hideKeyBoard();
            resetPassword();
        });

        goToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPassword.this, Login.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

    }

    private void resetPassword() {
        if (!checkEmail()) {
            return;
        }

        String email = Objects.requireNonNull(textInputLayout.getEditText()).getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);
        send.setEnabled(false);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    send.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassword.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPassword.this, "Failed to send reset link", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(textInputLayout.getEditText()).getText().clear();
        textInputLayout.clearFocus();
    }

    private boolean checkEmail() {
        String email = Objects.requireNonNull(textInputLayout.getEditText()).getText().toString().trim();
        if (email.isEmpty()) {
            textInputLayout.setError("Invalid Email");
            return false;
        } else {
            textInputLayout.setError(null);
            return true;
        }
    }

    private void hideKeyBoard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(ForgotPassword.this, Login.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

}