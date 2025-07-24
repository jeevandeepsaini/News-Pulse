package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUp extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextInputLayout userName, userEmail, userPassword, userCnfPassword;
    private MaterialButton createAccount;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayList<String> selectedCategories;

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
        setContentView(R.layout.activity_sign_up);

        TextView goToLogin;
        progressBar = findViewById(R.id.progress);
        userName = findViewById(R.id.nameLayout);
        userEmail = findViewById(R.id.emailLayout);
        userPassword = findViewById(R.id.passwordLayout);
        userCnfPassword = findViewById(R.id.confirmPasswordLayout);
        createAccount = findViewById(R.id.createAccount);
        goToLogin = findViewById(R.id.loginActivity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        selectedCategories = getIntent().getStringArrayListExtra("selectedCategories");

        createAccount.setOnClickListener(view -> {
            hideKeyBoard();
            registerUser();
        });

        goToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(SignUp.this, Login.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

    } //endOfOnCreate

    private void registerUser() {
        String name = userName.getEditText() != null ? userName.getEditText().getText().toString().trim() : "";
        String email = userEmail.getEditText() != null ? userEmail.getEditText().getText().toString().trim() : "";
        String password = userPassword.getEditText() != null ? userPassword.getEditText().getText().toString().trim() : "";
        String confirmPassword = userCnfPassword.getEditText() != null ? userCnfPassword.getEditText().getText().toString().trim() : "";

        if (!validateInputs(name, email, password, confirmPassword)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        createAccount.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        // Store user details in FireStore
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("email", email);
                        userMap.put("categories", selectedCategories);

                        db.collection("users").document(userId).set(userMap)
                                .addOnCompleteListener(task1 -> {
                                    progressBar.setVisibility(View.GONE);
                                    createAccount.setEnabled(true);

                                    if (task1.isSuccessful()) {
                                        Toast.makeText(SignUp.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUp.this, Login.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                        finish();
                                    } else {
                                        Toast.makeText(SignUp.this, "Error storing user data", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        progressBar.setVisibility(View.GONE);
                        createAccount.setEnabled(true);
                        Toast.makeText(SignUp.this, "Registration Failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (name.isEmpty()) {
            userName.setError("Invalid Name");
            isValid = false;
        } else {
            userName.setError(null);
        }

        if (email.isEmpty() | !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            userEmail.setError("Invalid Email");
            isValid = false;
        } else {
            userEmail.setError(null);
        }

        if (password.isEmpty()) {
            userPassword.setError("Invalid Password");
            isValid = false;
        } else if (password.length() < 6) {
            userPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            userPassword.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            userCnfPassword.setError("Invalid Confirm Password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            userCnfPassword.setError("Passwords do not match");
            isValid = false;
        } else if (password.length() < 6) {
            userCnfPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            userCnfPassword.setError(null);
        }

        if (selectedCategories == null || selectedCategories.isEmpty()) {
            Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void hideKeyBoard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(userName.getEditText()).getText().clear();
        Objects.requireNonNull(userEmail.getEditText()).getText().clear();
        Objects.requireNonNull(userPassword.getEditText()).getText().clear();
        Objects.requireNonNull(userCnfPassword.getEditText()).getText().clear();
        userName.clearFocus();
        userEmail.clearFocus();
        userPassword.clearFocus();
        userCnfPassword.clearFocus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(SignUp.this, Categories.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}