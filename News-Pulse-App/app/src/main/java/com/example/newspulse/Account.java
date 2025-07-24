package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Account extends AppCompatActivity {

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

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
        setContentView(R.layout.activity_account);


        auth = FirebaseAuth.getInstance();
        // Initialize Google Sign-In Client
        googleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());

        RelativeLayout profile, bookmarks, changePass, helpSupport, about;
        MaterialButton logout, deleteAccount;

        profile = findViewById(R.id.layout1);
        bookmarks = findViewById(R.id.layout2);
        changePass = findViewById(R.id.layout3);
        helpSupport = findViewById(R.id.layout4);
        about = findViewById(R.id.layout5);
        logout = findViewById(R.id.logout);
        deleteAccount = findViewById(R.id.deleteAccount);

        profile.setOnClickListener(view -> {
            Intent intent = new Intent(Account.this, Profile.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        bookmarks.setOnClickListener(view -> {
            Intent intent = new Intent(Account.this, Bookmarks.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        changePass.setOnClickListener(view -> {
            Intent intent = new Intent(Account.this, ChangePassword.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        helpSupport.setOnClickListener(view -> {
            Intent intent = new Intent(Account.this, Help.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        about.setOnClickListener(view -> {
            Intent intent = new Intent(Account.this, About.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        logout.setOnClickListener(view -> showLogoutDialog());

        deleteAccount.setOnClickListener(view -> showDeleteDialog());
    }

    private void showLogoutDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.logout_layout, null);

        MaterialButton cancelBtn = view.findViewById(R.id.cancel);
        MaterialButton logoutBtn = view.findViewById(R.id.logout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        logoutBtn.setOnClickListener(v -> {
            dialog.dismiss();
            logoutUser();
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void logoutUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // Check if user signed in with Google
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    auth.signOut(); // Also sign out from Firebase
                    navigateToLogin();
                });
            } else {
                // Firebase Email/Password Sign-Out
                auth.signOut();
                navigateToLogin();
            }
        }
    }

    private void showDeleteDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.delete_layout, null);

        MaterialButton cancelBtn = view.findViewById(R.id.cancel);
        MaterialButton deleteBtn = view.findViewById(R.id.delete);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        deleteBtn.setOnClickListener(v -> {
            dialog.dismiss();
            confirmDeleteDialog();
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void confirmDeleteDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.confirm_delete_layout, null);

        MaterialButton cancelBtn = view.findViewById(R.id.cancel);
        MaterialButton deleteBtn = view.findViewById(R.id.confirmDelete);
        TextInputLayout userEmail = view.findViewById(R.id.emailLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        deleteBtn.setOnClickListener(v -> {
            String email = userEmail.getEditText().getText().toString().trim();
            String currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                userEmail.setError("Invalid Email");
            } else if (!email.equals(currentEmail)) {
                userEmail.setError("Email doesn't match your account");
            } else {
                userEmail.setError(null); // clear any previous errors
                dialog.dismiss();
                deleteUser(); // only called when everything is valid
            }
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void deleteUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user != null) {
            String uid = user.getUid();
            db.collection("users").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> user.delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Account.this, Login.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                    finish();
                                } else {
                                    Toast.makeText(this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private void navigateToLogin() {
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Account.this, Login.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(Account.this, Home.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}