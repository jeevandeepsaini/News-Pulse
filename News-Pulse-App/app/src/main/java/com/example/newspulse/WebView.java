package com.example.newspulse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class WebView extends AppCompatActivity {

    private android.webkit.WebView webView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorView;
    private String url;
    private boolean hasError = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipe);
        errorView = findViewById(R.id.error_view);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("news_url")) {
            url = intent.getStringExtra("news_url");
        } else {
            Toast.makeText(this, "No URL received!", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupWebView();
        setupSwipeRefreshLayout();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                webView.setVisibility(View.INVISIBLE);
                hasError = false;  // Reset error flag on new page load
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);

                if (!hasError) {  // Only show WebView if no error occurred
                    webView.setVisibility(View.VISIBLE);
                    errorView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    hasError = true;
                    Toast.makeText(WebView.this, "Failed to load page!", Toast.LENGTH_SHORT).show();
                    handleNetworkError();
                }
            }
        });

        // Load the URL received from the adapter
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            Log.e("WebView", "URL is null or empty");
        }
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isInternetConnected()) {
                hasError = false;
                errorView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                webView.reload();
            } else {
                handleNetworkError();
            }
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void handleNetworkError() {
        hasError = true;  // Set error flag
        Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
        webView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    public void retryLoading(View view) {
        if (isInternetConnected()) {
            hasError = false;  // Reset error state
            webView.setVisibility(View.VISIBLE);
            errorView.setVisibility(View.GONE);
            webView.reload();
        } else {
            Toast.makeText(this, "Still no connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void finish() {
        super.finish();
        // Exit animation: slide down
        overridePendingTransition(R.anim.none, R.anim.slide_out_down);
    }

}