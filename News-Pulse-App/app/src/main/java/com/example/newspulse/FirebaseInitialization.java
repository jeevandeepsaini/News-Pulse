package com.example.newspulse;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class FirebaseInitialization extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase once globally
        FirebaseApp.initializeApp(this);
    }
}