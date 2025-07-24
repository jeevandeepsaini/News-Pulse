package com.example.newspulse.model;

public class News {
    private String title;
    private String summary;
    private String imageurl;
    private String url;
    private String publication_date;
    private String sentiment_label;
    private double sentiment_score;
    private String category;

    public News() {
    }

    public News(String title, String summary, String imageurl, String url, String publication_date,
                String sentiment_label, double sentiment_score, String category) {
        this.title = title;
        this.summary = summary;
        this.imageurl = imageurl;
        this.url = url;
        this.publication_date = publication_date;
        this.sentiment_label = sentiment_label;
        this.sentiment_score = sentiment_score;
        this.category = category;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getImageurl() {
        return imageurl;
    }

    public String getUrl() {
        return url;
    }

    public String getPublication_date() {
        return publication_date;
    }

    public String getSentiment_label() {
        return sentiment_label;
    }

    public double getSentiment_score() {
        return sentiment_score;
    }

    public String getCategory() {
        return category;
    }
}
