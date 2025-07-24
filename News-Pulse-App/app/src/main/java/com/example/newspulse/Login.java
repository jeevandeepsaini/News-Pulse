package com.example.newspulse;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Login extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextInputLayout textInputEmail, textInputPassword;
    private MaterialButton login;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        TextView goToSignUp, goToForgotPassword;
        progressBar = findViewById(R.id.progress);
        textInputEmail = findViewById(R.id.emailLayout);
        textInputPassword = findViewById(R.id.passwordLayout);
        login = findViewById(R.id.loginButton);
        goToSignUp = findViewById(R.id.signUpActivity);
        goToForgotPassword = findViewById(R.id.forgotPassword);
        firebaseAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(view -> {
            hideKeyBoard();
            loginUser();
        });

        goToSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, Categories.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        goToForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, ForgotPassword.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

    } //endOfOnCreate

    private void loginUser() {
        if (!checkEmail() | !checkPassword()) {
            return;
        }

        String email = Objects.requireNonNull(textInputEmail.getEditText()).getText().toString().trim();
        String password = Objects.requireNonNull(textInputPassword.getEditText()).getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);
        login.setEnabled(false);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    login.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, Home.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Please verify your email before logging in", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Login.this, "Login Failed. Check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Objects.requireNonNull(textInputEmail.getEditText()).getText().clear();
        Objects.requireNonNull(textInputPassword.getEditText()).getText().clear();
        textInputEmail.clearFocus();
        textInputPassword.clearFocus();
    }

    private boolean checkEmail() {
        String email = Objects.requireNonNull(textInputEmail.getEditText()).getText().toString().trim();
        if (email.isEmpty() | !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEmail.setError("Invalid Email");
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean checkPassword() {
        String password = Objects.requireNonNull(textInputPassword.getEditText()).getText().toString().trim();
        if (password.isEmpty() | password.length() < 6) {
            textInputPassword.setError("Invalid Password");
            return false;
        } else {
            textInputPassword.setError(null);
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
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in, go to Home
            Intent intent = new Intent(Login.this, Home.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish(); // Prevent going back to login screen
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

}