package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newspulse.adapter.BookmarksAdapter;
import com.example.newspulse.model.News;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Bookmarks extends AppCompatActivity {

    private final FirebaseFirestore firebaseFireStore = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final List<News> bookmarkedNewsList = new ArrayList<>();
    private BookmarksAdapter bookmarksAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        setContentView(R.layout.activity_bookmarks);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookmarksAdapter = new BookmarksAdapter(this, bookmarkedNewsList);
        recyclerView.setAdapter(bookmarksAdapter);

        loadBookmarkedNews();
        swipeRefreshLayout.setOnRefreshListener(this::loadBookmarkedNews);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadBookmarkedNews() {
        swipeRefreshLayout.setColorSchemeResources(
                R.color.newsPulse
        );
        swipeRefreshLayout.setRefreshing(true); // Show refresh indicator

        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        firebaseFireStore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> bookmarkedIds = (List<String>) documentSnapshot.get("bookmarks");
                        if (bookmarkedIds == null || bookmarkedIds.isEmpty()) {
                            bookmarkedNewsList.clear();
                            bookmarksAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            return;
                        }

                        firebaseFireStore.collection("news")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        bookmarkedNewsList.clear();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String newsId = document.getId(); // Get document ID
                                            if (bookmarkedIds.contains(newsId)) { // Check if it's bookmarked
                                                News news = document.toObject(News.class);
                                                bookmarkedNewsList.add(news);
                                            }
                                        }
                                        bookmarksAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.e("FireStoreError", "Error fetching news", task.getException());
                                    }
                                    swipeRefreshLayout.setRefreshing(false);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FireStoreError", "Error fetching bookmarks", e);
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Calls the default back button behavior
        Intent intent = new Intent(Bookmarks.this, Account.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}