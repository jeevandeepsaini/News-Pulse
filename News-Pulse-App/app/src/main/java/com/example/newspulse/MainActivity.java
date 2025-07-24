package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "GoogleSignIn";
    TextView createAccount;
    MaterialButton google, goToLogin;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore; // FireStore instance

    @SuppressLint({"MissingInflatedId", "SourceLockedOrientationActivity"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            Objects.requireNonNull(getWindow().getInsetsController()).setSystemBarsAppearance(
//                    0, // Remove the LIGHT_STATUS_BAR flag
//                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
//            );
//        }  //for white icons in status bar
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        goToLogin = findViewById(R.id.loginActivity);
        createAccount = findViewById(R.id.createAccount);
        google = findViewById(R.id.google);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, redirect to HomeActivity
            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }

        goToLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
        createAccount.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Categories.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        google.setOnClickListener(view -> signInWithGoogle());

        // Initialize One Tap Google Sign-In
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id)) // Must match OAuth2 Client ID
                        .setFilterByAuthorizedAccounts(false) // Show all Google accounts
                        .build())
                .build();
    }

    private void signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        startIntentSenderForResult(
                                result.getPendingIntent().getIntentSender(),
                                100,
                                null,
                                0,
                                0,
                                0
                        );
                    } catch (Exception e) {
                        Log.e(TAG, "Google Sign-In Failed: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "One Tap Sign-In failed: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {
                    authenticateWithFirebase(idToken);
                }
            } catch (ApiException e) {
                Log.e(TAG, "Sign-in failed: " + e.getMessage());
            }
        }
    }

    private void authenticateWithFirebase(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            saveUserDataToFireStore(user);
                        }
                        Intent intent = new Intent(MainActivity.this, Home.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                        Toast.makeText(MainActivity.this, "Welcome " + firebaseAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Firebase Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDataToFireStore(FirebaseUser user) {
        String uid = user.getUid();
        String name = user.getDisplayName();
        String email = user.getEmail();

        DocumentReference userRef = firebaseFirestore.collection("users").document(uid);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("name", name);
                userData.put("email", email);
                userData.put("categories", new ArrayList<>());
                userRef.set(userData)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "New user data saved"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error saving user data", e));
            } else {
                Log.d(TAG, "User already exists, no overwrite");
            }
        });
    }
}
