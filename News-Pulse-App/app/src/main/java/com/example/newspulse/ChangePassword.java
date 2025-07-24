package com.example.newspulse;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePassword extends AppCompatActivity {

    private TextInputLayout oldPass, newPass, cnfPass;
    private MaterialButton update;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

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
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        oldPass = findViewById(R.id.oldPasswordLayout);
        newPass = findViewById(R.id.newPasswordLayout);
        cnfPass = findViewById(R.id.confirmPasswordLayout);
        update = findViewById(R.id.updatePass);

        update.setOnClickListener(view -> {
            hideKeyboard();
            if (validateInputs()) {
                updatePassword();
            }
        });
    }

    private boolean validateInputs() {
        String oldPassword = oldPass.getEditText().getText().toString().trim();
        String newPassword = newPass.getEditText().getText().toString().trim();
        String confirmPassword = cnfPass.getEditText().getText().toString().trim();

        boolean isValid = true;

        if (oldPassword.isEmpty()) {
            oldPass.setError("Enter your current password");
            isValid = false;
        } else {
            oldPass.setError(null);
        }

        if (newPassword.isEmpty()) {
            newPass.setError("Enter a new password");
            isValid = false;
        } else if (newPassword.length() < 6) {
            newPass.setError("Password must be at least 6 characters long");
            isValid = false;
        } else {
            newPass.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            cnfPass.setError("Confirm your new password");
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            cnfPass.setError("Passwords do not match");
            isValid = false;
        } else {
            cnfPass.setError(null);
        }

        return isValid;
    }

    private void updatePassword() {
        if (user != null) {
            String newPassword = newPass.getEditText().getText().toString().trim();

            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ChangePassword.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(ChangePassword.this, "Failed to update password. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(ChangePassword.this, Account.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}