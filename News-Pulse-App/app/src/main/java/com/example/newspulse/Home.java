package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newspulse.adapter.NewsAdapter;
import com.example.newspulse.model.News;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Home extends AppCompatActivity {

    FirebaseFirestore firebaseFireStore = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    List<News> newsList = new ArrayList<>();
    List<News> originalList = new ArrayList<>();
    //News Filter Layout
    HorizontalScrollView newsFilterBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter newsAdapter;
    private Chip chipPositive, chipNegative, chipNeutral, chipLatest, chipOldest;
    private ChipGroup chipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        SearchView searchView;
        ImageButton myAccount;

        chipGroup = findViewById(R.id.chipGroup);
        int selectedBg = ContextCompat.getColor(this, R.color.newsPulse);
        int unselectedBg = ContextCompat.getColor(this, R.color.lightPurple);
        int selectedText = ContextCompat.getColor(this, R.color.white);
        int unselectedText = ContextCompat.getColor(this, R.color.white);

        chipPositive = findViewById(R.id.chipPositive);
        chipNegative = findViewById(R.id.chipNegative);
        chipNeutral = findViewById(R.id.chipNeutral);
        chipLatest = findViewById(R.id.chipLatest);
        chipOldest = findViewById(R.id.chipOldest);

        myAccount = findViewById(R.id.account);
        searchView = findViewById(R.id.searchNews);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        newsFilterBar = findViewById(R.id.scrollView);
        recyclerView = findViewById(R.id.recyclerViewNews);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, newsList);
        recyclerView.setAdapter(newsAdapter);
        newsFilterBar.setTranslationY(0);

        loadNewsFromFireStore();

        myAccount.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Account.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNews(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // Reset RecyclerView to show all news again
                    newsAdapter.updateList(originalList);
                } else {
                    filterNews(newText);
                }
                return true;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            chipGroup.clearCheck(); // Clears selected chip
            loadNewsFromFireStore();
        });

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> filterByChip());

        // Apply to all chips
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(selectedBg));
                    chip.setTextColor(selectedText);
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(unselectedBg));
                    chip.setTextColor(unselectedText);
                }
            });

            // Set initial unselected state
            chip.setChipBackgroundColor(ColorStateList.valueOf(unselectedBg));
            chip.setTextColor(unselectedText);
        }
    }

    private void filterNews(String query) {
        List<News> filteredList = new ArrayList<>();
        for (News item : originalList) {
            if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        newsAdapter.updateList(filteredList);
    }

    private void filterByChip() {
        List<News> filtered = new ArrayList<>(originalList);

        if (chipPositive.isChecked()) {
            filtered.removeIf(news -> !news.getSentiment_label().equalsIgnoreCase("positive"));
        } else if (chipNegative.isChecked()) {
            filtered.removeIf(news -> !news.getSentiment_label().equalsIgnoreCase("negative"));
        } else if (chipNeutral.isChecked()) {
            filtered.removeIf(news -> !news.getSentiment_label().equalsIgnoreCase("neutral"));
        }

        if (chipLatest.isChecked()) {
            filtered.sort((o1, o2) -> o2.getPublication_date().compareTo(o1.getPublication_date()));
        } else if (chipOldest.isChecked()) {
            filtered.sort(Comparator.comparing(News::getPublication_date));
        }

        newsAdapter.updateList(filtered);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadNewsFromFireStore() {
        swipeRefreshLayout.setColorSchemeResources(R.color.newsPulse);
        swipeRefreshLayout.setRefreshing(true); // Show refresh indicator

        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("Debug", "Fetching categories for user: " + userId);

        // Fetch user categories from FireStore
        firebaseFireStore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userCategories = (List<String>) documentSnapshot.get("categories");

                        if (userCategories == null || userCategories.isEmpty()) {
                            Log.d("Debug", "User has no categories, loading all news.");
                            fetchAllNews();
                        } else {
                            Log.d("Debug", "User categories: " + userCategories);
                            fetchAndFilterNews(userCategories);
                        }
                    } else {
                        Log.e("FireStoreError", "User document does not exist.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FireStoreError", "Error fetching user categories", e);
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    // Fetch all news
    private void fetchAllNews() {
        Log.d("Debug", "Fetching all news...");
        firebaseFireStore.collection("news")
                .orderBy("publication_date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        newsList.clear();
                        originalList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            News news = document.toObject(News.class);
                            newsList.add(news);
                        }
                        originalList.addAll(newsList); // Keep a copy of the original list
                        newsAdapter.updateList(newsList); // Update adapter with fetched news
                        Log.d("Debug", "Total news fetched: " + newsList.size());
                        newsAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("FireStoreError", "Error fetching news", task.getException());
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    // Fetch all news and filter manually
    private void fetchAndFilterNews(List<String> userCategories) {
        Log.d("Debug", "Fetching news for categories: " + userCategories);

        firebaseFireStore.collection("news")
                .orderBy("publication_date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        newsList.clear();
                        originalList.clear(); // Ensure originalList is updated
                        int filteredCount = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            News news = document.toObject(News.class);
                            String newsCategory = news.getCategory(); // Ensure this field exists

                            Log.d("Debug", "News Item - Title: " + news.getTitle() + ", Category: " + newsCategory);

                            if (userCategories.contains(newsCategory)) { // Manual filtering
                                newsList.add(news);
                                filteredCount++;
                            }
                        }
                        originalList.addAll(newsList); // Keep original copy for search filter
                        newsAdapter.updateList(newsList); // Update adapter with filtered news
                        newsAdapter.notifyDataSetChanged(); // Ensure UI reflects changes
                        Log.d("Debug", "Total filtered news: " + filteredCount);
                    } else {
                        Log.e("FireStoreError", "Error fetching news", task.getException());
                    }
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chipGroup.clearCheck();
        loadNewsFromFireStore();  // Reload news when returning
    }

}