package com.example.newspulse.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newspulse.R;
import com.example.newspulse.WebView;
import com.example.newspulse.model.News;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final Context context;
    private final List<News> newsList;

    public NewsAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = new ArrayList<>(newsList);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_layout, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.newsTitle.setText(news.getTitle());
        holder.newsSummary.setText(news.getSummary());
        holder.publishDate.setText(news.getPublication_date());

        //Sentiment Label Setup
        String label = news.getSentiment_label();
        if (label == null || label.isEmpty()) {
            holder.sentimentLabel.setVisibility(View.GONE);
        } else {
            holder.sentimentLabel.setVisibility(View.VISIBLE);
            holder.sentimentLabel.bringToFront();

            switch (label) {
                case "positive":
                    holder.sentimentLabel.setColorFilter(ContextCompat.getColor(context, R.color.sentiment_positive));
                    break;
                case "neutral":
                    holder.sentimentLabel.setColorFilter(ContextCompat.getColor(context, R.color.sentiment_neutral));
                    break;
                case "negative":
                    holder.sentimentLabel.setColorFilter(ContextCompat.getColor(context, R.color.sentiment_negative));
                    break;
            }
        }

        //Open Article in WebView
        holder.readMoreButton.setOnClickListener(view -> {
            Intent intent = new Intent(context, WebView.class);
            intent.putExtra("news_url", news.getUrl()); // Send the URL
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.slide_in_up, R.anim.none);
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            String newsId = news.getUrl().replace("/", "_");
            DocumentReference newsRef = db.collection("news").document(newsId);

            // Fetch and set the like button state initially
            newsRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> likedUsers = (List<String>) documentSnapshot.get("likedUsers");
                    if (likedUsers != null && likedUsers.contains(userId)) {
                        holder.likeButton.setImageResource(R.drawable.like_filled); // Liked
                    } else {
                        holder.likeButton.setImageResource(R.drawable.like); // Not Liked
                    }
                }
            });

            // Handle Like Button Click
            holder.likeButton.setOnClickListener(view -> {
                newsRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> likedUsers = (List<String>) documentSnapshot.get("likedUsers");
                        int likeCount = documentSnapshot.getLong("likeCount") != null
                                ? documentSnapshot.getLong("likeCount").intValue()
                                : 0;

                        if (likedUsers == null) likedUsers = new ArrayList<>();

                        if (likedUsers.contains(userId)) {
                            // User already liked, so unlike
                            likedUsers.remove(userId);
                            likeCount--;
                            holder.likeButton.setImageResource(R.drawable.like); // Change to outlined heart
                        } else {
                            // User has not liked yet, so like
                            likedUsers.add(userId);
                            likeCount++;
                            holder.likeButton.setImageResource(R.drawable.like_filled); // Change to filled heart
                        }

                        // Update FireStore with new like state
                        newsRef.update("likedUsers", likedUsers, "likeCount", likeCount)
                                .addOnSuccessListener(aVoid -> Log.d("Like", "Updated successfully"))
                                .addOnFailureListener(e -> Log.e("Like", "Error updating", e));
                    }
                });
            });
        }

        // Set Bookmark Button
        String newsId = news.getUrl().replace("/", "_");

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);

            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> bookmarks = (List<String>) documentSnapshot.get("bookmarks");
                    if (bookmarks != null && bookmarks.contains(newsId)) {
                        holder.bookmarkButton.setImageResource(R.drawable.bookmark_added); // Filled icon
                        holder.bookmarkButton.setTag(true);
                    } else {
                        holder.bookmarkButton.setImageResource(R.drawable.bookmark_add); // Unfilled icon
                        holder.bookmarkButton.setTag(false);
                    }
                }
            });

            holder.bookmarkButton.setOnClickListener(view -> userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> bookmarks = (List<String>) documentSnapshot.get("bookmarks");
                    if (bookmarks == null) bookmarks = new ArrayList<>();

                    boolean isBookmarked = (boolean) holder.bookmarkButton.getTag();

                    if (isBookmarked) {
                        // Remove from bookmarks
                        bookmarks.remove(newsId);
                        holder.bookmarkButton.setImageResource(R.drawable.bookmark_add);
                    } else {
                        // Add to bookmarks
                        bookmarks.add(newsId);
                        holder.bookmarkButton.setImageResource(R.drawable.bookmark_added);
                    }

                    // Toggle tag to keep state
                    holder.bookmarkButton.setTag(!isBookmarked);

                    // Update FireStore
                    userDocRef.update("bookmarks", bookmarks)
                            .addOnSuccessListener(aVoid -> Log.d("Bookmark", "Updated successfully"))
                            .addOnFailureListener(e -> Log.e("Bookmark", "Error updating", e));
                }
            }));
        }

        //Share Article
        holder.shareButton.setOnClickListener(view -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain"); // Share as plain text
            String shareMessage = "Check out this news: " + news.getUrl();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            context.startActivity(Intent.createChooser(shareIntent, "Share this news via"));
        });

        // Load image using Glide
        Glide.with(context)
                .load(news.getImageurl())
                .placeholder(R.drawable.logo_text1)
                .into(holder.newsImage);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void updateList(List<News> newList) {
        newsList.clear();
        newsList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage, sentimentLabel;
        TextView newsTitle, newsSummary, publishDate;
        ImageButton shareButton, bookmarkButton, readMoreButton, likeButton;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsSummary = itemView.findViewById(R.id.newsSummary);
            publishDate = itemView.findViewById(R.id.publishDate);
            sentimentLabel = itemView.findViewById(R.id.sentimentLabel);
            shareButton = itemView.findViewById(R.id.Share);
            bookmarkButton = itemView.findViewById(R.id.bookmarkAdd);
            readMoreButton = itemView.findViewById(R.id.readMore);
            likeButton = itemView.findViewById(R.id.like);
        }
    }
}
